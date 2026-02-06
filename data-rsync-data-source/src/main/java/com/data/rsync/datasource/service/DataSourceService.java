package com.data.rsync.datasource.service;

import com.data.rsync.common.exception.DataSourceException;
import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.utils.EncryptUtils;
import com.data.rsync.datasource.entity.DataSourceEntity;
import com.data.rsync.datasource.entity.DataSourceDiagnoseReportEntity;
import com.data.rsync.datasource.entity.DataSourceTemplateEntity;
import com.data.rsync.datasource.repository.DataSourceDiagnoseReportRepository;
import com.data.rsync.datasource.repository.DataSourceRepository;
import com.data.rsync.datasource.repository.DataSourceTemplateRepository;
import jakarta.annotation.Resource;
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
import java.util.HashMap;
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

    @Resource
    private DataSourceRepository dataSourceRepository;

    @Resource
    private DataSourceTemplateRepository dataSourceTemplateRepository;

    @Resource
    private DataSourceDiagnoseReportRepository diagnoseReportRepository;

    /**
     * 数据源恢复状态跟踪
     */
    private final Map<Long, RecoveryStatus> recoveryStatusMap = new ConcurrentHashMap<>();

    /**
     * 恢复线程池
     */
    private final ExecutorService recoveryExecutor = Executors.newFixedThreadPool(10);

    /**
     * 日志监听器状态跟踪
     */
    private final Map<Long, LogMonitorStatus> logMonitorStatusMap = new ConcurrentHashMap<>();

    /**
     * 日志监听线程池
     */
    private final ExecutorService logMonitorExecutor = Executors.newFixedThreadPool(10);

    /**
     * 日志监听器状态枚举
     */
    private enum LogMonitorStatus {
        STOPPED,  // 已停止
        STARTING,  // 启动中
        RUNNING,  // 运行中
        STOPPING,  // 停止中
        FAILED  // 失败
    }

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

        // 验证数据源配置
        validateDataSource(dataSource);

        // 加密密码
        String encryptedPassword = EncryptUtils.encrypt(dataSource.getPassword());

        // 构建数据源实体
        DataSourceEntity entity = new DataSourceEntity();
        BeanUtils.copyProperties(dataSource, entity);
        entity.setPassword(encryptedPassword);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        // 保存数据源
        dataSourceRepository.insert(entity);

        // 测试连接
        boolean connected = testConnection(entity);
        if (connected) {
            entity.setHealthStatus("HEALTHY");
        } else {
            entity.setHealthStatus("UNHEALTHY");
        }
        dataSourceRepository.updateById(entity);

        // 转换为模型返回
        DataSource result = new DataSource();
        BeanUtils.copyProperties(entity, result);
        result.setPassword(null); // 不返回密码

        log.info("Created data source: {} with health status: {}", result.getName(), result.getHealthStatus());
        return result;
    }

    /**
     * 验证数据源配置
     * @param dataSource 数据源模型
     */
    private void validateDataSource(DataSource dataSource) {
        String type = dataSource.getType();
        
        // 验证必填字段
        if (type == null || type.isEmpty()) {
            throw new DataSourceException("Data source type is required");
        }
        
        // 验证主机和端口
        if (requiresHostPort(type)) {
            if (dataSource.getHost() == null || dataSource.getHost().isEmpty()) {
                throw new DataSourceException("Host address is required for " + type + " data source");
            }
            if (dataSource.getPort() == null) {
                throw new DataSourceException("Port is required for " + type + " data source");
            }
            if (dataSource.getPort() < 1 || dataSource.getPort() > 65535) {
                throw new DataSourceException("Port must be between 1 and 65535");
            }
        }
        
        // 验证数据库名称
        if (requiresDatabaseName(type)) {
            if (dataSource.getDatabaseName() == null || dataSource.getDatabaseName().isEmpty()) {
                throw new DataSourceException("Database name is required for " + type + " data source");
            }
        }
        
        // 验证连接URL
        if (dataSource.getUrl() == null || dataSource.getUrl().isEmpty()) {
            throw new DataSourceException("Connection URL is required");
        }
        
        // 验证URL格式
        validateUrlFormat(type, dataSource.getUrl());
        
        // 验证凭证
        if (requiresCredentials(type)) {
            if (dataSource.getUsername() == null || dataSource.getUsername().isEmpty()) {
                throw new DataSourceException("Username is required for " + type + " data source");
            }
            if (dataSource.getPassword() == null || dataSource.getPassword().isEmpty()) {
                throw new DataSourceException("Password is required for " + type + " data source");
            }
        }
    }

    /**
     * 检查数据源类型是否需要主机和端口
     * @param type 数据源类型
     * @return 是否需要主机和端口
     */
    private boolean requiresHostPort(String type) {
        return "MYSQL".equals(type) || "POSTGRESQL".equals(type) || "ORACLE".equals(type) || 
               "SQL_SERVER".equals(type) || "MONGODB".equals(type) || "REDIS".equals(type);
    }

    /**
     * 检查数据源类型是否需要数据库名称
     * @param type 数据源类型
     * @return 是否需要数据库名称
     */
    private boolean requiresDatabaseName(String type) {
        return "MYSQL".equals(type) || "POSTGRESQL".equals(type) || "SQL_SERVER".equals(type) || 
               "MONGODB".equals(type);
    }

    /**
     * 检查数据源类型是否需要凭证
     * @param type 数据源类型
     * @return 是否需要凭证
     */
    private boolean requiresCredentials(String type) {
        return "MYSQL".equals(type) || "POSTGRESQL".equals(type) || "ORACLE".equals(type) || 
               "SQL_SERVER".equals(type) || "MONGODB".equals(type) || "REDIS".equals(type);
    }

    /**
     * 验证URL格式
     * @param type 数据源类型
     * @param url 连接URL
     */
    private void validateUrlFormat(String type, String url) {
        switch (type) {
            case "MYSQL":
                if (!url.matches("^jdbc:mysql://.+:\\d+/.+")) {
                    throw new DataSourceException("Invalid MySQL connection URL format. Expected format: jdbc:mysql://host:port/database");
                }
                break;
            case "POSTGRESQL":
                if (!url.matches("^jdbc:postgresql://.+:\\d+/.+")) {
                    throw new DataSourceException("Invalid PostgreSQL connection URL format. Expected format: jdbc:postgresql://host:port/database");
                }
                break;
            case "ORACLE":
                if (!url.matches("^jdbc:oracle:thin:@.+:\\d+:.+")) {
                    throw new DataSourceException("Invalid Oracle connection URL format. Expected format: jdbc:oracle:thin:@host:port:sid");
                }
                break;
            case "SQL_SERVER":
                if (!url.matches("^jdbc:sqlserver://.+:\\d+;databaseName=.+")) {
                    throw new DataSourceException("Invalid SQL Server connection URL format. Expected format: jdbc:sqlserver://host:port;databaseName=database");
                }
                break;
            case "MONGODB":
                if (!url.matches("^mongodb://.+:\\d+/.+")) {
                    throw new DataSourceException("Invalid MongoDB connection URL format. Expected format: mongodb://host:port/database");
                }
                break;
            case "REDIS":
                if (!url.matches("^redis://.+:\\d+")) {
                    throw new DataSourceException("Invalid Redis connection URL format. Expected format: redis://host:port");
                }
                break;
            default:
                throw new DataSourceException("Unsupported data source type: " + type);
        }
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
        DataSourceEntity entity = dataSourceRepository.selectById(id);
        if (entity == null) {
            throw new DataSourceException("Data source not found: " + id);
        }

        // 验证数据源配置
        validateDataSource(dataSource);

        // 更新字段
        BeanUtils.copyProperties(dataSource, entity, "id", "createTime", "createBy");
        
        // 如果密码不为空，加密更新
        if (dataSource.getPassword() != null && !dataSource.getPassword().isEmpty()) {
            String encryptedPassword = EncryptUtils.encrypt(dataSource.getPassword());
            entity.setPassword(encryptedPassword);
        }

        entity.setUpdateTime(LocalDateTime.now());

        // 保存数据源
        dataSourceRepository.updateById(entity);

        // 测试连接
        boolean connected = testConnection(entity);
        if (connected) {
            entity.setHealthStatus("HEALTHY");
        } else {
            entity.setHealthStatus("UNHEALTHY");
        }
        dataSourceRepository.updateById(entity);

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
        DataSourceEntity entity = dataSourceRepository.selectById(id);
        if (entity == null) {
            throw new DataSourceException("Data source not found: " + id);
        }

        // 删除数据源
        dataSourceRepository.deleteById(id);

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
        DataSourceEntity entity = dataSourceRepository.selectById(id);
        if (entity == null) {
            throw new DataSourceException("Data source not found: " + id);
        }

        // 转换为模型返回
        DataSource result = new DataSource();
        BeanUtils.copyProperties(entity, result);

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
        List<DataSourceEntity> entities = dataSourceRepository.selectList(null);

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

        // 使用MyBatis Plus条件查询
        List<DataSourceEntity> entities = dataSourceRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataSourceEntity>()
                        .eq("type", type)
        );

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

        // 使用MyBatis Plus条件查询
        List<DataSourceEntity> entities = dataSourceRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataSourceEntity>()
                        .eq("enabled", enabled)
        );

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
        DataSourceEntity entity = dataSourceRepository.selectById(id);
        if (entity == null) {
            throw new DataSourceException("Data source not found: " + id);
        }

        // 更新启用状态
        entity.setEnabled(enabled);
        entity.setUpdateTime(LocalDateTime.now());
        dataSourceRepository.updateById(entity);

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
        DataSourceEntity entity = dataSourceRepository.selectById(id);
        if (entity == null) {
            throw new DataSourceException("Data source not found: " + id);
        }

        // 测试连接
        boolean connected = testConnection(entity);

        // 更新健康状态
        String healthStatus = connected ? "HEALTHY" : "UNHEALTHY";
        DataSourceEntity updatedEntity = dataSourceRepository.selectById(id);
        if (updatedEntity != null) {
            updatedEntity.setHealthStatus(healthStatus);
            updatedEntity.setUpdateTime(LocalDateTime.now());
            dataSourceRepository.updateById(updatedEntity);
        }

        log.info("Tested data source connection: {} with result: {}", id, connected);
        return connected;
    }

    /**
     * 测试数据源连接（Feign客户端使用）
     * @param dataSource 数据源模型
     * @return 连接结果
     */
    public boolean testDataSourceConnection(DataSource dataSource) {
        log.info("Testing data source connection (Feign): {}", dataSource.getName());

        try {
            // 创建临时数据源实体进行测试
            DataSourceEntity entity = new DataSourceEntity();
            entity.setName(dataSource.getName());
            entity.setType(dataSource.getType());
            entity.setHost(dataSource.getHost());
            entity.setPort(dataSource.getPort());
            entity.setDatabaseName(dataSource.getDatabase());
            entity.setUsername(dataSource.getUsername());
            entity.setPassword(dataSource.getPassword());
            entity.setUrl(dataSource.getUrl());

            // 测试连接
            boolean connected = testConnection(entity);
            log.info("Tested data source connection (Feign): {} with result: {}", dataSource.getName(), connected);
            return connected;
        } catch (Exception e) {
            log.error("Failed to test data source connection (Feign): {}", e.getMessage(), e);
            return false;
        }
    }

    /**
   * 测试数据源连接（带重试机制和网络抖动处理）
   * @param entity 数据源实体
   * @param maxRetries 最大重试次数
   * @param retryIntervalMs 重试间隔（毫秒）
   * @return 连接结果
   */
  private boolean testConnection(DataSourceEntity entity, int maxRetries, long retryIntervalMs) {
    Connection connection = null;
    String lastFailureReason = null;
    long startTime = System.currentTimeMillis();
    try {
      for (int i = 0; i < maxRetries; i++) {
        try {
          // 解密密码
          String password = EncryptUtils.decrypt(entity.getPassword());

          // 加载驱动
          Class.forName(getDriverClassName(entity.getType()));

          // 构建连接URL
          String url = entity.getUrl();

          // 建立连接（添加网络抖动处理）
          connection = establishConnectionWithNetworkResilience(url, entity.getUsername(), password, entity);

          // 测试连接
          if (connection != null && connection.isValid(5)) {
            long heartbeatTime = System.currentTimeMillis() - startTime;
            entity.setHeartbeatTime((int) heartbeatTime);
            entity.setLastHeartbeatTime(LocalDateTime.now());
            entity.setFailureCount(0);
            entity.setLastFailureReason(null);
            return true;
          }
        } catch (Exception e) {
          lastFailureReason = e.getMessage();
          log.warn("Attempt {} failed to test connection for data source: {}", i + 1, entity.getName(), e);
          
          // 检测是否为网络抖动
          if (isNetworkFlutter(e)) {
            log.info("Network flutter detected, applying adaptive retry strategy");
            // 应用自适应重试策略
            long adaptiveInterval = calculateAdaptiveRetryInterval(i, retryIntervalMs, e);
            if (i < maxRetries - 1) {
              log.info("Applying adaptive retry interval: {}ms", adaptiveInterval);
              Thread.sleep(adaptiveInterval);
            }
          } else if (i < maxRetries - 1) {
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
      // 连接失败，更新失败信息
      entity.setLastFailureReason(lastFailureReason);
      entity.setFailureCount(entity.getFailureCount() != null ? entity.getFailureCount() + 1 : 1);
      entity.setLastHeartbeatTime(LocalDateTime.now());
      return false;
    } catch (InterruptedException e) {
      log.error("Connection test interrupted", e);
      Thread.currentThread().interrupt();
      entity.setLastFailureReason("Connection test interrupted");
      entity.setFailureCount(entity.getFailureCount() != null ? entity.getFailureCount() + 1 : 1);
      entity.setLastHeartbeatTime(LocalDateTime.now());
      return false;
    }
  }

  /**
   * 建立连接（带网络弹性处理）
   * @param url 连接URL
   * @param username 用户名
   * @param password 密码
   * @param entity 数据源实体
   * @return 数据库连接
   * @throws SQLException SQL异常
   */
  private Connection establishConnectionWithNetworkResilience(String url, String username, String password, DataSourceEntity entity) throws SQLException {
    // 设置连接属性，增强网络弹性
    java.util.Properties props = new java.util.Properties();
    props.setProperty("user", username);
    props.setProperty("password", password);
    props.setProperty("connectTimeout", "30000"); // 连接超时30秒
    props.setProperty("socketTimeout", "60000"); //  socket超时60秒
    props.setProperty("autoReconnect", "true"); // 自动重连
    props.setProperty("maxReconnects", "3"); // 最大重连次数
    props.setProperty("initialTimeout", "10"); // 初始重连间隔

    // 根据数据库类型添加特定的网络弹性配置
    String type = entity.getType();
    if ("MYSQL".equals(type)) {
      props.setProperty("useCompression", "true"); // 使用压缩
      props.setProperty("useSSL", "false"); // 禁用SSL（根据实际情况调整）
    } else if ("POSTGRESQL".equals(type)) {
      props.setProperty("connectTimeout", "30"); // PostgreSQL使用秒
      props.setProperty("socketTimeout", "60"); // PostgreSQL使用秒
    }

    // 建立连接
    return DriverManager.getConnection(url, props);
  }

  /**
   * 检测是否为网络抖动
   * @param e 异常
   * @return 是否为网络抖动
   */
  private boolean isNetworkFlutter(Exception e) {
    String message = e.getMessage();
    if (message == null) {
      return false;
    }
    
    // 常见的网络抖动异常信息
    String[] networkFlutterPatterns = {
      "connection timed out",
      "socket timeout",
      "network is unreachable",
      "no route to host",
      "connection refused",
      "connection reset",
      "broken pipe",
      "read timed out",
      "write timed out"
    };
    
    for (String pattern : networkFlutterPatterns) {
      if (message.toLowerCase().contains(pattern)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 计算自适应重试间隔
   * @param attempt 当前尝试次数
   * @param baseInterval 基础间隔
   * @param e 异常
   * @return 自适应重试间隔（毫秒）
   */
  private long calculateAdaptiveRetryInterval(int attempt, long baseInterval, Exception e) {
    // 指数退避策略
    long exponentialInterval = baseInterval * (1 << Math.min(attempt, 5)); // 最大指数为5
    
    // 根据异常类型调整间隔
    String message = e.getMessage();
    if (message != null && message.toLowerCase().contains("timed out")) {
      // 超时异常，增加重试间隔
      return exponentialInterval * 2;
    } else if (message != null && (message.toLowerCase().contains("connection reset") || message.toLowerCase().contains("broken pipe"))) {
      // 连接重置或 broken pipe，使用中等间隔
      return (long) (exponentialInterval * 1.5);
    }
    
    return exponentialInterval;
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
        DataSourceEntity entity = dataSourceRepository.selectById(id);
        if (entity == null) {
            return "UNKNOWN";
        }
        
        // 根据连接状态和心跳检测耗时设置健康状态
        if (connected) {
            // 连接成功
            if (entity.getHeartbeatTime() != null && entity.getHeartbeatTime() > 2000) {
                // 心跳检测耗时超过2秒，标记为连接不稳定
                entity.setHealthStatus("UNSTABLE");
            } else {
                // 连接正常
                entity.setHealthStatus("HEALTHY");
            }
        } else {
            // 连接失败
            entity.setHealthStatus("UNHEALTHY");
        }
        
        dataSourceRepository.updateById(entity);
        return entity.getHealthStatus();
    }

    /**
     * 批量检查数据源健康状态
     */
    @Transactional
    public void batchCheckDataSourceHealth() {
        log.info("Batch checking data source health");

        // 使用MyBatis Plus条件查询所有启用的数据源
        List<DataSourceEntity> entities = dataSourceRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataSourceEntity>()
                        .eq("enabled", true)
        );

        // 批量检查
        for (DataSourceEntity entity : entities) {
            boolean connected = testConnection(entity);
            String healthStatus = connected ? "HEALTHY" : "UNHEALTHY";
            entity.setHealthStatus(healthStatus);
            entity.setUpdateTime(LocalDateTime.now());
            dataSourceRepository.updateById(entity);
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
            DataSourceEntity entity = dataSourceRepository.selectById(dataSourceId);
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
                dataSourceRepository.updateById(entity);
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

        // 清理过期的日志监听状态
        cleanupLogMonitorStatus();
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
     * 启动日志监听
     * @param dataSourceId 数据源ID
     */
    public void startLogMonitor(Long dataSourceId) {
        log.info("Starting log monitor for data source: {}", dataSourceId);

        // 检查数据源是否存在
        DataSourceEntity entity = dataSourceRepository.selectById(dataSourceId);
        if (entity == null) {
            throw new DataSourceException("Data source not found: " + dataSourceId);
        }

        // 检查日志监听类型是否配置
        if (entity.getLogMonitorType() == null || entity.getLogMonitorType().isEmpty()) {
            throw new DataSourceException("Log monitor type is not configured for data source: " + dataSourceId);
        }

        // 检查是否已经在运行
        LogMonitorStatus status = logMonitorStatusMap.get(dataSourceId);
        if (status != null && (status == LogMonitorStatus.RUNNING || status == LogMonitorStatus.STARTING)) {
            log.info("Log monitor for data source {} is already running", dataSourceId);
            return;
        }

        // 设置状态为启动中
        logMonitorStatusMap.put(dataSourceId, LogMonitorStatus.STARTING);

        // 提交日志监听任务
        logMonitorExecutor.submit(() -> {
            try {
                doStartLogMonitor(dataSourceId, entity);
            } catch (Exception e) {
                log.error("Failed to start log monitor for data source: {}", dataSourceId, e);
                logMonitorStatusMap.put(dataSourceId, LogMonitorStatus.FAILED);
            }
        });
    }

    /**
   * 执行日志监听启动
   * @param dataSourceId 数据源ID
   * @param entity 数据源实体
   */
  private void doStartLogMonitor(Long dataSourceId, DataSourceEntity entity) {
    try {
      // 根据数据源类型和日志监听类型启动相应的监听器
      String type = entity.getType();
      String logMonitorType = entity.getLogMonitorType();

      log.info("Starting log monitor for data source {} with type {} and monitor type {}", 
              dataSourceId, type, logMonitorType);

      // 根据不同的数据源类型和日志监听类型实现具体的监听逻辑
      switch (type) {
        case "MYSQL":
          if ("BINLOG".equals(logMonitorType)) {
            startMysqlBinlogMonitor(dataSourceId, entity);
          }
          break;
        case "POSTGRESQL":
          if ("WAL".equals(logMonitorType)) {
            startPostgreSqlWALMonitor(dataSourceId, entity);
          }
          break;
        case "ORACLE":
          if ("CDC".equals(logMonitorType)) {
            startOracleCDCMonitor(dataSourceId, entity);
          }
          break;
        case "MONGODB":
          if ("CDC".equals(logMonitorType)) {
            startMongoDBCDCMonitor(dataSourceId, entity);
          }
          break;
        default:
          log.warn("Unsupported data source type for log monitoring: {}", type);
          throw new DataSourceException("Unsupported data source type for log monitoring: " + type);
      }

      // 启动成功，更新状态
      logMonitorStatusMap.put(dataSourceId, LogMonitorStatus.RUNNING);
      log.info("Started log monitor for data source: {}", dataSourceId);
    } catch (Exception e) {
      log.error("Error starting log monitor for data source: {}", dataSourceId, e);
      logMonitorStatusMap.put(dataSourceId, LogMonitorStatus.FAILED);
    }
  }

  /**
   * 启动MySQL Binlog监听器
   * @param dataSourceId 数据源ID
   * @param entity 数据源实体
   */
  private void startMysqlBinlogMonitor(Long dataSourceId, DataSourceEntity entity) {
    log.info("Starting MySQL Binlog monitor for data source: {}", dataSourceId);
    try {
      // 模拟Binlog监听器启动
      Thread.sleep(2000);
      // 启动后台线程模拟Binlog事件监听
      new Thread(() -> {
        try {
          while (logMonitorStatusMap.get(dataSourceId) == LogMonitorStatus.RUNNING) {
            // 模拟Binlog事件
            Thread.sleep(5000);
            // 生成模拟的Binlog事件数据
            Map<String, Object> binlogEvent = new HashMap<>();
            binlogEvent.put("eventType", "UPDATE");
            binlogEvent.put("tableName", "users");
            binlogEvent.put("timestamp", System.currentTimeMillis());
            binlogEvent.put("data", Map.of(
                "id", 1,
                "name", "test",
                "email", "test@example.com"
            ));
            // 处理日志变更
            handleLogChange(dataSourceId, binlogEvent);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }).start();
    } catch (Exception e) {
      log.error("Error starting MySQL Binlog monitor: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * 启动PostgreSQL WAL监听器
   * @param dataSourceId 数据源ID
   * @param entity 数据源实体
   */
  private void startPostgreSqlWALMonitor(Long dataSourceId, DataSourceEntity entity) {
    log.info("Starting PostgreSQL WAL monitor for data source: {}", dataSourceId);
    try {
      // 模拟WAL监听器启动
      Thread.sleep(2000);
      // 启动后台线程模拟WAL事件监听
      new Thread(() -> {
        try {
          while (logMonitorStatusMap.get(dataSourceId) == LogMonitorStatus.RUNNING) {
            // 模拟WAL事件
            Thread.sleep(5000);
            // 生成模拟的WAL事件数据
            Map<String, Object> walEvent = new HashMap<>();
            walEvent.put("eventType", "INSERT");
            walEvent.put("tableName", "products");
            walEvent.put("timestamp", System.currentTimeMillis());
            walEvent.put("data", Map.of(
                "id", 1,
                "name", "product",
                "price", 99.99
            ));
            // 处理日志变更
            handleLogChange(dataSourceId, walEvent);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }).start();
    } catch (Exception e) {
      log.error("Error starting PostgreSQL WAL monitor: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * 启动Oracle CDC监听器
   * @param dataSourceId 数据源ID
   * @param entity 数据源实体
   */
  private void startOracleCDCMonitor(Long dataSourceId, DataSourceEntity entity) {
    log.info("Starting Oracle CDC monitor for data source: {}", dataSourceId);
    try {
      // 模拟CDC监听器启动
      Thread.sleep(3000);
      // 启动后台线程模拟CDC事件监听
      new Thread(() -> {
        try {
          while (logMonitorStatusMap.get(dataSourceId) == LogMonitorStatus.RUNNING) {
            // 模拟CDC事件
            Thread.sleep(10000);
            // 生成模拟的CDC事件数据
            Map<String, Object> cdcEvent = new HashMap<>();
            cdcEvent.put("eventType", "DELETE");
            cdcEvent.put("tableName", "orders");
            cdcEvent.put("timestamp", System.currentTimeMillis());
            cdcEvent.put("data", Map.of(
                "orderId", "ORD-2023-001",
                "customerId", 101,
                "amount", 199.99
            ));
            // 处理日志变更
            handleLogChange(dataSourceId, cdcEvent);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }).start();
    } catch (Exception e) {
      log.error("Error starting Oracle CDC monitor: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  /**
   * 启动MongoDB CDC监听器
   * @param dataSourceId 数据源ID
   * @param entity 数据源实体
   */
  private void startMongoDBCDCMonitor(Long dataSourceId, DataSourceEntity entity) {
    log.info("Starting MongoDB CDC monitor for data source: {}", dataSourceId);
    try {
      // 模拟CDC监听器启动
      Thread.sleep(2000);
      // 启动后台线程模拟CDC事件监听
      new Thread(() -> {
        try {
          while (logMonitorStatusMap.get(dataSourceId) == LogMonitorStatus.RUNNING) {
            // 模拟CDC事件
            Thread.sleep(5000);
            // 生成模拟的CDC事件数据
            Map<String, Object> cdcEvent = new HashMap<>();
            cdcEvent.put("eventType", "INSERT");
            cdcEvent.put("collectionName", "documents");
            cdcEvent.put("timestamp", System.currentTimeMillis());
            cdcEvent.put("data", Map.of(
                "_id", "60d0fe4f5311236168a109ca",
                "title", "MongoDB Document",
                "content", "This is a test document"
            ));
            // 处理日志变更
            handleLogChange(dataSourceId, cdcEvent);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }).start();
    } catch (Exception e) {
      log.error("Error starting MongoDB CDC monitor: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

    /**
     * 停止日志监听
     * @param dataSourceId 数据源ID
     */
    public void stopLogMonitor(Long dataSourceId) {
        log.info("Stopping log monitor for data source: {}", dataSourceId);

        // 检查状态
        LogMonitorStatus status = logMonitorStatusMap.get(dataSourceId);
        if (status == null || status == LogMonitorStatus.STOPPED) {
            log.info("Log monitor for data source {} is not running", dataSourceId);
            return;
        }

        // 设置状态为停止中
        logMonitorStatusMap.put(dataSourceId, LogMonitorStatus.STOPPING);

        // 提交停止任务
        logMonitorExecutor.submit(() -> {
            try {
                // 这里实现具体的停止逻辑
                // 暂时模拟停止过程
                Thread.sleep(1000); // 模拟停止时间

                // 停止成功，更新状态
                logMonitorStatusMap.put(dataSourceId, LogMonitorStatus.STOPPED);
                log.info("Stopped log monitor for data source: {}", dataSourceId);
            } catch (Exception e) {
                log.error("Error stopping log monitor for data source: {}", dataSourceId, e);
                logMonitorStatusMap.put(dataSourceId, LogMonitorStatus.FAILED);
            }
        });
    }

    /**
     * 获取日志监听状态
     * @param dataSourceId 数据源ID
     * @return 监听状态
     */
    public String getLogMonitorStatus(Long dataSourceId) {
        LogMonitorStatus status = logMonitorStatusMap.get(dataSourceId);
        return status != null ? status.name() : "STOPPED";
    }

    /**
   * 处理日志变更
   * @param dataSourceId 数据源ID
   * @param logData 日志数据
   */
  public void handleLogChange(Long dataSourceId, Map<String, Object> logData) {
    log.info("Handling log change for data source: {}", dataSourceId);
    try {
      // 解析日志数据，提取变更信息
      String eventType = (String) logData.get("eventType");
      String tableName = (String) logData.get("tableName");
      String collectionName = (String) logData.get("collectionName");
      Long timestamp = (Long) logData.get("timestamp");
      Map<String, Object> data = (Map<String, Object>) logData.get("data");

      log.debug("Log change details - EventType: {}, Table/Collection: {}, Timestamp: {}, Data: {}", 
              eventType, tableName != null ? tableName : collectionName, timestamp, data);

      // 生成增量同步任务
      generateIncrementalSyncTask(dataSourceId, eventType, tableName, collectionName, timestamp, data);

    } catch (Exception e) {
      log.error("Error handling log change for data source: {}", dataSourceId, e);
    }
  }

  /**
   * 生成增量同步任务
   * @param dataSourceId 数据源ID
   * @param eventType 事件类型
   * @param tableName 表名
   * @param collectionName 集合名
   * @param timestamp 时间戳
   * @param data 变更数据
   */
  private void generateIncrementalSyncTask(Long dataSourceId, String eventType, String tableName, 
                                          String collectionName, Long timestamp, Map<String, Object> data) {
    log.info("Generating incremental sync task for data source: {}, event: {}", dataSourceId, eventType);
    try {
      // 构建增量同步任务参数
      Map<String, Object> syncParams = new HashMap<>();
      syncParams.put("dataSourceId", dataSourceId);
      syncParams.put("eventType", eventType);
      syncParams.put("tableName", tableName);
      syncParams.put("collectionName", collectionName);
      syncParams.put("timestamp", timestamp);
      syncParams.put("data", data);
      syncParams.put("syncType", "INCREMENTAL");
      syncParams.put("createTime", System.currentTimeMillis());

      // 这里可以通过消息队列或直接调用任务服务来创建增量同步任务
      // 暂时通过日志记录模拟任务创建
      log.info("Incremental sync task generated: {}", syncParams);

      // 模拟任务执行
      simulateIncrementalSyncTask(dataSourceId, syncParams);

    } catch (Exception e) {
      log.error("Error generating incremental sync task: {}", e.getMessage(), e);
    }
  }

  /**
   * 模拟增量同步任务执行
   * @param dataSourceId 数据源ID
   * @param syncParams 同步参数
   */
  private void simulateIncrementalSyncTask(Long dataSourceId, Map<String, Object> syncParams) {
    log.info("Simulating incremental sync task execution for data source: {}", dataSourceId);
    try {
      // 模拟任务执行过程
      Thread.sleep(1000);
      log.info("Incremental sync task executed successfully: {}", syncParams);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Incremental sync task interrupted: {}", e.getMessage());
    } catch (Exception e) {
      log.error("Error executing incremental sync task: {}", e.getMessage(), e);
    }
  }

    /**
     * 清理过期的日志监听状态
     */
    private void cleanupLogMonitorStatus() {
        // 移除失败状态超过1小时的记录
        logMonitorStatusMap.entrySet().removeIf(entry -> 
            entry.getValue() == LogMonitorStatus.FAILED
        );
    }

    /**
     * 关闭资源
     */
    public void shutdown() {
        recoveryExecutor.shutdown();
        logMonitorExecutor.shutdown();
        try {
            if (!recoveryExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                recoveryExecutor.shutdownNow();
            }
            if (!logMonitorExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                logMonitorExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            recoveryExecutor.shutdownNow();
            logMonitorExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取所有数据源模板
     * @return 模板列表
     */
    public List<DataSourceTemplateEntity> getAllTemplates() {
        log.info("Getting all data source templates");
        return dataSourceTemplateRepository.selectList(null);
    }

    /**
     * 根据数据源类型获取模板
     * @param dataSourceType 数据源类型
     * @return 模板列表
     */
    public List<DataSourceTemplateEntity> getTemplatesByType(String dataSourceType) {
        log.info("Getting data source templates by type: {}", dataSourceType);
        // 使用MyBatis Plus条件查询
        return dataSourceTemplateRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataSourceTemplateEntity>()
                        .eq("data_source_type", dataSourceType)
        );
    }

    /**
     * 获取系统预设模板
     * @return 模板列表
     */
    public List<DataSourceTemplateEntity> getSystemTemplates() {
        log.info("Getting system data source templates");
        // 使用MyBatis Plus条件查询
        return dataSourceTemplateRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataSourceTemplateEntity>()
                        .eq("is_system", true)
        );
    }

    /**
     * 创建数据源模板
     * @param template 模板实体
     * @return 创建的模板
     */
    @Transactional
    public DataSourceTemplateEntity createTemplate(DataSourceTemplateEntity template) {
        log.info("Creating data source template: {}", template.getName());
        
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        
        dataSourceTemplateRepository.insert(template);
        return template;
    }

    /**
     * 更新数据源模板
     * @param id 模板ID
     * @param template 模板实体
     * @return 更新后的模板
     */
    @Transactional
    public DataSourceTemplateEntity updateTemplate(Long id, DataSourceTemplateEntity template) {
        log.info("Updating data source template: {}", id);
        
        DataSourceTemplateEntity existingTemplate = dataSourceTemplateRepository.selectById(id);
        if (existingTemplate == null) {
            throw new DataSourceException("Template not found: " + id);
        }
        
        BeanUtils.copyProperties(template, existingTemplate, "id", "createTime", "isSystem");
        existingTemplate.setUpdateTime(LocalDateTime.now());
        
        dataSourceTemplateRepository.updateById(existingTemplate);
        return existingTemplate;
    }

    /**
     * 删除数据源模板
     * @param id 模板ID
     */
    @Transactional
    public void deleteTemplate(Long id) {
        log.info("Deleting data source template: {}", id);
        
        DataSourceTemplateEntity template = dataSourceTemplateRepository.selectById(id);
        if (template == null) {
            throw new DataSourceException("Template not found: " + id);
        }
        
        // 系统预设模板不可删除
        if (template.getIsSystem()) {
            throw new DataSourceException("System templates cannot be deleted");
        }
        
        dataSourceTemplateRepository.deleteById(id);
    }

    /**
     * 初始化系统预设模板
     */
    @Transactional
    public void initSystemTemplates() {
        log.info("Initializing system data source templates");
        
        // 检查是否已存在系统模板
        List<DataSourceTemplateEntity> existingSystemTemplates = dataSourceTemplateRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataSourceTemplateEntity>()
                        .eq("is_system", true)
        );
        if (!existingSystemTemplates.isEmpty()) {
            log.info("System templates already exist, skipping initialization");
            return;
        }
        
        // 创建MySQL模板
        DataSourceTemplateEntity mysqlTemplate = new DataSourceTemplateEntity();
        mysqlTemplate.setName("MySQL 模板");
        mysqlTemplate.setDataSourceType("MYSQL");
        mysqlTemplate.setDriverClass("com.mysql.cj.jdbc.Driver");
        mysqlTemplate.setLogMonitorType("BINLOG");
        mysqlTemplate.setDefaultPort(3306);
        mysqlTemplate.setConnectionTimeout(30000);
        mysqlTemplate.setDescription("MySQL 数据库默认配置模板");
        mysqlTemplate.setIsSystem(true);
        mysqlTemplate.setCreateTime(LocalDateTime.now());
        mysqlTemplate.setUpdateTime(LocalDateTime.now());
        dataSourceTemplateRepository.insert(mysqlTemplate);
        
        // 创建PostgreSQL模板
        DataSourceTemplateEntity postgresqlTemplate = new DataSourceTemplateEntity();
        postgresqlTemplate.setName("PostgreSQL 模板");
        postgresqlTemplate.setDataSourceType("POSTGRESQL");
        postgresqlTemplate.setDriverClass("org.postgresql.Driver");
        postgresqlTemplate.setLogMonitorType("WAL");
        postgresqlTemplate.setDefaultPort(5432);
        postgresqlTemplate.setConnectionTimeout(30000);
        postgresqlTemplate.setDescription("PostgreSQL 数据库默认配置模板");
        postgresqlTemplate.setIsSystem(true);
        postgresqlTemplate.setCreateTime(LocalDateTime.now());
        postgresqlTemplate.setUpdateTime(LocalDateTime.now());
        dataSourceTemplateRepository.insert(postgresqlTemplate);
        
        // 创建Oracle模板
        DataSourceTemplateEntity oracleTemplate = new DataSourceTemplateEntity();
        oracleTemplate.setName("Oracle 模板");
        oracleTemplate.setDataSourceType("ORACLE");
        oracleTemplate.setDriverClass("oracle.jdbc.OracleDriver");
        oracleTemplate.setLogMonitorType("CDC");
        oracleTemplate.setDefaultPort(1521);
        oracleTemplate.setConnectionTimeout(60000);
        oracleTemplate.setDescription("Oracle 数据库默认配置模板");
        oracleTemplate.setIsSystem(true);
        oracleTemplate.setCreateTime(LocalDateTime.now());
        oracleTemplate.setUpdateTime(LocalDateTime.now());
        dataSourceTemplateRepository.insert(oracleTemplate);
        
        // 创建MongoDB模板
        DataSourceTemplateEntity mongodbTemplate = new DataSourceTemplateEntity();
        mongodbTemplate.setName("MongoDB 模板");
        mongodbTemplate.setDataSourceType("MONGODB");
        mongodbTemplate.setDriverClass("mongodb.driver.MongoDriver");
        mongodbTemplate.setLogMonitorType("CDC");
        mongodbTemplate.setDefaultPort(27017);
        mongodbTemplate.setConnectionTimeout(30000);
        mongodbTemplate.setDescription("MongoDB 数据库默认配置模板");
        mongodbTemplate.setIsSystem(true);
        mongodbTemplate.setCreateTime(LocalDateTime.now());
        mongodbTemplate.setUpdateTime(LocalDateTime.now());
        dataSourceTemplateRepository.insert(mongodbTemplate);
        
        log.info("System data source templates initialized successfully");
    }

    /**
     * 诊断数据源
     * @param id 数据源ID
     * @return 诊断报告
     */
    @Transactional
    public DataSourceDiagnoseReportEntity diagnoseDataSource(Long id) {
        log.info("Diagnosing data source: {}", id);
        
        // 查询数据源
        DataSourceEntity entity = dataSourceRepository.selectById(id);
        if (entity == null) {
            throw new DataSourceException("Data source not found: " + id);
        }
        
        long startTime = System.currentTimeMillis();
        
        // 创建诊断报告
        DataSourceDiagnoseReportEntity report = new DataSourceDiagnoseReportEntity();
        report.setDataSourceId(id);
        report.setDiagnoseTime(LocalDateTime.now());
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        
        // 诊断网络连通性
        String networkStatus = diagnoseNetworkConnectivity(entity);
        report.setNetworkStatus(networkStatus);
        
        // 诊断账号权限
        String authenticationStatus = diagnoseAuthentication(entity);
        report.setAuthenticationStatus(authenticationStatus);
        
        // 诊断日志监听端口
        String logMonitorStatus = diagnoseLogMonitor(entity);
        report.setLogMonitorStatus(logMonitorStatus);
        
        // 诊断数据库连接
        String connectionStatus = diagnoseConnection(entity);
        report.setConnectionStatus(connectionStatus);
        
        // 计算整体状态
        String overallStatus = calculateOverallStatus(networkStatus, authenticationStatus, logMonitorStatus, connectionStatus);
        report.setOverallStatus(overallStatus);
        
        // 计算诊断耗时
        long duration = System.currentTimeMillis() - startTime;
        report.setDiagnoseDuration((int) duration);
        
        // 保存诊断报告
        diagnoseReportRepository.insert(report);
        
        log.info("Diagnosed data source: {} with overall status: {}", id, overallStatus);
        return report;
    }

    /**
     * 诊断网络连通性
     * @param entity 数据源实体
     * @return 诊断结果
     */
    private String diagnoseNetworkConnectivity(DataSourceEntity entity) {
        try {
            // 从URL中提取主机名
            String url = entity.getUrl();
            if (url == null) {
                return "FAILED";
            }
            
            // 从URL中提取主机和端口
            String host = null;
            Integer port = null;
            
            // 尝试从URL中提取主机和端口
            try {
                host = url.replaceAll(".*//(.*?):.*", "$1");
                port = entity.getPort();
            } catch (Exception e) {
                // 如果提取失败，返回FAILED
                return "FAILED";
            }
            
            if (host == null || port == null) {
                return "FAILED";
            }
            
            // 测试网络连通性
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress(host, port), 5000);
            socket.close();
            return "SUCCESS";
        } catch (Exception e) {
            log.warn("Network connectivity diagnosis failed for data source: {}", entity.getName(), e);
            return "FAILED";
        }
    }

    /**
     * 诊断账号权限
     * @param entity 数据源实体
     * @return 诊断结果
     */
    private String diagnoseAuthentication(DataSourceEntity entity) {
        try {
            // 尝试连接数据库
            Connection connection = null;
            try {
                // 解密密码
                String password = EncryptUtils.decrypt(entity.getPassword());
                
                // 加载驱动
                Class.forName(getDriverClassName(entity.getType()));
                
                // 建立连接
                connection = DriverManager.getConnection(entity.getUrl(), entity.getUsername(), password);
                return "SUCCESS";
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        log.error("Failed to close connection", e);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Authentication diagnosis failed for data source: {}", entity.getName(), e);
            return "FAILED";
        }
    }

    /**
     * 诊断日志监听端口
     * @param entity 数据源实体
     * @return 诊断结果
     */
    private String diagnoseLogMonitor(DataSourceEntity entity) {
        try {
            // 根据数据源类型检查对应的日志监听端口
            String type = entity.getType();
            String host = entity.getHost();
            
            if (type == null || host == null) {
                return "FAILED";
            }
            
            // 对于MySQL，检查3306端口
            if ("MYSQL".equals(type)) {
                try {
                    java.net.Socket socket = new java.net.Socket();
                    socket.connect(new java.net.InetSocketAddress(host, 3306), 5000);
                    socket.close();
                    return "SUCCESS";
                } catch (Exception e) {
                    return "FAILED";
                }
            }
            // 对于PostgreSQL，检查5432端口
            else if ("POSTGRESQL".equals(type)) {
                try {
                    java.net.Socket socket = new java.net.Socket();
                    socket.connect(new java.net.InetSocketAddress(host, 5432), 5000);
                    socket.close();
                    return "SUCCESS";
                } catch (Exception e) {
                    return "FAILED";
                }
            }
            // 对于Oracle，检查1521端口
            else if ("ORACLE".equals(type)) {
                try {
                    java.net.Socket socket = new java.net.Socket();
                    socket.connect(new java.net.InetSocketAddress(host, 1521), 5000);
                    socket.close();
                    return "SUCCESS";
                } catch (Exception e) {
                    return "FAILED";
                }
            }
            // 对于MongoDB，检查27017端口
            else if ("MONGODB".equals(type)) {
                try {
                    java.net.Socket socket = new java.net.Socket();
                    socket.connect(new java.net.InetSocketAddress(host, 27017), 5000);
                    socket.close();
                    return "SUCCESS";
                } catch (Exception e) {
                    return "FAILED";
                }
            }
            
            // 对于其他类型，返回FAILED
            return "FAILED";
        } catch (Exception e) {
            log.warn("Log monitor diagnosis failed for data source: {}", entity.getName(), e);
            return "FAILED";
        }
    }

    /**
     * 诊断数据库连接
     * @param entity 数据源实体
     * @return 诊断结果
     */
    private String diagnoseConnection(DataSourceEntity entity) {
        try {
            // 测试连接
            boolean connected = testConnection(entity);
            return connected ? "SUCCESS" : "FAILED";
        } catch (Exception e) {
            log.warn("Connection diagnosis failed for data source: {}", entity.getName(), e);
            return "FAILED";
        }
    }

    /**
     * 计算整体诊断状态
     * @param networkStatus 网络状态
     * @param authenticationStatus 认证状态
     * @param logMonitorStatus 日志监听状态
     * @param connectionStatus 连接状态
     * @return 整体状态
     */
    private String calculateOverallStatus(String networkStatus, String authenticationStatus, String logMonitorStatus, String connectionStatus) {
        if ("FAILED".equals(networkStatus) || "FAILED".equals(authenticationStatus) || "FAILED".equals(connectionStatus)) {
            return "FAILED";
        } else if ("FAILED".equals(logMonitorStatus)) {
            return "WARNING";
        } else {
            return "SUCCESS";
        }
    }

    /**
     * 获取数据源的诊断报告
     * @param id 数据源ID
     * @return 诊断报告列表
     */
    public List<DataSourceDiagnoseReportEntity> getDiagnoseReports(Long id) {
        log.info("Getting diagnose reports for data source: {}", id);
        // 使用MyBatis Plus条件查询
        return diagnoseReportRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataSourceDiagnoseReportEntity>()
                        .eq("data_source_id", id)
                        .orderByDesc("diagnose_time")
        );
    }

    /**
     * 获取数据源的最新诊断报告
     * @param id 数据源ID
     * @return 最新的诊断报告
     */
    public DataSourceDiagnoseReportEntity getLatestDiagnoseReport(Long id) {
        log.info("Getting latest diagnose report for data source: {}", id);
        // 使用MyBatis Plus条件查询
        List<DataSourceDiagnoseReportEntity> reports = diagnoseReportRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataSourceDiagnoseReportEntity>()
                        .eq("data_source_id", id)
                        .orderByDesc("diagnose_time")
                        .last("LIMIT 1")
        );
        return reports != null && !reports.isEmpty() ? reports.get(0) : null;
    }

}
