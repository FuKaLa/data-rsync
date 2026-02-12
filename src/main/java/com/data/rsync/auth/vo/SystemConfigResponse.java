package com.data.rsync.auth.vo;

import lombok.Data;

/**
 * 系统配置响应对象
 */
@Data
public class SystemConfigResponse {
    private String appName;
    private String appVersion;
    private int maxConnections;
    private int taskThreadPoolSize;
    private int dataSyncBatchSize;
    private int milvusConnectionTimeout;
    private int metricsCollectionInterval;
}
