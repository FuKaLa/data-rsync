package com.data.rsync.task.manager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.manager.entity.TaskDependencyEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 任务依赖仓库接口
 */
@Mapper
public interface TaskDependencyRepository extends BaseMapper<TaskDependencyEntity> {

    /**
     * 根据任务ID查询依赖
     * @param taskId 任务ID
     * @return 依赖列表
     */
    List<TaskDependencyEntity> findByTaskId(Long taskId);

    /**
     * 根据依赖任务ID查询依赖
     * @param dependencyTaskId 依赖任务ID
     * @return 依赖列表
     */
    List<TaskDependencyEntity> findByDependencyTaskId(Long dependencyTaskId);

}
