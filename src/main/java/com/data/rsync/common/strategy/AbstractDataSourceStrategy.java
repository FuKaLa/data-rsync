package com.data.rsync.common.strategy;

import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.model.Task;
import com.data.rsync.common.utils.DatabaseUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象数据源策略实现类
 */
public abstract class AbstractDataSourceStrategy implements DataSourceStrategy {

    @Override
    public Connection connect(DataSource dataSource) throws Exception {
        return DatabaseUtils.createConnection(dataSource);
    }

    @Override
    public List<Map<String, Object>> executeQuery(Connection connection, String sql, Object... params) throws Exception {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = executeQueryWithParams(preparedStatement, params)) {
            return convertResultSetToList(resultSet);
        }
    }

    @Override
    public int executeUpdate(Connection connection, String sql, Object... params) throws Exception {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameters(preparedStatement, params);
            return preparedStatement.executeUpdate();
        }
    }

    @Override
    public String buildShardQuery(Task task, long startId, long endId) {
        return DatabaseUtils.buildShardQuery(task.getTableName(), task.getPrimaryKey(), startId, endId);
    }

    @Override
    public void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // 忽略关闭异常
        }
    }

    /**
     * 执行带参数的查询
     * @param preparedStatement 预编译语句
     * @param params 参数
     * @return 结果集
     * @throws Exception 异常
     */
    protected ResultSet executeQueryWithParams(PreparedStatement preparedStatement, Object... params) throws Exception {
        setParameters(preparedStatement, params);
        return preparedStatement.executeQuery();
    }

    /**
     * 设置参数
     * @param preparedStatement 预编译语句
     * @param params 参数
     * @throws SQLException 异常
     */
    protected void setParameters(PreparedStatement preparedStatement, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
        }
    }

    /**
     * 将结果集转换为列表
     * @param resultSet 结果集
     * @return 结果列表
     * @throws SQLException 异常
     */
    protected List<Map<String, Object>> convertResultSetToList(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object value = resultSet.getObject(i);
                row.put(columnName, value);
            }
            resultList.add(row);
        }

        return resultList;
    }
}
