package com.data.rsync.task.manager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.task.manager.entity.MilvusIndexEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Milvus索引仓库接口
 */
@Mapper
public interface MilvusIndexRepository extends BaseMapper<MilvusIndexEntity> {

    /**
     * 根据集合名称查询索引
     * @param collectionName 集合名称
     * @return 索引列表
     */
    List<MilvusIndexEntity> findByCollectionName(String collectionName);

    /**
     * 根据集合名称和索引名称查询索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     * @return 索引实体
     */
    MilvusIndexEntity findByCollectionNameAndIndexName(String collectionName, String indexName);

    /**
     * 根据集合名称和状态查询索引
     * @param collectionName 集合名称
     * @param status 状态
     * @return 索引列表
     */
    List<MilvusIndexEntity> findByCollectionNameAndStatus(String collectionName, String status);

    /**
     * 根据状态查询索引
     * @param status 状态
     * @return 索引列表
     */
    List<MilvusIndexEntity> findByStatus(String status);

    /**
     * 根据集合名称删除索引
     * @param collectionName 集合名称
     */
    void deleteByCollectionName(String collectionName);

    /**
     * 根据集合名称和索引名称删除索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     */
    void deleteByCollectionNameAndIndexName(String collectionName, String indexName);

}
