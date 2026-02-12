package com.data.rsync.data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.data.rsync.data.entity.DataSourceEntity;
import com.data.rsync.data.entity.DataSourceTemplateEntity;
import com.data.rsync.data.entity.DataSourceDiagnoseReportEntity;
import com.data.rsync.data.mapper.DataSourceMapper;
import com.data.rsync.data.service.DataSourceService;
import com.data.rsync.data.vo.*;
import com.data.rsync.common.exception.DataSourceException;
import com.data.rsync.common.utils.ConnectionPoolManager;
import com.data.rsync.common.utils.DatabaseUtils;
import com.data.rsync.common.utils.LogUtils;
import com.data.rsync.common.utils.SensitiveInfoEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源服务实现类
 */
@Service
public class DataSourceServiceImpl implements DataSourceService {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private SensitiveInfoEncryptor sensitiveInfoEncryptor;

    @Override
    @Transactional
    public DataSourceEntity createDataSource(DataSourceEntity dataSourceEntity) {
        // 保存 plain text password for testing
        String plainTextPassword = dataSourceEntity.getPassword();
        
        // 加密敏感信息
        dataSourceEntity.setPassword(sensitiveInfoEncryptor.encrypt(dataSourceEntity.getPassword()));
        
        // 设置默认值
        dataSourceEntity.setHealthStatus("UNKNOWN");
        dataSourceEntity.setFailureCount(0);
        dataSourceEntity.setEnabled(true);
        dataSourceEntity.setCreateTime(LocalDateTime.now());
        dataSourceEntity.setUpdateTime(LocalDateTime.now());
        
        // 生成连接URL
        if (dataSourceEntity.getUrl() == null || dataSourceEntity.getUrl().isEmpty()) {
            dataSourceEntity.setUrl(generateDataSourceUrl(dataSourceEntity));
        }
        
        // 保存数据源
        dataSourceMapper.insert(dataSourceEntity);
        
        // 测试连接 with plain text password
        DataSourceEntity testEntity = new DataSourceEntity();
        testEntity.setId(dataSourceEntity.getId());
        testEntity.setName(dataSourceEntity.getName());
        testEntity.setType(dataSourceEntity.getType());
        testEntity.setHost(dataSourceEntity.getHost());
        testEntity.setPort(dataSourceEntity.getPort());
        testEntity.setDatabaseName(dataSourceEntity.getDatabaseName());
        testEntity.setUsername(dataSourceEntity.getUsername());
        testEntity.setPassword(plainTextPassword); // Use plain text password for testing
        testEntity.setUrl(dataSourceEntity.getUrl());
        
        testDataSourceConnection(testEntity);
        
        return dataSourceEntity;
    }

    @Override
    public DataSourceEntity updateDataSource(DataSourceEntity dataSourceEntity) {
        // 保存 plain text password if being updated
        String plainTextPassword = dataSourceEntity.getPassword();
        
        // 加密敏感信息
        if (dataSourceEntity.getPassword() != null && !dataSourceEntity.getPassword().isEmpty()) {
            dataSourceEntity.setPassword(sensitiveInfoEncryptor.encrypt(dataSourceEntity.getPassword()));
        }
        
        // 生成连接URL
        if (dataSourceEntity.getUrl() == null || dataSourceEntity.getUrl().isEmpty()) {
            dataSourceEntity.setUrl(generateDataSourceUrl(dataSourceEntity));
        }
        
        // 更新时间
        dataSourceEntity.setUpdateTime(LocalDateTime.now());
        
        // 更新数据源
        dataSourceMapper.updateById(dataSourceEntity);
        
        // 测试连接 with plain text password if updated
        DataSourceEntity testEntity = new DataSourceEntity();
        testEntity.setId(dataSourceEntity.getId());
        testEntity.setName(dataSourceEntity.getName());
        testEntity.setType(dataSourceEntity.getType());
        testEntity.setHost(dataSourceEntity.getHost());
        testEntity.setPort(dataSourceEntity.getPort());
        testEntity.setDatabaseName(dataSourceEntity.getDatabaseName());
        testEntity.setUsername(dataSourceEntity.getUsername());
        testEntity.setPassword(plainTextPassword != null ? plainTextPassword : getDataSourceById(dataSourceEntity.getId()).getPassword());
        testEntity.setUrl(dataSourceEntity.getUrl());
        
        testDataSourceConnection(testEntity);
        
        return dataSourceEntity;
    }

    @Override
    public void deleteDataSource(Long id) {
        dataSourceMapper.deleteById(id);
    }

    @Override
    public void toggleDataSource(Long id, Boolean enabled) {
        DataSourceEntity dataSourceEntity = getDataSourceById(id);
        if (dataSourceEntity != null) {
            dataSourceEntity.setEnabled(enabled);
            dataSourceEntity.setUpdateTime(LocalDateTime.now());
            dataSourceMapper.updateById(dataSourceEntity);
        }
    }

    @Override
    public DataSourceEntity getDataSourceById(Long id) {
        DataSourceEntity dataSourceEntity = dataSourceMapper.selectById(id);
        if (dataSourceEntity != null) {
            // 解密敏感信息
            dataSourceEntity.setPassword(sensitiveInfoEncryptor.decrypt(dataSourceEntity.getPassword()));
        }
        return dataSourceEntity;
    }

    @Override
    public DataSourceEntity getDataSourceByName(String name) {
        QueryWrapper<DataSourceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        DataSourceEntity dataSourceEntity = dataSourceMapper.selectOne(queryWrapper);
        if (dataSourceEntity != null) {
            // 解密敏感信息
            dataSourceEntity.setPassword(sensitiveInfoEncryptor.decrypt(dataSourceEntity.getPassword()));
        }
        return dataSourceEntity;
    }

    @Override
    public List<DataSourceEntity> getAllDataSources() {
        List<DataSourceEntity> dataSources = dataSourceMapper.selectList(null);
        // 解密敏感信息
        for (DataSourceEntity dataSource : dataSources) {
            dataSource.setPassword(sensitiveInfoEncryptor.decrypt(dataSource.getPassword()));
        }
        return dataSources;
    }

    @Override
    public List<DataSourceEntity> getDataSourcesByType(String type) {
        QueryWrapper<DataSourceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", type);
        List<DataSourceEntity> dataSources = dataSourceMapper.selectList(queryWrapper);
        // 解密敏感信息
        for (DataSourceEntity dataSource : dataSources) {
            dataSource.setPassword(sensitiveInfoEncryptor.decrypt(dataSource.getPassword()));
        }
        return dataSources;
    }

    @Override
    public List<DataSourceEntity> getDataSourcesByHealthStatus(String healthStatus) {
        QueryWrapper<DataSourceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("health_status", healthStatus);
        List<DataSourceEntity> dataSources = dataSourceMapper.selectList(queryWrapper);
        // 解密敏感信息
        for (DataSourceEntity dataSource : dataSources) {
            dataSource.setPassword(sensitiveInfoEncryptor.decrypt(dataSource.getPassword()));
        }
        return dataSources;
    }

    @Override
    public DataSourceConnectionTestResponse testDataSourceConnection(DataSourceEntity dataSourceEntity) {
        DataSourceConnectionTestResponse result = new DataSourceConnectionTestResponse();
        long startTime = System.currentTimeMillis();
        
        try {
            // 转换为DataSource对象
            com.data.rsync.common.model.DataSource dataSource = new com.data.rsync.common.model.DataSource();
            dataSource.setId(dataSourceEntity.getId());
            dataSource.setName(dataSourceEntity.getName());
            dataSource.setType(dataSourceEntity.getType());
            dataSource.setHost(dataSourceEntity.getHost());
            dataSource.setPort(dataSourceEntity.getPort());
            dataSource.setDatabase(dataSourceEntity.getDatabaseName());
            dataSource.setUsername(dataSourceEntity.getUsername());
            // Don't decrypt password for testing - it's already plain text for new connections
            dataSource.setPassword(dataSourceEntity.getPassword());
            dataSource.setUrl(dataSourceEntity.getUrl());
            
            // 获取数据源适配器
            com.data.rsync.common.adapter.DataSourceAdapter adapter = com.data.rsync.common.adapter.DataSourceAdapterFactory.getAdapter(dataSourceEntity.getType());
            
            // 测试连接
            boolean success = adapter.testConnection(dataSource);
            
            if (success) {
                result.setResponseTimeMs(System.currentTimeMillis() - startTime);
                
                // 只有当数据源ID不为null时才更新健康状态
                if (dataSourceEntity.getId() != null) {
                    // 更新数据源健康状态
                    updateDataSourceHealthStatus(dataSourceEntity.getId(), "HEALTHY", null);
                }
            } else {
                // 只有当数据源ID不为null时才更新健康状态
                if (dataSourceEntity.getId() != null) {
                    // 更新数据源健康状态
                    updateDataSourceHealthStatus(dataSourceEntity.getId(), "UNHEALTHY", "测试连接返回失败");
                }
                throw new DataSourceException("测试连接返回失败");
            }
        } catch (DataSourceException e) {
            // 已经是DataSourceException，直接抛出
            throw e;
        } catch (Exception e) {
            // 只有当数据源ID不为null时才更新健康状态
            if (dataSourceEntity.getId() != null) {
                // 更新数据源健康状态
                updateDataSourceHealthStatus(dataSourceEntity.getId(), "UNHEALTHY", e.getMessage());
            }
            throw new DataSourceException("连接失败：" + e.getMessage(), e);
        }
        
        return result;
    }

    @Override
    public DataSourceDiagnoseReportEntity diagnoseDataSource(Long id) {
        DataSourceEntity dataSourceEntity = getDataSourceById(id);
        DataSourceDiagnoseReportEntity report = new DataSourceDiagnoseReportEntity();
        
        if (dataSourceEntity == null) {
            report.setOverallStatus("ERROR");
            report.setConnectionMessage("数据源不存在");
            return report;
        }
        
        // 设置报告基本信息
        report.setDataSourceId(id);
        report.setOverallStatus("HEALTHY");
        report.setNetworkStatus("OK");
        report.setAuthenticationStatus("OK");
        report.setLogMonitorStatus("OK");
        report.setConnectionStatus("OK");
        report.setDiagnoseTime(LocalDateTime.now());
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        
        // 测试网络连接
        long startTime = System.currentTimeMillis();
        try {
            DataSourceConnectionTestResponse connectionResult = testDataSourceConnection(dataSourceEntity);
            long diagnoseDuration = System.currentTimeMillis() - startTime;
            report.setDiagnoseDuration((int) diagnoseDuration);
        } catch (Exception e) {
            long diagnoseDuration = System.currentTimeMillis() - startTime;
            report.setDiagnoseDuration((int) diagnoseDuration);
            report.setOverallStatus("UNHEALTHY");
            report.setConnectionStatus("ERROR");
            report.setConnectionMessage(e.getMessage());
        }
        
        // 测试日志监听
        try {
            testLogMonitor(dataSourceEntity);
        } catch (Exception e) {
            report.setOverallStatus("UNHEALTHY");
            report.setLogMonitorStatus("ERROR");
            report.setLogMonitorMessage("日志监听测试失败：" + e.getMessage());
        }
        
        return report;
    }

    @Override
    public DataSourceHealthCheckResponse executeHeartbeat(Long id) {
        DataSourceHealthCheckResponse result = new DataSourceHealthCheckResponse();
        DataSourceEntity dataSourceEntity = getDataSourceById(id);
        long startTime = System.currentTimeMillis();
        
        if (dataSourceEntity == null) {
            result.setHealthy(false);
            result.setStatus("ERROR");
            result.setResponseTimeMs(System.currentTimeMillis() - startTime);
            return result;
        }
        
        try {
            // 执行心跳检测
            DataSourceConnectionTestResponse connectionResult = testDataSourceConnection(dataSourceEntity);
            
            // 更新心跳时间
            updateDataSourceHealthStatus(id, "HEALTHY", null);
            
            result.setHealthy(true);
            result.setStatus("HEALTHY");
            result.setResponseTimeMs(System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            // 更新心跳时间和失败状态
            updateDataSourceHealthStatus(id, "UNHEALTHY", e.getMessage());
            
            result.setHealthy(false);
            result.setStatus("UNHEALTHY");
            result.setResponseTimeMs(System.currentTimeMillis() - startTime);
        }
        
        return result;
    }

    @Override
    public DataSourceBatchHealthCheckResponse executeBatchHeartbeat() {
        DataSourceBatchHealthCheckResponse result = new DataSourceBatchHealthCheckResponse();
        List<DataSourceBatchHealthCheckResponse.DataSourceHealthCheckItem> healthCheckItems = new ArrayList<>();
        
        // 获取所有启用的数据源
        List<DataSourceEntity> dataSources = getAllDataSources();
        int successCount = 0;
        int totalCount = dataSources.size();
        
        for (DataSourceEntity dataSource : dataSources) {
            if (dataSource.getEnabled() != null && dataSource.getEnabled()) {
                DataSourceHealthCheckResponse heartbeatResult = executeHeartbeat(dataSource.getId());
                DataSourceBatchHealthCheckResponse.DataSourceHealthCheckItem item = new DataSourceBatchHealthCheckResponse.DataSourceHealthCheckItem();
                item.setDataSourceId(dataSource.getId());
                item.setDataSourceName(dataSource.getName());
                item.setDataSourceType(dataSource.getType());
                item.setHealthy(heartbeatResult.isHealthy());
                item.setStatus(heartbeatResult.getStatus());
                healthCheckItems.add(item);
                
                if (heartbeatResult.isHealthy()) {
                    successCount++;
                }
            }
        }
        
        result.setTotalCount(totalCount);
        result.setHealthyCount(successCount);
        result.setUnhealthyCount(totalCount - successCount);
        result.setHealthCheckItems(healthCheckItems);
        return result;
    }

    @Override
    public List<DataSourceTemplateEntity> getDataSourceTemplates(String dataSourceType) {
        // 暂时返回空列表，后续需要从数据库查询
        return new ArrayList<>();
    }

    @Override
    public DataSourceTemplateEntity createDataSourceTemplate(DataSourceTemplateEntity templateEntity) {
        // 暂时返回模板，后续需要保存到数据库
        templateEntity.setCreateTime(LocalDateTime.now());
        templateEntity.setUpdateTime(LocalDateTime.now());
        return templateEntity;
    }

    @Override
    public DataSourceTemplateEntity updateDataSourceTemplate(DataSourceTemplateEntity templateEntity) {
        // 暂时返回模板，后续需要更新到数据库
        templateEntity.setUpdateTime(LocalDateTime.now());
        return templateEntity;
    }

    @Override
    public void deleteDataSourceTemplate(Long id) {
        // 暂时留空，后续需要从数据库删除
    }

    @Override
    public List<DataSourceDiagnoseReportEntity> getDataSourceDiagnoseReports(Long dataSourceId) {
        // 暂时返回空列表，后续需要从数据库查询
        return new ArrayList<>();
    }

    @Override
    public DataSourceConnectionInfoResponse getDataSourceConnectionInfo(Long id) {
        DataSourceConnectionInfoResponse result = new DataSourceConnectionInfoResponse();
        DataSourceEntity dataSourceEntity = getDataSourceById(id);
        
        if (dataSourceEntity == null) {
            result.setDataSourceId(id);
            result.setStatus("ERROR");
            return result;
        }
        
        result.setDataSourceId(dataSourceEntity.getId());
        result.setDataSourceName(dataSourceEntity.getName());
        result.setDataSourceType(dataSourceEntity.getType());
        result.setHost(dataSourceEntity.getHost());
        result.setPort(dataSourceEntity.getPort());
        result.setDatabase(dataSourceEntity.getDatabaseName());
        result.setUsername(dataSourceEntity.getUsername());
        result.setStatus(dataSourceEntity.getHealthStatus());
        Map<String, Object> properties = new HashMap<>();
        properties.put("lastHeartbeatTime", dataSourceEntity.getLastHeartbeatTime());
        properties.put("failureCount", dataSourceEntity.getFailureCount());
        result.setProperties(properties);
        
        return result;
    }

    @Override
    public boolean refreshDataSourceConfig(Long id) {
        DataSourceEntity dataSourceEntity = getDataSourceById(id);
        if (dataSourceEntity == null) {
            return false;
        }
        
        // 刷新配置
        dataSourceEntity.setUpdateTime(LocalDateTime.now());
        dataSourceMapper.updateById(dataSourceEntity);
        
        // 测试连接
        try {
            DataSourceConnectionTestResponse connectionResult = testDataSourceConnection(dataSourceEntity);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public DataSourceConfigExportResponse exportDataSourceConfig(Long id) {
        DataSourceConfigExportResponse result = new DataSourceConfigExportResponse();
        DataSourceEntity dataSourceEntity = getDataSourceById(id);
        
        if (dataSourceEntity == null) {
            result.setDataSourceId(id);
            result.setDataSourceName("未知");
            result.setDataSourceType("未知");
            return result;
        }
        
        result.setDataSourceId(dataSourceEntity.getId());
        result.setDataSourceName(dataSourceEntity.getName());
        result.setDataSourceType(dataSourceEntity.getType());
        Map<String, Object> connectionInfo = new HashMap<>();
        connectionInfo.put("host", dataSourceEntity.getHost());
        connectionInfo.put("port", dataSourceEntity.getPort());
        connectionInfo.put("database", dataSourceEntity.getDatabaseName());
        connectionInfo.put("username", dataSourceEntity.getUsername());
        result.setConnectionInfo(connectionInfo);
        result.setAdvancedSettings(new HashMap<>());
        result.setExportTime(LocalDateTime.now().toString());
        
        return result;
    }

    @Override
    public DataSourceEntity importDataSourceConfig(Map<String, Object> config) {
        // 暂时返回空数据源，后续需要实现
        return new DataSourceEntity();
    }

    @Override
    public DataSourceStatisticsResponse getDataSourceStatistics() {
        DataSourceStatisticsResponse statistics = new DataSourceStatisticsResponse();
        List<DataSourceEntity> allDataSources = getAllDataSources();
        
        int totalCount = allDataSources.size();
        int healthyCount = 0;
        int unhealthyCount = 0;
        int enabledCount = 0;
        int disabledCount = 0;
        
        Map<String, Integer> typeCount = new HashMap<>();
        Map<String, Integer> statusCount = new HashMap<>();
        
        for (DataSourceEntity dataSource : allDataSources) {
            // 统计健康状态
            String healthStatus = dataSource.getHealthStatus();
            if ("HEALTHY".equals(healthStatus)) {
                healthyCount++;
            } else if ("UNHEALTHY".equals(healthStatus)) {
                unhealthyCount++;
            }
            statusCount.put(healthStatus, statusCount.getOrDefault(healthStatus, 0) + 1);
            
            // 统计启用状态
            if (dataSource.getEnabled() != null && dataSource.getEnabled()) {
                enabledCount++;
            } else {
                disabledCount++;
            }
            
            // 统计类型
            String type = dataSource.getType();
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }
        
        statistics.setTotalCount(totalCount);
        statistics.setEnabledCount(enabledCount);
        statistics.setDisabledCount(disabledCount);
        statistics.setHealthyCount(healthyCount);
        statistics.setUnhealthyCount(unhealthyCount);
        statistics.setTypeDistribution(typeCount);
        statistics.setStatusDistribution(statusCount);
        
        return statistics;
    }

    /**
     * 生成数据源连接URL
     * @param dataSourceEntity 数据源实体
     * @return 连接URL
     */
    private String generateDataSourceUrl(DataSourceEntity dataSourceEntity) {
        String type = dataSourceEntity.getType();
        String url = "";
        
        switch (type) {
            case "MYSQL":
                url = String.format(
                        "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai",
                        dataSourceEntity.getHost(),
                        dataSourceEntity.getPort(),
                        dataSourceEntity.getDatabaseName()
                );
                break;
            case "POSTGRESQL":
                url = String.format(
                        "jdbc:postgresql://%s:%d/%s",
                        dataSourceEntity.getHost(),
                        dataSourceEntity.getPort(),
                        dataSourceEntity.getDatabaseName()
                );
                break;
            case "ORACLE":
                url = String.format(
                        "jdbc:oracle:thin:@%s:%d:%s",
                        dataSourceEntity.getHost(),
                        dataSourceEntity.getPort(),
                        dataSourceEntity.getDatabaseName()
                );
                break;
            case "MONGODB":
                url = String.format(
                        "mongodb://%s:%d/%s",
                        dataSourceEntity.getHost(),
                        dataSourceEntity.getPort(),
                        dataSourceEntity.getDatabaseName()
                );
                break;
            default:
                url = dataSourceEntity.getUrl();
        }
        
        return url;
    }

    /**
     * 更新数据源健康状态
     * @param dataSourceId 数据源ID
     * @param healthStatus 健康状态
     * @param failureReason 失败原因
     */
    private void updateDataSourceHealthStatus(Long dataSourceId, String healthStatus, String failureReason) {
        DataSourceEntity existingDataSource = getDataSourceById(dataSourceId);
        if (existingDataSource != null) {
            existingDataSource.setHealthStatus(healthStatus);
            if (failureReason != null) {
                existingDataSource.setLastFailureReason(failureReason);
                existingDataSource.setFailureCount(existingDataSource.getFailureCount() != null ? existingDataSource.getFailureCount() + 1 : 1);
            }
            existingDataSource.setLastHeartbeatTime(LocalDateTime.now());
            existingDataSource.setUpdateTime(LocalDateTime.now());
            dataSourceMapper.updateById(existingDataSource);
        }
    }

    /**
     * 测试日志监听
     * @param dataSourceEntity 数据源实体
     * @throws Exception 测试失败异常
     */
    private void testLogMonitor(DataSourceEntity dataSourceEntity) throws Exception {
        // 根据数据源类型测试不同的日志监听
        String type = dataSourceEntity.getType();
        String logMonitorType = dataSourceEntity.getLogMonitorType();
        
        if ("MYSQL".equals(type) && "BINLOG".equals(logMonitorType)) {
            // 测试MySQL binlog监听
            // 这里需要实现具体的测试逻辑
        } else if ("POSTGRESQL".equals(type) && "WAL".equals(logMonitorType)) {
            // 测试PostgreSQL WAL监听
            // 这里需要实现具体的测试逻辑
        } else if ("ORACLE".equals(type) && "REDO_LOG".equals(logMonitorType)) {
            // 测试Oracle redo log监听
            // 这里需要实现具体的测试逻辑
        } else if ("MONGODB".equals(type) && "OPLOG".equals(logMonitorType)) {
            // 测试MongoDB oplog监听
            // 这里需要实现具体的测试逻辑
        }
    }

    @Override
    public List<DataSourceTemplateEntity> getSystemDataSourceTemplates() {
        // 实现获取系统数据源模板的逻辑
        return new ArrayList<>();
    }

    @Override
    public void initSystemDataSourceTemplates() {
        // 实现初始化系统数据源模板的逻辑
    }

    @Override
    public DataSourceDiagnoseReportEntity getLatestDataSourceDiagnoseReport(Long dataSourceId) {
        // 实现获取最新的数据源诊断报告的逻辑
        return null;
    }

    @Override
    public List<DataSourceEntity> getDataSourcesByEnabled(boolean enabled) {
        // 实现根据启用状态获取数据源的逻辑
        return new ArrayList<>();
    }

    @Override
    public boolean testDataSourceConnection(Long id) {
        // 实现测试数据源连接的逻辑
        return false;
    }

    @Override
    public String checkDataSourceHealth(Long id) {
        // 实现检查数据源健康状态的逻辑
        return "HEALTHY";
    }

    @Override
    public List<DataSourceBatchHealthCheckResponse.DataSourceHealthCheckItem> batchCheckDataSourceHealth() {
        // 实现批量检查数据源健康状态的逻辑
        return new ArrayList<>();
    }

    @Override
    public List<DataSourceTemplateEntity> getAllDataSourceTemplates() {
        // 实现获取所有数据源模板的逻辑
        return new ArrayList<>();
    }

    @Override
    public List<DataSourceTemplateEntity> getDataSourceTemplatesByType(String type) {
        // 实现根据类型获取数据源模板的逻辑
        return new ArrayList<>();
    }

}
