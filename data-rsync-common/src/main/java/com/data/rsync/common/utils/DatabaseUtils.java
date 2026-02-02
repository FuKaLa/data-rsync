package com.data.rsync.common.utils;

import com.data.rsync.common.constants.DataRsyncConstants;
import com.data.rsync.common.model.DataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * 数据库工具类
 */
public class DatabaseUtils {

    /**
     * 构建 JDBC URL
     * @param dataSource 数据源
     * @return JDBC URL
     */
    public static String buildJdbcUrl(DataSource dataSource) {
        String type = dataSource.getType();
        switch (type) {
            case DataRsyncConstants.DataSourceType.MYSQL:
                return "jdbc:mysql://" + dataSource.getHost() + ":" + dataSource.getPort() + "/" + dataSource.getDatabase() + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            case DataRsyncConstants.DataSourceType.POSTGRESQL:
                return "jdbc:postgresql://" + dataSource.getHost() + ":" + dataSource.getPort() + "/" + dataSource.getDatabase();
            case DataRsyncConstants.DataSourceType.ORACLE:
                return "jdbc:oracle:thin:@" + dataSource.getHost() + ":" + dataSource.getPort() + ":" + dataSource.getDatabase();
            case DataRsyncConstants.DataSourceType.SQL_SERVER:
                return "jdbc:sqlserver://" + dataSource.getHost() + ":" + dataSource.getPort() + ";databaseName=" + dataSource.getDatabase();
            default:
                throw new IllegalArgumentException("Unsupported data source type: " + type);
        }
    }

    /**
     * 获取驱动类名
     * @param dataSourceType 数据源类型
     * @return 驱动类名
     */
    public static String getDriverClassName(String dataSourceType) {
        switch (dataSourceType) {
            case DataRsyncConstants.DataSourceType.MYSQL:
                return "com.mysql.cj.jdbc.Driver";
            case DataRsyncConstants.DataSourceType.POSTGRESQL:
                return "org.postgresql.Driver";
            case DataRsyncConstants.DataSourceType.ORACLE:
                return "oracle.jdbc.OracleDriver";
            case DataRsyncConstants.DataSourceType.SQL_SERVER:
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            default:
                throw new IllegalArgumentException("Unsupported data source type: " + dataSourceType);
        }
    }

    /**
     * 构建分片查询语句
     * @param tableName 表名
     * @param primaryKey 主键
     * @param startId 开始ID
     * @param endId 结束ID
     * @return 查询语句
     */
    public static String buildShardQuery(String tableName, String primaryKey, long startId, long endId) {
        return "SELECT * FROM " + tableName + " WHERE " + primaryKey + " >= " + startId + " AND " + primaryKey + " <= " + endId;
    }

    /**
     * 创建数据库连接
     * @param dataSource 数据源
     * @return 数据库连接
     * @throws Exception 异常
     */
    public static Connection createConnection(DataSource dataSource) throws Exception {
        String driverClassName = getDriverClassName(dataSource.getType());
        Class.forName(driverClassName);
        
        String url = buildJdbcUrl(dataSource);
        Properties props = new Properties();
        props.setProperty("user", dataSource.getUsername());
        props.setProperty("password", dataSource.getPassword());
        
        // 从Nacos配置获取连接超时设置
        NacosConfig.DatabaseConfig dbConfig = ConfigUtils.getDatabaseConfig();
        if (dbConfig != null) {
            props.setProperty("connectTimeout", String.valueOf(dbConfig.getConnectionTimeout()));
            props.setProperty("socketTimeout", String.valueOf(dbConfig.getSocketTimeout()));
        }
        
        return DriverManager.getConnection(url, props);
    }

    /**
     * 安全关闭数据库资源
     * @param connection 数据库连接
     * @param statement 语句
     * @param resultSet 结果集
     */
    public static void closeResources(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (Exception e) {
            // 忽略关闭异常
        }
    }

} 
