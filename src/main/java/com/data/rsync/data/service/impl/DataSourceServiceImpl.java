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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
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

    private static final Logger log = LoggerFactory.getLogger(DataSourceServiceImpl.class);

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private SensitiveInfoEncryptor sensitiveInfoEncryptor;

    @Override
    @Transactional
    public DataSourceEntity createDataSource(DataSourceEntity dataSourceEntity) {
        // 校验数据源配置
        validateDataSourceConfig(dataSourceEntity);
        
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
        // 校验数据源配置
        validateDataSourceConfig(dataSourceEntity);
        
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
    
    /**
     * 校验数据源配置
     * @param dataSourceEntity 数据源实体
     * @throws DataSourceException 数据源配置异常
     */
    private void validateDataSourceConfig(DataSourceEntity dataSourceEntity) {
        // 校验数据源名称
        if (dataSourceEntity.getName() == null || dataSourceEntity.getName().isEmpty()) {
            throw new DataSourceException("数据源名称不能为空");
        }
        
        // 校验数据源类型
        if (dataSourceEntity.getType() == null || dataSourceEntity.getType().isEmpty()) {
            throw new DataSourceException("数据源类型不能为空");
        }
        
        // 校验主机地址
        if (dataSourceEntity.getHost() == null || dataSourceEntity.getHost().isEmpty()) {
            throw new DataSourceException("主机地址不能为空");
        }
        
        // 校验端口号
        if (dataSourceEntity.getPort() == null || dataSourceEntity.getPort() <= 0) {
            throw new DataSourceException("端口号必须是正整数");
        }
        
        // 校验数据库名称
        if (dataSourceEntity.getDatabaseName() == null || dataSourceEntity.getDatabaseName().isEmpty()) {
            throw new DataSourceException("数据库名称不能为空");
        }
        
        // 校验用户名
        if (dataSourceEntity.getUsername() == null || dataSourceEntity.getUsername().isEmpty()) {
            throw new DataSourceException("用户名不能为空");
        }
        
        // 校验密码
        if (dataSourceEntity.getPassword() == null || dataSourceEntity.getPassword().isEmpty()) {
            throw new DataSourceException("密码不能为空");
        }
        
        // 校验驱动类
        if (dataSourceEntity.getDriverClass() == null || dataSourceEntity.getDriverClass().isEmpty()) {
            throw new DataSourceException("驱动类不能为空");
        }
        
        // 校验连接URL
        if (dataSourceEntity.getUrl() == null || dataSourceEntity.getUrl().isEmpty()) {
            // 如果URL为空，检查是否可以生成
            try {
                String generatedUrl = generateDataSourceUrl(dataSourceEntity);
                if (generatedUrl == null || generatedUrl.isEmpty()) {
                    throw new DataSourceException("无法生成连接URL，请检查配置");
                }
            } catch (Exception e) {
                throw new DataSourceException("无法生成连接URL: " + e.getMessage());
            }
        }
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
        // 从数据库查询数据源模板
        try {
            LogUtils.info("获取数据源模板，类型: {}", dataSourceType);
            
            // 由于DataSourceTemplateMapper不存在，返回默认模板
            List<DataSourceTemplateEntity> templates = new ArrayList<>();
            
            // 根据类型返回对应的默认模板
            if (dataSourceType == null || dataSourceType.isEmpty()) {
                // 返回所有类型的默认模板
                addDefaultTemplates(templates);
            } else {
                // 返回指定类型的默认模板
                addDefaultTemplate(templates, dataSourceType);
            }
            
            return templates;
        } catch (Exception e) {
            LogUtils.error("获取数据源模板失败，类型: {}", dataSourceType, e);
            return new ArrayList<>();
        }
    }

    @Override
    public DataSourceTemplateEntity createDataSourceTemplate(DataSourceTemplateEntity templateEntity) {
        // 保存数据源模板到数据库
        try {
            templateEntity.setCreateTime(LocalDateTime.now());
            templateEntity.setUpdateTime(LocalDateTime.now());
            
            // 由于DataSourceTemplateMapper不存在，记录日志并返回模板
            LogUtils.info("创建数据源模板: {}", templateEntity.getName());
            LogUtils.info("注意: 由于DataSourceTemplateMapper不存在，模板未保存到数据库");
            
            return templateEntity;
        } catch (Exception e) {
            LogUtils.error("创建数据源模板失败: {}", templateEntity.getName(), e);
            throw new DataSourceException("创建数据源模板失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DataSourceTemplateEntity updateDataSourceTemplate(DataSourceTemplateEntity templateEntity) {
        // 更新数据源模板到数据库
        try {
            templateEntity.setUpdateTime(LocalDateTime.now());
            
            // 由于DataSourceTemplateMapper不存在，记录日志并返回模板
            LogUtils.info("更新数据源模板: {}", templateEntity.getName());
            LogUtils.info("注意: 由于DataSourceTemplateMapper不存在，模板未更新到数据库");
            
            return templateEntity;
        } catch (Exception e) {
            LogUtils.error("更新数据源模板失败: {}", templateEntity.getName(), e);
            throw new DataSourceException("更新数据源模板失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDataSourceTemplate(Long id) {
        // 从数据库删除数据源模板
        try {
            // 由于DataSourceTemplateMapper不存在，记录日志
            LogUtils.info("删除数据源模板，ID: {}", id);
            LogUtils.info("注意: 由于DataSourceTemplateMapper不存在，模板未从数据库删除");
        } catch (Exception e) {
            LogUtils.error("删除数据源模板失败，ID: {}", id, e);
            throw new DataSourceException("删除数据源模板失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加所有默认模板
     */
    private void addDefaultTemplates(List<DataSourceTemplateEntity> templates) {
        addDefaultTemplate(templates, "MYSQL");
        addDefaultTemplate(templates, "POSTGRESQL");
        addDefaultTemplate(templates, "ORACLE");
        addDefaultTemplate(templates, "MONGODB");
    }

    /**
     * 添加指定类型的默认模板
     */
    private void addDefaultTemplate(List<DataSourceTemplateEntity> templates, String dataSourceType) {
        DataSourceTemplateEntity template = new DataSourceTemplateEntity();
        template.setId((long) (templates.size() + 1));
        
        switch (dataSourceType) {
            case "MYSQL":
                template.setName("MySQL默认模板");
                template.setDataSourceType("MYSQL");
                template.setDescription("MySQL数据库默认配置模板");
                break;
            case "POSTGRESQL":
                template.setName("PostgreSQL默认模板");
                template.setDataSourceType("POSTGRESQL");
                template.setDescription("PostgreSQL数据库默认配置模板");
                break;
            case "ORACLE":
                template.setName("Oracle默认模板");
                template.setDataSourceType("ORACLE");
                template.setDescription("Oracle数据库默认配置模板");
                break;
            case "MONGODB":
                template.setName("MongoDB默认模板");
                template.setDataSourceType("MONGODB");
                template.setDescription("MongoDB数据库默认配置模板");
                break;
            default:
                template.setName(dataSourceType + "默认模板");
                template.setDataSourceType(dataSourceType);
                template.setDescription(dataSourceType + "数据库默认配置模板");
                break;
        }
        
        template.setIsSystem(true);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        templates.add(template);
    }

    @Override
    public List<DataSourceDiagnoseReportEntity> getDataSourceDiagnoseReports(Long dataSourceId) {
        // 从数据库查询数据源诊断报告
        try {
            LogUtils.info("获取数据源诊断报告，数据源ID: {}", dataSourceId);
            
            // 由于DataSourceDiagnoseReportMapper不存在，返回空列表
            // 后续可以添加实际的数据库查询逻辑
            return new ArrayList<>();
        } catch (Exception e) {
            LogUtils.error("获取数据源诊断报告失败，数据源ID: {}", dataSourceId, e);
            return new ArrayList<>();
        }
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
            LogUtils.warn("刷新数据源配置失败，数据源不存在，ID: {}", id);
            return false;
        }
        
        // 刷新配置
        dataSourceEntity.setUpdateTime(LocalDateTime.now());
        dataSourceMapper.updateById(dataSourceEntity);
        
        // 测试连接
        try {
            DataSourceConnectionTestResponse connectionResult = testDataSourceConnection(dataSourceEntity);
            LogUtils.info("刷新数据源配置成功，ID: {}", id);
            return true;
        } catch (Exception e) {
            LogUtils.error("刷新数据源配置失败，ID: {}", id, e);
            return false;
        }
    }

    @Override
    public DataSourceConfigExportResponse exportDataSourceConfig(Long id) {
        DataSourceConfigExportResponse result = new DataSourceConfigExportResponse();
        DataSourceEntity dataSourceEntity = getDataSourceById(id);
        
        if (dataSourceEntity == null) {
            LogUtils.warn("导出数据源配置失败，数据源不存在，ID: {}", id);
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
        
        LogUtils.info("导出数据源配置成功，ID: {}", id);
        return result;
    }

    @Override
    public DataSourceEntity importDataSourceConfig(Map<String, Object> config) {
        // 实现数据源配置导入
        try {
            DataSourceEntity dataSourceEntity = new DataSourceEntity();
            dataSourceEntity.setName((String) config.get("name"));
            dataSourceEntity.setType((String) config.get("type"));
            dataSourceEntity.setHost((String) config.get("host"));
            dataSourceEntity.setPort((Integer) config.get("port"));
            dataSourceEntity.setDatabaseName((String) config.get("database"));
            dataSourceEntity.setUsername((String) config.get("username"));
            dataSourceEntity.setPassword((String) config.get("password"));
            dataSourceEntity.setUrl((String) config.get("url"));
            dataSourceEntity.setHealthStatus("UNKNOWN");
            dataSourceEntity.setFailureCount(0);
            dataSourceEntity.setEnabled(true);
            dataSourceEntity.setCreateTime(LocalDateTime.now());
            dataSourceEntity.setUpdateTime(LocalDateTime.now());
            
            // 加密敏感信息
            dataSourceEntity.setPassword(sensitiveInfoEncryptor.encrypt(dataSourceEntity.getPassword()));
            
            // 生成连接URL
            if (dataSourceEntity.getUrl() == null || dataSourceEntity.getUrl().isEmpty()) {
                dataSourceEntity.setUrl(generateDataSourceUrl(dataSourceEntity));
            }
            
            // 保存到数据库
            dataSourceMapper.insert(dataSourceEntity);
            
            LogUtils.info("导入数据源配置成功: {}", dataSourceEntity.getName());
            return dataSourceEntity;
        } catch (Exception e) {
            LogUtils.error("导入数据源配置失败", e);
            throw new DataSourceException("导入数据源配置失败: " + e.getMessage(), e);
        }
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
        
        LogUtils.info("获取数据源统计信息，总数量: {}", totalCount);
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
        // 直接查询数据源，不解密密码
        DataSourceEntity existingDataSource = dataSourceMapper.selectById(dataSourceId);
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
        
        // 转换为DataSource对象
        com.data.rsync.common.model.DataSource dataSource = new com.data.rsync.common.model.DataSource();
        dataSource.setId(dataSourceEntity.getId());
        dataSource.setName(dataSourceEntity.getName());
        dataSource.setType(dataSourceEntity.getType());
        dataSource.setHost(dataSourceEntity.getHost());
        dataSource.setPort(dataSourceEntity.getPort());
        dataSource.setDatabase(dataSourceEntity.getDatabaseName());
        dataSource.setUsername(dataSourceEntity.getUsername());
        dataSource.setPassword(dataSourceEntity.getPassword());
        dataSource.setUrl(dataSourceEntity.getUrl());
        
        // 获取数据源适配器
        com.data.rsync.common.adapter.DataSourceAdapter adapter = com.data.rsync.common.adapter.DataSourceAdapterFactory.getAdapter(dataSourceEntity.getType());
        
        if ("MYSQL".equals(type) && "BINLOG".equals(logMonitorType)) {
            // 测试MySQL binlog监听
            log.info("测试MySQL binlog监听");
            // 检查MySQL binlog是否启用
            try (Connection connection = adapter.getConnection(dataSource)) {
                try (Statement statement = connection.createStatement()) {
                    try (ResultSet rs = statement.executeQuery("SHOW VARIABLES LIKE 'log_bin'")) {
                        if (rs.next()) {
                            String logBinValue = rs.getString(2);
                            if ("ON".equalsIgnoreCase(logBinValue)) {
                                log.info("MySQL binlog已启用");
                            } else {
                                throw new Exception("MySQL binlog未启用");
                            }
                        }
                    }
                } finally {
                    adapter.closeConnection(connection);
                }
            }
        } else if ("POSTGRESQL".equals(type) && "WAL".equals(logMonitorType)) {
            // 测试PostgreSQL WAL监听
            log.info("测试PostgreSQL WAL监听");
            // 检查PostgreSQL WAL级别
            try (Connection connection = adapter.getConnection(dataSource)) {
                try (Statement statement = connection.createStatement()) {
                    try (ResultSet rs = statement.executeQuery("SHOW wal_level")) {
                        if (rs.next()) {
                            String walLevel = rs.getString(1);
                            if ("logical".equalsIgnoreCase(walLevel) || "replica".equalsIgnoreCase(walLevel)) {
                                log.info("PostgreSQL WAL级别为: {}", walLevel);
                            } else {
                                throw new Exception("PostgreSQL WAL级别需要设置为logical或replica");
                            }
                        }
                    }
                } finally {
                    adapter.closeConnection(connection);
                }
            }
        } else if ("ORACLE".equals(type) && "REDO_LOG".equals(logMonitorType)) {
            // 测试Oracle redo log监听
            log.info("测试Oracle redo log监听");
            // 检查Oracle redo log配置
            try (Connection connection = adapter.getConnection(dataSource)) {
                try (Statement statement = connection.createStatement()) {
                    try (ResultSet rs = statement.executeQuery("SELECT LOG_MODE FROM V$DATABASE")) {
                        if (rs.next()) {
                            String logMode = rs.getString(1);
                            if ("ARCHIVELOG".equalsIgnoreCase(logMode)) {
                                log.info("Oracle已启用归档模式");
                            } else {
                                throw new Exception("Oracle需要启用归档模式");
                            }
                        }
                    }
                } finally {
                    adapter.closeConnection(connection);
                }
            }
        } else if ("MONGODB".equals(type) && "OPLOG".equals(logMonitorType)) {
            // 测试MongoDB oplog监听
            log.info("测试MongoDB oplog监听");
            // MongoDB oplog测试逻辑
            // 这里可以添加MongoDB连接测试和oplog访问测试
            throw new Exception("MongoDB oplog监听测试暂未实现");
        }
    }

    @Override
    public List<DataSourceTemplateEntity> getSystemDataSourceTemplates() {
        // 实现获取系统数据源模板的逻辑
        try {
            LogUtils.info("获取系统数据源模板");
            
            // 由于DataSourceTemplateMapper不存在，返回默认系统模板
            List<DataSourceTemplateEntity> templates = new ArrayList<>();
            addDefaultTemplates(templates);
            
            return templates;
        } catch (Exception e) {
            LogUtils.error("获取系统数据源模板失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void initSystemDataSourceTemplates() {
        // 实现初始化系统数据源模板的逻辑
        try {
            LogUtils.info("初始化系统数据源模板");
            
            // 由于DataSourceTemplateMapper不存在，记录日志并返回
            LogUtils.info("注意: 由于DataSourceTemplateMapper不存在，系统数据源模板未保存到数据库");
            
            // 初始化系统预设模板
            List<DataSourceTemplateEntity> systemTemplates = new ArrayList<>();
            addDefaultTemplates(systemTemplates);
            
            LogUtils.info("初始化系统数据源模板成功，数量: {}", systemTemplates.size());
        } catch (Exception e) {
            LogUtils.error("初始化系统数据源模板失败", e);
            throw new DataSourceException("初始化系统数据源模板失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DataSourceDiagnoseReportEntity getLatestDataSourceDiagnoseReport(Long dataSourceId) {
        // 实现获取最新的数据源诊断报告的逻辑
        try {
            LogUtils.info("获取最新的数据源诊断报告，数据源ID: {}", dataSourceId);
            
            // 由于DataSourceDiagnoseReportMapper不存在，返回null
            // 后续可以添加实际的数据库查询逻辑
            return null;
        } catch (Exception e) {
            LogUtils.error("获取最新的数据源诊断报告失败，数据源ID: {}", dataSourceId, e);
            return null;
        }
    }

    @Override
    public List<DataSourceEntity> getDataSourcesByEnabled(boolean enabled) {
        // 实现根据启用状态获取数据源的逻辑
        try {
            QueryWrapper<DataSourceEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("enabled", enabled);
            List<DataSourceEntity> dataSources = dataSourceMapper.selectList(queryWrapper);
            // 解密敏感信息
            for (DataSourceEntity dataSource : dataSources) {
                dataSource.setPassword(sensitiveInfoEncryptor.decrypt(dataSource.getPassword()));
            }
            LogUtils.info("根据启用状态获取数据源，启用状态: {}, 数量: {}", enabled, dataSources.size());
            return dataSources;
        } catch (Exception e) {
            LogUtils.error("根据启用状态获取数据源失败，启用状态: {}", enabled, e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean testDataSourceConnection(Long id) {
        // 实现测试数据源连接的逻辑
        try {
            DataSourceEntity dataSourceEntity = getDataSourceById(id);
            if (dataSourceEntity == null) {
                LogUtils.warn("测试数据源连接失败，数据源不存在，ID: {}", id);
                return false;
            }
            
            // 测试连接
            DataSourceConnectionTestResponse response = testDataSourceConnection(dataSourceEntity);
            LogUtils.info("测试数据源连接成功，ID: {}, 响应时间: {}ms", id, response.getResponseTimeMs());
            return true;
        } catch (Exception e) {
            LogUtils.error("测试数据源连接失败，ID: {}", id, e);
            return false;
        }
    }

    @Override
    public String checkDataSourceHealth(Long id) {
        // 实现检查数据源健康状态的逻辑
        try {
            DataSourceEntity dataSourceEntity = getDataSourceById(id);
            if (dataSourceEntity == null) {
                LogUtils.warn("检查数据源健康状态失败，数据源不存在，ID: {}", id);
                return "NOT_FOUND";
            }
            
            // 执行心跳检测
            DataSourceHealthCheckResponse response = executeHeartbeat(id);
            LogUtils.info("检查数据源健康状态成功，ID: {}, 状态: {}", id, response.getStatus());
            return response.getStatus();
        } catch (Exception e) {
            LogUtils.error("检查数据源健康状态失败，ID: {}", id, e);
            return "ERROR";
        }
    }

    @Override
    public List<DataSourceBatchHealthCheckResponse.DataSourceHealthCheckItem> batchCheckDataSourceHealth() {
        // 实现批量检查数据源健康状态的逻辑
        try {
            List<DataSourceBatchHealthCheckResponse.DataSourceHealthCheckItem> healthCheckItems = new ArrayList<>();
            
            // 获取所有启用的数据源
            List<DataSourceEntity> dataSources = getAllDataSources();
            
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
                }
            }
            
            LogUtils.info("批量检查数据源健康状态完成，检查数量: {}", healthCheckItems.size());
            return healthCheckItems;
        } catch (Exception e) {
            LogUtils.error("批量检查数据源健康状态失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<DataSourceTemplateEntity> getAllDataSourceTemplates() {
        // 实现获取所有数据源模板的逻辑
        try {
            LogUtils.info("获取所有数据源模板");
            
            // 由于DataSourceTemplateMapper不存在，返回默认模板
            List<DataSourceTemplateEntity> templates = new ArrayList<>();
            addDefaultTemplates(templates);
            
            return templates;
        } catch (Exception e) {
            LogUtils.error("获取所有数据源模板失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<DataSourceTemplateEntity> getDataSourceTemplatesByType(String type) {
        // 实现根据类型获取数据源模板的逻辑
        try {
            LogUtils.info("根据类型获取数据源模板，类型: {}", type);
            
            // 由于DataSourceTemplateMapper不存在，返回默认模板
            List<DataSourceTemplateEntity> templates = new ArrayList<>();
            addDefaultTemplate(templates, type);
            
            return templates;
        } catch (Exception e) {
            LogUtils.error("根据类型获取数据源模板失败，类型: {}", type, e);
            return new ArrayList<>();
        }
    }

}
