package com.data.rsync.common.strategy;

import com.data.rsync.common.strategy.impl.MySQLDataSourceStrategy;
import com.data.rsync.common.strategy.impl.PostgreSQLDataSourceStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据源策略工厂
 */
public class DataSourceStrategyFactory {

    private static final Map<String, DataSourceStrategy> strategyMap = new HashMap<>();

    static {
        // 初始化所有数据源策略
        strategyMap.put(new MySQLDataSourceStrategy().getDataSourceType(), new MySQLDataSourceStrategy());
        strategyMap.put(new PostgreSQLDataSourceStrategy().getDataSourceType(), new PostgreSQLDataSourceStrategy());
        // 可以根据需要添加其他数据源策略
    }

    /**
     * 获取数据源策略
     * @param dataSourceType 数据源类型
     * @return 数据源策略
     * @throws IllegalArgumentException 当数据源类型不支持时抛出
     */
    public static DataSourceStrategy getStrategy(String dataSourceType) {
        DataSourceStrategy strategy = strategyMap.get(dataSourceType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported data source type: " + dataSourceType);
        }
        return strategy;
    }

    /**
     * 注册新的数据源策略
     * @param dataSourceType 数据源类型
     * @param strategy 数据源策略
     */
    public static void registerStrategy(String dataSourceType, DataSourceStrategy strategy) {
        strategyMap.put(dataSourceType, strategy);
    }

    /**
     * 移除数据源策略
     * @param dataSourceType 数据源类型
     */
    public static void removeStrategy(String dataSourceType) {
        strategyMap.remove(dataSourceType);
    }
}
