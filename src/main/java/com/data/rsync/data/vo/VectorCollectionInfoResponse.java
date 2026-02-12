package com.data.rsync.data.vo;

import lombok.Data;

import java.util.Map;

/**
 * 向量库集合信息响应类
 */
@Data
public class VectorCollectionInfoResponse {
    private String collectionName;
    private int dimension;
    private String metricType;
    private long rowCount;
    private String status;
    private Map<String, Object> fields;
    private Map<String, Object> indexes;
}
