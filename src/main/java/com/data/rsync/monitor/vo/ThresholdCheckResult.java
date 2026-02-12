package com.data.rsync.monitor.vo;

import lombok.Data;

/**
 * 阈值检查结果对象
 */
@Data
public class ThresholdCheckResult {
    /**
     * 是否超出阈值
     */
    private boolean thresholdExceeded;
    
    /**
     * 阈值
     */
    private double threshold;
    
    /**
     * 当前值
     */
    private double currentValue;
    
    /**
     * 告警级别
     */
    private String severity;
    
    /**
     * 告警消息
     */
    private String message;
    
    /**
     * 指标名称
     */
    private String metricName;
    
    /**
     * 检查时间戳
     */
    private long timestamp;
    
    /**
     * 构造方法
     */
    public ThresholdCheckResult() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 构造方法
     */
    public ThresholdCheckResult(boolean thresholdExceeded, double threshold, double currentValue, String severity, String message, String metricName) {
        this.thresholdExceeded = thresholdExceeded;
        this.threshold = threshold;
        this.currentValue = currentValue;
        this.severity = severity;
        this.message = message;
        this.metricName = metricName;
        this.timestamp = System.currentTimeMillis();
    }
}
