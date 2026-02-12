package com.data.rsync.common.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分页查询工具类
 */
public class PaginationUtils {

    /**
     * 执行分页查询
     * @param connection 数据库连接
     * @param sql SQL语句
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param params 参数
     * @return 查询结果
     * @throws Exception 异常
     */
    public static PaginationResult<Map<String, Object>> executePaginationQuery(Connection connection, String sql, int pageNum, int pageSize, Object... params) throws Exception {
        // 计算起始位置
        int offset = (pageNum - 1) * pageSize;
        
        // 构建分页SQL语句
        String paginationSql = buildPaginationSql(sql, offset, pageSize);
        
        // 执行查询
        List<Map<String, Object>> data;
        try (PreparedStatement preparedStatement = connection.prepareStatement(paginationSql)) {
            // 设置参数
            setParameters(preparedStatement, params);
            
            // 执行查询
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                data = convertResultSetToList(resultSet);
            }
        }
        
        // 计算总记录数
        long total = countTotal(connection, sql, params);
        
        // 计算总页数
        long totalPages = (total + pageSize - 1) / pageSize;
        
        // 构建分页结果
        PaginationResult<Map<String, Object>> result = new PaginationResult<>();
        result.setData(data);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotalPages(totalPages);
        
        return result;
    }

    /**
     * 构建分页SQL语句
     * @param sql 原始SQL语句
     * @param offset 起始位置
     * @param limit 每页大小
     * @return 分页SQL语句
     */
    private static String buildPaginationSql(String sql, int offset, int limit) {
        // 对于不同的数据库，可以实现不同的分页SQL构建逻辑
        // 这里使用MySQL的LIMIT子句
        return sql + " LIMIT ? OFFSET ?";
    }

    /**
     * 计算总记录数
     * @param connection 数据库连接
     * @param sql 原始SQL语句
     * @param params 参数
     * @return 总记录数
     * @throws Exception 异常
     */
    private static long countTotal(Connection connection, String sql, Object... params) throws Exception {
        // 构建计数SQL语句
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS t";
        
        // 执行计数查询
        try (PreparedStatement preparedStatement = connection.prepareStatement(countSql)) {
            // 设置参数
            setParameters(preparedStatement, params);
            
            // 执行查询
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        }
        
        return 0;
    }

    /**
     * 设置参数
     * @param preparedStatement 预编译语句
     * @param params 参数
     * @throws SQLException 异常
     */
    private static void setParameters(PreparedStatement preparedStatement, Object... params) throws SQLException {
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
    private static List<Map<String, Object>> convertResultSetToList(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        
        while (resultSet.next()) {
            Map<String, Object> row = new java.util.HashMap<>();
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                String columnName = resultSet.getMetaData().getColumnName(i);
                Object value = resultSet.getObject(i);
                row.put(columnName, value);
            }
            result.add(row);
        }
        
        return result;
    }

    /**
     * 分页结果类
     * @param <T> 数据类型
     */
    public static class PaginationResult<T> {
        private List<T> data;
        private long total;
        private int pageNum;
        private int pageSize;
        private long totalPages;

        public List<T> getData() {
            return data;
        }

        public void setData(List<T> data) {
            this.data = data;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }

        public int getPageNum() {
            return pageNum;
        }

        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public long getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(long totalPages) {
            this.totalPages = totalPages;
        }
    }
}
