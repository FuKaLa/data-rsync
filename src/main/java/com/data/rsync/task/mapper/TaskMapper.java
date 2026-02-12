package com.data.rsync.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.entity.TaskEntity;

import java.util.List;

/**
 * 任务Mapper接口
 */
public interface TaskMapper extends BaseMapper<TaskEntity> {

    /**
     * 根据任务名称查询任务
     * @param name 任务名称
     * @return 任务信息
     */
    TaskEntity selectByName(String name);

    /**
     * 根据状态查询任务列表
     * @param status 任务状态
     * @return 任务列表
     */
    List<TaskEntity> selectByStatus(String status);

    /**
     * 根据数据源ID查询任务列表
     * @param dataSourceId 数据源ID
     * @return 任务列表
     */
    List<TaskEntity> selectByDataSourceId(Long dataSourceId);

    /**
     * 更新任务状态
     * @param id 任务ID
     * @param status 任务状态
     * @param errorMessage 错误信息
     * @return 更新结果
     */
    int updateStatus(Long id, String status, String errorMessage);

    /**
     * 更新任务进度
     * @param id 任务ID
     * @param progress 任务进度
     * @return 更新结果
     */
    int updateProgress(Long id, Integer progress);
}
