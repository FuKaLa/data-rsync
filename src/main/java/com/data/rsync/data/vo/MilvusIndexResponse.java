package com.data.rsync.data.vo;

import lombok.Data;

import java.util.Map;

/**
 * Milvus索引响应类
 */
@Data
public class MilvusIndexResponse {
    private String indexName;
    private String fieldName;
    private String indexType;
    private Map<String, Object> indexParams;
    private String status;
    private long buildTimeMs;
}
