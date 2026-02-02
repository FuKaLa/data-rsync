package com.data.rsync.common.adapter;

import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.utils.EncryptUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象数据源适配器
 * 提供通用的实现方法
 */
@Slf4j
public abstract class AbstractDataSourceAdapter implements DataSourceAdapter {

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
            // 加载驱动
            Class.forName(getDriverClassName(dataSource));

            // 构建连接URL
            String url = buildConnectionUrl(dataSource);

            // 解密密码
            String password = EncryptUtils.decrypt(dataSource.getPassword());

            // 建立连接
            return DriverManager.getConnection(url, dataSource.getUsername(), password);
        } catch (Exception e) {
            log.error("Failed to get connection: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get connection", e);
        }
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
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection(dataSource);
            statement = connection.prepareStatement(sql);

            // 设置参数
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    statement.setObject(i + 1, params.get(i));
                }
            }

            // 执行查询
            resultSet = statement.executeQuery();

            // 处理结果
            return processResultSet(resultSet);
        } catch (Exception e) {
            log.error("Failed to execute query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute query", e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(connection);
        }
    }

    @Override
    public int executeUpdate(DataSource dataSource, String sql, List<Object> params) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection(dataSource);
            statement = connection.prepareStatement(sql);

            // 设置参数
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    statement.setObject(i + 1, params.get(i));
                }
            }

            // 执行更新
            return statement.executeUpdate();
        } catch (Exception e) {
            log.error("Failed to execute update: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute update", e);
        } finally {
            closeStatement(statement);
            closeConnection(connection);
        }
    }

    @Override
    public int[] executeBatchUpdate(DataSource dataSource, String sql, List<List<Object>> paramsList) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection(dataSource);
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);

            // 添加批量参数
            if (paramsList != null && !paramsList.isEmpty()) {
                for (List<Object> params : paramsList) {
                    statement.clearParameters();
                    if (params != null && !params.isEmpty()) {
                        for (int i = 0; i < params.size(); i++) {
                            statement.setObject(i + 1, params.get(i));
                        }
                    }
                    statement.addBatch();
                }
            }

            // 执行批量更新
            int[] result = statement.executeBatch();
            connection.commit();
            return result;
        } catch (Exception e) {
            log.error("Failed to execute batch update: {}", e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("Failed to rollback: {}", ex.getMessage(), ex);
                }
            }
            throw new RuntimeException("Failed to execute batch update", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    log.error("Failed to set auto commit: {}", e.getMessage(), e);
                }
            }
            closeStatement(statement);
            closeConnection(connection);
        }
    }

    @Override
    public long getCount(DataSource dataSource, String databaseName, String tableName, String whereClause) {
        String sql = "SELECT COUNT(*) FROM " + databaseName + "." + tableName;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        List<Map<String, Object>> result = executeQuery(dataSource, sql, null);
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
        String sql = "SELECT " + fields + " FROM " + databaseName + "." + tableName;
        if (whereClause != null && !whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        if (orderBy != null && !orderBy.isEmpty()) {
            sql += " ORDER BY " + orderBy;
        }
        sql += " LIMIT ? OFFSET ?";

        List<Object> params = new ArrayList<>();
        params.add(limit);
        params.add(offset);

        return executeQuery(dataSource, sql, params);
    }

    @Override
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Failed to close connection: {}", e.getMessage(), e);
            }
        }
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
     * 关闭结果集
     * @param resultSet 结果集
     */
    protected void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("Failed to close result set: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 关闭语句
     * @param statement 语句
     */
    protected void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                log.error("Failed to close statement: {}", e.getMessage(), e);
            }
        }
    }

}
