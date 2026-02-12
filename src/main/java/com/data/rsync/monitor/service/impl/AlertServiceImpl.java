package com.data.rsync.monitor.service.impl;

import com.data.rsync.monitor.service.AlertService;
import com.data.rsync.monitor.vo.ThresholdCheckResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 告警服务实现类
 */
@Service
public class AlertServiceImpl implements AlertService {

    // 告警速率限制缓存
    private final Map<String, Long> alertRateLimitCache = new ConcurrentHashMap<>();
    
    // 告警阈值配置
    private final Map<String, Double> metricThresholds = new HashMap<>();
    
    // 告警历史记录
    private final ConcurrentHashMap<String, Map<String, Object>> alertHistory = new ConcurrentHashMap<>();
    
    // 初始化告警阈值
    {
        metricThresholds.put("cpu_usage", 80.0);
        metricThresholds.put("memory_usage", 85.0);
        metricThresholds.put("disk_usage", 90.0);
        metricThresholds.put("task_failure_rate", 10.0);
        metricThresholds.put("data_delay", 500.0);
        metricThresholds.put("sync_success_rate", 95.0);
        metricThresholds.put("milvus_write_qps", 500.0);
        metricThresholds.put("api_response_time", 500.0);
        metricThresholds.put("redis_hit_rate", 90.0);
        metricThresholds.put("thread_pool_queue_size", 80.0);
    }
    
    // 告警通知渠道
    private enum NotificationChannel {
        EMAIL, SMS, WEBHOOK, DINGTALK, WECHAT
    }

    @Override
    public void sendTaskFailureAlert(Long taskId, String errorMessage) {
        // 实现任务失败告警
        String alertKey = "task_failure:" + taskId;
        if (checkAlertRateLimit(alertKey, taskId)) {
            // 1. 记录告警到数据库
            // 2. 发送告警通知（邮件、短信等）
            System.out.println("[ALERT] Task failure alert: Task ID: " + taskId + ", Error: " + errorMessage);
        }
    }

    @Override
    public void sendTaskWarningAlert(Long taskId, String warningMessage) {
        // 实现任务警告告警
        String alertKey = "task_warning:" + taskId;
        if (checkAlertRateLimit(alertKey, taskId)) {
            System.out.println("[ALERT] Task warning alert: Task ID: " + taskId + ", Warning: " + warningMessage);
        }
    }

    @Override
    public void sendTaskTimeoutAlert(Long taskId, String timeoutMessage) {
        // 实现任务超时告警
        String alertKey = "task_timeout:" + taskId;
        if (checkAlertRateLimit(alertKey, taskId)) {
            System.out.println("[ALERT] Task timeout alert: Task ID: " + taskId + ", Timeout: " + timeoutMessage);
        }
    }

    @Override
    public void sendTaskStatusAlert(Long taskId, String statusMessage) {
        // 实现任务状态告警
        String alertKey = "task_status:" + taskId;
        if (checkAlertRateLimit(alertKey, taskId)) {
            System.out.println("[ALERT] Task status alert: Task ID: " + taskId + ", Status: " + statusMessage);
        }
    }

    @Override
    public void sendSystemResourceAlert(String resourceType, double usage) {
        // 实现系统资源告警
        String alertKey = "system_resource:" + resourceType;
        if (checkAlertRateLimit(alertKey, null)) {
            System.out.println("[ALERT] System resource alert: Resource Type: " + resourceType + ", Usage: " + usage);
        }
    }

    @Override
    public void sendCustomAlert(String type, String title, String message, Long taskId) {
        // 实现自定义告警
        String alertKey = "custom:" + type + ":" + (taskId != null ? taskId : "system");
        if (checkAlertRateLimit(alertKey, taskId)) {
            System.out.println("[ALERT] Custom alert: Type: " + type + ", Title: " + title + ", Message: " + message + ", Task ID: " + taskId);
        }
    }

    @Override
    public boolean checkAlertRateLimit(String type, Long taskId) {
        // 实现告警速率限制检查
        String key = type + ":" + (taskId != null ? taskId : "system");
        long currentTime = System.currentTimeMillis();
        long lastAlertTime = alertRateLimitCache.getOrDefault(key, 0L);
        
        // 限制：同一类型的告警每60秒最多发送一次
        if (currentTime - lastAlertTime > TimeUnit.SECONDS.toMillis(60)) {
            alertRateLimitCache.put(key, currentTime);
            return true;
        }
        return false;
    }

    @Override
    public void cleanExpiredAlerts() {
        // 实现过期告警清理
        // 1. 删除数据库中过期的告警记录
        // 2. 清理内存中的告警缓存
        System.out.println("[ALERT] Cleaning expired alerts...");
    }

    @Override
    public ThresholdCheckResult checkThreshold(String metricName, double metricValue) {
        // 实现阈值检查逻辑
        double threshold = metricThresholds.getOrDefault(metricName, 0.0);
        boolean isAlert = metricValue > threshold;
        String severity = "INFO";
        
        if (isAlert) {
            if (metricValue > threshold * 1.2) {
                severity = "ERROR";
            } else if (metricValue > threshold * 1.1) {
                severity = "WARNING";
            } else {
                severity = "ALERT";
            }
        }
        
        return new ThresholdCheckResult(isAlert, (int) threshold, metricValue, severity, 
                "Threshold check completed for " + metricName, metricName);
    }

    @Override
    public boolean sendAlert(String severity, String message, Map<String, Object> metrics) {
        // 实现告警发送逻辑
        // 1. 记录告警到数据库
        // 2. 根据严重性选择发送渠道（邮件、短信、消息队列等）
        // 3. 发送告警通知
        System.out.println("[ALERT] Severity: " + severity + ", Message: " + message + ", Metrics: " + metrics);
        
        // 记录告警历史
        String alertId = severity + ":" + System.currentTimeMillis();
        Map<String, Object> alertInfo = new HashMap<>();
        alertInfo.put("id", alertId);
        alertInfo.put("severity", severity);
        alertInfo.put("message", message);
        alertInfo.put("metrics", metrics);
        alertInfo.put("timestamp", System.currentTimeMillis());
        alertInfo.put("status", "ACTIVE");
        alertHistory.put(alertId, alertInfo);
        
        // 根据严重性选择通知渠道
        if ("ERROR".equals(severity)) {
            sendNotification(NotificationChannel.EMAIL, severity, message, metrics);
            sendNotification(NotificationChannel.SMS, severity, message, metrics);
        } else if ("WARNING".equals(severity)) {
            sendNotification(NotificationChannel.EMAIL, severity, message, metrics);
            sendNotification(NotificationChannel.WEBHOOK, severity, message, metrics);
        } else {
            sendNotification(NotificationChannel.WEBHOOK, severity, message, metrics);
        }
        
        return true;
    }
    
    /**
     * 发送告警通知到指定渠道
     * @param channel 通知渠道
     * @param severity 告警级别
     * @param message 告警消息
     * @param metrics 相关指标
     */
    private void sendNotification(NotificationChannel channel, String severity, String message, Map<String, Object> metrics) {
        // 实现不同渠道的通知发送逻辑
        switch (channel) {
            case EMAIL:
                // 发送邮件通知
                System.out.println("[NOTIFICATION] Sending email notification: " + severity + " - " + message);
                break;
            case SMS:
                // 发送短信通知
                System.out.println("[NOTIFICATION] Sending SMS notification: " + severity + " - " + message);
                break;
            case WEBHOOK:
                // 发送Webhook通知
                System.out.println("[NOTIFICATION] Sending webhook notification: " + severity + " - " + message);
                break;
            case DINGTALK:
                // 发送钉钉通知
                System.out.println("[NOTIFICATION] Sending DingTalk notification: " + severity + " - " + message);
                break;
            case WECHAT:
                // 发送微信通知
                System.out.println("[NOTIFICATION] Sending WeChat notification: " + severity + " - " + message);
                break;
        }
    }
    
    /**
     * 获取告警历史记录
     * @param limit 限制数量
     * @return 告警历史记录
     */
    public Map<String, Map<String, Object>> getAlertHistory(int limit) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        int count = 0;
        for (Map.Entry<String, Map<String, Object>> entry : alertHistory.entrySet()) {
            if (count < limit) {
                result.put(entry.getKey(), entry.getValue());
                count++;
            } else {
                break;
            }
        }
        return result;
    }
    
    /**
     * 解决告警
     * @param alertId 告警ID
     * @return 是否解决成功
     */
    public boolean resolveAlert(String alertId) {
        Map<String, Object> alertInfo = alertHistory.get(alertId);
        if (alertInfo != null) {
            alertInfo.put("status", "RESOLVED");
            alertInfo.put("resolvedTime", System.currentTimeMillis());
            System.out.println("[ALERT] Alert resolved: " + alertId);
            return true;
        }
        return false;
    }
    
    /**
     * 批量发送指标告警
     * @param metrics 指标数据
     * @return 发送的告警数量
     */
    public int sendMetricAlerts(Map<String, Double> metrics) {
        int alertCount = 0;
        for (Map.Entry<String, Double> entry : metrics.entrySet()) {
            String metricName = entry.getKey();
            double metricValue = entry.getValue();
            if (metricThresholds.containsKey(metricName)) {
                double threshold = metricThresholds.get(metricName);
                if (metricValue > threshold) {
                    String severity = metricValue > threshold * 1.2 ? "ERROR" : "WARNING";
                    String message = metricName + " exceeded threshold: " + metricValue + " > " + threshold;
                    Map<String, Object> alertMetrics = new HashMap<>();
                    alertMetrics.put(metricName, metricValue);
                    alertMetrics.put("threshold", threshold);
                    sendAlert(severity, message, alertMetrics);
                    alertCount++;
                }
            }
        }
        return alertCount;
    }
}
