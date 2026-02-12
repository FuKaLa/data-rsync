package com.data.rsync.data.service;

import com.data.rsync.data.entity.DataSourceEntity;
import com.data.rsync.data.entity.DataSourceTemplateEntity;
import com.data.rsync.data.entity.DataSourceDiagnoseReportEntity;
import com.data.rsync.data.vo.*;

import java.util.List;
import java.util.Map;

/**
 * 数据源服务接口
 */
public interface DataSourceService {

    /**
     * 创建数据源
     * @param dataSourceEntity 数据源实体
     * @return 创建的数据源
     */
    DataSourceEntity createDataSource(DataSourceEntity dataSourceEntity);

    /**
     * 更新数据源
     * @param dataSourceEntity 数据源实体
     * @return 更新后的数据源
     */
    DataSourceEntity updateDataSource(DataSourceEntity dataSourceEntity);

    /**
     * 删除数据源
     * @param id 数据源ID
     */
    void deleteDataSource(Long id);

    /**
     * 启用/禁用数据源
     * @param id 数据源ID
     * @param enabled 是否启用
     */
    void toggleDataSource(Long id, Boolean enabled);

    /**
     * 根据ID获取数据源
     * @param id 数据源ID
     * @return 数据源实体
     */
    DataSourceEntity getDataSourceById(Long id);

    /**
     * 根据名称获取数据源
     * @param name 数据源名称
     * @return 数据源实体
     */
    DataSourceEntity getDataSourceByName(String name);

    /**
     * 获取所有数据源
     * @return 数据源列表
     */
    List<DataSourceEntity> getAllDataSources();

    /**
     * 根据类型获取数据源
     * @param type 数据源类型
     * @return 数据源列表
     */
    List<DataSourceEntity> getDataSourcesByType(String type);

    /**
     * 根据健康状态获取数据源
     * @param healthStatus 健康状态
     * @return 数据源列表
     */
    List<DataSourceEntity> getDataSourcesByHealthStatus(String healthStatus);

    /**
     * 测试数据源连接
     * @param dataSourceEntity 数据源实体
     * @return 连接测试结果
     */
    DataSourceConnectionTestResponse testDataSourceConnection(DataSourceEntity dataSourceEntity);

    /**
     * 执行数据源诊断
     * @param id 数据源ID
     * @return 诊断报告
     */
    DataSourceDiagnoseReportEntity diagnoseDataSource(Long id);

    /**
     * 执行心跳检测
     * @param id 数据源ID
     * @return 心跳检测结果
     */
    DataSourceHealthCheckResponse executeHeartbeat(Long id);

    /**
     * 执行批量心跳检测
     * @return 心跳检测结果
     */
    DataSourceBatchHealthCheckResponse executeBatchHeartbeat();

    /**
     * 获取数据源模板
     * @param dataSourceType 数据源类型
     * @return 数据源模板列表
     */
    List<DataSourceTemplateEntity> getDataSourceTemplates(String dataSourceType);

    /**
     * 获取所有数据源模板
     * @return 数据源模板列表
     */
    List<DataSourceTemplateEntity> getAllDataSourceTemplates();

    /**
     * 根据类型获取数据源模板
     * @param type 数据源类型
     * @return 数据源模板列表
     */
    List<DataSourceTemplateEntity> getDataSourceTemplatesByType(String type);

    /**
     * 创建数据源模板
     * @param templateEntity 数据源模板实体
     * @return 创建的模板
     */
    DataSourceTemplateEntity createDataSourceTemplate(DataSourceTemplateEntity templateEntity);

    /**
     * 更新数据源模板
     * @param templateEntity 数据源模板实体
     * @return 更新后的模板
     */
    DataSourceTemplateEntity updateDataSourceTemplate(DataSourceTemplateEntity templateEntity);

    /**
     * 删除数据源模板
     * @param id 模板ID
     */
    void deleteDataSourceTemplate(Long id);

    /**
     * 获取数据源诊断报告
     * @param dataSourceId 数据源ID
     * @return 诊断报告列表
     */
    List<DataSourceDiagnoseReportEntity> getDataSourceDiagnoseReports(Long dataSourceId);

    /**
     * 获取数据源连接信息
     * @param id 数据源ID
     * @return 连接信息
     */
    DataSourceConnectionInfoResponse getDataSourceConnectionInfo(Long id);

    /**
     * 刷新数据源配置
     * @param id 数据源ID
     * @return 刷新结果
     */
    boolean refreshDataSourceConfig(Long id);

    /**
     * 导出数据源配置
     * @param id 数据源ID
     * @return 配置信息
     */
    DataSourceConfigExportResponse exportDataSourceConfig(Long id);

    /**
     * 导入数据源配置
     * @param config 配置信息
     * @return 导入结果
     */
    DataSourceEntity importDataSourceConfig(Map<String, Object> config);

    /**
     * 获取数据源统计信息
     * @return 统计信息
     */
    DataSourceStatisticsResponse getDataSourceStatistics();

    /**
     * 获取系统数据源模板
     * @return 系统数据源模板列表
     */
    List<DataSourceTemplateEntity> getSystemDataSourceTemplates();

    /**
     * 初始化系统数据源模板
     */
    void initSystemDataSourceTemplates();

    /**
     * 获取最新的数据源诊断报告
     * @param dataSourceId 数据源ID
     * @return 最新的诊断报告
     */
    DataSourceDiagnoseReportEntity getLatestDataSourceDiagnoseReport(Long dataSourceId);

    /**
     * 根据启用状态获取数据源
     * @param enabled 是否启用
     * @return 数据源列表
     */
    List<DataSourceEntity> getDataSourcesByEnabled(boolean enabled);

    /**
     * 测试数据源连接
     * @param id 数据源ID
     * @return 测试结果
     */
    boolean testDataSourceConnection(Long id);

    /**
     * 检查数据源健康状态
     * @param id 数据源ID
     * @return 健康状态
     */
    String checkDataSourceHealth(Long id);

    /**
     * 批量检查数据源健康状态
     * @return 健康状态列表
     */
    List<DataSourceBatchHealthCheckResponse.DataSourceHealthCheckItem> batchCheckDataSourceHealth();
}
