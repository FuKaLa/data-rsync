package com.data.rsync.data.vo;

import lombok.Data;

import java.util.Map;

/**
 * 数据源配置导出响应类
 */
@Data
public class DataSourceConfigExportResponse {
    private Long dataSourceId;
    private String dataSourceName;
    private String dataSourceType;
    private Map<String, Object> connectionInfo;
    private Map<String, Object> advancedSettings;
    private String exportTime;
}
