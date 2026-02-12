package com.data.rsync.monitor.vo;

import lombok.Data;

/**
 * 阈值检查响应对象
 */
@Data
public class ThresholdCheckResponse {
    private boolean exceedsThreshold;
    private double metricValue;
    private double thresholdValue;
    private String message;
}
