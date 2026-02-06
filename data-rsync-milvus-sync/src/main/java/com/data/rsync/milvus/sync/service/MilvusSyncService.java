package com.data.rsync.milvus.sync.service;

import com.data.rsync.common.model.Task;

import java.util.Map;

/**
 * Milvus 同步服务接口
 */
public interface MilvusSyncService {

    /**
     * 写入数据到 Milvus
     * @param taskId 任务ID
     * @param data 数据
     * @return 写入结果
     */
    boolean writeDataToMilvus(Long taskId, Map<String, Object> data);

    /**
     * 批量写入数据到 Milvus
     * @param taskId 任务ID
     * @param dataList 数据列表
     * @return 写入结果
     */
    boolean batchWriteDataToMilvus(Long taskId, java.util.List<Map<String, Object>> dataList);

    /**
     * 删除 Milvus 中的数据
     * @param taskId 任务ID
     * @param primaryKey 主键
     * @return 删除结果
     */
    boolean deleteDataFromMilvus(Long taskId, Object primaryKey);

    /**
     * 清空 Milvus 集合中的所有数据
     * @param taskId 任务ID
     * @return 清空结果
     */
    boolean clearCollectionData(Long taskId);

    /**
     * 基于主键的幂等写入
     * @param taskId 任务ID
     * @param data 数据
     * @return 写入结果
     */
    boolean idempotentWriteDataToMilvus(Long taskId, Map<String, Object> data);

    /**
     * 创建 Milvus 集合
     * @param task 任务
     * @return 创建结果
     */
    boolean createMilvusCollection(Task task);

    /**
     * 创建 Milvus 索引
     * @param task 任务
     * @return 创建结果
     */
    boolean createMilvusIndex(Task task);

    /**
     * 重建 Milvus 索引
     * @param task 任务
     * @return 重建结果
     */
    boolean rebuildMilvusIndex(Task task);

    /**
     * 执行 Milvus 查询
     * @param task 任务
     * @param queryParam 查询参数
     * @return 查询结果
     */
    java.util.List<Map<String, Object>> executeMilvusQuery(Task task, Map<String, Object> queryParam);

    /**
     * 验证 Milvus 数据
     * @param task 任务
     * @return 验证结果
     */
    boolean validateMilvusData(Task task);

    /**
     * 执行数据一致性校验
     * @param task 任务
     * @param sourceCount 源数据数量
     * @param sampleData 源数据样本
     * @return 校验结果
     */
    ConsistencyCheckResult checkDataConsistency(Task task, long sourceCount, java.util.List<Map<String, Object>> sampleData);

    /**
     * 数据一致性校验结果
     */
    class ConsistencyCheckResult {
        private boolean consistent;
        private long sourceCount;
        private long targetCount;
        private int sampleCheckPassed;
        private int sampleCheckTotal;
        private String errorMessage;
        private java.util.List<String> discrepancies;

        // Getters and setters
        public boolean isConsistent() { return consistent; }
        public void setConsistent(boolean consistent) { this.consistent = consistent; }
        public long getSourceCount() { return sourceCount; }
        public void setSourceCount(long sourceCount) { this.sourceCount = sourceCount; }
        public long getTargetCount() { return targetCount; }
        public void setTargetCount(long targetCount) { this.targetCount = targetCount; }
        public int getSampleCheckPassed() { return sampleCheckPassed; }
        public void setSampleCheckPassed(int sampleCheckPassed) { this.sampleCheckPassed = sampleCheckPassed; }
        public int getSampleCheckTotal() { return sampleCheckTotal; }
        public void setSampleCheckTotal(int sampleCheckTotal) { this.sampleCheckTotal = sampleCheckTotal; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public java.util.List<String> getDiscrepancies() { return discrepancies; }
        public void setDiscrepancies(java.util.List<String> discrepancies) { this.discrepancies = discrepancies; }
    }

    /**
     * 获取同步状态
     * @param taskId 任务ID
     * @return 同步状态
     */
    String getSyncStatus(Long taskId);

    /**
     * 检查 Milvus 连接状态
     * @return 连接状态
     */
    boolean checkMilvusConnection();

}
