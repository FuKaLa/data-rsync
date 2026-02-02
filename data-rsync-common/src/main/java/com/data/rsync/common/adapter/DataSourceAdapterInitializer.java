package com.data.rsync.common.adapter;

import com.data.rsync.common.adapter.impl.MySQLDataSourceAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据源适配器初始化器
 * 用于在系统启动时注册所有的数据源适配器
 */
@Slf4j
public class DataSourceAdapterInitializer {

    /**
     * 初始化数据源适配器
     */
    public static void initialize() {
        log.info("Initializing data source adapters");

        // 注册默认的数据源适配器
        registerDefaultAdapters();

        log.info("Data source adapters initialized successfully");
        log.info("Supported data source types: {}", DataSourceAdapterFactory.getSupportedTypes());
    }

    /**
     * 注册默认的数据源适配器
     */
    private static void registerDefaultAdapters() {
        // 注册 MySQL 适配器
        DataSourceAdapterFactory.registerAdapter(new MySQLDataSourceAdapter());

        // TODO: 注册其他数据源适配器
        // DataSourceAdapterFactory.registerAdapter(new PostgreSQLDataSourceAdapter());
        // DataSourceAdapterFactory.registerAdapter(new OracleDataSourceAdapter());
        // DataSourceAdapterFactory.registerAdapter(new SQLServerDataSourceAdapter());
        // DataSourceAdapterFactory.registerAdapter(new MongoDBDataSourceAdapter());
        // DataSourceAdapterFactory.registerAdapter(new RedisDataSourceAdapter());
    }

}
