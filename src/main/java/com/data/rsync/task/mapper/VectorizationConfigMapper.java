package com.data.rsync.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.entity.VectorizationConfigEntity;
import java.util.List;

/**
 * 向量化配置映射器
 */
public interface VectorizationConfigMapper extends BaseMapper<VectorizationConfigEntity> {

    /**
     * 根据任务ID查询向量化配置
     * @param taskId 任务ID
     * @return 向量化配置列表
     */
    List<VectorizationConfigEntity> selectByTaskId(Long taskId);
}