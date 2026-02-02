package com.data.rsync.monitor.service;

import java.util.Map;

/**
 * 告警服务接口
 */
public interface AlertService {

    /**
     * 发送告警
     * @param severity 告警级别
     * @param message 告警消息
     * @param metrics 相关指标
     * @return 发送结果
     */
    boolean sendAlert(String severity, String message, Map<String, Object> metrics);

    /**
     * 检查指标是否超过阈值
     * @param metricName 指标名称
     * @param metricValue 指标值
     * @return 检查结果
     */
    Map<String, Object> checkThreshold(String metricName, double metricValue);

    /**
     * 发送邮件告警
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @return 发送结果
     */
    boolean sendEmailAlert(String to, String subject, String content);

    /**
     * 发送钉钉告警
     * @param webhook 钉钉机器人 webhook
     * @param message 告警消息
     * @return 发送结果
     */
    boolean sendDingtalkAlert(String webhook, String message);

    /**
     * 发送企业微信告警
     * @param webhook 企业微信机器人 webhook
     * @param message 告警消息
     * @return 发送结果
     */
    boolean sendWechatAlert(String webhook, String message);

}
