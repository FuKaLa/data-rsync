package com.data.rsync.common.adapter;

import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.utils.EncryptionUtils;
import com.data.rsync.common.constants.DataRsyncConstants;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象数据源适配器
 * 提供通用的实现方法
 */
public abstract class AbstractDataSourceAdapter implements DataSourceAdapter {

    private static final Logger log = LoggerFactory.getLogger(AbstractDataSourceAdapter.class);

    /**
     * 数据源缓存，用于管理连接池
     */
    private static final Map<String, HikariDataSource> dataSourceCache = new ConcurrentHashMap<>();

    /**
     * 获取驱动类名
     * @param dataSource 数据源配置
     * @return 驱动类名
     */
    protected abstract String getDriverClassName(DataSource dataSource);

    /**
     * 构建连接URL
     * @param dataSource 数据源配置
     * @return 连接URL
     */
    protected abstract String buildConnectionUrl(DataSource dataSource);

    /**
     * 获取数据源缓存键
     * @param dataSource 数据源配置
     * @return 缓存键
     */
    protected String getDataSourceCacheKey(DataSource dataSource) {
        return dataSource.getHost() + ":" + dataSource.getPort() + ":" + dataSource.getDatabase() + ":" + dataSource.getUsername();
    }

    /**
     * 获取或创建数据源
     * @param dataSource 数据源配置
     * @return 数据源
     */
    protected HikariDataSource getOrCreateDataSource(DataSource dataSource) {
        validateDataSource(dataSource);
        
        String cacheKey = getDataSourceCacheKey(dataSource);
        return dataSourceCache.computeIfAbsent(cacheKey, k -> {
            try {
                HikariConfig config = new HikariConfig();
                config.setDriverClassName(getDriverClassName(dataSource));
                config.setJdbcUrl(buildConnectionUrl(dataSource));
                config.setUsername(dataSource.getUsername());
                config.setPassword(dataSource.getPassword());
                
                // 连接池配置
                config.setMinimumIdle(5);
                config.setMaximumPoolSize(20);
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
                config.setValidationTimeout(5000);
                config.setLeakDetectionThreshold(60000);
                
                // 连接测试SQL
                config.setConnectionTestQuery("SELECT 1");
                
                // 连接池名称
                config.setPoolName("HikariPool-" + cacheKey.hashCode());
                
                log.info("Created new data source for: {}", cacheKey);
                return new HikariDataSource(config);
            } catch (Exception e) {
                log.error("Failed to create data source for: {}", cacheKey, e);
                throw new RuntimeException("Failed to create data source", e);
            }
        });
    }

    /**
     * 验证数据源配置
     * @param dataSource 数据源配置
     */
    protected void validateDataSource(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        if (dataSource.getHost() == null || dataSource.getHost().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }
        if (dataSource.getPort() == null || dataSource.getPort() <= 0) {
            throw new IllegalArgumentException("Port must be a positive integer");
        }
        if (dataSource.getUsername() == null || dataSource.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (dataSource.getPassword() == null || dataSource.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (dataSource.getDatabase() == null || dataSource.getDatabase().isEmpty()) {
            throw new IllegalArgumentException("Database cannot be null or empty");
        }
    }

    @Override
    public boolean testConnection(DataSource dataSource) {
        Connection connection = null;
        try {
            connection = getConnection(dataSource);
            return connection != null && connection.isValid(5);
        } catch (Exception e) {
            log.error("Failed to test connection: {}", e.getMessage(), e);
            return false;
        } finally {
            closeConnection(connection);
        }
    }

    @Override
    public Connection getConnection(DataSource dataSource) {
        try {
            HikariDataSource ds = getOrCreateDataSource(dataSource);
            Connection connection = ds.getConnection();
            connection.setAutoCommit(true);
            return connection;
        } catch (Exception e) {
            log.error("Failed to get connection: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get connection", e);
        }
    }

    /**
     * 清理数据源缓存
     */
    public static void clearDataSourceCache() {
        dataSourceCache.forEach((key, dataSource) -> {
            try {
                dataSource.close();
                log.info("Closed data source: {}", key);
            } catch (Exception e) {
                log.error("Failed to close data source: {}", key, e);
            }
        });
        dataSourceCache.clear();
        log.info("Cleared data source cache");
    }

    @Override
    public List<String> getDatabases(DataSource dataSource) {
        throw new UnsupportedOperationException("Get databases is not supported for this data source type");
    }

    @Override
    public List<String> getTables(DataSource dataSource, String databaseName) {
        throw new UnsupportedOperationException("Get tables is not supported for this data source type");
    }

    @Override
    public Map<String, String> getTableSchema(DataSource dataSource, String databaseName, String tableName) {
        throw new UnsupportedOperationException("Get table schema is not supported for this data source type");
    }

    @Override
    public List<Map<String, Object>> executeQuery(DataSource dataSource, String sql, List<Object> params) {
        validateSql(sql);
        
        try (Connection connection = getConnection(dataSource);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = executeStatement(statement, params)) {
            return processResultSet(resultSet);
        } catch (Exception e) {
            log.error("Failed to execute query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    @Override
    public int executeUpdate(DataSource dataSource, String sql, List<Object> params) {
        validateSql(sql);
        
        try (Connection connection = getConnection(dataSource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, params);
            return statement.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to execute update: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute update", e);
        }
    }

    @Override
    public int[] executeBatchUpdate(DataSource dataSource, String sql, List<List<Object>> paramsList) {
        validateSql(sql);
        
        try (Connection connection = getConnection(dataSource);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            
            try {
                if (paramsList != null && !paramsList.isEmpty()) {
                    for (List<Object> params : paramsList) {
                        statement.clearParameters();
                        setParameters(statement, params);
                        statement.addBatch();
                    }
                }
                
                int[] result = statement.executeBatch();
                connection.commit();
                return result;
            } catch (Exception e) {
                connection.rollback();
                log.error("Failed to execute batch update: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to execute batch update", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            log.error("Failed to execute batch update: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute batch update", e);
        }
    }

    /**
     * 验证SQL语句
     * @param sql SQL语句
     */
    protected void validateSql(String sql) {
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }
    }

    /**
     * 设置参数
     * @param statement 预处理语句
     * @param params 参数列表
     * @throws SQLException SQL异常
     */
    protected void setParameters(PreparedStatement statement, List<Object> params) throws SQLException {
        if (params != null && !params.isEmpty()) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
        }
    }

    /**
     * 执行语句
     * @param statement 预处理语句
     * @param params 参数列表
     * @return 结果集
     * @throws SQLException SQL异常
     */
    protected ResultSet executeStatement(PreparedStatement statement, List<Object> params) throws SQLException {
        setParameters(statement, params);
        return statement.executeQuery();
    }

    @Override
    public long getCount(DataSource dataSource, String databaseName, String tableName, String whereClause) {
        validateDatabaseAndTable(databaseName, tableName);
        
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ");
        sql.append(quoteIdentifier(databaseName)).append(".").append(quoteIdentifier(tableName));
        
        List<Object> params = new ArrayList<>();
        if (whereClause != null && !whereClause.isEmpty()) {
            sql.append(" WHERE " + whereClause);
        }

        List<Map<String, Object>> result = executeQuery(dataSource, sql.toString(), params);
        if (result != null && !result.isEmpty()) {
            Object count = result.get(0).values().iterator().next();
            return Long.parseLong(count.toString());
        }
        return 0;
    }

    @Override
    public List<Map<String, Object>> queryWithPagination(DataSource dataSource, String databaseName, String tableName, 
                                                       String fields, String whereClause, String orderBy, 
                                                       long offset, long limit) {
        validateDatabaseAndTable(databaseName, tableName);
        validatePaginationParams(offset, limit);
        
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(fields).append(" FROM ")
           .append(quoteIdentifier(databaseName)).append(".").append(quoteIdentifier(tableName));
        
        if (whereClause != null && !whereClause.isEmpty()) {
            sql.append(" WHERE " + whereClause);
        }
        if (orderBy != null && !orderBy.isEmpty()) {
            sql.append(" ORDER BY " + orderBy);
        }
        sql.append(" LIMIT ? OFFSET ?");

        List<Object> params = new ArrayList<>();
        params.add(limit);
        params.add(offset);

        return executeQuery(dataSource, sql.toString(), params);
    }

    /**
     * 验证数据库和表名
     * @param databaseName 数据库名称
     * @param tableName 表名称
     */
    protected void validateDatabaseAndTable(String databaseName, String tableName) {
        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
    }

    /**
     * 验证分页参数
     * @param offset 偏移量
     * @param limit 限制数量
     */
    protected void validatePaginationParams(long offset, long limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must be non-negative");
        }
        if (limit <= 0 || limit > 1000) {
            throw new IllegalArgumentException("Limit must be between 1 and 1000");
        }
    }

    /**
     * 引用标识符，防止SQL注入
     * @param identifier 标识符
     * @return 引用后的标识符
     */
    protected String quoteIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        // 对于MySQL，使用反引号引用标识符
        return "`" + identifier.replace("`", "``") + "`";
    }

    @Override
    public void closeConnection(Connection connection) {
        // 注意：使用try-with-resources时，连接会自动关闭
        // 此方法保留用于向后兼容
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Failed to close connection: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getTableColumns(DataSource dataSource, String database, String table) {
        throw new UnsupportedOperationException("Get table columns is not supported for this data source type");
    }

    @Override
    public String getSyncQuery(DataSource dataSource, String database, String table, String syncCondition) {
        throw new UnsupportedOperationException("Get sync query is not supported for this data source type");
    }

    @Override
    public String getIncrementalSyncQuery(DataSource dataSource, String database, String table, String incrementColumn, Object lastValue) {
        throw new UnsupportedOperationException("Get incremental sync query is not supported for this data source type");
    }

    /**
     * 处理结果集
     * @param resultSet 结果集
     * @return 处理后的结果
     * @throws SQLException SQL异常
     */
    protected List<Map<String, Object>> processResultSet(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = resultSet.getObject(i);
                row.put(columnName, value);
            }
            result.add(row);
        }

        return result;
    }

    /**
     * 清理指定数据源
     * @param dataSource 数据源配置
     */
    protected void removeDataSource(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        
        String cacheKey = getDataSourceCacheKey(dataSource);
        HikariDataSource ds = dataSourceCache.remove(cacheKey);
        if (ds != null) {
            try {
                ds.close();
                log.info("Removed and closed data source: {}", cacheKey);
            } catch (Exception e) {
                log.error("Failed to close data source: {}", cacheKey, e);
            }
        }
    }

    /**
     * 获取数据源缓存大小
     * @return 缓存大小
     */
    public static int getDataSourceCacheSize() {
        return dataSourceCache.size();
    }

    /**
     * 检查数据源是否存在于缓存中
     * @param dataSource 数据源配置
     * @return 是否存在
     */
    protected boolean isDataSourceInCache(DataSource dataSource) {
        validateDataSource(dataSource);
        String cacheKey = getDataSourceCacheKey(dataSource);
        return dataSourceCache.containsKey(cacheKey);
    }

}
