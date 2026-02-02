package com.data.rsync.task.manager.service.impl;

import com.data.rsync.task.manager.entity.TaskEntity;
import com.data.rsync.task.manager.repository.TaskRepository;
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

}
