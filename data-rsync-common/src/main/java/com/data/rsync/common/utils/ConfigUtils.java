package com.data.rsync.common.utils;

import com.data.rsync.common.config.NacosConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigUtils {

    private static NacosConfig nacosConfig;

    @Autowired
    public void setNacosConfig(NacosConfig nacosConfig) {
        ConfigUtils.nacosConfig = nacosConfig;
    }

    public static NacosConfig getNacosConfig() {
        return nacosConfig;
    }

    public static NacosConfig.DatabaseConfig getDatabaseConfig() {
        return nacosConfig.getDatabase();
    }

    public static NacosConfig.MilvusConfig getMilvusConfig() {
        return nacosConfig.getMilvus();
    }

    public static NacosConfig.KafkaConfig getKafkaConfig() {
        return nacosConfig.getKafka();
    }

    public static NacosConfig.RedisConfig getRedisConfig() {
        return nacosConfig.getRedis();
    }

    public static NacosConfig.TaskConfig getTaskConfig() {
        return nacosConfig.getTask();
    }

    public static NacosConfig.MonitorConfig getMonitorConfig() {
        return nacosConfig.getMonitor();
    }

    public static NacosConfig.SecurityConfig getSecurityConfig() {
        return nacosConfig.getSecurity();
    }
}
