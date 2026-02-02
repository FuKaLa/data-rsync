package com.data.rsync.common.plugin.desensitize;

import com.data.rsync.common.plugin.desensitize.impl.PhoneDesensitizePlugin;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 脱敏插件管理器
 */
@Slf4j
public class DesensitizePluginManager {

    private static final DesensitizePluginManager INSTANCE = new DesensitizePluginManager();
    private final Map<String, DesensitizePlugin> pluginMap;

    private DesensitizePluginManager() {
        pluginMap = new HashMap<>();
        initDefaultPlugins();
        log.info("DesensitizePluginManager initialized");
    }

    /**
     * 获取脱敏插件管理器实例
     * @return 脱敏插件管理器实例
     */
    public static DesensitizePluginManager getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化默认插件
     */
    private void initDefaultPlugins() {
        registerPlugin(new PhoneDesensitizePlugin());
        // 后续可以添加其他默认插件
    }

    /**
     * 注册脱敏插件
     * @param plugin 脱敏插件
     */
    public void registerPlugin(DesensitizePlugin plugin) {
        pluginMap.put(plugin.getType(), plugin);
        log.info("Registered desensitize plugin: {}", plugin.getName());
    }

    /**
     * 获取脱敏插件
     * @param type 脱敏类型
     * @return 脱敏插件
     */
    public DesensitizePlugin getPlugin(String type) {
        return pluginMap.get(type);
    }

    /**
     * 执行脱敏处理
     * @param data 原始数据
     * @param type 脱敏类型
     * @return 脱敏后的数据
     */
    public String desensitize(String data, String type) {
        DesensitizePlugin plugin = getPlugin(type);
        if (plugin == null) {
            log.warn("No desensitize plugin found for type: {}", type);
            return data;
        }

        DesensitizeConfig config = DesensitizeConfig.getDefaultConfig(type);
        return plugin.desensitize(data, config);
    }

    /**
     * 执行脱敏处理
     * @param data 原始数据
     * @param type 脱敏类型
     * @param config 脱敏配置
     * @return 脱敏后的数据
     */
    public String desensitize(String data, String type, DesensitizeConfig config) {
        DesensitizePlugin plugin = getPlugin(type);
        if (plugin == null) {
            log.warn("No desensitize plugin found for type: {}", type);
            return data;
        }

        return plugin.desensitize(data, config);
    }

    /**
     * 获取所有脱敏插件
     * @return 脱敏插件映射
     */
    public Map<String, DesensitizePlugin> getAllPlugins() {
        return new HashMap<>(pluginMap);
    }

    /**
     * 检查是否支持该类型的脱敏
     * @param type 脱敏类型
     * @return 是否支持
     */
    public boolean supports(String type) {
        return pluginMap.containsKey(type);
    }

}
