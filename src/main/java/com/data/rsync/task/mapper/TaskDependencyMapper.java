package com.data.rsync.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.entity.TaskDependencyEntity;
import java.util.List;

/**
 * 任务依赖映射器
 */
public interface TaskDependencyMapper extends BaseMapper<TaskDependencyEntity> {

    /**
     * 根据任务ID查询依赖
     * @param taskId 任务ID
     * @return 依赖列表
     */
    List<TaskDependencyEntity> selectByTaskId(Long taskId);
}