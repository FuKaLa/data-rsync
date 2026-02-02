package com.data.rsync.datasource.service;

import com.data.rsync.common.exception.DataSourceException;
import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.utils.EncryptUtils;
import com.data.rsync.datasource.entity.DataSourceEntity;
import com.data.rsync.datasource.repository.DataSourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
     * 数据源恢复状态跟踪
     */
    private final Map<Long, RecoveryStatus> recoveryStatusMap = new ConcurrentHashMap<>();

    /**
     * 恢复线程池
     */
    private final ExecutorService recoveryExecutor = Executors.newFixedThreadPool(10);

    /**
     * 恢复状态枚举
     */
    private enum RecoveryStatus {
        PENDING,  // 等待恢复
        RECOVERING,  // 正在恢复
        RECOVERED,  // 恢复成功
        FAILED  // 恢复失败
    }

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
                .orElseThrow(() -> new DataSourceException("Data source not found: " + id));

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
                .orElseThrow(() -> new DataSourceException("Data source not found: " + id));

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
                .orElseThrow(() -> new DataSourceException("Data source not found: " + id));

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
                .orElseThrow(() -> new DataSourceException("Data source not found: " + id));

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
                .orElseThrow(() -> new DataSourceException("Data source not found: " + id));

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
     * 测试数据源连接（带重试机制）
     * @param entity 数据源实体
     * @param maxRetries 最大重试次数
     * @param retryIntervalMs 重试间隔（毫秒）
     * @return 连接结果
     */
    private boolean testConnection(DataSourceEntity entity, int maxRetries, long retryIntervalMs) {
        Connection connection = null;
        try {
            for (int i = 0; i < maxRetries; i++) {
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
                    if (connection.isValid(5)) {
                        return true;
                    }
                } catch (Exception e) {
                    log.warn("Attempt {} failed to test connection for data source: {}", i + 1, entity.getName(), e);
                    if (i < maxRetries - 1) {
                        Thread.sleep(retryIntervalMs);
                    }
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
            return false;
        } catch (InterruptedException e) {
            log.error("Connection test interrupted", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 测试数据源连接（默认参数）
     * @param entity 数据源实体
     * @return 连接结果
     */
    private boolean testConnection(DataSourceEntity entity) {
        return testConnection(entity, 3, 1000);
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
                throw new DataSourceException("Unsupported data source type: " + type);
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

            // 如果数据源不健康，触发自动恢复
            if (!connected) {
                triggerAutoRecovery(entity.getId());
            }
        }

        log.info("Batch checked {} data sources health", entities.size());
    }

    /**
     * 触发自动恢复
     * @param dataSourceId 数据源ID
     */
    public void triggerAutoRecovery(Long dataSourceId) {
        log.info("Triggering auto recovery for data source: {}", dataSourceId);

        // 检查是否已经在恢复中
        if (recoveryStatusMap.containsKey(dataSourceId) && 
                (recoveryStatusMap.get(dataSourceId) == RecoveryStatus.RECOVERING || 
                 recoveryStatusMap.get(dataSourceId) == RecoveryStatus.PENDING)) {
            log.info("Data source {} is already in recovery process", dataSourceId);
            return;
        }

        // 设置恢复状态为待处理
        recoveryStatusMap.put(dataSourceId, RecoveryStatus.PENDING);

        // 提交恢复任务
        recoveryExecutor.submit(() -> {
            try {
                recoverDataSource(dataSourceId);
            } catch (Exception e) {
                log.error("Failed to execute recovery task for data source: {}", dataSourceId, e);
                recoveryStatusMap.put(dataSourceId, RecoveryStatus.FAILED);
            }
        });
    }

    /**
     * 恢复数据源
     * @param dataSourceId 数据源ID
     */
    private void recoverDataSource(Long dataSourceId) {
        log.info("Recovering data source: {}", dataSourceId);

        // 设置恢复状态为正在恢复
        recoveryStatusMap.put(dataSourceId, RecoveryStatus.RECOVERING);

        try {
            // 查询数据源
            DataSourceEntity entity = dataSourceRepository.findById(dataSourceId).orElse(null);
            if (entity == null) {
                log.error("Data source not found: {}", dataSourceId);
                recoveryStatusMap.put(dataSourceId, RecoveryStatus.FAILED);
                return;
            }

            // 尝试恢复连接（增加重试次数和间隔）
            boolean recovered = testConnection(entity, 5, 3000);

            if (recovered) {
                // 更新数据源状态为健康
                entity.setHealthStatus("HEALTHY");
                entity.setUpdateTime(LocalDateTime.now());
                dataSourceRepository.save(entity);
                recoveryStatusMap.put(dataSourceId, RecoveryStatus.RECOVERED);
                log.info("Successfully recovered data source: {}", entity.getName());
            } else {
                // 恢复失败
                recoveryStatusMap.put(dataSourceId, RecoveryStatus.FAILED);
                log.error("Failed to recover data source: {}", entity.getName());
            }
        } catch (Exception e) {
            log.error("Error during data source recovery: {}", dataSourceId, e);
            recoveryStatusMap.put(dataSourceId, RecoveryStatus.FAILED);
        }
    }

    /**
     * 获取数据源恢复状态
     * @param dataSourceId 数据源ID
     * @return 恢复状态
     */
    public String getRecoveryStatus(Long dataSourceId) {
        RecoveryStatus status = recoveryStatusMap.get(dataSourceId);
        return status != null ? status.name() : "NOT_RECOVERING";
    }

    /**
     * 定时监控和恢复任务
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void scheduledRecoveryMonitor() {
        log.debug("Running scheduled recovery monitor");

        // 执行批量健康检查
        batchCheckDataSourceHealth();

        // 清理过期的恢复状态
        cleanupRecoveryStatus();
    }

    /**
     * 清理过期的恢复状态
     */
    private void cleanupRecoveryStatus() {
        // 移除已完成或失败超过1小时的恢复状态
        long cutoffTime = System.currentTimeMillis() - 3600000;
        recoveryStatusMap.entrySet().removeIf(entry -> {
            RecoveryStatus status = entry.getValue();
            return status == RecoveryStatus.RECOVERED || status == RecoveryStatus.FAILED;
        });
    }

    /**
     * 关闭资源
     */
    public void shutdown() {
        recoveryExecutor.shutdown();
        try {
            if (!recoveryExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                recoveryExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            recoveryExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
