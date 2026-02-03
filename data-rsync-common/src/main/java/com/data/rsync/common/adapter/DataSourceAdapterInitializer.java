package com.data.rsync.common.adapter;

import com.data.rsync.common.adapter.impl.MySQLDataSourceAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

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

        // 1. 通过SPI机制加载适配器
        loadAdaptersBySPI();

        // 2. 注册默认的数据源适配器（作为SPI的补充）
        registerDefaultAdapters();

        log.info("Data source adapters initialized successfully");
        log.info("Supported data source types: {}", DataSourceAdapterFactory.getSupportedTypes());
    }

    /**
     * 通过SPI机制加载数据源适配器
     */
    private static void loadAdaptersBySPI() {
        log.info("Loading data source adapters by SPI");

        ServiceLoader<DataSourceAdapter> serviceLoader = ServiceLoader.load(DataSourceAdapter.class);
        int loadedCount = 0;

        for (DataSourceAdapter adapter : serviceLoader) {
            try {
                DataSourceAdapterFactory.registerAdapter(adapter);
                loadedCount++;
                log.info("Loaded data source adapter via SPI: {}", adapter.getDataSourceType());
            } catch (Exception e) {
                log.error("Failed to load data source adapter via SPI: {}", e.getMessage(), e);
            }
        }

        log.info("Loaded {} data source adapters via SPI", loadedCount);
    }

    /**
     * 注册默认的数据源适配器
     */
    private static void registerDefaultAdapters() {
        // 注册 MySQL 适配器（作为SPI的补充）
        try {
            DataSourceAdapterFactory.registerAdapter(new MySQLDataSourceAdapter());
            log.info("Registered default MySQL data source adapter");
        } catch (Exception e) {
            log.error("Failed to register default MySQL data source adapter: {}", e.getMessage(), e);
        }

        // 注册其他默认数据源适配器
        // PostgreSQL 适配器
        try {
            Class<?> postgresAdapterClass = Class.forName("com.data.rsync.common.adapter.impl.PostgreSQLDataSourceAdapter");
            DataSourceAdapter postgresAdapter = (DataSourceAdapter) postgresAdapterClass.getDeclaredConstructor().newInstance();
            DataSourceAdapterFactory.registerAdapter(postgresAdapter);
            log.info("Registered default PostgreSQL data source adapter");
        } catch (ClassNotFoundException e) {
            log.debug("PostgreSQL data source adapter not found, skipping registration");
        } catch (Exception e) {
            log.error("Failed to register default PostgreSQL data source adapter: {}", e.getMessage(), e);
        }

        // Oracle 适配器
        try {
            Class<?> oracleAdapterClass = Class.forName("com.data.rsync.common.adapter.impl.OracleDataSourceAdapter");
            DataSourceAdapter oracleAdapter = (DataSourceAdapter) oracleAdapterClass.getDeclaredConstructor().newInstance();
            DataSourceAdapterFactory.registerAdapter(oracleAdapter);
            log.info("Registered default Oracle data source adapter");
        } catch (ClassNotFoundException e) {
            log.debug("Oracle data source adapter not found, skipping registration");
        } catch (Exception e) {
            log.error("Failed to register default Oracle data source adapter: {}", e.getMessage(), e);
        }

        // SQL Server 适配器
        try {
            Class<?> sqlServerAdapterClass = Class.forName("com.data.rsync.common.adapter.impl.SQLServerDataSourceAdapter");
            DataSourceAdapter sqlServerAdapter = (DataSourceAdapter) sqlServerAdapterClass.getDeclaredConstructor().newInstance();
            DataSourceAdapterFactory.registerAdapter(sqlServerAdapter);
            log.info("Registered default SQL Server data source adapter");
        } catch (ClassNotFoundException e) {
            log.debug("SQL Server data source adapter not found, skipping registration");
        } catch (Exception e) {
            log.error("Failed to register default SQL Server data source adapter: {}", e.getMessage(), e);
        }

        // MongoDB 适配器
        try {
            Class<?> mongoAdapterClass = Class.forName("com.data.rsync.common.adapter.impl.MongoDBDataSourceAdapter");
            DataSourceAdapter mongoAdapter = (DataSourceAdapter) mongoAdapterClass.getDeclaredConstructor().newInstance();
            DataSourceAdapterFactory.registerAdapter(mongoAdapter);
            log.info("Registered default MongoDB data source adapter");
        } catch (ClassNotFoundException e) {
            log.debug("MongoDB data source adapter not found, skipping registration");
        } catch (Exception e) {
            log.error("Failed to register default MongoDB data source adapter: {}", e.getMessage(), e);
        }

        // Redis 适配器
        try {
            Class<?> redisAdapterClass = Class.forName("com.data.rsync.common.adapter.impl.RedisDataSourceAdapter");
            DataSourceAdapter redisAdapter = (DataSourceAdapter) redisAdapterClass.getDeclaredConstructor().newInstance();
            DataSourceAdapterFactory.registerAdapter(redisAdapter);
            log.info("Registered default Redis data source adapter");
        } catch (ClassNotFoundException e) {
            log.debug("Redis data source adapter not found, skipping registration");
        } catch (Exception e) {
            log.error("Failed to register default Redis data source adapter: {}", e.getMessage(), e);
        }
    }

}
