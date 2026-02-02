package com.data.rsync.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "data.rsync")
public class NacosConfig {

    private DatabaseConfig database;
    private MilvusConfig milvus;
    private KafkaConfig kafka;
    private RedisConfig redis;
    private TaskConfig task;
    private MonitorConfig monitor;
    private SecurityConfig security;

    @Data
    public static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        private int maxConnections;
        private int connectionTimeout;
        private int socketTimeout;
    }

    @Data
    public static class MilvusConfig {
        private String host;
        private int port;
        private String username;
        private String password;
        private String collectionName;
        private int vectorDimension;
        private int batchSize;
        private int maxRetries;
    }

    @Data
    public static class KafkaConfig {
        private String bootstrapServers;
        private String groupId;
        private String topic;
        private int maxPollRecords;
        private int sessionTimeout;
        private int heartbeatInterval;
    }

    @Data
    public static class RedisConfig {
        private String host;
        private int port;
        private String password;
        private int database;
        private int maxTotal;
        private int maxIdle;
        private int minIdle;
        private long maxWaitMillis;
    }

    @Data
    public static class TaskConfig {
        private int batchSize;
        private int threadPoolSize;
        private int maxRetryTimes;
        private long retryInterval;
        private long timeout;
    }

    @Data
    public static class MonitorConfig {
        private String prometheusEndpoint;
        private String grafanaUrl;
        private int alertThreshold;
        private String alertChannels;
    }

    @Data
    public static class SecurityConfig {
        private String jwtSecret;
        private long jwtExpiration;
        private String encryptionKey;
        private boolean enableSsl;
    }
}
