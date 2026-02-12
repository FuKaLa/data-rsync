package com.data.rsync.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.entity.TaskConnectionEntity;
import java.util.List;

/**
 * 任务连接映射器
 */
public interface TaskConnectionMapper extends BaseMapper<TaskConnectionEntity> {

    /**
     * 根据任务ID删除连接
     * @param taskId 任务ID
     * @return 删除数量
     */
    int deleteByTaskId(Long taskId);

    /**
     * 根据任务ID查询连接
     * @param taskId 任务ID
     * @return 连接列表
     */
    List<TaskConnectionEntity> selectByTaskId(Long taskId);
}