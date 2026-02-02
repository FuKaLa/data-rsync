package com.data.rsync.common.config;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置管理器
 * 实现配置的灰度发布和回滚机制
 */
@Slf4j
public class ConfigManager {

    private static final ConfigManager INSTANCE = new ConfigManager();
    private final ConfigCenterClient configCenterClient;
    private final Map<String, String> currentConfigs;
    private final Map<String, Map<String, String>> configVersions;
    private final Map<String, String> grayConfigs;

    private ConfigManager() {
        configCenterClient = ConfigCenterClient.getInstance();
        currentConfigs = new ConcurrentHashMap<>();
        configVersions = new ConcurrentHashMap<>();
        grayConfigs = new ConcurrentHashMap<>();
        log.info("ConfigManager initialized");
    }

    /**
     * 获取配置管理器实例
     * @return 配置管理器实例
     */
    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化配置管理器
     * @param serverAddr Nacos服务地址
     * @param namespace Nacos命名空间
     * @return 初始化是否成功
     */
    public boolean initialize(String serverAddr, String namespace) {
        return configCenterClient.initialize(serverAddr, namespace);
    }

    /**
     * 获取配置
     * @param key 配置键
     * @return 配置值
     */
    public String getConfig(String key) {
        return currentConfigs.get(key);
    }

    /**
     * 获取灰度配置
     * @param key 配置键
     * @return 灰度配置值
     */
    public String getGrayConfig(String key) {
        return grayConfigs.get(key);
    }

    /**
     * 加载配置
     * @param dataId 配置ID
     * @param group 配置分组
     * @return 加载是否成功
     */
    public boolean loadConfig(String dataId, String group) {
        try {
            String config = configCenterClient.getConfig(dataId, group, 5000);
            if (config != null) {
                currentConfigs.put(dataId, config);
                // 保存配置版本
                saveConfigVersion(dataId, config);
                log.info("Loaded config: dataId={}, group={}", dataId, group);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to load config: dataId={}, group={}, error={}", dataId, group, e.getMessage(), e);
            return false;
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
        try {
            boolean success = configCenterClient.publishConfig(dataId, group, content);
            if (success) {
                currentConfigs.put(dataId, content);
                saveConfigVersion(dataId, content);
                log.info("Published config: dataId={}, group={}", dataId, group);
            }
            return success;
        } catch (Exception e) {
            log.error("Failed to publish config: dataId={}, group={}, error={}", dataId, group, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发布灰度配置
     * @param dataId 配置ID
     * @param group 配置分组
     * @param content 配置内容
     * @return 发布是否成功
     */
    public boolean publishGrayConfig(String dataId, String group, String content) {
        try {
            // 灰度配置可以发布到不同的group
            String grayGroup = group + ".gray";
            boolean success = configCenterClient.publishConfig(dataId, grayGroup, content);
            if (success) {
                grayConfigs.put(dataId, content);
                log.info("Published gray config: dataId={}, group={}", dataId, grayGroup);
            }
            return success;
        } catch (Exception e) {
            log.error("Failed to publish gray config: dataId={}, group={}, error={}", dataId, group, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 回滚配置
     * @param dataId 配置ID
     * @param version 版本号
     * @return 回滚是否成功
     */
    public boolean rollbackConfig(String dataId, String version) {
        try {
            Map<String, String> versions = configVersions.get(dataId);
            if (versions != null && versions.containsKey(version)) {
                String oldConfig = versions.get(version);
                boolean success = configCenterClient.publishConfig(dataId, "DEFAULT_GROUP", oldConfig);
                if (success) {
                    currentConfigs.put(dataId, oldConfig);
                    log.info("Rolled back config: dataId={}, version={}", dataId, version);
                }
                return success;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to rollback config: dataId={}, version={}, error={}", dataId, version, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 保存配置版本
     * @param dataId 配置ID
     * @param content 配置内容
     */
    private void saveConfigVersion(String dataId, String content) {
        Map<String, String> versions = configVersions.computeIfAbsent(dataId, k -> new HashMap<>());
        String version = String.valueOf(System.currentTimeMillis());
        versions.put(version, content);
        // 只保留最近10个版本
        if (versions.size() > 10) {
            // 删除最旧的版本
            String oldestVersion = versions.keySet().stream().sorted().findFirst().orElse(null);
            if (oldestVersion != null) {
                versions.remove(oldestVersion);
            }
        }
    }

    /**
     * 获取配置版本历史
     * @param dataId 配置ID
     * @return 配置版本历史
     */
    public Map<String, String> getConfigVersions(String dataId) {
        return configVersions.get(dataId);
    }

    /**
     * 激活灰度配置
     * @param dataId 配置ID
     * @return 激活是否成功
     */
    public boolean activateGrayConfig(String dataId) {
        String grayConfig = grayConfigs.get(dataId);
        if (grayConfig != null) {
            return publishConfig(dataId, "DEFAULT_GROUP", grayConfig);
        }
        return false;
    }

    /**
     * 取消灰度配置
     * @param dataId 配置ID
     * @return 取消是否成功
     */
    public boolean cancelGrayConfig(String dataId) {
        grayConfigs.remove(dataId);
        return configCenterClient.removeConfig(dataId, "DEFAULT_GROUP.gray");
    }

    /**
     * 监听配置变更
     * @param dataId 配置ID
     * @param group 配置分组
     */
    public void listenConfigChange(String dataId, String group) {
        configCenterClient.addConfigListener(dataId, group, new ConfigChangeListener() {
            @Override
            public void receiveConfigChange(String configInfo) {
                currentConfigs.put(dataId, configInfo);
                saveConfigVersion(dataId, configInfo);
                log.info("Config changed: dataId={}, group={}", dataId, group);
            }
        });
    }

    /**
     * 检查配置中心连接状态
     * @return 连接是否可用
     */
    public boolean checkConnection() {
        return configCenterClient.checkConnection();
    }

    /**
     * 关闭配置管理器
     */
    public void close() {
        configCenterClient.close();
        log.info("ConfigManager closed");
    }

}
