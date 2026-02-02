package com.data.rsync.common.adapter;

import com.data.rsync.common.model.DataSource;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * 数据源适配器接口
 * 定义了对各种数据源的通用操作方法
 */
public interface DataSourceAdapter {

    /**
     * 获取数据源类型
     * @return 数据源类型
     */
    String getDataSourceType();

    /**
     * 测试连接
     * @param dataSource 数据源配置
     * @return 连接是否成功
     */
    boolean testConnection(DataSource dataSource);

    /**
     * 获取数据库连接
     * @param dataSource 数据源配置
     * @return 数据库连接
     */
    Connection getConnection(DataSource dataSource);

    /**
     * 获取数据库列表
     * @param dataSource 数据源配置
     * @return 数据库列表
     */
    List<String> getDatabases(DataSource dataSource);

    /**
     * 获取表列表
     * @param dataSource 数据源配置
     * @param databaseName 数据库名称
     * @return 表列表
     */
    List<String> getTables(DataSource dataSource, String databaseName);

    /**
     * 获取表结构
     * @param dataSource 数据源配置
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @return 表结构
     */
    Map<String, String> getTableSchema(DataSource dataSource, String databaseName, String tableName);

    /**
     * 执行查询
     * @param dataSource 数据源配置
     * @param sql SQL语句
     * @param params 查询参数
     * @return 查询结果
     */
    List<Map<String, Object>> executeQuery(DataSource dataSource, String sql, List<Object> params);

    /**
     * 执行更新
     * @param dataSource 数据源配置
     * @param sql SQL语句
     * @param params 更新参数
     * @return 更新行数
     */
    int executeUpdate(DataSource dataSource, String sql, List<Object> params);

    /**
     * 批量执行更新
     * @param dataSource 数据源配置
     * @param sql SQL语句
     * @param paramsList 参数列表
     * @return 更新行数
     */
    int[] executeBatchUpdate(DataSource dataSource, String sql, List<List<Object>> paramsList);

    /**
     * 获取数据总量
     * @param dataSource 数据源配置
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param whereClause 条件语句
     * @return 数据总量
     */
    long getCount(DataSource dataSource, String databaseName, String tableName, String whereClause);

    /**
     * 分页查询
     * @param dataSource 数据源配置
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param fields 查询字段
     * @param whereClause 条件语句
     * @param orderBy 排序语句
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 查询结果
     */
    List<Map<String, Object>> queryWithPagination(DataSource dataSource, String databaseName, String tableName, 
                                                 String fields, String whereClause, String orderBy, 
                                                 long offset, long limit);

    /**
     * 关闭连接
     * @param connection 数据库连接
     */
    void closeConnection(Connection connection);

}
