package com.data.rsync.data.vo;

import lombok.Data;

import java.util.Map;

/**
 * Milvus索引创建请求对象
 */
@Data
public class MilvusCreateIndexRequest {
    private String fieldName;
    private String indexType;
    private String metricType;
    private Map<String, Object> indexParams;
}
