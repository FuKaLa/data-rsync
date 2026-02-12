package com.data.rsync.data.vo;

import lombok.Data;

import java.util.List;

/**
 * 批量数据源健康检查响应类
 */
@Data
public class DataSourceBatchHealthCheckResponse {
    private int totalCount;
    private int healthyCount;
    private int unhealthyCount;
    private List<DataSourceHealthCheckItem> healthCheckItems;

    /**
     * 数据源健康检查项
     */
    @Data
    public static class DataSourceHealthCheckItem {
        private Long dataSourceId;
        private String dataSourceName;
        private String dataSourceType;
        private boolean healthy;
        private String status;
    }
}
