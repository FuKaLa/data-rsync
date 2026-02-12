package com.data.rsync.common.adapter.impl;

import com.data.rsync.common.adapter.AbstractDataSourceAdapter;
import com.data.rsync.common.model.DataSource;
import com.zaxxer.hikari.HikariConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PostgreSQL 数据源适配器
 */
public class PostgreSQLDataSourceAdapter extends AbstractDataSourceAdapter {

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLDataSourceAdapter.class);

    @Override
    public String getDataSourceType() {
        return "POSTGRESQL";
    }

    @Override
    protected String getDriverClassName(DataSource dataSource) {
        return "org.postgresql.Driver";
    }

    @Override
    protected String buildConnectionUrl(DataSource dataSource) {
        StringBuilder url = new StringBuilder("jdbc:postgresql://");
        url.append(dataSource.getHost()).append(":").append(dataSource.getPort());
        if (dataSource.getDatabase() != null && !dataSource.getDatabase().isEmpty()) {
            url.append("/").append(dataSource.getDatabase());
        }
        url.append("?ssl=false&encoding=UTF-8&charSet=UTF-8");
        url.append("&prepareThreshold=5&binaryTransfer=true&socketTimeout=30000");
        return url.toString();
    }

    protected void configureConnectionPool(HikariConfig config, DataSource dataSource) {
        // 企业级连接池配置
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        
        // PostgreSQL 特定配置
        config.addDataSourceProperty("ssl", "false");
        config.addDataSourceProperty("encoding", "UTF-8");
        config.addDataSourceProperty("charSet", "UTF-8");
        config.addDataSourceProperty("prepareThreshold", "5");
        config.addDataSourceProperty("binaryTransfer", "true");
        config.addDataSourceProperty("socketTimeout", "30000");
    }

    @Override
    public List<String> getDatabases(DataSource dataSource) {
        List<String> databases = new ArrayList<>();
        try (Connection conn = getConnection(dataSource)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getCatalogs();
            while (rs.next()) {
                String databaseName = rs.getString("TABLE_CAT");
                // 排除系统数据库
                if (!"postgres".equals(databaseName) && 
                    !"template0".equals(databaseName) && 
                    !"template1".equals(databaseName)) {
                    databases.add(databaseName);
                }
            }
            rs.close();
        } catch (SQLException e) {
            log.error("Failed to get databases: {}", e.getMessage(), e);
        }
        return databases;
    }

    @Override
    public List<String> getTables(DataSource dataSource, String database) {
        List<String> tables = new ArrayList<>();
        try (Connection conn = getConnection(dataSource)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(database, null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            rs.close();
        } catch (SQLException e) {
            log.error("Failed to get tables: {}", e.getMessage(), e);
        }
        return tables;
    }

    @Override
    public List<Map<String, Object>> getTableColumns(DataSource dataSource, String database, String table) {
        List<Map<String, Object>> columns = new ArrayList<>();
        try (Connection conn = getConnection(dataSource)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getColumns(database, null, table, "%");
            while (rs.next()) {
                Map<String, Object> column = Map.of(
                    "name", rs.getString("COLUMN_NAME"),
                    "type", rs.getString("TYPE_NAME"),
                    "size", rs.getInt("COLUMN_SIZE"),
                    "nullable", rs.getBoolean("NULLABLE"),
                    "defaultValue", rs.getString("COLUMN_DEF"),
                    "description", rs.getString("REMARKS")
                );
                columns.add(column);
            }
            rs.close();
        } catch (SQLException e) {
            log.error("Failed to get table columns: {}", e.getMessage(), e);
        }
        return columns;
    }

    @Override
    public String getSyncQuery(DataSource dataSource, String database, String table, String syncCondition) {
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(database).append(".").append(table);
        if (syncCondition != null && !syncCondition.isEmpty()) {
            query.append(" WHERE ").append(syncCondition);
        }
        return query.toString();
    }

    @Override
    public String getIncrementalSyncQuery(DataSource dataSource, String database, String table, String incrementColumn, Object lastValue) {
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(database).append(".").append(table);
        query.append(" WHERE ").append(incrementColumn).append(" > ?");
        query.append(" ORDER BY ").append(incrementColumn).append(" ASC");
        return query.toString();
    }

}
