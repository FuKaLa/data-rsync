package com.data.rsync.task.manager.repository;

import com.data.rsync.task.manager.entity.TaskDependencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务依赖仓库接口
 */
@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependencyEntity, Long> {

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
