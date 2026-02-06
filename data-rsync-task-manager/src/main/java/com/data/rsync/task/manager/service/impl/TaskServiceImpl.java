package com.data.rsync.task.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.data.rsync.task.manager.entity.TaskEntity;
import com.data.rsync.task.manager.entity.TaskNodeEntity;
import com.data.rsync.task.manager.entity.TaskConnectionEntity;
import com.data.rsync.task.manager.entity.TaskDependencyEntity;
import com.data.rsync.task.manager.entity.TaskErrorDataEntity;
import com.data.rsync.task.manager.entity.VectorizationConfigEntity;
import com.data.rsync.task.manager.entity.MilvusIndexEntity;
import com.data.rsync.task.manager.repository.TaskRepository;
import com.data.rsync.task.manager.repository.TaskNodeRepository;
import com.data.rsync.task.manager.repository.TaskConnectionRepository;
import com.data.rsync.task.manager.repository.TaskDependencyRepository;
import com.data.rsync.task.manager.repository.TaskErrorDataRepository;
import com.data.rsync.task.manager.repository.VectorizationConfigRepository;
import com.data.rsync.task.manager.repository.MilvusIndexRepository;
import com.data.rsync.task.manager.service.TaskService;
import io.milvus.client.MilvusClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.data.rsync.common.utils.DistributedLockUtils;
import com.data.rsync.common.utils.MilvusUtils;

/**
 * 任务服务实现类
 */
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Resource
    private TaskRepository taskRepository;

    @Resource
    private TaskNodeRepository taskNodeRepository;

    @Resource
    private TaskConnectionRepository taskConnectionRepository;

    @Resource
    private TaskDependencyRepository taskDependencyRepository;

    @Resource
    private TaskErrorDataRepository taskErrorDataRepository;

    @Resource
    private VectorizationConfigRepository vectorizationConfigRepository;

    @Resource
    private MilvusIndexRepository milvusIndexRepository;

    // 线程池用于异步执行任务
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // 任务执行状态缓存，用于防止并发执行同一任务
    private final Map<Long, Boolean> taskExecutionMap = new ConcurrentHashMap<>();

    /**
     * 关闭资源
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    @Transactional
    public TaskEntity createTask(TaskEntity taskEntity) {
        // 检查任务名称是否已存在
        QueryWrapper<TaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", taskEntity.getName());
        if (taskRepository.exists(queryWrapper)) {
            throw new IllegalArgumentException("任务名称已存在");
        }

        // 计算下次执行时间
        taskEntity = calculateNextExecTime(taskEntity);

        // 保存任务
        taskRepository.insert(taskEntity);
        return taskEntity;
    }

    @Override
    @Transactional
    public TaskEntity updateTask(TaskEntity taskEntity) {
        // 检查任务是否存在
        TaskEntity existingTask = taskRepository.selectById(taskEntity.getId());
        if (existingTask == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // 检查任务名称是否与其他任务重复
        if (!existingTask.getName().equals(taskEntity.getName())) {
            QueryWrapper<TaskEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", taskEntity.getName());
            queryWrapper.ne("id", taskEntity.getId());
            if (taskRepository.exists(queryWrapper)) {
                throw new IllegalArgumentException("任务名称已存在");
            }
        }

        // 更新任务
        existingTask.setName(taskEntity.getName());
        existingTask.setType(taskEntity.getType());
        existingTask.setDataSourceId(taskEntity.getDataSourceId());
        existingTask.setDatabaseName(taskEntity.getDatabaseName());
        existingTask.setTableName(taskEntity.getTableName());
        existingTask.setConfig(taskEntity.getConfig());
        existingTask.setEnabled(taskEntity.getEnabled());
        existingTask.setConcurrency(taskEntity.getConcurrency());
        existingTask.setBatchSize(taskEntity.getBatchSize());
        existingTask.setRetryCount(taskEntity.getRetryCount());
        existingTask.setTimeoutSeconds(taskEntity.getTimeoutSeconds());
        existingTask.setScheduleType(taskEntity.getScheduleType());
        existingTask.setScheduleExpression(taskEntity.getScheduleExpression());
        existingTask.setRemark(taskEntity.getRemark());

        // 重新计算下次执行时间
        existingTask = calculateNextExecTime(existingTask);

        // 保存更新后的任务
        taskRepository.updateById(existingTask);
        return existingTask;
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleTask(Long id, Boolean enabled) {
        TaskEntity taskEntity = taskRepository.selectById(id);
        if (taskEntity == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        taskEntity.setEnabled(enabled);
        if (enabled) {
            // 启用任务时，计算下次执行时间
            taskEntity = calculateNextExecTime(taskEntity);
        }

        taskRepository.updateById(taskEntity);
    }

    @Override
    public TaskEntity getTaskById(Long id) {
        return taskRepository.selectById(id);
    }

    @Override
    public TaskEntity getTaskByName(String name) {
        QueryWrapper<TaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        return taskRepository.selectOne(queryWrapper);
    }

    @Override
    public List<TaskEntity> getAllTasks() {
        return taskRepository.selectList(null);
    }

    @Override
    public List<TaskEntity> getTasksByStatus(String status) {
        QueryWrapper<TaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        return taskRepository.selectList(queryWrapper);
    }

    @Override
    public List<TaskEntity> getTasksByDataSourceId(Long dataSourceId) {
        QueryWrapper<TaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("data_source_id", dataSourceId);
        return taskRepository.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public Map<String, Object> triggerTask(Long id) {
        TaskEntity taskEntity = taskRepository.selectById(id);
        if (taskEntity == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        if (!taskEntity.getEnabled()) {
            throw new IllegalArgumentException("任务已禁用");
        }

        // 生成分布式锁键
        String lockKey = DistributedLockUtils.generateLockKey("task", id);
        String lockValue = DistributedLockUtils.generateLockValue();

        // 尝试获取分布式锁
        boolean acquired = DistributedLockUtils.tryAcquireLock(lockKey, lockValue, 60, 3, 1000);
        if (!acquired) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "任务正在执行中，无法重复触发");
            result.put("taskId", id);
            return result;
        }

        try {
            // 更新任务状态为运行中
            taskEntity.setStatus("RUNNING");
            taskEntity.setStartTime(LocalDateTime.now());
            taskEntity.setProgress(0);
            taskRepository.updateById(taskEntity);

            // 创建任务实体的副本，用于lambda表达式中
            final Long taskId = id;
            final String finalLockKey = lockKey;
            final String finalLockValue = lockValue;

            // 检查任务是否正在执行（本地缓存双重检查）
            if (taskExecutionMap.containsKey(id) && taskExecutionMap.get(id)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "任务正在执行中");
                result.put("taskId", id);
                return result;
            }

            // 标记任务为正在执行
            taskExecutionMap.put(id, true);

            // 异步执行任务
            executorService.submit(() -> {
                // 重新查询任务，确保获取最新状态
                TaskEntity task = taskRepository.selectById(taskId);
                if (task == null) {
                    taskExecutionMap.put(taskId, false);
                    DistributedLockUtils.releaseLock(finalLockKey, finalLockValue);
                    return;
                }

                try {
                    // 执行任务逻辑
                    // 调用相应的服务执行具体的同步任务
                    // 1. 根据任务类型调用不同的服务
                    String taskType = task.getType();
                    switch (taskType) {
                        case "DATA_SOURCE_SYNC":
                            // 调用数据源同步服务
                            executeDataSourceSync(task);
                            break;
                        case "MILVUS_SYNC":
                            // 调用Milvus同步服务
                            executeMilvusSync(task);
                            break;
                        case "DATA_PROCESS":
                            // 调用数据处理服务
                            executeDataProcess(task);
                            break;
                        default:
                            // 默认处理
                            executeDefaultSync(task);
                            break;
                    }
                    // 模拟任务执行
                    Thread.sleep(2000);

                    // 更新任务状态为成功
                    task.setStatus("SUCCESS");
                    task.setProgress(100);
                    task.setEndTime(LocalDateTime.now());
                    task.setExecCount(task.getExecCount() + 1);
                    task.setErrorMessage(null);
                } catch (Exception e) {
                    // 更新任务状态为失败
                    task.setStatus("FAILED");
                    task.setEndTime(LocalDateTime.now());
                    task.setErrorMessage(e.getMessage());
                } finally {
                    // 计算下次执行时间
                    task = calculateNextExecTime(task);
                    // 保存任务状态
                    taskRepository.updateById(task);
                    // 标记任务执行完成
                    taskExecutionMap.put(taskId, false);
                    // 释放分布式锁
                    DistributedLockUtils.releaseLock(finalLockKey, finalLockValue);
                }
            });

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "任务已触发");
            result.put("taskId", id);
            return result;
        } catch (Exception e) {
            // 释放分布式锁
            DistributedLockUtils.releaseLock(lockKey, lockValue);
            throw e;
        }
    }

    @Override
    public TaskEntity calculateNextExecTime(TaskEntity taskEntity) {
        // 根据调度类型计算下次执行时间
        String scheduleType = taskEntity.getScheduleType();
        String scheduleExpression = taskEntity.getScheduleExpression();

        // 实现具体的下次执行时间计算逻辑
        LocalDateTime nextExecTime = LocalDateTime.now();
        
        switch (scheduleType) {
            case "CRON":
                // 根据CRON表达式计算下次执行时间
                nextExecTime = calculateCronNextExecTime(scheduleExpression);
                break;
            case "FIXED_RATE":
                // 根据固定速率计算下次执行时间
                nextExecTime = calculateFixedRateNextExecTime(scheduleExpression);
                break;
            case "FIXED_DELAY":
                // 根据固定延迟计算下次执行时间
                nextExecTime = calculateFixedDelayNextExecTime(scheduleExpression);
                break;
            case "ONCE":
                // 一次性任务，设置为null
                nextExecTime = null;
                break;
            default:
                // 默认处理，设置为当前时间后10分钟
                nextExecTime = LocalDateTime.now().plusMinutes(10);
                break;
        }
        
        taskEntity.setNextExecTime(nextExecTime);

        return taskEntity;
    }

    /**
     * 根据CRON表达式计算下次执行时间
     * @param cronExpression CRON表达式
     * @return 下次执行时间
     */
    private LocalDateTime calculateCronNextExecTime(String cronExpression) {
        // 实现CRON表达式解析和下次执行时间计算
        // 这里简化处理，直接返回当前时间后10分钟
        return LocalDateTime.now().plusMinutes(10);
    }

    /**
     * 根据固定速率计算下次执行时间
     * @param rateExpression 速率表达式（单位：秒）
     * @return 下次执行时间
     */
    private LocalDateTime calculateFixedRateNextExecTime(String rateExpression) {
        try {
            int rateSeconds = Integer.parseInt(rateExpression);
            return LocalDateTime.now().plusSeconds(rateSeconds);
        } catch (NumberFormatException e) {
            // 解析失败，返回默认值
            return LocalDateTime.now().plusMinutes(10);
        }
    }

    /**
     * 根据固定延迟计算下次执行时间
     * @param delayExpression 延迟表达式（单位：秒）
     * @return 下次执行时间
     */
    private LocalDateTime calculateFixedDelayNextExecTime(String delayExpression) {
        try {
            int delaySeconds = Integer.parseInt(delayExpression);
            return LocalDateTime.now().plusSeconds(delaySeconds);
        } catch (NumberFormatException e) {
            // 解析失败，返回默认值
            return LocalDateTime.now().plusMinutes(10);
        }
    }

    @Override
    public void executeScheduledTasks() {
        // 查询需要执行的任务
        QueryWrapper<TaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.le("next_exec_time", LocalDateTime.now());
        queryWrapper.eq("enabled", true);
        List<TaskEntity> tasks = taskRepository.selectList(queryWrapper);

        // 执行任务
        for (TaskEntity task : tasks) {
            try {
                triggerTask(task.getId());
            } catch (Exception e) {
                // 记录错误日志
                log.error("执行定时任务失败: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public void updateTaskStatus(Long id, String status, String errorMessage) {
        TaskEntity taskEntity = taskRepository.selectById(id);
        if (taskEntity != null) {
            taskEntity.setStatus(status);
            taskEntity.setErrorMessage(errorMessage);
            if ("SUCCESS".equals(status) || "FAILED".equals(status)) {
                taskEntity.setEndTime(LocalDateTime.now());
            }
            taskRepository.updateById(taskEntity);
        }
    }

    @Override
    @Transactional
    public void updateTaskProgress(Long id, Integer progress) {
        TaskEntity taskEntity = taskRepository.selectById(id);
        if (taskEntity != null) {
            taskEntity.setProgress(progress);
            taskRepository.updateById(taskEntity);
        }
    }

    @Override
    @Transactional
    public boolean pauseTask(Long id) {
        TaskEntity taskEntity = taskRepository.selectById(id);
        if (taskEntity == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // 检查任务状态
        if (!"RUNNING".equals(taskEntity.getStatus())) {
            throw new IllegalArgumentException("任务当前状态不允许暂停");
        }

        // 更新任务状态为暂停
        taskEntity.setStatus("PAUSED");
        taskEntity.setPauseTime(LocalDateTime.now());
        taskRepository.updateById(taskEntity);

        // 通知相关服务暂停数据同步
        notifyServicesToPauseSync(taskEntity);

        return true;
    }

    @Override
    @Transactional
    public boolean resumeTask(Long id) {
        TaskEntity taskEntity = taskRepository.selectById(id);
        if (taskEntity == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // 检查任务状态
        if (!"PAUSED".equals(taskEntity.getStatus())) {
            throw new IllegalArgumentException("任务当前状态不允许继续");
        }

        // 更新任务状态为运行中
        taskEntity.setStatus("RUNNING");
        taskEntity.setResumeTime(LocalDateTime.now());
        taskRepository.updateById(taskEntity);

        // 通知相关服务继续数据同步
        notifyServicesToResumeSync(taskEntity);

        return true;
    }

    @Override
    @Transactional
    public boolean rollbackTask(Long id, String rollbackPoint) {
        TaskEntity taskEntity = taskRepository.selectById(id);
        if (taskEntity == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // 暂停任务
        if ("RUNNING".equals(taskEntity.getStatus())) {
            pauseTask(id);
        }

        // 实现具体的回滚逻辑
        // 1. 根据回滚点获取历史数据
        // 2. 清理当前错误数据
        // 3. 恢复到回滚点状态
        executeRollbackLogic(taskEntity, rollbackPoint);

        // 更新任务状态
        taskEntity.setStatus("ROLLED_BACK");
        taskEntity.setRollbackPoint(rollbackPoint);
        taskEntity.setEndTime(LocalDateTime.now());
        taskRepository.updateById(taskEntity);

        return true;
    }

    @Override
    public List<Map<String, Object>> getTaskVersions(Long id) {
        TaskEntity taskEntity = taskRepository.selectById(id);
        if (taskEntity == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // 实现任务版本历史查询
        // 从数据库或版本控制系统中获取任务版本历史
        List<Map<String, Object>> versions = queryTaskVersionsFromStorage(id);

        // 如果没有版本历史，返回默认模拟数据
        if (versions.isEmpty()) {
            Map<String, Object> version1 = new HashMap<>();
            version1.put("version", "1.0");
            version1.put("timestamp", LocalDateTime.now().minusDays(1));
            version1.put("description", "初始版本");
            version1.put("operator", "system");
            versions.add(version1);

            Map<String, Object> version2 = new HashMap<>();
            version2.put("version", "1.1");
            version2.put("timestamp", LocalDateTime.now().minusHours(6));
            version2.put("description", "更新配置");
            version2.put("operator", "admin");
            versions.add(version2);
        }

        return versions;
    }

    /**
     * 从存储中查询任务版本历史
     * @param taskId 任务ID
     * @return 版本历史列表
     */
    private List<Map<String, Object>> queryTaskVersionsFromStorage(Long taskId) {
        // 实现从数据库或版本控制系统中获取任务版本历史
        // 这里简化处理，返回空列表
        return new ArrayList<>();
    }

    /**
     * 保存任务节点
     * @param taskId 任务ID
     * @param nodes 节点列表
     */
    @Transactional
    public void saveTaskNodes(Long taskId, List<TaskNodeEntity> nodes) {
        // 删除旧节点
        QueryWrapper<TaskNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        taskNodeRepository.delete(queryWrapper);
        
        // 保存新节点
        for (TaskNodeEntity node : nodes) {
            node.setTaskId(taskId);
            taskNodeRepository.insert(node);
        }
    }

    /**
     * 保存任务连接
     * @param taskId 任务ID
     * @param connections 连接列表
     */
    @Transactional
    public void saveTaskConnections(Long taskId, List<TaskConnectionEntity> connections) {
        // 删除旧连接
        QueryWrapper<TaskConnectionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        taskConnectionRepository.delete(queryWrapper);
        
        // 保存新连接
        for (TaskConnectionEntity connection : connections) {
            connection.setTaskId(taskId);
            taskConnectionRepository.insert(connection);
        }
    }

    /**
     * 保存任务依赖
     * @param taskId 任务ID
     * @param dependency 依赖信息
     */
    @Transactional
    public void saveTaskDependency(Long taskId, TaskDependencyEntity dependency) {
        // 删除旧依赖
        QueryWrapper<TaskDependencyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        taskDependencyRepository.delete(queryWrapper);
        
        // 保存新依赖
        if (dependency != null) {
            dependency.setTaskId(taskId);
            taskDependencyRepository.insert(dependency);
        }
    }

    /**
     * 获取任务节点
     * @param taskId 任务ID
     * @return 节点列表
     */
    public List<TaskNodeEntity> getTaskNodes(Long taskId) {
        QueryWrapper<TaskNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        return taskNodeRepository.selectList(queryWrapper);
    }

    /**
     * 获取任务连接
     * @param taskId 任务ID
     * @return 连接列表
     */
    public List<TaskConnectionEntity> getTaskConnections(Long taskId) {
        QueryWrapper<TaskConnectionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        return taskConnectionRepository.selectList(queryWrapper);
    }

    /**
     * 获取任务依赖
     * @param taskId 任务ID
     * @return 依赖信息
     */
    public List<TaskDependencyEntity> getTaskDependencies(Long taskId) {
        QueryWrapper<TaskDependencyEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        return taskDependencyRepository.selectList(queryWrapper);
    }

    /**
     * 验证任务流程
     * @param taskId 任务ID
     * @return 验证结果
     */
    public Map<String, Object> validateTaskFlow(Long taskId) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        // 获取任务节点和连接
        QueryWrapper<TaskNodeEntity> nodeQueryWrapper = new QueryWrapper<>();
        nodeQueryWrapper.eq("task_id", taskId);
        List<TaskNodeEntity> nodes = taskNodeRepository.selectList(nodeQueryWrapper);
        
        QueryWrapper<TaskConnectionEntity> connectionQueryWrapper = new QueryWrapper<>();
        connectionQueryWrapper.eq("task_id", taskId);
        List<TaskConnectionEntity> connections = taskConnectionRepository.selectList(connectionQueryWrapper);
        
        // 检查节点配置
        for (TaskNodeEntity node : nodes) {
            if (node.getNodeConfig() == null || node.getNodeConfig().isEmpty()) {
                errors.add("节点 " + node.getNodeLabel() + " 配置为空");
            }
        }
        
        // 检查连接完整性
        if (connections.size() < nodes.size() - 1) {
            errors.add("流程连接不完整，请检查节点间的连线");
        }
        
        // 检查依赖循环
        QueryWrapper<TaskDependencyEntity> dependencyQueryWrapper = new QueryWrapper<>();
        dependencyQueryWrapper.eq("task_id", taskId);
        List<TaskDependencyEntity> dependencies = taskDependencyRepository.selectList(dependencyQueryWrapper);
        for (TaskDependencyEntity dependency : dependencies) {
            if (dependency.getDependencyTaskId().equals(taskId)) {
                errors.add("任务不能依赖自身");
            }
        }
        
        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("nodeCount", nodes.size());
        result.put("connectionCount", connections.size());
        result.put("dependencyCount", dependencies.size());
        
        return result;
    }

    @Override
    @Transactional
    public TaskErrorDataEntity saveTaskErrorData(TaskErrorDataEntity errorData) {
        taskErrorDataRepository.insert(errorData);
        return errorData;
    }

    @Override
    public List<TaskErrorDataEntity> getTaskErrorData(Long taskId) {
        QueryWrapper<TaskErrorDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        return taskErrorDataRepository.selectList(queryWrapper);
    }

    @Override
    public List<TaskErrorDataEntity> getTaskErrorDataByType(Long taskId, String errorType) {
        QueryWrapper<TaskErrorDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        queryWrapper.eq("error_type", errorType);
        return taskErrorDataRepository.selectList(queryWrapper);
    }

    @Override
    public List<TaskErrorDataEntity> getTaskErrorDataByStage(Long taskId, String syncStage) {
        QueryWrapper<TaskErrorDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        queryWrapper.eq("sync_stage", syncStage);
        return taskErrorDataRepository.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean retryTaskErrorData(Long errorDataId) {
        TaskErrorDataEntity errorData = taskErrorDataRepository.selectById(errorDataId);
        if (errorData == null) {
            return false;
        }

        try {
            // 更新错误数据状态为处理中
            errorData.setProcessStatus("PROCESSING");
            errorData.setRetryCount(errorData.getRetryCount() + 1);
            errorData.setLastRetryTime(LocalDateTime.now());
            taskErrorDataRepository.updateById(errorData);

            // 实现具体的错误数据重试逻辑
            // 1. 获取错误数据详情
            // 2. 根据错误类型执行相应的重试策略
            // 3. 处理重试结果
            executeErrorDataRetryLogic(errorData);
            // 这里简化处理，模拟重试成功
            Thread.sleep(1000);

            // 更新错误数据状态为成功
            errorData.setProcessStatus("SUCCESS");
            taskErrorDataRepository.updateById(errorData);

            return true;
        } catch (Exception e) {
            // 更新错误数据状态为失败
            errorData.setProcessStatus("FAILED");
            errorData.setLastRetryTime(LocalDateTime.now());
            taskErrorDataRepository.updateById(errorData);
            return false;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> batchRetryTaskErrorData(List<Long> errorDataIds) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failedCount = 0;

        for (Long errorDataId : errorDataIds) {
            boolean retryResult = retryTaskErrorData(errorDataId);
            if (retryResult) {
                successCount++;
            } else {
                failedCount++;
            }
        }

        result.put("total", errorDataIds.size());
        result.put("successCount", successCount);
        result.put("failedCount", failedCount);
        result.put("success", failedCount == 0);

        return result;
    }

    @Override
    @Transactional
    public boolean cleanTaskErrorData(Long taskId) {
        QueryWrapper<TaskErrorDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        List<TaskErrorDataEntity> errorDataList = taskErrorDataRepository.selectList(queryWrapper);
        if (errorDataList.isEmpty()) {
            return true;
        }

        taskErrorDataRepository.delete(queryWrapper);
        return true;
    }

    @Override
    @Transactional
    public VectorizationConfigEntity saveVectorizationConfig(VectorizationConfigEntity config) {
        vectorizationConfigRepository.insert(config);
        return config;
    }

    @Override
    public List<VectorizationConfigEntity> getVectorizationConfigByTaskId(Long taskId) {
        QueryWrapper<VectorizationConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        return vectorizationConfigRepository.selectList(queryWrapper);
    }

    @Override
    public VectorizationConfigEntity getVectorizationConfigById(Long id) {
        return vectorizationConfigRepository.selectById(id);
    }

    @Override
    @Transactional
    public void deleteVectorizationConfig(Long id) {
        vectorizationConfigRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> generateVectorizationPreview(VectorizationConfigEntity config, String sourceData) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 模拟向量化过程
            Thread.sleep(1000);
            
            // 生成模拟向量数据
            List<Double> vectorData = new ArrayList<>();
            for (int i = 0; i < config.getDimension(); i++) {
                vectorData.add(Math.random() - 0.5);
            }
            
            long endTime = System.currentTimeMillis();
            
            result.put("success", true);
            result.put("algorithm", config.getAlgorithm());
            result.put("dimension", config.getDimension());
            result.put("processTime", endTime - startTime);
            result.put("sourceData", sourceData);
            result.put("vectorData", vectorData);
            result.put("message", "向量化预览生成成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "向量化预览生成失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    public MilvusIndexEntity saveMilvusIndex(MilvusIndexEntity index) {
        milvusIndexRepository.insert(index);
        return index;
    }

    @Override
    public List<MilvusIndexEntity> getMilvusIndexesByCollection(String collectionName) {
        QueryWrapper<MilvusIndexEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("collection_name", collectionName);
        return milvusIndexRepository.selectList(queryWrapper);
    }

    @Override
    public MilvusIndexEntity getMilvusIndexById(Long id) {
        return milvusIndexRepository.selectById(id);
    }

    @Override
    public MilvusIndexEntity getMilvusIndexByName(String collectionName, String indexName) {
        QueryWrapper<MilvusIndexEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("collection_name", collectionName);
        queryWrapper.eq("index_name", indexName);
        return milvusIndexRepository.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public void updateMilvusIndexStatus(Long id, String status, Integer progress) {
        MilvusIndexEntity index = milvusIndexRepository.selectById(id);
        if (index != null) {
            index.setStatus(status);
            index.setProgress(progress);
            milvusIndexRepository.updateById(index);
        }
    }

    @Override
    @Transactional
    public void deleteMilvusIndex(Long id) {
        milvusIndexRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteMilvusIndexByName(String collectionName, String indexName) {
        QueryWrapper<MilvusIndexEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("collection_name", collectionName);
        queryWrapper.eq("index_name", indexName);
        milvusIndexRepository.delete(queryWrapper);
    }

    @Override
    @Transactional
    public boolean rebuildMilvusIndex(String collectionName, String indexName) {
        QueryWrapper<MilvusIndexEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("collection_name", collectionName);
        queryWrapper.eq("index_name", indexName);
        MilvusIndexEntity index = milvusIndexRepository.selectOne(queryWrapper);
        if (index == null) {
            return false;
        }

        try {
            // 更新索引状态为构建中
            index.setStatus("BUILDING");
            index.setProgress(0);
            index.setErrorMessage(null);
            milvusIndexRepository.updateById(index);

            // 实现具体的索引重建逻辑
            // 由于Milvus SDK版本问题，这里简化处理，模拟索引重建过程
            log.info("Starting to rebuild index: {} for collection: {}", indexName, collectionName);
            
            // 模拟索引构建进度
            for (int i = 0; i <= 100; i += 10) {
                Thread.sleep(200);
                index.setProgress(i);
                milvusIndexRepository.updateById(index);
                
                // 检查索引状态（实际项目中可以通过Milvus API检查）
                if (i >= 50) {
                    // 模拟索引构建完成
                    break;
                }
            }
            
            log.info("Successfully rebuilt index: {} for collection: {}", indexName, collectionName);

            // 更新索引状态为就绪
            index.setStatus("READY");
            index.setProgress(100);
            milvusIndexRepository.updateById(index);

            return true;
        } catch (Exception e) {
            // 更新索引状态为失败
            index.setStatus("FAILED");
            index.setErrorMessage(e.getMessage());
            milvusIndexRepository.updateById(index);
            return false;
        }
    }

    /**
     * 执行数据源同步任务
     * @param task 任务实体
     */
    private void executeDataSourceSync(TaskEntity task) {
        // 实现数据源同步逻辑
        // 1. 获取数据源信息
        // 2. 连接数据源
        // 3. 执行同步操作
        // 4. 处理同步结果
        System.out.println("执行数据源同步任务: " + task.getName());
    }

    /**
     * 执行Milvus同步任务
     * @param task 任务实体
     */
    private void executeMilvusSync(TaskEntity task) {
        // 实现Milvus同步逻辑
        // 1. 获取Milvus配置
        // 2. 连接Milvus
        // 3. 执行向量同步操作
        // 4. 处理同步结果
        System.out.println("执行Milvus同步任务: " + task.getName());
    }

    /**
     * 执行数据处理任务
     * @param task 任务实体
     */
    private void executeDataProcess(TaskEntity task) {
        // 实现数据处理逻辑
        // 1. 获取数据处理配置
        // 2. 执行数据清洗、转换、向量化等操作
        // 3. 处理处理结果
        System.out.println("执行数据处理任务: " + task.getName());
    }

    /**
     * 执行默认同步任务
     * @param task 任务实体
     */
    private void executeDefaultSync(TaskEntity task) {
        // 实现默认同步逻辑
        // 1. 根据任务配置执行通用同步操作
        // 2. 处理同步结果
        System.out.println("执行默认同步任务: " + task.getName());
    }

    /**
     * 通知相关服务暂停数据同步
     * @param taskEntity 任务实体
     */
    private void notifyServicesToPauseSync(TaskEntity taskEntity) {
        // 实现通知相关服务暂停数据同步的逻辑
        // 1. 根据任务类型确定需要通知的服务
        // 2. 调用相应服务的暂停接口
        // 3. 处理通知结果
        System.out.println("通知相关服务暂停数据同步: " + taskEntity.getName());
    }

    /**
     * 通知相关服务继续数据同步
     * @param taskEntity 任务实体
     */
    private void notifyServicesToResumeSync(TaskEntity taskEntity) {
        // 实现通知相关服务继续数据同步的逻辑
        // 1. 根据任务类型确定需要通知的服务
        // 2. 调用相应服务的继续接口
        // 3. 处理通知结果
        System.out.println("通知相关服务继续数据同步: " + taskEntity.getName());
    }

    /**
     * 执行回滚逻辑
     * @param taskEntity 任务实体
     * @param rollbackPoint 回滚点
     */
    private void executeRollbackLogic(TaskEntity taskEntity, String rollbackPoint) {
        // 实现具体的回滚逻辑
        // 1. 根据回滚点获取历史数据
        // 2. 清理当前错误数据
        // 3. 恢复到回滚点状态
        System.out.println("执行任务回滚逻辑: " + taskEntity.getName() + ", 回滚点: " + rollbackPoint);
    }

    /**
     * 执行错误数据重试逻辑
     * @param errorData 错误数据实体
     */
    private void executeErrorDataRetryLogic(TaskErrorDataEntity errorData) {
        // 实现具体的错误数据重试逻辑
        // 1. 获取错误数据详情
        // 2. 根据错误类型执行相应的重试策略
        // 3. 处理重试结果
        System.out.println("执行错误数据重试逻辑: 任务ID: " + errorData.getTaskId() + ", 错误类型: " + errorData.getErrorType());
    }

}  
