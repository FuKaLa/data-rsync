package com.data.rsync.data.vo;

import lombok.Data;

import java.util.Map;

/**
 * Milvus集合优化响应类
 */
@Data
public class MilvusOptimizeResponse {
    private String collectionName;
    private long optimizeTimeMs;
    private Map<String, Object> optimizeDetails;
}
