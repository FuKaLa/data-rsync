package com.data.rsync.monitor.service.impl;

import com.data.rsync.monitor.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 告警服务实现类
 */
@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private JavaMailSender javaMailSender;

    // 邮件配置
    @Value("${monitor.alert.channels.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${monitor.alert.channels.email.from:}")
    private String emailFrom;

    @Value("${monitor.alert.channels.email.to:}")
    private String emailTo;

    // 钉钉配置
    @Value("${monitor.alert.channels.dingtalk.enabled:false}")
    private boolean dingtalkEnabled;

    @Value("${monitor.alert.channels.dingtalk.webhook:}")
    private String dingtalkWebhook;

    // 企业微信配置
    @Value("${monitor.alert.channels.wechat.enabled:false}")
    private boolean wechatEnabled;

    @Value("${monitor.alert.channels.wechat.webhook:}")
    private String wechatWebhook;

    @Override
    public boolean sendAlert(String severity, String message, Map<String, Object> metrics) {
        boolean result = true;

        // 构建告警内容
        StringBuilder content = new StringBuilder();
        content.append("告警级别: ").append(severity).append("\n");
        content.append("告警消息: ").append(message).append("\n");
        content.append("相关指标: \n");
        if (metrics != null) {
            for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                content.append("  - " + entry.getKey() + ": " + entry.getValue() + "\n");
            }
        }

        // 发送邮件告警
        if (emailEnabled) {
            boolean emailResult = sendEmailAlert(emailTo, "[Data Rsync] " + severity + " 告警", content.toString());
            result = result && emailResult;
        }

        // 发送钉钉告警
        if (dingtalkEnabled) {
            boolean dingtalkResult = sendDingtalkAlert(dingtalkWebhook, content.toString());
            result = result && dingtalkResult;
        }

        // 发送企业微信告警
        if (wechatEnabled) {
            boolean wechatResult = sendWechatAlert(wechatWebhook, content.toString());
            result = result && wechatResult;
        }

        return result;
    }

    @Override
    public Map<String, Object> checkThreshold(String metricName, double metricValue) {
        Map<String, Object> result = new HashMap<>();

        // 默认阈值配置
        double criticalThreshold = 90.0;
        double warningThreshold = 70.0;
        double infoThreshold = 50.0;

        // 根据指标名称调整阈值
        if (metricName.contains("heap")) {
            criticalThreshold = 95.0;
            warningThreshold = 85.0;
        } else if (metricName.contains("cpu")) {
            criticalThreshold = 90.0;
            warningThreshold = 80.0;
        } else if (metricName.contains("memory")) {
            criticalThreshold = 90.0;
            warningThreshold = 80.0;
        }

        // 检查阈值
        if (metricValue >= criticalThreshold) {
            result.put("severity", "critical");
            result.put("threshold", criticalThreshold);
            result.put("message", metricName + " 超过临界阈值: " + metricValue);
        } else if (metricValue >= warningThreshold) {
            result.put("severity", "warning");
            result.put("threshold", warningThreshold);
            result.put("message", metricName + " 超过警告阈值: " + metricValue);
        } else if (metricValue >= infoThreshold) {
            result.put("severity", "info");
            result.put("threshold", infoThreshold);
            result.put("message", metricName + " 接近阈值: " + metricValue);
        } else {
            result.put("severity", "normal");
            result.put("threshold", infoThreshold);
            result.put("message", metricName + " 正常: " + metricValue);
        }

        return result;
    }

    @Override
    public boolean sendEmailAlert(String to, String subject, String content) {
        try {
            // 创建简单邮件消息
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            // 发送邮件
            javaMailSender.send(message);
            return true;
        } catch (Exception e) {
            // 记录错误日志
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean sendDingtalkAlert(String webhook, String message) {
        try {
            // TODO: 实现钉钉告警发送
            // 这里需要使用钉钉机器人的 API 发送告警消息
            // 可以使用 RestTemplate 或 HttpClient 发送 HTTP 请求
            System.out.println("发送钉钉告警: " + message);
            return true;
        } catch (Exception e) {
            // 记录错误日志
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean sendWechatAlert(String webhook, String message) {
        try {
            // TODO: 实现企业微信告警发送
            // 这里需要使用企业微信机器人的 API 发送告警消息
            // 可以使用 RestTemplate 或 HttpClient 发送 HTTP 请求
            System.out.println("发送企业微信告警: " + message);
            return true;
        } catch (Exception e) {
            // 记录错误日志
            e.printStackTrace();
            return false;
        }
    }

}
