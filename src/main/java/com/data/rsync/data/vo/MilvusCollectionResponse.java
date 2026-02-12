package com.data.rsync.data.vo;

import lombok.Data;

import java.util.Map;

/**
 * Milvus集合响应类
 */
@Data
public class MilvusCollectionResponse {
    private String collectionName;
    private int dimension;
    private String metricType;
    private long rowCount;
    private String status;
    private Map<String, Object> properties;
}
