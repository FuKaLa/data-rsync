package com.data.rsync.task.manager.service;

import com.data.rsync.task.manager.entity.TaskEntity;
import java.util.List;
import java.util.Map;

/**
 * 任务服务接口
 */
public interface TaskService {

    /**
     * 创建任务
     * @param taskEntity 任务实体
     * @return 创建的任务
     */
    TaskEntity createTask(TaskEntity taskEntity);

    /**
     * 更新任务
     * @param taskEntity 任务实体
     * @return 更新后的任务
     */
    TaskEntity updateTask(TaskEntity taskEntity);

    /**
     * 删除任务
     * @param id 任务ID
     */
    void deleteTask(Long id);

    /**
     * 启停任务
     * @param id 任务ID
     * @param enabled 启用状态
     */
    void toggleTask(Long id, Boolean enabled);

    /**
     * 根据ID查询任务
     * @param id 任务ID
     * @return 任务实体
     */
    TaskEntity getTaskById(Long id);

    /**
     * 根据名称查询任务
     * @param name 任务名称
     * @return 任务实体
     */
    TaskEntity getTaskByName(String name);

    /**
     * 查询所有任务
     * @return 任务列表
     */
    List<TaskEntity> getAllTasks();

    /**
     * 根据状态查询任务
     * @param status 任务状态
     * @return 任务列表
     */
    List<TaskEntity> getTasksByStatus(String status);

    /**
     * 根据数据源ID查询任务
     * @param dataSourceId 数据源ID
     * @return 任务列表
     */
    List<TaskEntity> getTasksByDataSourceId(Long dataSourceId);

    /**
     * 触发任务执行
     * @param id 任务ID
     * @return 任务执行结果
     */
    Map<String, Object> triggerTask(Long id);

    /**
     * 计算下次执行时间
     * @param taskEntity 任务实体
     * @return 下次执行时间
     */
    TaskEntity calculateNextExecTime(TaskEntity taskEntity);

    /**
     * 执行定时任务
     */
    void executeScheduledTasks();

    /**
     * 更新任务状态
     * @param id 任务ID
     * @param status 任务状态
     * @param errorMessage 错误信息
     */
    void updateTaskStatus(Long id, String status, String errorMessage);

    /**
     * 更新任务进度
     * @param id 任务ID
     * @param progress 任务进度
     */
    void updateTaskProgress(Long id, Integer progress);

}
