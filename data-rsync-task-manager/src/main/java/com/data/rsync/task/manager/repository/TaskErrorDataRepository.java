package com.data.rsync.task.manager.repository;

import com.data.rsync.task.manager.entity.TaskErrorDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务错误数据仓库接口
 */
@Repository
public interface TaskErrorDataRepository extends JpaRepository<TaskErrorDataEntity, Long> {

    /**
     * 根据任务ID查询错误数据
     * @param taskId 任务ID
     * @return 错误数据列表
     */
    List<TaskErrorDataEntity> findByTaskId(Long taskId);

    /**
     * 根据任务ID和错误类型查询错误数据
     * @param taskId 任务ID
     * @param errorType 错误类型
     * @return 错误数据列表
     */
    List<TaskErrorDataEntity> findByTaskIdAndErrorType(Long taskId, String errorType);

    /**
     * 根据任务ID和同步环节查询错误数据
     * @param taskId 任务ID
     * @param syncStage 同步环节
     * @return 错误数据列表
     */
    List<TaskErrorDataEntity> findByTaskIdAndSyncStage(Long taskId, String syncStage);

    /**
     * 根据任务ID和处理状态查询错误数据
     * @param taskId 任务ID
     * @param processStatus 处理状态
     * @return 错误数据列表
     */
    List<TaskErrorDataEntity> findByTaskIdAndProcessStatus(Long taskId, String processStatus);

    /**
     * 根据ID列表查询错误数据
     * @param ids ID列表
     * @return 错误数据列表
     */
    List<TaskErrorDataEntity> findByIdIn(List<Long> ids);

}
