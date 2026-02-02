package com.data.rsync.common.adapter;

import com.data.rsync.common.model.DataSource;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源适配器工厂
 * 用于管理和加载不同的数据源适配器
 */
@Slf4j
public class DataSourceAdapterFactory {

    /**
     * 适配器映射
     */
    private static final Map<String, DataSourceAdapter> adapterMap = new ConcurrentHashMap<>();

    /**
     * 注册适配器
     * @param adapter 数据源适配器
     */
    public static void registerAdapter(DataSourceAdapter adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("Adapter cannot be null");
        }

        String dataSourceType = adapter.getDataSourceType();
        if (dataSourceType == null || dataSourceType.isEmpty()) {
            throw new IllegalArgumentException("DataSource type cannot be null or empty");
        }

        adapterMap.put(dataSourceType, adapter);
        log.info("Registered data source adapter for type: {}", dataSourceType);
    }

    /**
     * 获取适配器
     * @param dataSourceType 数据源类型
     * @return 数据源适配器
     */
    public static DataSourceAdapter getAdapter(String dataSourceType) {
        DataSourceAdapter adapter = adapterMap.get(dataSourceType);
        if (adapter == null) {
            throw new UnsupportedOperationException("Unsupported data source type: " + dataSourceType);
        }
        return adapter;
    }

    /**
     * 获取适配器
     * @param dataSource 数据源配置
     * @return 数据源适配器
     */
    public static DataSourceAdapter getAdapter(DataSource dataSource) {
        return getAdapter(dataSource.getType());
    }

    /**
     * 检查是否支持该数据源类型
     * @param dataSourceType 数据源类型
     * @return 是否支持
     */
    public static boolean supports(String dataSourceType) {
        return adapterMap.containsKey(dataSourceType);
    }

    /**
     * 检查是否支持该数据源
     * @param dataSource 数据源配置
     * @return 是否支持
     */
    public static boolean supports(DataSource dataSource) {
        return supports(dataSource.getType());
    }

    /**
     * 获取所有支持的数据源类型
     * @return 数据源类型列表
     */
    public static java.util.Set<String> getSupportedTypes() {
        return adapterMap.keySet();
    }

    /**
     * 初始化默认适配器
     */
    public static void initDefaultAdapters() {
        // 注册默认的数据源适配器
        // 注意：实际实现中，这些适配器应该在各自的模块中实现
        // 这里只是作为示例
        log.info("Initializing default data source adapters");
    }

    /**
     * 清理所有适配器
     */
    public static void clearAdapters() {
        adapterMap.clear();
        log.info("Cleared all data source adapters");
    }

}
