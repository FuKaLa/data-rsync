package com.data.rsync.task.manager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.manager.entity.VectorizationConfigEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 向量化配置仓库接口
 */
@Mapper
public interface VectorizationConfigRepository extends BaseMapper<VectorizationConfigEntity> {

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
