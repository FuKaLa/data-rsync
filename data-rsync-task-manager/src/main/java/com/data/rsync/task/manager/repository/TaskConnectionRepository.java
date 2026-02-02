package com.data.rsync.task.manager.repository;

import com.data.rsync.task.manager.entity.TaskConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务连接仓库接口
 */
@Repository
public interface TaskConnectionRepository extends JpaRepository<TaskConnectionEntity, Long> {

    /**
     * 根据任务ID查询连接
     * @param taskId 任务ID
     * @return 连接列表
     */
    List<TaskConnectionEntity> findByTaskId(Long taskId);

}
