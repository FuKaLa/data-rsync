package com.data.rsync.task.manager.repository;

import com.data.rsync.task.manager.entity.VectorizationConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 向量化配置仓库接口
 */
@Repository
public interface VectorizationConfigRepository extends JpaRepository<VectorizationConfigEntity, Long> {

    /**
     * 根据任务ID查询向量化配置
     * @param taskId 任务ID
     * @return 向量化配置列表
     */
    List<VectorizationConfigEntity> findByTaskId(Long taskId);

    /**
     * 根据任务ID和算法类型查询向量化配置
     * @param taskId 任务ID
     * @param algorithm 算法类型
     * @return 向量化配置
     */
    VectorizationConfigEntity findByTaskIdAndAlgorithm(Long taskId, String algorithm);

    /**
     * 根据任务ID删除向量化配置
     * @param taskId 任务ID
     */
    void deleteByTaskId(Long taskId);

}
