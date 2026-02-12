package com.data.rsync.data.vo;

import lombok.Data;

/**
 * Milvus创建集合请求类
 */
@Data
public class MilvusCreateCollectionRequest {
    private String collectionName;
    private Integer dimension;
    private String metricType;
}
