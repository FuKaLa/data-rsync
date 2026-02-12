package com.data.rsync.data.service;

import com.data.rsync.task.entity.TaskEntity;
import com.data.rsync.task.entity.VectorizationConfigEntity;
import com.data.rsync.data.vo.VectorCollectionInfoResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 向量同步服务接口
 * 定义从关系型数据库到向量库的同步核心方法
 */
public interface VectorSyncService {

    /**
     * 执行向量同步任务
     * @param taskEntity 任务实体
     * @return 同步结果
     */
    CompletableFuture<Boolean> executeVectorSync(TaskEntity taskEntity);

    /**
     * 批量同步数据到向量库
     * @param dataSourceId 数据源ID
     * @param collectionName 目标集合名称
     * @param data 待同步数据
     * @param vectorizationConfig 向量化配置
     * @return 同步结果
     */
    boolean syncToVectorDB(String dataSourceId, String collectionName, Map<String, Object> data, VectorizationConfigEntity vectorizationConfig);

    /**
     * 自定义集合映射
     * @param dataSourceId 数据源ID
     * @param sourceTable 源表名
     * @param targetCollection 目标集合名
     * @return 映射结果
     */
    boolean mapCollection(String dataSourceId, String sourceTable, String targetCollection);

    /**
     * 配置分片策略
     * @param collectionName 集合名称
     * @param shardingStrategy 分片策略
     * @param shardingParams 分片参数
     * @return 配置结果
     */
    boolean configureSharding(String collectionName, String shardingStrategy, Map<String, Object> shardingParams);

    /**
     * 执行文本提词
     * @param text 待处理文本
     * @param extractorType 提词器类型
     * @param params 提词参数
     * @return 提取的关键词
     */
    String[] extractKeywords(String text, String extractorType, Map<String, Object> params);

    /**
     * 验证向量库连接
     * @param vectorDbConfig 向量库配置
     * @return 连接状态
     */
    boolean validateVectorDbConnection(Map<String, Object> vectorDbConfig);

    /**
     * 获取向量库集合信息
     * @param collectionName 集合名称
     * @return 集合信息
     */
    VectorCollectionInfoResponse getCollectionInfo(String collectionName);

    /**
     * 优化向量库索引
     * @param collectionName 集合名称
     * @param indexParams 索引参数
     * @return 优化结果
     */
    boolean optimizeVectorIndex(String collectionName, Map<String, Object> indexParams);
}
