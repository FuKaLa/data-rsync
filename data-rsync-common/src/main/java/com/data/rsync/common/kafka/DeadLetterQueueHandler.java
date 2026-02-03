package com.data.rsync.common.kafka;

import com.data.rsync.common.constants.DataRsyncConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 死信队列处理器
 * 处理Kafka消息消费失败的情况，实现消息重发和错误处理
 */
@Component
@Slf4j
public class DeadLetterQueueHandler {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 消息重试计数器
     */
    private final Map<String, AtomicInteger> retryCounter = new ConcurrentHashMap<>();

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 10;

    /**
     * 监听死信队列
     * @param message 错误消息
     * @param originalTopic 原始主题
     * @param originalKey 原始消息键
     */
    @KafkaListener(topics = DataRsyncConstants.KafkaTopic.ERROR_TOPIC, groupId = "dead-letter-group")
    public void handleDeadLetterMessage(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header("originalTopic") String originalTopic,
            @Header("originalKey") String originalKey,
            @Header("errorMessage") String errorMessage) {

        log.warn("Received dead letter message from topic {}: {}", topic, message);

        try {
            // 生成重试键
            String retryKey = originalTopic + "-" + originalKey;
            AtomicInteger counter = retryCounter.computeIfAbsent(retryKey, k -> new AtomicInteger(0));
            int retryCount = counter.incrementAndGet();

            log.info("Processing retry {} for message: {}, error: {}", retryCount, originalKey, errorMessage);

            // 检查重试次数
            if (retryCount <= MAX_RETRY_COUNT) {
                // 计算重试延迟（指数退避策略）
                long delayMs = calculateRetryDelay(retryCount);
                log.info("Retrying message after {}ms", delayMs);

                // 延迟重试
                Thread.sleep(delayMs);

                // 重新发送消息到原始主题
                kafkaTemplate.send(originalTopic, originalKey, message);
                log.info("Resent message to original topic: {}", originalTopic);
            } else {
                // 超过最大重试次数，记录到错误日志并通知人工处理
                log.error("Max retry count exceeded for message: {}", originalKey);
                log.error("Error details: {}", errorMessage);
                log.error("Original message: {}", message);

                // 清理重试计数器
                retryCounter.remove(retryKey);

                // 发送告警通知（邮件、钉钉等）
                sendAlertNotification(originalTopic, originalKey, errorMessage, message);
            }
        } catch (Exception e) {
            log.error("Failed to process dead letter message: {}", e.getMessage(), e);
        }
    }

    /**
     * 计算重试延迟（指数退避策略）
     * @param retryCount 重试次数
     * @return 延迟时间（毫秒）
     */
    private long calculateRetryDelay(int retryCount) {
        // 基础延迟1秒，每次重试翻倍，最大延迟10分钟
        long baseDelay = 1000;
        long maxDelay = 10 * 60 * 1000;
        long delay = baseDelay * (1 << (retryCount - 1));
        return Math.min(delay, maxDelay);
    }

    /**
     * 发送消息到死信队列
     * @param originalTopic 原始主题
     * @param originalKey 原始消息键
     * @param message 消息内容
     * @param errorMessage 错误信息
     */
    public void sendToDeadLetterQueue(String originalTopic, String originalKey, String message, String errorMessage) {
        try {
            // 构建死信消息
            org.springframework.messaging.Message<String> deadLetterMessage = MessageBuilder
                    .withPayload(message)
                    .setHeader("originalTopic", originalTopic)
                    .setHeader("originalKey", originalKey)
                    .setHeader("errorMessage", errorMessage)
                    .build();

            // 发送到死信队列
            kafkaTemplate.send(deadLetterMessage);
            log.info("Sent message to dead letter queue: {}", originalKey);
        } catch (Exception e) {
            log.error("Failed to send message to dead letter queue: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送消息到指定主题
     * @param topic 主题
     * @param key 消息键
     * @param message 消息内容
     */
    public void sendMessage(String topic, String key, String message) {
        try {
            kafkaTemplate.send(topic, key, message);
            log.debug("Sent message to topic {}: {}", topic, message);
        } catch (Exception e) {
            log.error("Failed to send message to topic {}: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * 发送消息到指定主题
     * @param topic 主题
     * @param message 消息内容
     */
    public void sendMessage(String topic, String message) {
        try {
            kafkaTemplate.send(topic, message);
            log.debug("Sent message to topic {}: {}", topic, message);
        } catch (Exception e) {
            log.error("Failed to send message to topic {}: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * 发送告警通知
     * @param originalTopic 原始主题
     * @param originalKey 原始消息键
     * @param errorMessage 错误信息
     * @param originalMessage 原始消息
     */
    private void sendAlertNotification(String originalTopic, String originalKey, String errorMessage, String originalMessage) {
        try {
            // 构建告警消息
            StringBuilder alertMessage = new StringBuilder();
            alertMessage.append("【Data Rsync 告警】")
                    .append("\n主题: ").append(originalTopic)
                    .append("\n消息键: ").append(originalKey)
                    .append("\n错误信息: ").append(errorMessage)
                    .append("\n原始消息: ").append(originalMessage)
                    .append("\n时间: ").append(new java.util.Date());

            // 发送邮件告警
            sendEmailAlert(alertMessage.toString());

            // 发送钉钉告警
            sendDingTalkAlert(alertMessage.toString());

            log.info("Sent alert notification for dead letter message: {}", originalKey);
        } catch (Exception e) {
            log.error("Failed to send alert notification: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送邮件告警
     * @param message 告警消息
     */
    private void sendEmailAlert(String message) {
        // 实现邮件告警发送逻辑
        // 这里简化处理，只记录日志
        log.info("Sending email alert: {}", message.substring(0, Math.min(message.length(), 100)) + "...");
    }

    /**
     * 发送钉钉告警
     * @param message 告警消息
     */
    private void sendDingTalkAlert(String message) {
        // 实现钉钉告警发送逻辑
        // 这里简化处理，只记录日志
        log.info("Sending DingTalk alert: {}", message.substring(0, Math.min(message.length(), 100)) + "...");
    }
}
