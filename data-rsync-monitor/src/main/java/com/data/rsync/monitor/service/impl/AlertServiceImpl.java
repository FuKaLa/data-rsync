package com.data.rsync.monitor.service.impl;

import com.data.rsync.common.constants.DataRsyncConstants;
import com.data.rsync.monitor.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 告警服务实现类
 * 负责发送告警信息，在任务执行异常时发送告警通知
 */
@Service
@Slf4j
public class AlertServiceImpl implements AlertService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 告警类型
     */
    private static class AlertType {
        public static final String TASK_FAILURE = "TASK_FAILURE";
        public static final String TASK_WARNING = "TASK_WARNING";
        public static final String TASK_TIMEOUT = "TASK_TIMEOUT";
        public static final String TASK_STATUS = "TASK_STATUS";
        public static final String SYSTEM_RESOURCE = "SYSTEM_RESOURCE";
    }

    /**
     * 发送任务失败告警
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    @Override
    public void sendTaskFailureAlert(Long taskId, String errorMessage) {
        try {
            log.warn("Sending task failure alert: taskId={}, errorMessage={}", taskId, errorMessage);
            
            // 构建告警信息
            Map<String, Object> alertInfo = buildAlertInfo(
                    AlertType.TASK_FAILURE,
                    "任务执行失败",
                    "任务 " + taskId + " 执行失败: " + errorMessage,
                    taskId
            );
            
            // 发送告警
            sendAlert(alertInfo);
            
        } catch (Exception e) {
            log.error("Failed to send task failure alert: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送任务警告告警
     * @param taskId 任务ID
     * @param warningMessage 警告信息
     */
    @Override
    public void sendTaskWarningAlert(Long taskId, String warningMessage) {
        try {
            log.warn("Sending task warning alert: taskId={}, warningMessage={}", taskId, warningMessage);
            
            // 构建告警信息
            Map<String, Object> alertInfo = buildAlertInfo(
                    AlertType.TASK_WARNING,
                    "任务执行警告",
                    "任务 " + taskId + " 执行警告: " + warningMessage,
                    taskId
            );
            
            // 发送告警
            sendAlert(alertInfo);
            
        } catch (Exception e) {
            log.error("Failed to send task warning alert: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送任务超时告警
     * @param taskId 任务ID
     * @param timeoutMessage 超时信息
     */
    @Override
    public void sendTaskTimeoutAlert(Long taskId, String timeoutMessage) {
        try {
            log.warn("Sending task timeout alert: taskId={}, timeoutMessage={}", taskId, timeoutMessage);
            
            // 构建告警信息
            Map<String, Object> alertInfo = buildAlertInfo(
                    AlertType.TASK_TIMEOUT,
                    "任务执行超时",
                    "任务 " + taskId + " 执行超时: " + timeoutMessage,
                    taskId
            );
            
            // 发送告警
            sendAlert(alertInfo);
            
        } catch (Exception e) {
            log.error("Failed to send task timeout alert: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送任务状态告警
     * @param taskId 任务ID
     * @param statusMessage 状态信息
     */
    @Override
    public void sendTaskStatusAlert(Long taskId, String statusMessage) {
        try {
            log.warn("Sending task status alert: taskId={}, statusMessage={}", taskId, statusMessage);
            
            // 构建告警信息
            Map<String, Object> alertInfo = buildAlertInfo(
                    AlertType.TASK_STATUS,
                    "任务状态告警",
                    "任务 " + taskId + " 状态异常: " + statusMessage,
                    taskId
            );
            
            // 发送告警
            sendAlert(alertInfo);
            
        } catch (Exception e) {
            log.error("Failed to send task status alert: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送系统资源告警
     * @param resourceType 资源类型
     * @param usage 资源使用率
     */
    @Override
    public void sendSystemResourceAlert(String resourceType, double usage) {
        try {
            log.warn("Sending system resource alert: resourceType={}, usage={}%", resourceType, usage);
            
            // 构建告警信息
            Map<String, Object> alertInfo = buildAlertInfo(
                    AlertType.SYSTEM_RESOURCE,
                    "系统资源使用过高",
                    "系统 " + resourceType + " 使用率过高: " + usage + "%",
                    null
            );
            
            // 发送告警
            sendAlert(alertInfo);
            
        } catch (Exception e) {
            log.error("Failed to send system resource alert: {}", e.getMessage(), e);
        }
    }

    /**
     * 构建告警信息
     * @param type 告警类型
     * @param title 告警标题
     * @param message 告警消息
     * @param taskId 任务ID
     * @return 告警信息
     */
    private Map<String, Object> buildAlertInfo(String type, String title, String message, Long taskId) {
        Map<String, Object> alertInfo = new HashMap<>();
        alertInfo.put("type", type);
        alertInfo.put("title", title);
        alertInfo.put("message", message);
        alertInfo.put("taskId", taskId);
        alertInfo.put("timestamp", LocalDateTime.now().toString());
        alertInfo.put("severity", getAlertSeverity(type));
        return alertInfo;
    }

    /**
     * 获取告警严重程度
     * @param type 告警类型
     * @return 严重程度
     */
    private String getAlertSeverity(String type) {
        switch (type) {
            case AlertType.TASK_FAILURE:
                return "HIGH";
            case AlertType.TASK_TIMEOUT:
                return "HIGH";
            case AlertType.TASK_WARNING:
                return "MEDIUM";
            case AlertType.TASK_STATUS:
                return "MEDIUM";
            case AlertType.SYSTEM_RESOURCE:
                return "MEDIUM";
            default:
                return "LOW";
        }
    }

    /**
     * 发送告警
     * @param alertInfo 告警信息
     */
    private void sendAlert(Map<String, Object> alertInfo) {
        try {
            // 1. 记录告警到Redis
            recordAlertToRedis(alertInfo);
            
            // 2. 发送告警到Kafka
            sendAlertToKafka(alertInfo);
            
            // 3. 打印告警日志
            logAlert(alertInfo);
            
        } catch (Exception e) {
            log.error("Failed to send alert: {}", e.getMessage(), e);
        }
    }

    /**
     * 记录告警到Redis
     * @param alertInfo 告警信息
     */
    private void recordAlertToRedis(Map<String, Object> alertInfo) {
        try {
            String alertKey = DataRsyncConstants.RedisKey.MONITOR_PREFIX + "alert:" + System.currentTimeMillis();
            redisTemplate.opsForHash().putAll(alertKey, alertInfo);
            
            // 设置过期时间为7天
            redisTemplate.expire(alertKey, 7, java.util.concurrent.TimeUnit.DAYS);
            
            log.debug("Recorded alert to Redis: {}", alertKey);
        } catch (Exception e) {
            log.error("Failed to record alert to Redis: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送告警到Kafka
     * @param alertInfo 告警信息
     */
    private void sendAlertToKafka(Map<String, Object> alertInfo) {
        try {
            String topic = DataRsyncConstants.KafkaTopic.ERROR_TOPIC;
            String key = "alert:" + System.currentTimeMillis();
            String message = alertInfo.toString();
            
            kafkaTemplate.send(topic, key, message);
            log.debug("Sent alert to Kafka topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to send alert to Kafka: {}", e.getMessage(), e);
        }
    }

    /**
     * 打印告警日志
     * @param alertInfo 告警信息
     */
    private void logAlert(Map<String, Object> alertInfo) {
        try {
            String type = (String) alertInfo.get("type");
            String title = (String) alertInfo.get("title");
            String message = (String) alertInfo.get("message");
            String severity = (String) alertInfo.get("severity");
            Long taskId = (Long) alertInfo.get("taskId");
            
            switch (severity) {
                case "HIGH":
                    log.error("[ALERT][{}][{}][{}] TaskId: {} - {}", 
                            severity, type, title, taskId, message);
                    break;
                case "MEDIUM":
                    log.warn("[ALERT][{}][{}][{}] TaskId: {} - {}", 
                            severity, type, title, taskId, message);
                    break;
                default:
                    log.info("[ALERT][{}][{}][{}] TaskId: {} - {}", 
                            severity, type, title, taskId, message);
            }
        } catch (Exception e) {
            log.error("Failed to log alert: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送自定义告警
     * @param type 告警类型
     * @param title 告警标题
     * @param message 告警消息
     * @param taskId 任务ID
     */
    @Override
    public void sendCustomAlert(String type, String title, String message, Long taskId) {
        try {
            log.info("Sending custom alert: type={}, title={}, taskId={}", type, title, taskId);
            
            // 构建告警信息
            Map<String, Object> alertInfo = buildAlertInfo(type, title, message, taskId);
            
            // 发送告警
            sendAlert(alertInfo);
            
        } catch (Exception e) {
            log.error("Failed to send custom alert: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查告警频率限制
     * @param type 告警类型
     * @param taskId 任务ID
     * @return 是否允许发送告警
     */
    @Override
    public boolean checkAlertRateLimit(String type, Long taskId) {
        try {
            // 构建限流键
            String rateLimitKey = DataRsyncConstants.RedisKey.MONITOR_PREFIX + "alert:rate_limit:" + type;
            if (taskId != null) {
                rateLimitKey += ":" + taskId;
            }
            
            // 检查是否在限流窗口内
            String lastAlertTime = redisTemplate.opsForValue().get(rateLimitKey);
            if (lastAlertTime != null) {
                LocalDateTime lastTime = LocalDateTime.parse(lastAlertTime);
                LocalDateTime now = LocalDateTime.now();
                
                // 限制同一类型的告警每分钟最多发送一次
                if (now.minusMinutes(1).isBefore(lastTime)) {
                    log.debug("Alert rate limit exceeded: type={}, taskId={}", type, taskId);
                    return false;
                }
            }
            
            // 更新最后告警时间
            redisTemplate.opsForValue().set(rateLimitKey, LocalDateTime.now().toString());
            redisTemplate.expire(rateLimitKey, 5, java.util.concurrent.TimeUnit.MINUTES);
            
            return true;
        } catch (Exception e) {
            log.error("Failed to check alert rate limit: {}", e.getMessage(), e);
            // 限流检查失败时，默认允许发送告警
            return true;
        }
    }

    /**
     * 清理过期告警
     */
    @Override
    public void cleanExpiredAlerts() {
        try {
            // 实际项目中，这里可以实现清理过期告警的逻辑
            // 例如，删除超过7天的告警记录
            log.debug("Cleaning expired alerts");
        } catch (Exception e) {
            log.error("Failed to clean expired alerts: {}", e.getMessage(), e);
        }
    }
}
