package com.data.rsync.monitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

/**
 * 监控服务配置类
 * 配置邮件发送、RestTemplate等核心功能
 */
@Configuration
public class MonitorConfig {

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean mailAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean mailStarttls;

    /**
     * 创建RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 创建JavaMailSender实例
     */
    @Bean
    public JavaMailSender javaMailSender() {
        // 返回一个默认的JavaMailSenderImpl实例，不进行实际配置
        // 这样即使没有邮件配置，应用也能正常启动
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        if (!mailHost.isEmpty()) {
            mailSender.setHost(mailHost);
            mailSender.setPort(mailPort);
            mailSender.setUsername(mailUsername);
            mailSender.setPassword(mailPassword);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", mailAuth);
            props.put("mail.smtp.starttls.enable", mailStarttls);
        }
        return mailSender;
    }
}
