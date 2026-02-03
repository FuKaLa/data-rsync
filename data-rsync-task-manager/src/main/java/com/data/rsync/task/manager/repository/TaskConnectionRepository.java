package com.data.rsync.task.manager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.manager.entity.TaskConnectionEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 任务连接仓库接口
 */
@Mapper
public interface TaskConnectionRepository extends BaseMapper<TaskConnectionEntity> {

    /**
     * 根据任务ID查询连接
     * @param taskId 任务ID
     * @return 连接列表
     */
    List<TaskConnectionEntity> findByTaskId(Long taskId);

}
