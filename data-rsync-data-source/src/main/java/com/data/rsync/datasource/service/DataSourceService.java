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

    @Autowired
    private DataSourceTemplateRepository dataSourceTemplateRepository;

    @Autowired
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
     * 测试数据源连接（带重试机制）
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

                    // 建立连接
                    connection = DriverManager.getConnection(url, entity.getUsername(), password);

                    // 测试连接
                    if (connection.isValid(5)) {
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

        // 查询所有启用的数据源
        List<DataSourceEntity> entities = dataSourceRepository.findByEnabled(true);

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
        return dataSourceTemplateRepository.findByDataSourceType(dataSourceType);
    }

    /**
     * 获取系统预设模板
     * @return 模板列表
     */
    public List<DataSourceTemplateEntity> getSystemTemplates() {
        log.info("Getting system data source templates");
        return dataSourceTemplateRepository.findByIsSystem(true);
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
        List<DataSourceTemplateEntity> existingSystemTemplates = dataSourceTemplateRepository.findByIsSystem(true);
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
            String host = url.replaceAll(".*//(.*?):.*", "$1");
            int port = entity.getPort();
            
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
            int logPort = entity.getPort();
            
            // 对于MySQL，检查3306端口
            if ("MYSQL".equals(type)) {
                logPort = 3306;
            }
            // 对于PostgreSQL，检查5432端口
            else if ("POSTGRESQL".equals(type)) {
                logPort = 5432;
            }
            // 对于Oracle，检查1521端口
            else if ("ORACLE".equals(type)) {
                logPort = 1521;
            }
            // 对于MongoDB，检查27017端口
            else if ("MONGODB".equals(type)) {
                logPort = 27017;
            }
            
            // 测试端口可用性
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress(entity.getHost(), logPort), 5000);
            socket.close();
            return "SUCCESS";
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
        return diagnoseReportRepository.findByDataSourceId(id);
    }

    /**
     * 获取数据源的最新诊断报告
     * @param id 数据源ID
     * @return 最新的诊断报告
     */
    public DataSourceDiagnoseReportEntity getLatestDiagnoseReport(Long id) {
        log.info("Getting latest diagnose report for data source: {}", id);
        return diagnoseReportRepository.findTopByDataSourceIdOrderByDiagnoseTimeDesc(id);
    }

}
