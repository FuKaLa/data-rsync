package com.data.rsync.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.entity.TaskNodeEntity;
import java.util.List;

/**
 * 任务节点映射器
 */
public interface TaskNodeMapper extends BaseMapper<TaskNodeEntity> {

    /**
     * 根据任务ID删除节点
     * @param taskId 任务ID
     * @return 删除数量
     */
    int deleteByTaskId(Long taskId);

    /**
     * 根据任务ID查询节点
     * @param taskId 任务ID
     * @return 节点列表
     */
    List<TaskNodeEntity> selectByTaskId(Long taskId);
}