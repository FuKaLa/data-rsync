package com.data.rsync.data.service;

import com.data.rsync.common.model.Task;
import com.data.rsync.data.vo.*;
import java.util.List;
import java.util.Map;

/**
 * Milvus同步服务接口
 */
public interface MilvusSyncService {

    /**
     * 向Milvus写入数据
     * @param taskId 任务ID
     * @param data 数据
     * @return 是否成功
     */
    boolean writeDataToMilvus(Long taskId, Map<String, Object> data);

    /**
     * 批量向Milvus写入数据
     * @param taskId 任务ID
     * @param dataList 数据列表
     * @return 是否成功
     */
    boolean batchWriteDataToMilvus(Long taskId, List<Map<String, Object>> dataList);

    /**
     * 从Milvus删除数据
     * @param taskId 任务ID
     * @param primaryKey 主键
     * @return 是否成功
     */
    boolean deleteDataFromMilvus(Long taskId, Object primaryKey);

    /**
     * 清空Milvus集合数据
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean clearCollectionData(Long taskId);

    /**
     * 幂等写入数据到Milvus
     * @param taskId 任务ID
     * @param data 数据
     * @return 是否成功
     */
    boolean idempotentWriteDataToMilvus(Long taskId, Map<String, Object> data);

    /**
     * 创建Milvus集合
     * @param task 任务
     * @return 是否成功
     */
    boolean createMilvusCollection(Task task);

    /**
     * 创建Milvus索引
     * @param task 任务
     * @return 是否成功
     */
    boolean createMilvusIndex(Task task);

    /**
     * 重建Milvus索引
     * @param task 任务
     * @return 是否成功
     */
    boolean rebuildMilvusIndex(Task task);

    /**
     * 执行Milvus查询
     * @param task 任务
     * @param queryParam 查询参数
     * @return 查询结果
     */
    List<Map<String, Object>> executeMilvusQuery(Task task, Map<String, Object> queryParam);

    /**
     * 验证Milvus数据
     * @param task 任务
     * @return 是否成功
     */
    boolean validateMilvusData(Task task);

    /**
     * 检查数据一致性
     * @param task 任务
     * @param sourceCount 源数据数量
     * @param sampleData 样本数据
     * @return 一致性检查结果
     */
    ConsistencyCheckResult checkDataConsistency(Task task, long sourceCount, List<Map<String, Object>> sampleData);

    /**
     * 获取同步状态
     * @param taskId 任务ID
     * @return 同步状态
     */
    String getSyncStatus(Long taskId);

    /**
     * 检查Milvus连接
     * @return 是否连接成功
     */
    boolean checkMilvusConnection();

    /**
     * 列出Milvus集合
     * @return 集合列表
     */
    List<MilvusCollectionResponse> listCollections();

    /**
     * 列出集合索引
     * @param collectionName 集合名称
     * @return 索引列表
     */
    List<MilvusIndexResponse> listCollectionIndexes(String collectionName);

    /**
     * 创建集合索引
     * @param collectionName 集合名称
     * @param indexParams 索引参数
     * @return 结果
     */
    MilvusIndexResponse createCollectionIndex(String collectionName, Map<String, Object> indexParams);

    /**
     * 删除集合索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     */
    void dropCollectionIndex(String collectionName, String indexName);

    /**
     * 检查Milvus健康状态
     * @return 健康状态
     */
    MilvusHealthResponse checkMilvusHealth();

    /**
     * 优化集合
     * @param collectionName 集合名称
     * @return 结果
     */
    MilvusOptimizeResponse optimizeCollection(String collectionName);

    /**
     * 获取集合统计信息
     * @param collectionName 集合名称
     * @return 统计信息
     */
    MilvusCollectionStatsResponse getCollectionStats(String collectionName);

    /**
     * 创建Milvus集合
     * @param collectionName 集合名称
     * @param dimension 维度
     * @param metricType 度量类型
     * @return 结果
     */
    MilvusCollectionResponse createMilvusCollection(String collectionName, Integer dimension, String metricType);

    /**
     * 删除Milvus集合
     * @param collectionName 集合名称
     */
    void dropMilvusCollection(String collectionName);

    /**
     * 向Milvus写入数据
     * @param collectionName 集合名称
     * @param data 数据
     * @return 结果
     */
    MilvusWriteResponse writeDataToMilvus(String collectionName, List<Map<String, Object>> data);

    /**
     * 从Milvus删除数据
     * @param collectionName 集合名称
     * @param ids ID列表
     * @return 结果
     */
    MilvusDeleteResponse deleteDataFromMilvus(String collectionName, List<Long> ids);

    /**
     * 在Milvus中搜索数据
     * @param collectionName 集合名称
     * @param vector 向量
     * @param topK 前K个结果
     * @param radius 搜索半径
     * @return 搜索结果
     */
    MilvusSearchResponse searchDataInMilvus(String collectionName, List<Float> vector, Integer topK, Float radius);

    /**
     * 批量向Milvus写入数据
     * @param collectionName 集合名称
     * @param data 数据
     * @return 结果
     */
    MilvusWriteResponse batchWriteDataToMilvus(String collectionName, List<Map<String, Object>> data);

    /**
     * 检查数据一致性
     * @param collectionName 集合名称
     * @return 结果
     */
    DataConsistencyCheckResponse checkDataConsistency(String collectionName);

    /**
     * 重建集合索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     * @return 结果
     */
    MilvusIndexResponse rebuildCollectionIndex(String collectionName, String indexName);

    /**
     * 查询Milvus数据
     * @param collectionName 集合名称
     * @param expr 查询表达式
     * @param outputFields 输出字段
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 查询结果
     */
    List<Map<String, Object>> queryMilvusData(String collectionName, String expr, List<String> outputFields, int limit, int offset);

    /**
     * 从数据库同步数据到向量库
     * @param databaseConfig 数据库配置
     * @param collectionName 向量库集合名称
     * @param sql SQL查询语句
     * @param vectorFields 需要向量化的字段列表
     * @return 同步结果
     */
    MilvusSyncResponse syncDatabaseToVectorDB(Map<String, Object> databaseConfig, String collectionName, String sql, List<String> vectorFields);

    /**
     * 从数据库同步表数据到向量库
     * @param databaseConfig 数据库配置
     * @param collectionName 向量库集合名称
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param vectorFields 需要向量化的字段列表
     * @param whereClause 查询条件
     * @return 同步结果
     */
    MilvusSyncResponse syncTableToVectorDB(Map<String, Object> databaseConfig, String collectionName, String databaseName, String tableName, List<String> vectorFields, String whereClause);

    /**
     * 验证数据库同步到向量库的结果
     * @param collectionName 集合名称
     * @param expectedCount 预期数据数量
     * @return 验证结果
     */
    MilvusVerifyResponse verifyDatabaseSyncToVectorDB(String collectionName, long expectedCount);

    /**
     * 获取同步任务进度
     * @param taskId 任务ID
     * @return 同步进度
     */
    com.data.rsync.common.model.SyncProgress getSyncProgress(Long taskId);

    /**
     * 一致性检查结果类
     */
    class ConsistencyCheckResult {
        private boolean consistent;
        private long sourceCount;
        private long targetCount;
        private int sampleCheckPassed;
        private int sampleCheckTotal;
        private String errorMessage;
        private List<String> discrepancies;

        public boolean isConsistent() {
            return consistent;
        }

        public void setConsistent(boolean consistent) {
            this.consistent = consistent;
        }

        public long getSourceCount() {
            return sourceCount;
        }

        public void setSourceCount(long sourceCount) {
            this.sourceCount = sourceCount;
        }

        public long getTargetCount() {
            return targetCount;
        }

        public void setTargetCount(long targetCount) {
            this.targetCount = targetCount;
        }

        public int getSampleCheckPassed() {
            return sampleCheckPassed;
        }

        public void setSampleCheckPassed(int sampleCheckPassed) {
            this.sampleCheckPassed = sampleCheckPassed;
        }

        public int getSampleCheckTotal() {
            return sampleCheckTotal;
        }

        public void setSampleCheckTotal(int sampleCheckTotal) {
            this.sampleCheckTotal = sampleCheckTotal;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public List<String> getDiscrepancies() {
            return discrepancies;
        }

        public void setDiscrepancies(List<String> discrepancies) {
            this.discrepancies = discrepancies;
        }
    }
}