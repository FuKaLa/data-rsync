package com.data.rsync.data.vo;

import lombok.Data;

import java.util.Map;

/**
 * 数据源连接信息响应类
 */
@Data
public class DataSourceConnectionInfoResponse {
    private Long dataSourceId;
    private String dataSourceName;
    private String dataSourceType;
    private String host;
    private Integer port;
    private String database;
    private String username;
    private String status;
    private Map<String, Object> properties;
}
