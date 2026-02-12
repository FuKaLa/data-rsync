package com.data.rsync.data.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Milvus健康检查响应类
 */
@Getter
@Setter
public class MilvusHealthResponse {
    private boolean healthy;
    private String status;
    private long responseTimeMs;
    private Map<String, Object> details;
}
