package com.data.rsync.monitor.vo;

import lombok.Data;

import java.util.Map;

/**
 * 指标响应对象
 */
@Data
public class MetricsResponse {
    private long timestamp;
    private Map<String, Object> metrics;
}
