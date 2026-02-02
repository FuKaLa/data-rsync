package com.data.rsync.datasource.service;

import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.utils.EncryptUtils;
import com.data.rsync.datasource.entity.DataSourceEntity;
import com.data.rsync.datasource.repository.DataSourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据源服务
 */
@Service
@Slf4j
public class DataSourceService {

    @Autowired
    private DataSourceRepository dataSourceRepository;

    /**
     * 创建数据源
     * @param dataSource 数据源模型
     * @return 创建的数据源
     */
    @Transactional
    public DataSource createDataSource(DataSource dataSource) {
        log.info("Creating data source: {}", dataSource.getName());

        // 加密密码
        String encryptedPassword = EncryptUtils.encrypt(dataSource.getPassword());

        // 构建数据源实体
        DataSourceEntity entity = new DataSourceEntity();
        BeanUtils.copyProperties(dataSource, entity);
        entity.setPassword(encryptedPassword);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        // 保存数据源
        entity = dataSourceRepository.save(entity);

        // 测试连接
        boolean connected = testConnection(entity);
        if (connected) {
            entity.setHealthStatus("HEALTHY");
        } else {
            entity.setHealthStatus("UNHEALTHY");
        }
        dataSourceRepository.save(entity);

        // 转换为模型返回
        DataSource result = new DataSource();
        BeanUtils.copyProperties(entity, result);
        result.setPassword(null); // 不返回密码

        log.info("Created data source: {} with health status: {}", result.getName(), result.getHealthStatus());
        return result;
    }

    /**
     * 更新数据源
     * @param id 数据源ID
     * @param dataSource 数据源模型
     * @return 更新后的数据源
     */
    @Transactional
    public DataSource updateDataSource(Long id, DataSource dataSource) {
        log.info("Updating data source: {}", id);

        // 查询数据源
        DataSourceEntity entity = dataSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data source not found: " + id));

        // 更新字段
        BeanUtils.copyProperties(dataSource, entity, "id", "createTime", "createBy");
        
        // 如果密码不为空，加密更新
        if (dataSource.getPassword() != null && !dataSource.getPassword().isEmpty()) {
            String encryptedPassword = EncryptUtils.encrypt(dataSource.getPassword());
            entity.setPassword(encryptedPassword);
        }

        entity.setUpdateTime(LocalDateTime.now());

        // 保存数据源
        entity = dataSourceRepository.save(entity);

        // 测试连接
        boolean connected = testConnection(entity);
        if (connected) {
            entity.setHealthStatus("HEALTHY");
        } else {
            entity.setHealthStatus("UNHEALTHY");
        }
        dataSourceRepository.save(entity);

        // 转换为模型返回
        DataSource result = new DataSource();
        BeanUtils.copyProperties(entity, result);
        result.setPassword(null); // 不返回密码

        log.info("Updated data source: {} with health status: {}", result.getName(), result.getHealthStatus());
        return result;
    }

    /**
     * 删除数据源
     * @param id 数据源ID
     */
    @Transactional
    public void deleteDataSource(Long id) {
        log.info("Deleting data source: {}", id);

        // 查询数据源
        DataSourceEntity entity = dataSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data source not found: " + id));

        // 删除数据源
        dataSourceRepository.delete(entity);

        log.info("Deleted data source: {}", id);
    }

    /**
     * 获取数据源
     * @param id 数据源ID
     * @return 数据源
     */
    public DataSource getDataSource(Long id) {
        log.info("Getting data source: {}", id);

        // 查询数据源
        DataSourceEntity entity = dataSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data source not found: " + id));

        // 转换为模型返回
        DataSource result = new DataSource();
        BeanUtils.copyProperties(entity, result);
        result.setPassword(null); // 不返回密码

        log.info("Got data source: {}", result.getName());
        return result;
    }

    /**
     * 获取所有数据源
     * @return 数据源列表
     */
    public List<DataSource> getAllDataSources() {
        log.info("Getting all data sources");

        // 查询所有数据源
        List<DataSourceEntity> entities = dataSourceRepository.findAll();

        // 转换为模型返回
        List<DataSource> result = entities.stream()
                .map(entity -> {
                    DataSource dataSource = new DataSource();
                    BeanUtils.copyProperties(entity, dataSource);
                    dataSource.setPassword(null); // 不返回密码
                    return dataSource;
                })
                .collect(Collectors.toList());

        log.info("Got {} data sources", result.size());
        return result;
    }

    /**
     * 根据类型获取数据源
     * @param type 数据源类型
     * @return 数据源列表
     */
    public List<DataSource> getDataSourcesByType(String type) {
        log.info("Getting data sources by type: {}", type);

        // 查询数据源
        List<DataSourceEntity> entities = dataSourceRepository.findByType(type);

        // 转换为模型返回
        List<DataSource> result = entities.stream()
                .map(entity -> {
                    DataSource dataSource = new DataSource();
                    BeanUtils.copyProperties(entity, dataSource);
                    dataSource.setPassword(null); // 不返回密码
                    return dataSource;
                })
                .collect(Collectors.toList());

        log.info("Got {} data sources by type: {}", result.size(), type);
        return result;
    }

    /**
     * 根据启用状态获取数据源
     * @param enabled 启用状态
     * @return 数据源列表
     */
    public List<DataSource> getDataSourcesByEnabled(Boolean enabled) {
        log.info("Getting data sources by enabled: {}", enabled);

        // 查询数据源
        List<DataSourceEntity> entities = dataSourceRepository.findByEnabled(enabled);

        // 转换为模型返回
        List<DataSource> result = entities.stream()
                .map(entity -> {
                    DataSource dataSource = new DataSource();
                    BeanUtils.copyProperties(entity, dataSource);
                    dataSource.setPassword(null); // 不返回密码
                    return dataSource;
                })
                .collect(Collectors.toList());

        log.info("Got {} data sources by enabled: {}", result.size(), enabled);
        return result;
    }

    /**
     * 启用/禁用数据源
     * @param id 数据源ID
     * @param enabled 启用状态
     * @return 更新后的数据源
     */
    @Transactional
    public DataSource enableDataSource(Long id, Boolean enabled) {
        log.info("Enabling data source: {} to {}", id, enabled);

        // 查询数据源
        DataSourceEntity entity = dataSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data source not found: " + id));

        // 更新启用状态
        entity.setEnabled(enabled);
        entity.setUpdateTime(LocalDateTime.now());
        dataSourceRepository.save(entity);

        // 转换为模型返回
        DataSource result = new DataSource();
        BeanUtils.copyProperties(entity, result);
        result.setPassword(null); // 不返回密码

        log.info("Enabled data source: {} to {}", result.getName(), result.getEnabled());
        return result;
    }

    /**
     * 测试数据源连接
     * @param id 数据源ID
     * @return 连接结果
     */
    public boolean testDataSourceConnection(Long id) {
        log.info("Testing data source connection: {}", id);

        // 查询数据源
        DataSourceEntity entity = dataSourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data source not found: " + id));

        // 测试连接
        boolean connected = testConnection(entity);

        // 更新健康状态
        String healthStatus = connected ? "HEALTHY" : "UNHEALTHY";
        DataSourceEntity updatedEntity = dataSourceRepository.findById(id).orElse(null);
        if (updatedEntity != null) {
            updatedEntity.setHealthStatus(healthStatus);
            updatedEntity.setUpdateTime(LocalDateTime.now());
            dataSourceRepository.save(updatedEntity);
        }

        log.info("Tested data source connection: {} with result: {}", id, connected);
        return connected;
    }

    /**
     * 测试数据源连接
     * @param entity 数据源实体
     * @return 连接结果
     */
    private boolean testConnection(DataSourceEntity entity) {
        Connection connection = null;
        try {
            // 解密密码
            String password = EncryptUtils.decrypt(entity.getPassword());

            // 加载驱动
            Class.forName(getDriverClassName(entity.getType()));

            // 构建连接URL
            String url = entity.getUrl();

            // 建立连接
            connection = DriverManager.getConnection(url, entity.getUsername(), password);

            // 测试连接
            return connection.isValid(5);
        } catch (Exception e) {
            log.error("Failed to test connection for data source: {}", entity.getName(), e);
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Failed to close connection", e);
                }
            }
        }
    }

    /**
     * 获取驱动类名
     * @param type 数据源类型
     * @return 驱动类名
     */
    private String getDriverClassName(String type) {
        switch (type) {
            case "MYSQL":
                return "com.mysql.cj.jdbc.Driver";
            case "POSTGRESQL":
                return "org.postgresql.Driver";
            case "ORACLE":
                return "oracle.jdbc.OracleDriver";
            case "SQL_SERVER":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "MONGODB":
                return "com.mongodb.MongoClient";
            case "REDIS":
                return "redis.clients.jedis.Jedis";
            default:
                throw new RuntimeException("Unsupported data source type: " + type);
        }
    }

    /**
     * 检查数据源健康状态
     * @param id 数据源ID
     * @return 健康状态
     */
    public String checkDataSourceHealth(Long id) {
        log.info("Checking data source health: {}", id);

        // 测试连接
        boolean connected = testDataSourceConnection(id);
        return connected ? "HEALTHY" : "UNHEALTHY";
    }

    /**
     * 批量检查数据源健康状态
     */
    @Transactional
    public void batchCheckDataSourceHealth() {
        log.info("Batch checking data source health");

        // 查询所有启用的数据源
        List<DataSourceEntity> entities = dataSourceRepository.findByEnabled(true);

        // 批量检查
        for (DataSourceEntity entity : entities) {
            boolean connected = testConnection(entity);
            String healthStatus = connected ? "HEALTHY" : "UNHEALTHY";
            entity.setHealthStatus(healthStatus);
            entity.setUpdateTime(LocalDateTime.now());
            dataSourceRepository.save(entity);
            log.info("Checked data source: {} health status: {}", entity.getName(), healthStatus);
        }

        log.info("Batch checked {} data sources health", entities.size());
    }

}
