package com.data.rsync.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.entity.MilvusIndexEntity;
import java.util.List;

/**
 * Milvus索引映射器
 */
public interface MilvusIndexMapper extends BaseMapper<MilvusIndexEntity> {

    /**
     * 根据集合名称查询索引
     * @param collectionName 集合名称
     * @return 索引列表
     */
    List<MilvusIndexEntity> selectByCollectionName(String collectionName);

    /**
     * 根据集合名称和索引名称查询索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     * @return 索引实体
     */
    MilvusIndexEntity selectByCollectionAndName(String collectionName, String indexName);

    /**
     * 更新索引状态和进度
     * @param id 索引ID
     * @param status 状态
     * @param progress 进度
     * @return 更新数量
     */
    int updateStatusAndProgress(Long id, String status, Integer progress);
}