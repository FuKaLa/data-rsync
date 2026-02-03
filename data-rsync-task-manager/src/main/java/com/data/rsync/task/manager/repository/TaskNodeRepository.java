package com.data.rsync.task.manager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.manager.entity.TaskNodeEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 任务节点仓库接口
 */
@Mapper
public interface TaskNodeRepository extends BaseMapper<TaskNodeEntity> {

    /**
     * 根据任务ID查询节点
     * @param taskId 任务ID
     * @return 节点列表
     */
    List<TaskNodeEntity> findByTaskId(Long taskId);

}
