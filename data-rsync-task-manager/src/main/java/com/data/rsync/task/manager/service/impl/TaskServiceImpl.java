package com.data.rsync.task.manager.service.impl;

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

/**
 * 任务服务实现类
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskNodeRepository taskNodeRepository;

    @Autowired
    private TaskConnectionRepository taskConnectionRepository;

    @Autowired
    private TaskDependencyRepository taskDependencyRepository;

    @Autowired
    private TaskErrorDataRepository taskErrorDataRepository;

    @Autowired
    private VectorizationConfigRepository vectorizationConfigRepository;

    @Autowired
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
        if (taskRepository.existsByName(taskEntity.getName())) {
            throw new IllegalArgumentException("任务名称已存在");
        }

        // 计算下次执行时间
        taskEntity = calculateNextExecTime(taskEntity);

        // 保存任务
        return taskRepository.save(taskEntity);
    }

    @Override
    @Transactional
    public TaskEntity updateTask(TaskEntity taskEntity) {
        // 检查任务是否存在
        TaskEntity existingTask = taskRepository.findById(taskEntity.getId()).orElse(null);
        if (existingTask == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // 检查任务名称是否与其他任务重复
        if (!existingTask.getName().equals(taskEntity.getName()) && taskRepository.existsByName(taskEntity.getName())) {
            throw new IllegalArgumentException("任务名称已存在");
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
        return taskRepository.save(existingTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleTask(Long id, Boolean enabled) {
        TaskEntity taskEntity = taskRepository.findById(id).orElse(null);
        if (taskEntity == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        taskEntity.setEnabled(enabled);
        if (enabled) {
            // 启用任务时，计算下次执行时间
            taskEntity = calculateNextExecTime(taskEntity);
        }

        taskRepository.save(taskEntity);
    }

    @Override
    public TaskEntity getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @Override
    public TaskEntity getTaskByName(String name) {
        return taskRepository.findByName(name);
    }

    @Override
    public List<TaskEntity> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public List<TaskEntity> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status);
    }

    @Override
    public List<TaskEntity> getTasksByDataSourceId(Long dataSourceId) {
        return taskRepository.findByDataSourceId(dataSourceId);
    }

    @Override
    @Transactional
    public Map<String, Object> triggerTask(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id).orElse(null);
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
            taskRepository.save(taskEntity);

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
                TaskEntity task = taskRepository.findById(taskId).orElse(null);
                if (task == null) {
                    taskExecutionMap.put(taskId, false);
                    DistributedLockUtils.releaseLock(finalLockKey, finalLockValue);
                    return;
                }

                try {
                    // 执行任务逻辑
                    // TODO: 调用相应的服务执行具体的同步任务
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
                    taskRepository.save(task);
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

        // TODO: 实现具体的下次执行时间计算逻辑
        // 这里简化处理，直接设置为当前时间后10分钟
        taskEntity.setNextExecTime(LocalDateTime.now().plusMinutes(10));

        return taskEntity;
    }

    @Override
    public void executeScheduledTasks() {
        // 查询需要执行的任务
        List<TaskEntity> tasks = taskRepository.findTasksToExecute(LocalDateTime.now());

        // 执行任务
        for (TaskEntity task : tasks) {
            try {
                triggerTask(task.getId());
            } catch (Exception e) {
                // 记录错误日志
                e.printStackTrace();
            }
        }
    }

    @Override
    @Transactional
    public void updateTaskStatus(Long id, String status, String errorMessage) {
        TaskEntity taskEntity = taskRepository.findById(id).orElse(null);
        if (taskEntity != null) {
            taskEntity.setStatus(status);
            taskEntity.setErrorMessage(errorMessage);
            if ("SUCCESS".equals(status) || "FAILED".equals(status)) {
                taskEntity.setEndTime(LocalDateTime.now());
            }
            taskRepository.save(taskEntity);
        }
    }

    @Override
    @Transactional
    public void updateTaskProgress(Long id, Integer progress) {
        TaskEntity taskEntity = taskRepository.findById(id).orElse(null);
        if (taskEntity != null) {
            taskEntity.setProgress(progress);
            taskRepository.save(taskEntity);
        }
    }

    @Override
    @Transactional
    public boolean pauseTask(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id).orElse(null);
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
        taskRepository.save(taskEntity);

        // TODO: 通知相关服务暂停数据同步

        return true;
    }

    @Override
    @Transactional
    public boolean resumeTask(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id).orElse(null);
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
        taskRepository.save(taskEntity);

        // TODO: 通知相关服务继续数据同步

        return true;
    }

    @Override
    @Transactional
    public boolean rollbackTask(Long id, String rollbackPoint) {
        TaskEntity taskEntity = taskRepository.findById(id).orElse(null);
        if (taskEntity == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // 暂停任务
        if ("RUNNING".equals(taskEntity.getStatus())) {
            pauseTask(id);
        }

        // TODO: 实现具体的回滚逻辑
        // 1. 根据回滚点获取历史数据
        // 2. 清理当前错误数据
        // 3. 恢复到回滚点状态

        // 更新任务状态
        taskEntity.setStatus("ROLLED_BACK");
        taskEntity.setRollbackPoint(rollbackPoint);
        taskEntity.setEndTime(LocalDateTime.now());
        taskRepository.save(taskEntity);

        return true;
    }

    @Override
    public List<Map<String, Object>> getTaskVersions(Long id) {
        TaskEntity taskEntity = taskRepository.findById(id).orElse(null);
        if (taskEntity == null) {
            throw new IllegalArgumentException("任务不存在");
        }

        // TODO: 实现任务版本历史查询
        // 这里简化处理，返回模拟数据
        List<Map<String, Object>> versions = new ArrayList<>();

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

        return versions;
    }

    /**
     * 保存任务节点
     * @param taskId 任务ID
     * @param nodes 节点列表
     */
    @Transactional
    public void saveTaskNodes(Long taskId, List<TaskNodeEntity> nodes) {
        // 删除旧节点
        taskNodeRepository.deleteAll(taskNodeRepository.findByTaskId(taskId));
        
        // 保存新节点
        for (TaskNodeEntity node : nodes) {
            node.setTaskId(taskId);
            taskNodeRepository.save(node);
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
        taskConnectionRepository.deleteAll(taskConnectionRepository.findByTaskId(taskId));
        
        // 保存新连接
        for (TaskConnectionEntity connection : connections) {
            connection.setTaskId(taskId);
            taskConnectionRepository.save(connection);
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
        taskDependencyRepository.deleteAll(taskDependencyRepository.findByTaskId(taskId));
        
        // 保存新依赖
        if (dependency != null) {
            dependency.setTaskId(taskId);
            taskDependencyRepository.save(dependency);
        }
    }

    /**
     * 获取任务节点
     * @param taskId 任务ID
     * @return 节点列表
     */
    public List<TaskNodeEntity> getTaskNodes(Long taskId) {
        return taskNodeRepository.findByTaskId(taskId);
    }

    /**
     * 获取任务连接
     * @param taskId 任务ID
     * @return 连接列表
     */
    public List<TaskConnectionEntity> getTaskConnections(Long taskId) {
        return taskConnectionRepository.findByTaskId(taskId);
    }

    /**
     * 获取任务依赖
     * @param taskId 任务ID
     * @return 依赖信息
     */
    public List<TaskDependencyEntity> getTaskDependencies(Long taskId) {
        return taskDependencyRepository.findByTaskId(taskId);
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
        List<TaskNodeEntity> nodes = taskNodeRepository.findByTaskId(taskId);
        List<TaskConnectionEntity> connections = taskConnectionRepository.findByTaskId(taskId);
        
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
        List<TaskDependencyEntity> dependencies = taskDependencyRepository.findByTaskId(taskId);
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
        return taskErrorDataRepository.save(errorData);
    }

    @Override
    public List<TaskErrorDataEntity> getTaskErrorData(Long taskId) {
        return taskErrorDataRepository.findByTaskId(taskId);
    }

    @Override
    public List<TaskErrorDataEntity> getTaskErrorDataByType(Long taskId, String errorType) {
        return taskErrorDataRepository.findByTaskIdAndErrorType(taskId, errorType);
    }

    @Override
    public List<TaskErrorDataEntity> getTaskErrorDataByStage(Long taskId, String syncStage) {
        return taskErrorDataRepository.findByTaskIdAndSyncStage(taskId, syncStage);
    }

    @Override
    @Transactional
    public boolean retryTaskErrorData(Long errorDataId) {
        TaskErrorDataEntity errorData = taskErrorDataRepository.findById(errorDataId).orElse(null);
        if (errorData == null) {
            return false;
        }

        try {
            // 更新错误数据状态为处理中
            errorData.setProcessStatus("PROCESSING");
            errorData.setRetryCount(errorData.getRetryCount() + 1);
            errorData.setLastRetryTime(LocalDateTime.now());
            taskErrorDataRepository.save(errorData);

            // TODO: 实现具体的错误数据重试逻辑
            // 这里简化处理，模拟重试成功
            Thread.sleep(1000);

            // 更新错误数据状态为成功
            errorData.setProcessStatus("SUCCESS");
            taskErrorDataRepository.save(errorData);

            return true;
        } catch (Exception e) {
            // 更新错误数据状态为失败
            errorData.setProcessStatus("FAILED");
            errorData.setLastRetryTime(LocalDateTime.now());
            taskErrorDataRepository.save(errorData);
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
        List<TaskErrorDataEntity> errorDataList = taskErrorDataRepository.findByTaskId(taskId);
        if (errorDataList.isEmpty()) {
            return true;
        }

        taskErrorDataRepository.deleteAll(errorDataList);
        return true;
    }

    @Override
    @Transactional
    public VectorizationConfigEntity saveVectorizationConfig(VectorizationConfigEntity config) {
        return vectorizationConfigRepository.save(config);
    }

    @Override
    public List<VectorizationConfigEntity> getVectorizationConfigByTaskId(Long taskId) {
        return vectorizationConfigRepository.findByTaskId(taskId);
    }

    @Override
    public VectorizationConfigEntity getVectorizationConfigById(Long id) {
        return vectorizationConfigRepository.findById(id).orElse(null);
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
        return milvusIndexRepository.save(index);
    }

    @Override
    public List<MilvusIndexEntity> getMilvusIndexesByCollection(String collectionName) {
        return milvusIndexRepository.findByCollectionName(collectionName);
    }

    @Override
    public MilvusIndexEntity getMilvusIndexById(Long id) {
        return milvusIndexRepository.findById(id).orElse(null);
    }

    @Override
    public MilvusIndexEntity getMilvusIndexByName(String collectionName, String indexName) {
        return milvusIndexRepository.findByCollectionNameAndIndexName(collectionName, indexName);
    }

    @Override
    @Transactional
    public void updateMilvusIndexStatus(Long id, String status, Integer progress) {
        MilvusIndexEntity index = milvusIndexRepository.findById(id).orElse(null);
        if (index != null) {
            index.setStatus(status);
            index.setProgress(progress);
            milvusIndexRepository.save(index);
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
        milvusIndexRepository.deleteByCollectionNameAndIndexName(collectionName, indexName);
    }

    @Override
    @Transactional
    public boolean rebuildMilvusIndex(String collectionName, String indexName) {
        MilvusIndexEntity index = milvusIndexRepository.findByCollectionNameAndIndexName(collectionName, indexName);
        if (index == null) {
            return false;
        }

        try {
            // 更新索引状态为构建中
            index.setStatus("BUILDING");
            index.setProgress(0);
            index.setErrorMessage(null);
            milvusIndexRepository.save(index);

            // TODO: 实现具体的索引重建逻辑
            // 这里简化处理，模拟重建过程
            for (int i = 0; i <= 100; i += 10) {
                Thread.sleep(200);
                index.setProgress(i);
                milvusIndexRepository.save(index);
            }

            // 更新索引状态为就绪
            index.setStatus("READY");
            index.setProgress(100);
            milvusIndexRepository.save(index);

            return true;
        } catch (Exception e) {
            // 更新索引状态为失败
            index.setStatus("FAILED");
            index.setErrorMessage(e.getMessage());
            milvusIndexRepository.save(index);
            return false;
        }
    }

}  
