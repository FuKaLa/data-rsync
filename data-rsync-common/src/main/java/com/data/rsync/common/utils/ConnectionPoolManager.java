package com.data.rsync.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 连接池管理器
 * 统一管理数据库、消息队列、缓存等连接池配置
 */
@Component
@Slf4j
public class ConnectionPoolManager {

    private static final ConnectionPoolManager INSTANCE = new ConnectionPoolManager();

    private final Map<String, Properties> poolConfigs = new HashMap<>();

    private ConnectionPoolManager() {
        initDefaultConfigs();
    }

    /**
     * 获取连接池管理器实例
     * @return 连接池管理器实例
     */
    public static ConnectionPoolManager getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化默认配置
     */
    private void initDefaultConfigs() {
        // MySQL连接池配置
        Properties mysqlProps = new Properties();
        mysqlProps.setProperty("maximumPoolSize", "20");
        mysqlProps.setProperty("minimumIdle", "5");
        mysqlProps.setProperty("idleTimeout", "60000");
        mysqlProps.setProperty("maxLifetime", "1800000");
        mysqlProps.setProperty("connectionTimeout", "5000");
        poolConfigs.put("mysql", mysqlProps);

        // Kafka连接池配置
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("max.in.flight.requests.per.connection", "5");
        kafkaProps.setProperty("retries", "3");
        kafkaProps.setProperty("retry.backoff.ms", "100");
        kafkaProps.setProperty("request.timeout.ms", "30000");
        poolConfigs.put("kafka", kafkaProps);

        // Redis连接池配置
        Properties redisProps = new Properties();
        redisProps.setProperty("maxTotal", "100");
        redisProps.setProperty("maxIdle", "50");
        redisProps.setProperty("minIdle", "10");
        redisProps.setProperty("maxWaitMillis", "3000");
        redisProps.setProperty("testOnBorrow", "true");
        poolConfigs.put("redis", redisProps);

        // Milvus连接池配置
        Properties milvusProps = new Properties();
        milvusProps.setProperty("connectTimeout", "5000");
        milvusProps.setProperty("keepAliveTime", "30000");
        milvusProps.setProperty("maxConnectionPoolSize", "20");
        poolConfigs.put("milvus", milvusProps);

        log.info("ConnectionPoolManager initialized with default configurations");
    }

    /**
     * 获取连接池配置
     * @param poolType 连接池类型
     * @return 连接池配置
     */
    public Properties getPoolConfig(String poolType) {
        return poolConfigs.getOrDefault(poolType, new Properties());
    }

    /**
     * 更新连接池配置
     * @param poolType 连接池类型
     * @param configs 配置信息
     */
    public void updatePoolConfig(String poolType, Properties configs) {
        poolConfigs.put(poolType, configs);
        log.info("Updated connection pool config for type: {}", poolType);
    }

    /**
     * 根据环境获取连接池配置
     * @param poolType 连接池类型
     * @param environment 环境（dev/test/prod）
     * @return 连接池配置
     */
    public Properties getPoolConfigByEnvironment(String poolType, String environment) {
        // 根据环境返回不同的配置
        Properties baseConfig = getPoolConfig(poolType);
        Properties envConfig = new Properties();
        envConfig.putAll(baseConfig);

        switch (environment) {
            case "dev":
                // 开发环境配置
                if ("mysql".equals(poolType)) {
                    envConfig.setProperty("maximumPoolSize", "10");
                }
                break;
            case "test":
                // 测试环境配置
                if ("mysql".equals(poolType)) {
                    envConfig.setProperty("maximumPoolSize", "15");
                }
                break;
            case "prod":
                // 生产环境配置
                if ("mysql".equals(poolType)) {
                    envConfig.setProperty("maximumPoolSize", "20");
                }
                break;
        }

        return envConfig;
    }

    /**
     * 获取连接池状态
     * @return 连接池状态
     */
    public String getConnectionPoolStatus() {
        StringBuilder status = new StringBuilder();
        status.append("ConnectionPoolManager status:\n");
        poolConfigs.forEach((type, configs) -> {
            status.append(type).append(" pool configs: ").append(configs).append("\n");
        });
        return status.toString();
    }

    /**
     * 刷新配置
     * 从配置中心重新加载配置
     */
    public void refreshConfigs() {
        // 从配置中心加载配置
        // 这里可以集成Nacos配置中心
        log.info("Refreshed connection pool configurations");
    }
}
