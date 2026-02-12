package com.data.rsync.data.vo;

import lombok.Data;

import java.util.Map;

/**
 * Milvus集合统计信息响应类
 */
@Data
public class MilvusCollectionStatsResponse {
    private String collectionName;
    private long rowCount;
    private long sizeBytes;
    private Map<String, Object> fieldStats;
    private Map<String, Object> indexStats;
    private long statsTimeMs;
}
