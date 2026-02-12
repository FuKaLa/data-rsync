package com.data.rsync.common.strategy;

import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.model.Task;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * 数据源策略接口
 */
public interface DataSourceStrategy {

    /**
     * 连接数据源
     * @param dataSource 数据源配置
     * @return 数据库连接
     * @throws Exception 异常
     */
    Connection connect(DataSource dataSource) throws Exception;

    /**
     * 执行查询
     * @param connection 数据库连接
     * @param sql SQL语句
     * @param params 参数
     * @return 查询结果
     * @throws Exception 异常
     */
    List<Map<String, Object>> executeQuery(Connection connection, String sql, Object... params) throws Exception;

    /**
     * 执行更新
     * @param connection 数据库连接
     * @param sql SQL语句
     * @param params 参数
     * @return 影响行数
     * @throws Exception 异常
     */
    int executeUpdate(Connection connection, String sql, Object... params) throws Exception;

    /**
     * 构建分片查询语句
     * @param task 任务
     * @param startId 开始ID
     * @param endId 结束ID
     * @return 查询语句
     */
    String buildShardQuery(Task task, long startId, long endId);

    /**
     * 获取数据源类型
     * @return 数据源类型
     */
    String getDataSourceType();

    /**
     * 关闭连接
     * @param connection 数据库连接
     */
    void closeConnection(Connection connection);
}
