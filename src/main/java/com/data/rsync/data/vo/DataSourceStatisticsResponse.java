package com.data.rsync.data.vo;

import lombok.Data;

import java.util.Map;

/**
 * 数据源统计信息响应类
 */
@Data
public class DataSourceStatisticsResponse {
    private int totalCount;
    private int enabledCount;
    private int disabledCount;
    private int healthyCount;
    private int unhealthyCount;
    private Map<String, Integer> typeDistribution;
    private Map<String, Integer> statusDistribution;
}
