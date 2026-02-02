package com.data.rsync.common.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * 配置中心客户端
 * 与Nacos配置中心交互
 */
@Slf4j
public class ConfigCenterClient {

    private static final ConfigCenterClient INSTANCE = new ConfigCenterClient();
    private ConfigService configService;
    private boolean initialized = false;

    private ConfigCenterClient() {
    }

    /**
     * 获取配置中心客户端实例
     * @return 配置中心客户端实例
     */
    public static ConfigCenterClient getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化配置中心客户端
     * @param serverAddr Nacos服务地址
     * @param namespace Nacos命名空间
     * @return 初始化是否成功
     */
    public boolean initialize(String serverAddr, String namespace) {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("namespace", namespace);
            configService = NacosFactory.createConfigService(properties);
            initialized = true;
            log.info("ConfigCenterClient initialized successfully: serverAddr={}, namespace={}", serverAddr, namespace);
            return true;
        } catch (NacosException e) {
            log.error("Failed to initialize ConfigCenterClient: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取配置
     * @param dataId 配置ID
     * @param group 配置分组
     * @param timeoutMs 超时时间
     * @return 配置内容
     */
    public String getConfig(String dataId, String group, long timeoutMs) {
        if (!initialized) {
            log.warn("ConfigCenterClient not initialized");
            return null;
        }

        try {
            return configService.getConfig(dataId, group, timeoutMs);
        } catch (NacosException e) {
            log.error("Failed to get config: dataId={}, group={}, error={}", dataId, group, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 发布配置
     * @param dataId 配置ID
     * @param group 配置分组
     * @param content 配置内容
     * @return 发布是否成功
     */
    public boolean publishConfig(String dataId, String group, String content) {
        if (!initialized) {
            log.warn("ConfigCenterClient not initialized");
            return false;
        }

        try {
            return configService.publishConfig(dataId, group, content);
        } catch (NacosException e) {
            log.error("Failed to publish config: dataId={}, group={}, error={}", dataId, group, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 删除配置
     * @param dataId 配置ID
     * @param group 配置分组
     * @return 删除是否成功
     */
    public boolean removeConfig(String dataId, String group) {
        if (!initialized) {
            log.warn("ConfigCenterClient not initialized");
            return false;
        }

        try {
            return configService.removeConfig(dataId, group);
        } catch (NacosException e) {
            log.error("Failed to remove config: dataId={}, group={}, error={}", dataId, group, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 监听配置变更
     * @param dataId 配置ID
     * @param group 配置分组
     * @param listener 配置监听器
     */
    public void addConfigListener(String dataId, String group, ConfigChangeListener listener) {
        if (!initialized) {
            log.warn("ConfigCenterClient not initialized");
            return;
        }

        try {
            configService.addListener(dataId, group, listener);
            log.info("Added config listener: dataId={}, group={}", dataId, group);
        } catch (NacosException e) {
            log.error("Failed to add config listener: dataId={}, group={}, error={}", dataId, group, e.getMessage(), e);
        }
    }

    /**
     * 移除配置监听器
     * @param dataId 配置ID
     * @param group 配置分组
     * @param listener 配置监听器
     */
    public void removeConfigListener(String dataId, String group, ConfigChangeListener listener) {
        if (!initialized) {
            log.warn("ConfigCenterClient not initialized");
            return;
        }

        configService.removeListener(dataId, group, listener);
        log.info("Removed config listener: dataId={}, group={}", dataId, group);
    }

    /**
     * 检查配置中心连接状态
     * @return 连接是否可用
     */
    public boolean checkConnection() {
        if (!initialized) {
            return false;
        }

        try {
            // 尝试获取一个简单的配置来验证连接
            String testConfig = getConfig("test", "DEFAULT_GROUP", 1000);
            return true;
        } catch (Exception e) {
            log.error("ConfigCenter connection check failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 关闭配置中心客户端
     */
    public void close() {
        if (initialized) {
            try {
                // Nacos ConfigService没有显式的close方法，这里可以做一些清理工作
                initialized = false;
                log.info("ConfigCenterClient closed");
            } catch (Exception e) {
                log.error("Failed to close ConfigCenterClient: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 获取配置服务实例
     * @return 配置服务实例
     */
    public ConfigService getConfigService() {
        return configService;
    }

    /**
     * 检查是否初始化
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }

}
