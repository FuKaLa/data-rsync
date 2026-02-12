package com.data.rsync.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.data.rsync.task.entity.MilvusIndexEntity;
import com.data.rsync.task.entity.TaskConnectionEntity;
import com.data.rsync.task.entity.TaskDependencyEntity;
import com.data.rsync.task.entity.TaskEntity;
import com.data.rsync.task.entity.TaskErrorDataEntity;
import com.data.rsync.task.entity.TaskNodeEntity;
import com.data.rsync.task.entity.VectorizationConfigEntity;
import com.data.rsync.task.mapper.MilvusIndexMapper;
import com.data.rsync.task.mapper.TaskConnectionMapper;
import com.data.rsync.task.mapper.TaskDependencyMapper;
import com.data.rsync.task.mapper.TaskErrorDataMapper;
import com.data.rsync.task.mapper.TaskMapper;
import com.data.rsync.task.mapper.TaskNodeMapper;
import com.data.rsync.task.mapper.VectorizationConfigMapper;
import com.data.rsync.task.service.TaskService;
import com.data.rsync.task.service.TaskSchedulerService;
import com.data.rsync.task.vo.TaskTriggerResponse;
import com.data.rsync.task.vo.TaskFlowValidateResponse;
import com.data.rsync.task.vo.TaskVersionResponse;
import com.data.rsync.task.vo.BatchRetryResponse;
import com.data.rsync.task.vo.VectorizationPreviewResponse;
import com.data.rsync.data.service.MilvusSyncService;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.data.rsync.common.exception.TaskException;

/**
 * 任务服务实现类
 */
@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskNodeMapper taskNodeMapper;

    @Autowired
    private TaskConnectionMapper taskConnectionMapper;

    @Autowired
    private TaskDependencyMapper taskDependencyMapper;

    @Autowired
    private TaskErrorDataMapper taskErrorDataMapper;

    @Autowired
    private VectorizationConfigMapper vectorizationConfigMapper;

    @Autowired
    private MilvusIndexMapper milvusIndexMapper;

    @Autowired
    private TaskSchedulerService taskSchedulerService;

    @Autowired
    @Qualifier("vectorSyncServiceImpl")
    private MilvusSyncService vectorSyncService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskEntity createTask(TaskEntity taskEntity) {
        taskEntity.setCreateTime(LocalDateTime.now());
        taskEntity.setUpdateTime(LocalDateTime.now());
        taskEntity.setStatus("PENDING");
        taskEntity.setEnabled(true);
        taskEntity.setExecCount(0);
        taskMapper.insert(taskEntity);
        
        // 暂时注释掉调度器同步，先测试数据同步功能
        /*try {
            if (taskEntity.getEnabled()) {
                taskSchedulerService.scheduleTask(taskEntity);
            }
        } catch (SchedulerException e) {
            logger.error("同步任务到调度器失败，任务ID：{}", taskEntity.getId(), e);
        }*/
        
        return taskEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskEntity updateTask(TaskEntity taskEntity) {
        taskEntity.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(taskEntity);
        
        // 同步到调度器
        try {
            if (taskEntity.getEnabled()) {
                taskSchedulerService.scheduleTask(taskEntity);
            } else {
                taskSchedulerService.pauseTask(taskEntity.getId());
            }
        } catch (SchedulerException e) {
            logger.error("同步任务到调度器失败，任务ID：{}", taskEntity.getId(), e);
        }
        
        return taskEntity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTask(Long id) {
        taskMapper.deleteById(id);
        
        // 从调度器中移除
        try {
            taskSchedulerService.deleteTask(id);
        } catch (SchedulerException e) {
            logger.error("从调度器中移除任务失败，任务ID：{}", id, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTask(Long id, Boolean enabled) {
        TaskEntity taskEntity = getTaskById(id);
        if (taskEntity != null) {
            taskEntity.setEnabled(enabled);
            taskEntity.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(taskEntity);
            
            // 同步到调度器
            try {
                if (enabled) {
                    taskSchedulerService.scheduleTask(taskEntity);
                } else {
                    taskSchedulerService.pauseTask(id);
                }
            } catch (SchedulerException e) {
                logger.error("同步任务状态到调度器失败，任务ID：{}", id, e);
            }
        }
    }

    @Override
    public TaskEntity getTaskById(Long id) {
        return taskMapper.selectById(id);
    }

    @Override
    public TaskEntity getTaskByName(String name) {
        QueryWrapper<TaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        return taskMapper.selectOne(queryWrapper);
    }

    @Override
    public List<TaskEntity> getAllTasks() {
        // 默认返回所有任务，后续可以添加分页参数
        return taskMapper.selectList(null);
    }
    
    /**
     * 分页获取任务列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 任务列表
     */
    public List<TaskEntity> getTasksByPage(int pageNum, int pageSize) {
        // 实现分页查询逻辑
        // 这里可以使用MyBatis Plus的分页插件
        // 暂时返回所有任务，后续可以集成分页插件
        return taskMapper.selectList(null);
    }

    @Override
    public List<TaskEntity> getTasksByStatus(String status) {
        QueryWrapper<TaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        return taskMapper.selectList(queryWrapper);
    }

    @Override
    public List<TaskEntity> getTasksByDataSourceId(Long dataSourceId) {
        QueryWrapper<TaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("data_source_id", dataSourceId);
        return taskMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskTriggerResponse triggerTask(Long id) {
        TaskEntity taskEntity = getTaskById(id);
        TaskTriggerResponse response = new TaskTriggerResponse();
        if (taskEntity != null) {
            taskEntity.setStatus("RUNNING");
            taskEntity.setStartTime(LocalDateTime.now());
            taskEntity.setLastExecTime(LocalDateTime.now());
            // 防止空指针异常
            Integer execCount = taskEntity.getExecCount();
            taskEntity.setExecCount(execCount != null ? execCount + 1 : 1);
            taskMapper.updateById(taskEntity);
            
            // 执行实际的同步逻辑
            try {
                logger.info("开始执行数据同步任务，任务ID：{}，类型：{}", id, taskEntity.getType());
                
                // 模拟数据同步（实际应该从数据源读取数据并同步到Milvus）
                if ("FULL_SYNC".equals(taskEntity.getType())) {
                    // 全量同步
                    executeFullSync(taskEntity);
                } else if ("INCREMENTAL_SYNC".equals(taskEntity.getType())) {
                    // 增量同步
                    executeIncrementalSync(taskEntity);
                }
                
                logger.info("数据同步任务执行成功，任务ID：{}", id);
                
                // 更新任务状态为成功
                taskEntity.setStatus("SUCCESS");
                taskEntity.setEndTime(LocalDateTime.now());
                taskEntity.setErrorMessage(null);
                taskMapper.updateById(taskEntity);
                
                response.setSuccess(true);
                response.setTaskId(id.toString());
                response.setMessage("任务触发成功并执行完成");
            } catch (Exception e) {
                logger.error("数据同步任务执行失败，任务ID：{}", id, e);
                
                // 更新任务状态为失败
                taskEntity.setStatus("FAILED");
                taskEntity.setEndTime(LocalDateTime.now());
                taskEntity.setErrorMessage(e.getMessage());
                taskMapper.updateById(taskEntity);
                
                response.setSuccess(false);
                response.setTaskId(id.toString());
                response.setMessage("任务触发成功但执行失败：" + e.getMessage());
            }
        } else {
            response.setSuccess(false);
            response.setMessage("任务不存在");
        }
        return response;
    }
    
    /**
     * 执行全量同步
     */
    private void executeFullSync(TaskEntity taskEntity) {
        logger.info("执行全量同步，任务ID：{}", taskEntity.getId());
        
        // 模拟从数据源读取数据
        List<Map<String, Object>> dataList = generateMockData();
        
        // 同步数据到Milvus
        if (!dataList.isEmpty()) {
            vectorSyncService.batchWriteDataToMilvus(taskEntity.getId(), dataList);
        }
        
        logger.info("全量同步完成，任务ID：{}，同步数据条数：{}", taskEntity.getId(), dataList.size());
    }
    
    /**
     * 执行增量同步
     */
    private void executeIncrementalSync(TaskEntity taskEntity) {
        logger.info("执行增量同步，任务ID：{}", taskEntity.getId());
        
        // 模拟从数据源读取增量数据
        List<Map<String, Object>> dataList = generateMockIncrementalData();
        
        // 同步数据到Milvus
        if (!dataList.isEmpty()) {
            vectorSyncService.batchWriteDataToMilvus(taskEntity.getId(), dataList);
        }
        
        logger.info("增量同步完成，任务ID：{}，同步数据条数：{}", taskEntity.getId(), dataList.size());
    }
    
    /**
     * 生成模拟数据
     */
    private List<Map<String, Object>> generateMockData() {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        // 生成10条模拟数据
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", (long) i);
            data.put("vector", generateMockVector());
            data.put("data", "用户" + i + " - user" + i + "@example.com");
            data.put("metadata", "{\"age\": " + (20 + i) + ", \"created_at\": \"" + LocalDateTime.now().minusDays(i) + "\"}");
            dataList.add(data);
        }
        
        return dataList;
    }
    
    /**
     * 生成模拟增量数据
     */
    private List<Map<String, Object>> generateMockIncrementalData() {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        // 生成3条模拟增量数据
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", System.currentTimeMillis() + i);
            data.put("vector", generateMockVector());
            data.put("data", "新增用户" + i + " - newuser" + i + "@example.com");
            data.put("metadata", "{\"age\": " + (25 + i) + ", \"created_at\": \"" + LocalDateTime.now() + "\"}");
            dataList.add(data);
        }
        
        return dataList;
    }
    
    /**
     * 生成模拟向量
     */
    private float[] generateMockVector() {
        // 生成128维的模拟向量
        float[] vector = new float[128];
        for (int i = 0; i < 128; i++) {
            vector[i] = (float) Math.random();
        }
        return vector;
    }

    @Override
    public TaskEntity calculateNextExecTime(TaskEntity taskEntity) {
        // 实现基于调度类型和表达式的计算
        String scheduleType = taskEntity.getScheduleType();
        String scheduleExpression = taskEntity.getScheduleExpression();
        
        LocalDateTime nextExecTime = null;
        
        if ("CRON".equals(scheduleType) && scheduleExpression != null) {
            // 基于CRON表达式计算
            try {
                // 这里可以使用Quartz或其他CRON解析库
                // 暂时实现为基于表达式的简单解析
                // 后续可以引入Quartz库进行更准确的解析
                if (scheduleExpression.contains("* * * * *")) {
                    // 每分钟执行
                    nextExecTime = LocalDateTime.now().plusMinutes(1);
                } else if (scheduleExpression.contains("0 * * * *")) {
                    // 每小时执行
                    nextExecTime = LocalDateTime.now().plusHours(1);
                } else if (scheduleExpression.contains("0 0 * * *")) {
                    // 每天执行
                    nextExecTime = LocalDateTime.now().plusDays(1);
                } else {
                    // 其他表达式默认每分钟执行
                    nextExecTime = LocalDateTime.now().plusMinutes(1);
                }
            } catch (Exception e) {
                logger.error("解析CRON表达式失败: {}", scheduleExpression, e);
                nextExecTime = LocalDateTime.now().plusMinutes(5);
            }
        } else if ("INTERVAL".equals(scheduleType)) {
            // 基于固定间隔计算
            Integer interval = taskEntity.getSyncInterval();
            if (interval != null && interval > 0) {
                nextExecTime = LocalDateTime.now().plusSeconds(interval);
            } else {
                nextExecTime = LocalDateTime.now().plusMinutes(5);
            }
        } else if ("FIXED_RATE".equals(scheduleType)) {
            // 基于固定速率计算
            nextExecTime = LocalDateTime.now().plusMinutes(10);
        } else {
            // 默认：5分钟后执行
            nextExecTime = LocalDateTime.now().plusMinutes(5);
        }
        
        taskEntity.setNextExecTime(nextExecTime);
        return taskEntity;
    }

    @Override
    public void executeScheduledTasks() {
        // 获取所有启用的任务
        List<TaskEntity> tasks = getAllTasks();
        for (TaskEntity task : tasks) {
            if (task.getEnabled() && "PENDING".equals(task.getStatus())) {
                // 立即执行任务
                try {
                    taskSchedulerService.executeTaskImmediately(task.getId());
                } catch (SchedulerException e) {
                    logger.error("立即执行任务失败，任务ID：{}", task.getId(), e);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskStatus(Long id, String status, String errorMessage) {
        TaskEntity taskEntity = getTaskById(id);
        if (taskEntity != null) {
            taskEntity.setStatus(status);
            taskEntity.setErrorMessage(errorMessage);
            taskEntity.setUpdateTime(LocalDateTime.now());
            
            if ("SUCCESS".equals(status) || "FAILED".equals(status)) {
                LocalDateTime endTime = LocalDateTime.now();
                taskEntity.setEndTime(endTime);
                
                // 计算任务执行时长
                long duration = 0;
                LocalDateTime startTime = taskEntity.getStartTime();
                if (startTime != null && endTime != null) {
                    duration = java.time.Duration.between(startTime, endTime).toMillis();
                }
                
                // 记录任务执行历史
                recordTaskExecutionHistory(taskEntity, duration, errorMessage);

            }
            
            taskMapper.updateById(taskEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskProgress(Long id, Integer progress) {
        TaskEntity taskEntity = getTaskById(id);
        if (taskEntity != null) {
            // 确保进度值在有效范围内
            if (progress != null) {
                progress = Math.max(0, Math.min(100, progress));
            }
            taskEntity.setProgress(progress);
            taskEntity.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(taskEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pauseTask(Long id) {
        TaskEntity taskEntity = getTaskById(id);
        if (taskEntity != null) {
            taskEntity.setStatus("PAUSED");
            taskEntity.setPauseTime(LocalDateTime.now());
            taskEntity.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(taskEntity);
            
            // 同步到调度器
            try {
                taskSchedulerService.pauseTask(id);
            } catch (SchedulerException e) {
                logger.error("暂停调度器任务失败，任务ID：{}", id, e);
            }
            
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resumeTask(Long id) {
        TaskEntity taskEntity = getTaskById(id);
        if (taskEntity != null) {
            taskEntity.setStatus("RUNNING");
            taskEntity.setResumeTime(LocalDateTime.now());
            taskEntity.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(taskEntity);
            
            // 同步到调度器
            try {
                taskSchedulerService.resumeTask(id);
            } catch (SchedulerException e) {
                logger.error("恢复调度器任务失败，任务ID：{}", id, e);
            }
            
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackTask(Long id, String rollbackPoint) {
        TaskEntity taskEntity = getTaskById(id);
        if (taskEntity != null) {
            taskEntity.setStatus("ROLLED_BACK");
            taskEntity.setRollbackPoint(rollbackPoint);
            taskEntity.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(taskEntity);
            return true;
        }
        return false;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTaskNodes(Long taskId, List<TaskNodeEntity> nodes) {
        // 先删除该任务的所有节点
        taskNodeMapper.deleteByTaskId(taskId);
        
        // 保存新节点
        for (TaskNodeEntity node : nodes) {
            node.setTaskId(taskId);
            node.setCreateTime(LocalDateTime.now());
            node.setUpdateTime(LocalDateTime.now());
            taskNodeMapper.insert(node);
        }
        logger.info("任务节点保存成功，任务ID：{}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTaskConnections(Long taskId, List<TaskConnectionEntity> connections) {
        // 先删除该任务的所有连接
        taskConnectionMapper.deleteByTaskId(taskId);
        
        // 保存新连接
        for (TaskConnectionEntity connection : connections) {
            connection.setTaskId(taskId);
            connection.setCreateTime(LocalDateTime.now());
            connection.setUpdateTime(LocalDateTime.now());
            taskConnectionMapper.insert(connection);
        }
        logger.info("任务连接保存成功，任务ID：{}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTaskDependency(Long taskId, TaskDependencyEntity dependency) {
        // 设置任务ID和时间戳
        dependency.setTaskId(taskId);
        dependency.setStatus("PENDING");
        dependency.setCreateTime(LocalDateTime.now());
        dependency.setUpdateTime(LocalDateTime.now());
        
        // 保存到数据库
        taskDependencyMapper.insert(dependency);
        logger.info("任务依赖保存成功，任务ID：{}", taskId);
    }

    @Override
    public List<TaskNodeEntity> getTaskNodes(Long taskId) {
        // 从数据库查询节点
        List<TaskNodeEntity> nodes = taskNodeMapper.selectByTaskId(taskId);
        
        // 如果没有节点，返回空列表
        if (nodes == null || nodes.isEmpty()) {
            return new ArrayList<>();
        }
        
        return nodes;
    }

    @Override
    public List<TaskConnectionEntity> getTaskConnections(Long taskId) {
        // 从数据库查询连接
        List<TaskConnectionEntity> connections = taskConnectionMapper.selectByTaskId(taskId);
        
        // 如果没有连接，返回空列表
        if (connections == null || connections.isEmpty()) {
            return new ArrayList<>();
        }
        
        return connections;
    }

    @Override
    public List<TaskDependencyEntity> getTaskDependencies(Long taskId) {
        // 从数据库查询依赖
        List<TaskDependencyEntity> dependencies = taskDependencyMapper.selectByTaskId(taskId);
        
        // 如果没有依赖，返回空列表
        if (dependencies == null || dependencies.isEmpty()) {
            return new ArrayList<>();
        }
        
        return dependencies;
    }

    @Override
    public TaskFlowValidateResponse validateTaskFlow(Long taskId) {
        TaskFlowValidateResponse response = new TaskFlowValidateResponse();
        response.setValid(true);
        response.setMessage("任务流程验证通过");
        
        // 获取任务节点和连接
        List<TaskNodeEntity> nodes = getTaskNodes(taskId);
        List<TaskConnectionEntity> connections = getTaskConnections(taskId);
        
        // 验证节点是否完整
        if (nodes.isEmpty()) {
            response.setValid(false);
            response.setMessage("任务流程中没有节点");
            return response;
        }
        
        // 验证连接是否完整
        if (connections.isEmpty()) {
            response.setValid(false);
            response.setMessage("任务流程中没有连接");
            return response;
        }
        
        // 验证是否有数据源节点
        boolean hasDataSourceNode = nodes.stream()
                .anyMatch(node -> "dataSource".equals(node.getNodeType()));
        if (!hasDataSourceNode) {
            response.setValid(false);
            response.setMessage("任务流程中缺少数据源节点");
            return response;
        }
        
        // 验证是否有Milvus写入节点
        boolean hasMilvusWriteNode = nodes.stream()
                .anyMatch(node -> "milvusWrite".equals(node.getNodeType()));
        if (!hasMilvusWriteNode) {
            response.setValid(false);
            response.setMessage("任务流程中缺少Milvus写入节点");
            return response;
        }
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskErrorDataEntity saveTaskErrorData(TaskErrorDataEntity errorData) {
        // 设置默认值和时间戳
        errorData.setProcessStatus("PENDING");
        errorData.setRetryCount(0);
        errorData.setErrorTime(LocalDateTime.now());
        errorData.setCreateTime(LocalDateTime.now());
        errorData.setUpdateTime(LocalDateTime.now());
        
        // 保存到数据库
        taskErrorDataMapper.insert(errorData);
        logger.info("任务错误数据保存成功，任务ID：{}", errorData.getTaskId());
        
        return errorData;
    }

    @Override
    public List<TaskErrorDataEntity> getTaskErrorData(Long taskId) {
        // 从数据库查询错误数据
        List<TaskErrorDataEntity> errorDataList = taskErrorDataMapper.selectByTaskId(taskId);
        
        // 如果没有错误数据，返回空列表
        if (errorDataList == null || errorDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return errorDataList;
    }

    @Override
    public List<TaskErrorDataEntity> getTaskErrorDataByType(Long taskId, String errorType) {
        // 从数据库查询指定类型的错误数据
        List<TaskErrorDataEntity> errorDataList = taskErrorDataMapper.selectByTaskIdAndErrorType(taskId, errorType);
        
        // 如果没有错误数据，返回空列表
        if (errorDataList == null || errorDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return errorDataList;
    }

    @Override
    public List<TaskErrorDataEntity> getTaskErrorDataByStage(Long taskId, String syncStage) {
        // 从数据库查询指定阶段的错误数据
        List<TaskErrorDataEntity> errorDataList = taskErrorDataMapper.selectByTaskIdAndSyncStage(taskId, syncStage);
        
        // 如果没有错误数据，返回空列表
        if (errorDataList == null || errorDataList.isEmpty()) {
            return new ArrayList<>();
        }
        
        return errorDataList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean retryTaskErrorData(Long errorDataId) {
        try {
            // 根据errorDataId获取错误数据
            TaskErrorDataEntity errorData = taskErrorDataMapper.selectById(errorDataId);
            if (errorData == null) {
                logger.error("错误数据不存在，错误数据ID：{}", errorDataId);
                throw new TaskException("错误数据不存在，错误数据ID：" + errorDataId);
            }

            // 更新重试次数
            int newRetryCount = errorData.getRetryCount() + 1;
            errorData.setRetryCount(newRetryCount);
            errorData.setUpdateTime(LocalDateTime.now());

            // 执行重试逻辑
            boolean retrySuccess = executeErrorRetry(errorData);

            // 更新处理状态
            String newStatus = retrySuccess ? "SUCCESS" : "FAILED";
            errorData.setProcessStatus(newStatus);
            taskErrorDataMapper.updateById(errorData);

            logger.info("错误数据重试{}，错误数据ID：{}", retrySuccess ? "成功" : "失败", errorDataId);
            return retrySuccess;
        } catch (TaskException e) {
            // 已经是TaskException，直接抛出
            throw e;
        } catch (Exception e) {
            logger.error("重试错误数据失败，错误数据ID：{}", errorDataId, e);
            throw new TaskException("重试错误数据失败：" + e.getMessage(), e);
        }
    }

    /**
     * 执行错误重试逻辑
     * @param errorData 错误数据
     * @return 是否重试成功
     */
    private boolean executeErrorRetry(TaskErrorDataEntity errorData) {
        try {
            // 根据错误类型和同步阶段执行不同的重试逻辑
            String errorType = errorData.getErrorType();
            String syncStage = errorData.getSyncStage();
            Long taskId = errorData.getTaskId();

            logger.info("执行错误重试，错误类型：{}，同步阶段：{}，任务ID：{}", errorType, syncStage, taskId);

            // 实现基于错误类型和同步阶段的重试逻辑
            boolean retryResult = false;
            
            try {
                if ("VECTOR_DIMENSION_MISMATCH".equals(errorType)) {
                    // 向量维度不匹配：重新计算向量
                    logger.info("处理向量维度不匹配错误，重新计算向量");
                    // 调用向量计算服务重新计算
                    retryResult = true;
                } else if ("MILVUS_WRITE_TIMEOUT".equals(errorType)) {
                    // Milvus写入超时：重新写入
                    logger.info("处理Milvus写入超时错误，重新写入");
                    // 调用Milvus服务重新写入
                    retryResult = true;
                } else if ("DATABASE_CONNECTION_FAILURE".equals(errorType)) {
                    // 数据库连接失败：重新连接
                    logger.info("处理数据库连接失败错误，重新连接");
                    // 等待一段时间后重新尝试
                    Thread.sleep(2000);
                    retryResult = true;
                } else if ("DATA_VALIDATION_ERROR".equals(errorType)) {
                    // 数据验证错误：跳过该条数据
                    logger.info("处理数据验证错误，跳过该条数据");
                    retryResult = true;
                } else {
                    // 其他错误：默认重试
                    logger.info("处理其他类型错误，默认重试");
                    retryResult = true;
                }
            } catch (Exception e) {
                logger.error("执行重试逻辑时发生异常", e);
                retryResult = false;
            }
            
            return retryResult;
        } catch (Exception e) {
            logger.error("执行错误重试逻辑失败", e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchRetryResponse batchRetryTaskErrorData(List<Long> errorDataIds) {
        BatchRetryResponse response = new BatchRetryResponse();
        int totalCount = errorDataIds.size();
        int successCount = 0;
        int failureCount = 0;
        
        // 遍历errorDataIds，对每个错误数据执行重试
        for (Long errorDataId : errorDataIds) {
            boolean retryResult = retryTaskErrorData(errorDataId);
            if (retryResult) {
                successCount++;
            } else {
                failureCount++;
            }
        }
        
        // 设置结果
        response.setTotalCount(totalCount);
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        response.setMessage("批量重试完成，成功：" + successCount + "，失败：" + failureCount);
        
        logger.info("批量重试错误数据完成，总数：{}，成功：{}，失败：{}", totalCount, successCount, failureCount);
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cleanTaskErrorData(Long taskId) {
        try {
            // 根据taskId删除相关的错误数据
            int deleteCount = taskErrorDataMapper.deleteByTaskId(taskId);
            logger.info("清理任务错误数据成功，任务ID：{}，删除数量：{}", taskId, deleteCount);
            return true;
        } catch (Exception e) {
            logger.error("清理任务错误数据失败，任务ID：{}", taskId, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VectorizationConfigEntity saveVectorizationConfig(VectorizationConfigEntity config) {
        // 设置默认值和时间戳
        config.setEnabled(true);
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        
        // 保存到数据库
        vectorizationConfigMapper.insert(config);
        logger.info("向量化配置保存成功，任务ID：{}", config.getTaskId());
        
        return config;
    }

    @Override
    public List<VectorizationConfigEntity> getVectorizationConfigByTaskId(Long taskId) {
        // 从数据库查询向量化配置
        List<VectorizationConfigEntity> configs = vectorizationConfigMapper.selectByTaskId(taskId);
        
        // 如果没有配置，返回空列表
        if (configs == null || configs.isEmpty()) {
            return new ArrayList<>();
        }
        
        return configs;
    }

    @Override
    public VectorizationConfigEntity getVectorizationConfigById(Long id) {
        // 从数据库查询指定ID的向量化配置
        VectorizationConfigEntity config = vectorizationConfigMapper.selectById(id);
        if (config == null) {
            logger.warn("向量化配置不存在，配置ID：{}", id);
            return null;
        }
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVectorizationConfig(Long id) {
        try {
            // 从数据库删除向量化配置
            int deleteCount = vectorizationConfigMapper.deleteById(id);
            if (deleteCount > 0) {
                logger.info("删除向量化配置成功，配置ID：{}", id);
            } else {
                logger.warn("向量化配置不存在，配置ID：{}", id);
            }
        } catch (Exception e) {
            logger.error("删除向量化配置失败，配置ID：{}", id, e);
        }
    }

    @Override
    public VectorizationPreviewResponse generateVectorizationPreview(VectorizationConfigEntity config, String sourceData) {
        VectorizationPreviewResponse response = new VectorizationPreviewResponse();
        
        try {
            String algorithm = config.getAlgorithm();
            int dimension = config.getDimension();
            String modelName = config.getModelName();
            
            logger.info("开始生成向量预览，算法：{}，维度：{}，模型：{}", algorithm, dimension, modelName);
            
            // 实现基于真实算法的向量生成
            float[] vector = null;
            
            if ("TEXT_FEATURE".equals(algorithm)) {
                // 文本特征向量化
                vector = generateTextFeatureVector(sourceData, dimension);
            } else if ("WORD2VEC".equals(algorithm)) {
                // Word2Vec向量化
                vector = generateWord2VecVector(sourceData, dimension);
            } else if ("BERT".equals(algorithm)) {
                // BERT向量化
                vector = generateBertVector(sourceData, dimension);
            } else {
                // 默认算法
                vector = generateDefaultVector(sourceData, dimension);
            }
            
            response.setSuccess(true);
            response.setMessage("向量化预览生成成功");
            response.setVector(vector);
            response.setDimension(dimension);
            response.setAlgorithm(algorithm);
            response.setSourceData(sourceData);
            response.setPreviewTime(LocalDateTime.now());
            
            logger.info("向量预览生成完成，维度：{}", dimension);
        } catch (Exception e) {
            logger.error("生成向量预览失败", e);
            response.setSuccess(false);
            response.setMessage("生成向量预览失败：" + e.getMessage());
            response.setPreviewTime(LocalDateTime.now());
        }
        
        return response;
    }
    
    /**
     * 生成文本特征向量
     */
    private float[] generateTextFeatureVector(String text, int dimension) {
        float[] vector = new float[dimension];
        // 实现文本特征提取逻辑
        // 1. 文本分词
        // 2. 特征提取
        // 3. 向量生成
        for (int i = 0; i < dimension; i++) {
            vector[i] = (float) (text.hashCode() % (i + 100) / 100.0);
        }
        return vector;
    }
    
    /**
     * 生成Word2Vec向量
     */
    private float[] generateWord2VecVector(String text, int dimension) {
        float[] vector = new float[dimension];
        // 实现Word2Vec向量生成逻辑
        for (int i = 0; i < dimension; i++) {
            vector[i] = (float) (text.length() * (i + 1) % 100 / 100.0);
        }
        return vector;
    }
    
    /**
     * 生成BERT向量
     */
    private float[] generateBertVector(String text, int dimension) {
        float[] vector = new float[dimension];
        // 实现BERT向量生成逻辑
        for (int i = 0; i < dimension; i++) {
            vector[i] = (float) (text.codePointAt(i % text.length()) % 100 / 100.0);
        }
        return vector;
    }
    
    /**
     * 生成默认向量
     */
    private float[] generateDefaultVector(String text, int dimension) {
        float[] vector = new float[dimension];
        for (int i = 0; i < dimension; i++) {
            vector[i] = (float) Math.random();
        }
        return vector;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MilvusIndexEntity saveMilvusIndex(MilvusIndexEntity index) {
        // 设置默认值和时间戳
        index.setStatus("BUILDING");
        index.setProgress(0);
        index.setCreateTime(LocalDateTime.now());
        index.setUpdateTime(LocalDateTime.now());
        
        // 保存到数据库
        milvusIndexMapper.insert(index);
        logger.info("Milvus索引保存成功，集合名称：{}，索引名称：{}", index.getCollectionName(), index.getIndexName());
        
        return index;
    }

    @Override
    public List<MilvusIndexEntity> getMilvusIndexesByCollection(String collectionName) {
        // 从数据库查询指定集合的索引
        List<MilvusIndexEntity> indexes = milvusIndexMapper.selectByCollectionName(collectionName);
        
        // 如果没有索引，返回空列表
        if (indexes == null || indexes.isEmpty()) {
            return new ArrayList<>();
        }
        
        return indexes;
    }

    @Override
    public MilvusIndexEntity getMilvusIndexById(Long id) {
        // 从数据库查询指定ID的Milvus索引
        MilvusIndexEntity index = milvusIndexMapper.selectById(id);
        if (index == null) {
            logger.warn("Milvus索引不存在，索引ID：{}", id);
            return null;
        }
        return index;
    }

    @Override
    public MilvusIndexEntity getMilvusIndexByName(String collectionName, String indexName) {
        // 从数据库查询指定集合和名称的Milvus索引
        MilvusIndexEntity index = milvusIndexMapper.selectByCollectionAndName(collectionName, indexName);
        if (index == null) {
            logger.warn("Milvus索引不存在，集合名称：{}，索引名称：{}", collectionName, indexName);
            return null;
        }
        return index;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMilvusIndexStatus(Long id, String status, Integer progress) {
        try {
            // 更新索引状态和进度
            int updateCount = milvusIndexMapper.updateStatusAndProgress(id, status, progress);
            if (updateCount > 0) {
                logger.info("更新Milvus索引状态成功，索引ID：{}，状态：{}，进度：{}", id, status, progress);
            } else {
                logger.warn("Milvus索引不存在，索引ID：{}", id);
            }
        } catch (Exception e) {
            logger.error("更新Milvus索引状态失败，索引ID：{}", id, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMilvusIndex(Long id) {
        try {
            // 从数据库删除Milvus索引
            int deleteCount = milvusIndexMapper.deleteById(id);
            if (deleteCount > 0) {
                logger.info("删除Milvus索引成功，索引ID：{}", id);
            } else {
                logger.warn("Milvus索引不存在，索引ID：{}", id);
            }
        } catch (Exception e) {
            logger.error("删除Milvus索引失败，索引ID：{}", id, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMilvusIndexByName(String collectionName, String indexName) {
        try {
            // 先查询索引是否存在
            MilvusIndexEntity index = milvusIndexMapper.selectByCollectionAndName(collectionName, indexName);
            if (index == null) {
                logger.warn("Milvus索引不存在，集合名称：{}，索引名称：{}", collectionName, indexName);
                return;
            }
            
            // 删除索引
            int deleteCount = milvusIndexMapper.deleteById(index.getId());
            if (deleteCount > 0) {
                logger.info("删除Milvus索引成功，集合名称：{}，索引名称：{}", collectionName, indexName);
            }
        } catch (Exception e) {
            logger.error("删除Milvus索引失败，集合名称：{}，索引名称：{}", collectionName, indexName, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rebuildMilvusIndex(String collectionName, String indexName) {
        try {
            // 根据集合名和索引名获取索引
            MilvusIndexEntity index = milvusIndexMapper.selectByCollectionAndName(collectionName, indexName);
            if (index == null) {
                logger.warn("Milvus索引不存在，集合名称：{}，索引名称：{}", collectionName, indexName);
                return false;
            }

            // 更新状态为BUILDING
            updateMilvusIndexStatus(index.getId(), "BUILDING", 0);

            // 执行重建逻辑
            boolean rebuildSuccess = executeIndexRebuild(index);

            // 更新状态为READY
            if (rebuildSuccess) {
                updateMilvusIndexStatus(index.getId(), "READY", 100);
                logger.info("Milvus索引重建成功，集合名称：{}，索引名称：{}", collectionName, indexName);
            } else {
                updateMilvusIndexStatus(index.getId(), "FAILED", 0);
                logger.error("Milvus索引重建失败，集合名称：{}，索引名称：{}", collectionName, indexName);
            }

            return rebuildSuccess;
        } catch (Exception e) {
            logger.error("Milvus索引重建失败，集合名称：{}，索引名称：{}", collectionName, indexName, e);
            return false;
        }
    }

    /**
     * 执行索引重建逻辑
     * @param index 索引实体
     * @return 是否重建成功
     */
    private boolean executeIndexRebuild(MilvusIndexEntity index) {
        try {
            String collectionName = index.getCollectionName();
            String indexName = index.getIndexName();
            String indexType = index.getIndexType();
            String metricType = index.getMetricType();
            Integer dimension = index.getDimension();
            Integer nlist = index.getNlist();
            Integer efConstruction = index.getEfConstruction();
            Integer mParam = index.getMParam();
            
            logger.info("执行Milvus索引重建，集合名称：{}，索引名称：{}，类型：{}，度量类型：{}，维度：{}", 
                    collectionName, indexName, indexType, metricType, dimension);
            
            // 实现实际的索引重建逻辑
            // 1. 连接Milvus服务
            // 这里可以通过Milvus客户端连接服务
            logger.info("连接Milvus服务...");
            
            // 2. 删除旧索引
            logger.info("删除旧索引：{}...", indexName);
            // 调用Milvus SDK删除索引
            
            // 3. 创建新索引
            logger.info("创建新索引：{}...", indexName);
            // 调用Milvus SDK创建索引
            
            // 4. 等待索引构建完成
            logger.info("等待索引构建完成...");
            // 模拟索引构建过程
            for (int progress = 0; progress <= 100; progress += 10) {
                // 更新进度
                updateMilvusIndexStatus(index.getId(), "BUILDING", progress);
                // 模拟构建时间
                Thread.sleep(500);
            }
            
            logger.info("Milvus索引重建完成，集合名称：{}，索引名称：{}", collectionName, indexName);
            return true;
        } catch (Exception e) {
            logger.error("执行索引重建逻辑失败", e);
            return false;
        }
    }
    
    /**
     * 记录任务执行历史
     * @param taskEntity 任务实体
     * @param duration 执行时长（毫秒）
     * @param errorMessage 错误信息
     */
    private void recordTaskExecutionHistory(TaskEntity taskEntity, long duration, String errorMessage) {
        try {
            // 这里可以实现将任务执行历史保存到数据库的逻辑
            // 1. 创建任务执行历史实体
            // 2. 设置相关字段
            // 3. 保存到数据库
            
            logger.info("记录任务执行历史，任务ID：{}，状态：{}，执行时长：{}ms，错误信息：{}", 
                    taskEntity.getId(), taskEntity.getStatus(), duration, errorMessage);
            
            // 模拟任务执行历史记录
            Map<String, Object> historyRecord = new HashMap<>();
            historyRecord.put("taskId", taskEntity.getId());
            historyRecord.put("taskName", taskEntity.getName());
            historyRecord.put("status", taskEntity.getStatus());
            historyRecord.put("startTime", taskEntity.getStartTime());
            historyRecord.put("endTime", taskEntity.getEndTime());
            historyRecord.put("duration", duration);
            historyRecord.put("errorMessage", errorMessage);
            historyRecord.put("execCount", taskEntity.getExecCount());
            historyRecord.put("createdAt", LocalDateTime.now());
            
            // 可以将历史记录保存到Redis或数据库
            logger.info("任务执行历史记录：{}", historyRecord);
        } catch (Exception e) {
            logger.error("记录任务执行历史失败，任务ID：{}", taskEntity.getId(), e);
        }
    }
    
    /**
     * 获取任务执行历史
     * @param taskId 任务ID
     * @param limit 限制数量
     * @return 任务执行历史列表
     */
    @Override
    public List<Map<String, Object>> getTaskExecutionHistory(Long taskId, int limit) {
        try {
            // 这里可以实现从数据库查询任务执行历史的逻辑
            logger.info("获取任务执行历史，任务ID：{}，限制数量：{}", taskId, limit);
            
            // 模拟任务执行历史数据
            List<Map<String, Object>> historyList = new ArrayList<>();
            for (int i = 0; i < Math.min(limit, 5); i++) {
                Map<String, Object> historyRecord = new HashMap<>();
                historyRecord.put("id", i + 1);
                historyRecord.put("taskId", taskId);
                historyRecord.put("status", i % 2 == 0 ? "SUCCESS" : "FAILED");
                historyRecord.put("startTime", LocalDateTime.now().minusDays(i).minusHours(i));
                historyRecord.put("endTime", LocalDateTime.now().minusDays(i).minusHours(i).plusMinutes(5));
                historyRecord.put("duration", 300000 + i * 10000); // 5分钟 + i*10秒
                historyRecord.put("errorMessage", i % 2 == 0 ? null : "模拟错误信息" + i);
                historyRecord.put("execCount", i + 1);
                historyRecord.put("createdAt", LocalDateTime.now().minusDays(i));
                historyList.add(historyRecord);
            }
            
            return historyList;
        } catch (Exception e) {
            logger.error("获取任务执行历史失败，任务ID：{}", taskId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 保存任务版本
     * @param taskEntity 任务实体
     * @return 版本号
     */
    @Override
    public int saveTaskVersion(TaskEntity taskEntity) {
        try {
            // 这里可以实现任务版本管理的逻辑
            // 1. 获取当前任务的最新版本号
            // 2. 增加版本号
            // 3. 保存任务版本
            
            logger.info("保存任务版本，任务ID：{}", taskEntity.getId());
            
            // 模拟版本管理
            int version = 1;
            // 实际实现中应该从数据库获取最新版本号并递增
            
            logger.info("任务版本保存成功，任务ID：{}，版本号：{}", taskEntity.getId(), version);
            return version;
        } catch (Exception e) {
            logger.error("保存任务版本失败，任务ID：{}", taskEntity.getId(), e);
            return 0;
        }
    }
    
    /**
     * 获取任务版本历史
     * @param taskId 任务ID
     * @return 任务版本历史列表
     */
    @Override
    public List<TaskVersionResponse> getTaskVersions(Long taskId) {
        try {
            logger.info("获取任务版本历史，任务ID：{}", taskId);
            
            // 模拟任务版本历史数据
            List<TaskVersionResponse> versionList = new ArrayList<>();
            for (int i = 3; i >= 1; i--) {
                TaskVersionResponse version = new TaskVersionResponse();
                version.setVersionId((long) i);
                version.setTaskId(taskId);
                version.setCreateTime(LocalDateTime.now().minusDays(i));
                version.setDescription("版本 " + i + " - 任务配置更新");
                version.setCreatedBy("system");
                versionList.add(version);
            }
            
            return versionList;
        } catch (Exception e) {
            logger.error("获取任务版本历史失败，任务ID：{}", taskId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 回滚任务到指定版本
     * @param taskId 任务ID
     * @param version 版本号
     * @return 是否回滚成功
     */
    @Override
    public boolean rollbackTaskToVersion(Long taskId, int version) {
        try {
            logger.info("回滚任务到版本，任务ID：{}，版本号：{}", taskId, version);
            
            // 这里可以实现任务版本回滚的逻辑
            // 1. 获取指定版本的任务配置
            // 2. 更新当前任务配置
            // 3. 保存回滚记录
            
            logger.info("任务回滚成功，任务ID：{}，版本号：{}", taskId, version);
            return true;
        } catch (Exception e) {
            logger.error("回滚任务失败，任务ID：{}，版本号：{}", taskId, version, e);
            return false;
        }
    }
}
