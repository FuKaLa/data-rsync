package com.data.rsync.datasource.controller;

import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.model.Response;
import com.data.rsync.datasource.entity.DataSourceTemplateEntity;
import com.data.rsync.datasource.entity.DataSourceDiagnoseReportEntity;
import com.data.rsync.datasource.service.DataSourceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 数据源控制器
 */
@RestController
@RequestMapping("/api/data-source")
@Slf4j
public class DataSourceController {

    @Resource
    private DataSourceService dataSourceService;

    /**
     * 创建数据源
     * @param dataSource 数据源模型
     * @return 创建结果
     */
    @PostMapping
    public Response<DataSource> createDataSource(@RequestBody DataSource dataSource) {
        log.info("Received request to create data source: {}", dataSource.getName());
        try {
            DataSource result = dataSourceService.createDataSource(dataSource);
            return Response.success("Data source created successfully", result);
        } catch (Exception e) {
            log.error("Failed to create data source: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to create data source: " + e.getMessage());
        }
    }

    /**
     * 创建数据源（Feign客户端使用）
     * @param dataSource 数据源模型
     * @return 创建结果
     */
    @PostMapping("/create")
    public boolean createDataSourceFeign(@RequestBody DataSource dataSource) {
        log.info("Received request to create data source (Feign): {}", dataSource.getName());
        try {
            dataSourceService.createDataSource(dataSource);
            return true;
        } catch (Exception e) {
            log.error("Failed to create data source (Feign): {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 测试数据源连接（Feign客户端使用）
     * @param dataSource 数据源模型
     * @return 连接结果
     */
    @PostMapping("/test-connection")
    public boolean testDataSourceConnection(@RequestBody DataSource dataSource) {
        log.info("Received request to test data source connection (Feign): {}", dataSource.getName());
        try {
            return dataSourceService.testDataSourceConnection(dataSource);
        } catch (Exception e) {
            log.error("Failed to test data source connection (Feign): {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新数据源
     * @param id 数据源ID
     * @param dataSource 数据源模型
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Response<DataSource> updateDataSource(@PathVariable(name = "id") Long id, @RequestBody DataSource dataSource) {
        log.info("Received request to update data source: {}", id);
        try {
            DataSource result = dataSourceService.updateDataSource(id, dataSource);
            return Response.success("Data source updated successfully", result);
        } catch (Exception e) {
            log.error("Failed to update data source: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to update data source: " + e.getMessage());
        }
    }

    /**
     * 删除数据源
     * @param id 数据源ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Response<Void> deleteDataSource(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete data source: {}", id);
        try {
            dataSourceService.deleteDataSource(id);
            return Response.success("Data source deleted successfully", null);
        } catch (Exception e) {
            log.error("Failed to delete data source: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to delete data source: " + e.getMessage());
        }
    }

    /**
     * 获取数据源
     * @param id 数据源ID
     * @return 数据源
     */
    @GetMapping("/detail/{id}")
    public Response<DataSource> getDataSource(@PathVariable(name = "id") Long id) {
        log.info("Received request to get data source: {}", id);
        try {
            DataSource result = dataSourceService.getDataSource(id);
            return Response.success(result);
        } catch (Exception e) {
            log.error("Failed to get data source: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to get data source: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取数据源（Feign客户端使用）
     * @param id 数据源ID
     * @return 数据源
     */
    @GetMapping("/{id}")
    public DataSource getDataSourceById(@PathVariable("id") Long id) {
        log.info("Received request to get data source by ID (Feign): {}", id);
        try {
            return dataSourceService.getDataSource(id);
        } catch (Exception e) {
            log.error("Failed to get data source by ID (Feign): {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取所有数据源
     * @return 数据源列表
     */
    @GetMapping
    public Response<List<DataSource>> getAllDataSources() {
        log.info("Received request to get all data sources");
        try {
            List<DataSource> result = dataSourceService.getAllDataSources();
            return Response.success(result);
        } catch (Exception e) {
            log.error("Failed to get all data sources: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to get all data sources: " + e.getMessage());
        }
    }

    /**
     * 获取数据源列表（Feign客户端使用）
     * @return 数据源列表
     */
    @GetMapping("/list")
    public List<DataSource> getDataSourceList() {
        log.info("Received request to get data source list (Feign)");
        try {
            return dataSourceService.getAllDataSources();
        } catch (Exception e) {
            log.error("Failed to get data source list: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 根据类型获取数据源
     * @param type 数据源类型
     * @return 数据源列表
     */
    @GetMapping("/type/{type}")
    public Response<List<DataSource>> getDataSourcesByType(@PathVariable(name = "type") String type) {
        log.info("Received request to get data sources by type: {}", type);
        try {
            List<DataSource> result = dataSourceService.getDataSourcesByType(type);
            return Response.success(result);
        } catch (Exception e) {
            log.error("Failed to get data sources by type: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to get data sources by type: " + e.getMessage());
        }
    }

    /**
     * 根据启用状态获取数据源
     * @param enabled 启用状态
     * @return 数据源列表
     */
    @GetMapping("/enabled/{enabled}")
    public Response<List<DataSource>> getDataSourcesByEnabled(@PathVariable(name = "enabled") Boolean enabled) {
        log.info("Received request to get data sources by enabled: {}", enabled);
        try {
            List<DataSource> result = dataSourceService.getDataSourcesByEnabled(enabled);
            return Response.success(result);
        } catch (Exception e) {
            log.error("Failed to get data sources by enabled: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to get data sources by enabled: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用数据源
     * @param id 数据源ID
     * @param enabled 启用状态
     * @return 更新结果
     */
    @PutMapping("/{id}/enable")
    public Response<DataSource> enableDataSource(@PathVariable(name = "id") Long id, @RequestParam(name = "enabled") Boolean enabled) {
        log.info("Received request to enable data source: {} to {}", id, enabled);
        try {
            DataSource result = dataSourceService.enableDataSource(id, enabled);
            return Response.success("Data source enabled successfully", result);
        } catch (Exception e) {
            log.error("Failed to enable data source: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to enable data source: " + e.getMessage());
        }
    }

    /**
     * 测试数据源连接
     * @param id 数据源ID
     * @return 测试结果
     */
    @PostMapping("/{id}/test-connection")
    public Response<Boolean> testDataSourceConnection(@PathVariable(name = "id") Long id) {
        log.info("Received request to test data source connection: {}", id);
        try {
            boolean connected = dataSourceService.testDataSourceConnection(id);
            String message = connected ? "Connection successful" : "Connection failed";
            return Response.success(message, connected);
        } catch (Exception e) {
            log.error("Failed to test data source connection: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to test data source connection: " + e.getMessage());
        }
    }

    /**
     * 检查数据源健康状态
     * @param id 数据源ID
     * @return 健康状态
     */
    @GetMapping("/{id}/health")
    public Response<String> checkDataSourceHealth(@PathVariable(name = "id") Long id) {
        log.info("Received request to check data source health: {}", id);
        try {
            String healthStatus = dataSourceService.checkDataSourceHealth(id);
            return Response.success("Health status checked successfully", healthStatus);
        } catch (Exception e) {
            log.error("Failed to check data source health: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to check data source health: " + e.getMessage());
        }
    }

    /**
     * 批量检查数据源健康状态
     * @return 检查结果
     */
    @PostMapping("/batch-check-health")
    public Response<Void> batchCheckDataSourceHealth() {
        log.info("Received request to batch check data source health");
        try {
            dataSourceService.batchCheckDataSourceHealth();
            return Response.success("Batch health check completed successfully", null);
        } catch (Exception e) {
            log.error("Failed to batch check data source health: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to batch check data source health: " + e.getMessage());
        }
    }

    /**
     * 获取所有数据源模板
     * @return 模板列表
     */
    @GetMapping("/templates")
    public Response<List<DataSourceTemplateEntity>> getAllTemplates() {
        log.info("Received request to get all data source templates");
        try {
            List<DataSourceTemplateEntity> templates = dataSourceService.getAllTemplates();
            return Response.success(templates);
        } catch (Exception e) {
            log.error("Failed to get data source templates: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to get data source templates: " + e.getMessage());
        }
    }

    /**
     * 根据类型获取数据源模板
     * @param type 数据源类型
     * @return 模板列表
     */
    @GetMapping("/templates/type/{type}")
    public Response<List<DataSourceTemplateEntity>> getTemplatesByType(@PathVariable(name = "type") String type) {
        log.info("Received request to get data source templates by type: {}", type);
        try {
            List<DataSourceTemplateEntity> templates = dataSourceService.getTemplatesByType(type);
            return Response.success(templates);
        } catch (Exception e) {
            log.error("Failed to get data source templates by type: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to get data source templates by type: " + e.getMessage());
        }
    }

    /**
     * 获取系统预设模板
     * @return 模板列表
     */
    @GetMapping("/templates/system")
    public Response<List<DataSourceTemplateEntity>> getSystemTemplates() {
        log.info("Received request to get system data source templates");
        try {
            List<DataSourceTemplateEntity> templates = dataSourceService.getSystemTemplates();
            return Response.success(templates);
        } catch (Exception e) {
            log.error("Failed to get system data source templates: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to get system data source templates: " + e.getMessage());
        }
    }

    /**
     * 创建数据源模板
     * @param template 模板实体
     * @return 创建结果
     */
    @PostMapping("/templates")
    public Response<DataSourceTemplateEntity> createTemplate(@RequestBody DataSourceTemplateEntity template) {
        log.info("Received request to create data source template: {}", template.getName());
        try {
            DataSourceTemplateEntity result = dataSourceService.createTemplate(template);
            return Response.success("Template created successfully", result);
        } catch (Exception e) {
            log.error("Failed to create data source template: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to create data source template: " + e.getMessage());
        }
    }

    /**
     * 更新数据源模板
     * @param id 模板ID
     * @param template 模板实体
     * @return 更新结果
     */
    @PutMapping("/templates/{id}")
    public Response<DataSourceTemplateEntity> updateTemplate(@PathVariable(name = "id") Long id, @RequestBody DataSourceTemplateEntity template) {
        log.info("Received request to update data source template: {}", id);
        try {
            DataSourceTemplateEntity result = dataSourceService.updateTemplate(id, template);
            return Response.success("Template updated successfully", result);
        } catch (Exception e) {
            log.error("Failed to update data source template: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to update data source template: " + e.getMessage());
        }
    }

    /**
     * 删除数据源模板
     * @param id 模板ID
     * @return 删除结果
     */
    @DeleteMapping("/templates/{id}")
    public Response<Void> deleteTemplate(@PathVariable(name = "id") Long id) {
        log.info("Received request to delete data source template: {}", id);
        try {
            dataSourceService.deleteTemplate(id);
            return Response.success("Template deleted successfully", null);
        } catch (Exception e) {
            log.error("Failed to delete data source template: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to delete data source template: " + e.getMessage());
        }
    }

    /**
     * 初始化系统预设模板
     * @return 初始化结果
     */
    @PostMapping("/templates/init-system")
    public Response<Void> initSystemTemplates() {
        log.info("Received request to initialize system data source templates");
        try {
            dataSourceService.initSystemTemplates();
            return Response.success("System templates initialized successfully", null);
        } catch (Exception e) {
            log.error("Failed to initialize system data source templates: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to initialize system data source templates: " + e.getMessage());
        }
    }

    /**
     * 诊断数据源
     * @param id 数据源ID
     * @return 诊断结果
     */
    @PostMapping("/{id}/diagnose")
    public Response<DataSourceDiagnoseReportEntity> diagnoseDataSource(@PathVariable(name = "id") Long id) {
        log.info("Received request to diagnose data source: {}", id);
        try {
            DataSourceDiagnoseReportEntity report = dataSourceService.diagnoseDataSource(id);
            return Response.success("Data source diagnosed successfully", report);
        } catch (Exception e) {
            log.error("Failed to diagnose data source: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to diagnose data source: " + e.getMessage());
        }
    }

    /**
     * 获取数据源的诊断报告
     * @param id 数据源ID
     * @return 诊断报告列表
     */
    @GetMapping("/{id}/diagnose-reports")
    public Response<List<DataSourceDiagnoseReportEntity>> getDiagnoseReports(@PathVariable(name = "id") Long id) {
        log.info("Received request to get diagnose reports for data source: {}", id);
        try {
            List<DataSourceDiagnoseReportEntity> reports = dataSourceService.getDiagnoseReports(id);
            return Response.success(reports);
        } catch (Exception e) {
            log.error("Failed to get diagnose reports: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to get diagnose reports: " + e.getMessage());
        }
    }

    /**
     * 获取数据源的最新诊断报告
     * @param id 数据源ID
     * @return 最新的诊断报告
     */
    @GetMapping("/{id}/latest-diagnose-report")
    public Response<DataSourceDiagnoseReportEntity> getLatestDiagnoseReport(@PathVariable(name = "id") Long id) {
        log.info("Received request to get latest diagnose report for data source: {}", id);
        try {
            DataSourceDiagnoseReportEntity report = dataSourceService.getLatestDiagnoseReport(id);
            return Response.success(report);
        } catch (Exception e) {
            log.error("Failed to get latest diagnose report: {}", e.getMessage(), e);
            return Response.failure(500, "Failed to get latest diagnose report: " + e.getMessage());
        }
    }

}
