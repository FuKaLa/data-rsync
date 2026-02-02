package com.data.rsync.task.manager.service.impl;

import com.data.rsync.task.manager.entity.TaskEntity;
import com.data.rsync.task.manager.repository.TaskRepository;
import com.data.rsync.task.manager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 任务服务实现类
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // 线程池用于异步执行任务
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

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

        // 更新任务状态为运行中
        taskEntity.setStatus("RUNNING");
        taskEntity.setStartTime(LocalDateTime.now());
        taskEntity.setProgress(0);
        taskRepository.save(taskEntity);

        // 创建任务实体的副本，用于lambda表达式中
        final Long taskId = id;

        // 异步执行任务
        executorService.submit(() -> {
            // 重新查询任务，确保获取最新状态
            TaskEntity task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
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
            }
        });

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "任务已触发");
        result.put("taskId", id);
        return result;
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
}
