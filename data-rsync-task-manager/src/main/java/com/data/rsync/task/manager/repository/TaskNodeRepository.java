package com.data.rsync.task.manager.repository;

import com.data.rsync.task.manager.entity.TaskNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务节点仓库接口
 */
@Repository
public interface TaskNodeRepository extends JpaRepository<TaskNodeEntity, Long> {

    /**
     * 根据任务ID查询节点
     * @param taskId 任务ID
     * @return 节点列表
     */
    List<TaskNodeEntity> findByTaskId(Long taskId);

}
