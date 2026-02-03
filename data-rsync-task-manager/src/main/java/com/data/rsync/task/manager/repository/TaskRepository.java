package com.data.rsync.task.manager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.manager.entity.TaskEntity;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务仓库接口
 */
@Mapper
public interface TaskRepository extends BaseMapper<TaskEntity> {

    /**
     * 根据状态查询任务
     * @param status 任务状态
     * @return 任务列表
     */
    List<TaskEntity> findByStatus(String status);

    /**
     * 根据数据源ID查询任务
     * @param dataSourceId 数据源ID
     * @return 任务列表
     */
    List<TaskEntity> findByDataSourceId(Long dataSourceId);

    /**
     * 根据类型查询任务
     * @param type 任务类型
     * @return 任务列表
     */
    List<TaskEntity> findByType(String type);

    /**
     * 根据启用状态查询任务
     * @param enabled 启用状态
     * @return 任务列表
     */
    List<TaskEntity> findByEnabled(Boolean enabled);

    /**
     * 查询下次执行时间小于等于当前时间的任务
     * @param now 当前时间
     * @return 任务列表
     */
    List<TaskEntity> findTasksToExecute(LocalDateTime now);

    /**
     * 根据任务名称查询任务
     * @param name 任务名称
     * @return 任务实体
     */
    TaskEntity findByName(String name);

    /**
     * 根据任务名称查询任务是否存在
     * @param name 任务名称
     * @return 是否存在
     */
    boolean existsByName(String name);

}
