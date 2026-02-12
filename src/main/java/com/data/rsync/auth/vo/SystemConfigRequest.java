package com.data.rsync.auth.vo;

import lombok.Data;

/**
 * 系统配置请求对象
 */
@Data
public class SystemConfigRequest {
    private String appName;
    private String appVersion;
    private Integer maxConnections;
    private Integer taskThreadPoolSize;
    private Integer dataSyncBatchSize;
    private Integer milvusConnectionTimeout;
    private Integer metricsCollectionInterval;
}
