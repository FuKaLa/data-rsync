package com.data.rsync.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.entity.TaskErrorDataEntity;
import java.util.List;

/**
 * 任务错误数据映射器
 */
public interface TaskErrorDataMapper extends BaseMapper<TaskErrorDataEntity> {

    /**
     * 根据任务ID删除错误数据
     * @param taskId 任务ID
     * @return 删除数量
     */
    int deleteByTaskId(Long taskId);

    /**
     * 根据任务ID查询错误数据
     * @param taskId 任务ID
     * @return 错误数据列表
     */
    List<TaskErrorDataEntity> selectByTaskId(Long taskId);

    /**
     * 根据任务ID和错误类型查询错误数据
     * @param taskId 任务ID
     * @param errorType 错误类型
     * @return 错误数据列表
     */
    List<TaskErrorDataEntity> selectByTaskIdAndErrorType(Long taskId, String errorType);

    /**
     * 根据任务ID和同步阶段查询错误数据
     * @param taskId 任务ID
     * @param syncStage 同步阶段
     * @return 错误数据列表
     */
    List<TaskErrorDataEntity> selectByTaskIdAndSyncStage(Long taskId, String syncStage);
}