package com.data.rsync.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 配置管理器
 * 实现配置的灰度发布、回滚机制和统一管理
 */
@Slf4j
public class ConfigManager {

    private static final ConfigManager INSTANCE = new ConfigManager();
    private final ConfigCenterClient configCenterClient;
    private final Map<String, String> currentConfigs;
    private final Map<String, Map<String, String>> configVersions;
    private final Map<String, String> grayConfigs;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

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
        if (initialized.compareAndSet(false, true)) {
            boolean result = configCenterClient.initialize(serverAddr, namespace);
            if (result) {
                log.info("ConfigManager initialized successfully");
            } else {
                initialized.set(false);
                log.error("ConfigManager initialization failed");
            }
            return result;
        }
        return true;
    }

    /**
     * 获取配置
     * @param key 配置键
     * @return 配置值
     */
    public String getConfig(String key) {
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, cannot get config: {}", key);
            return null;
        }
        return currentConfigs.get(key);
    }

    /**
     * 获取灰度配置
     * @param key 配置键
     * @return 灰度配置值
     */
    public String getGrayConfig(String key) {
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, cannot get gray config: {}", key);
            return null;
        }
        return grayConfigs.get(key);
    }

    /**
     * 加载配置
     * @param dataId 配置ID
     * @param group 配置分组
     * @return 加载是否成功
     */
    public boolean loadConfig(String dataId, String group) {
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, cannot load config: dataId={}, group={}", dataId, group);
            return false;
        }

        try {
            String config = configCenterClient.getConfig(dataId, group, 5000);
            if (config != null) {
                currentConfigs.put(dataId, config);
                // 保存配置版本
                saveConfigVersion(dataId, config);
                // 监听配置变更
                listenConfigChange(dataId, group);
                log.info("Loaded config: dataId={}, group={}", dataId, group);
                return true;
            }
            log.warn("Config not found: dataId={}, group={}", dataId, group);
            return false;
        } catch (Exception e) {
            log.error("Failed to load config: dataId={}, group={}, error={}", dataId, group, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量加载配置
     * @param configs 配置列表，key为dataId，value为group
     * @return 加载成功的配置数量
     */
    public int batchLoadConfigs(Map<String, String> configs) {
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, cannot batch load configs");
            return 0;
        }

        int successCount = 0;
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            if (loadConfig(entry.getKey(), entry.getValue())) {
                successCount++;
            }
        }
        log.info("Batch loaded {} configs out of {}", successCount, configs.size());
        return successCount;
    }

    /**
     * 发布配置
     * @param dataId 配置ID
     * @param group 配置分组
     * @param content 配置内容
     * @return 发布是否成功
     */
    public boolean publishConfig(String dataId, String group, String content) {
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, cannot publish config: dataId={}, group={}", dataId, group);
            return false;
        }

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
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, cannot publish gray config: dataId={}, group={}", dataId, group);
            return false;
        }

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
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, cannot rollback config: dataId={}, version={}", dataId, version);
            return false;
        }

        try {
            Map<String, String> versions = configVersions.get(dataId);
            if (versions != null && versions.containsKey(version)) {
                String oldConfig = versions.get(version);
                boolean success = configCenterClient.publishConfig(dataId, "DEFAULT_GROUP", oldConfig);
                if (success) {
                    currentConfigs.put(dataId, oldConfig);
                    saveConfigVersion(dataId, oldConfig);
                    log.info("Rolled back config: dataId={}, version={}", dataId, version);
                }
                return success;
            }
            log.warn("Config version not found: dataId={}, version={}", dataId, version);
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
        Map<String, String> versions = configVersions.computeIfAbsent(dataId, k -> new ConcurrentHashMap<>());
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
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, cannot get config versions: {}", dataId);
            return new HashMap<>();
        }
        return configVersions.getOrDefault(dataId, new HashMap<>());
    }

    /**
     * 激活灰度配置
     * @param dataId 配置ID
     * @return 激活是否成功
     */
    public boolean activateGrayConfig(String dataId) {
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, cannot activate gray config: {}", dataId);
            return false;
        }

        String grayConfig = grayConfigs.get(dataId);
        if (grayConfig != null) {
            return publishConfig(dataId, "DEFAULT_GROUP", grayConfig);
        }
        log.warn("Gray config not found: {}", dataId);
        return false;
    }

    /**
     * 取消灰度配置
     * @param dataId 配置ID
     * @return 取消是否成功
     */
    public boolean cancelGrayConfig(String dataId) {
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, cannot cancel gray config: {}", dataId);
            return false;
        }

        grayConfigs.remove(dataId);
        return configCenterClient.removeConfig(dataId, "DEFAULT_GROUP.gray");
    }

    /**
     * 监听配置变更
     * @param dataId 配置ID
     * @param group 配置分组
     */
    public void listenConfigChange(String dataId, String group) {
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, cannot listen config change: dataId={}, group={}", dataId, group);
            return;
        }

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
        if (!initialized.get()) {
            log.warn("ConfigManager not initialized, connection check failed");
            return false;
        }
        return configCenterClient.checkConnection();
    }

    /**
     * 关闭配置管理器
     */
    public void close() {
        if (initialized.compareAndSet(true, false)) {
            configCenterClient.close();
            currentConfigs.clear();
            configVersions.clear();
            grayConfigs.clear();
            log.info("ConfigManager closed");
        }
    }

    /**
     * 检查是否已初始化
     * @return 是否已初始化
     */
    public boolean isInitialized() {
        return initialized.get();
    }

}
