package com.data.rsync.data.controller;

import com.data.rsync.common.model.Response;
import com.data.rsync.data.entity.DataSourceEntity;
import com.data.rsync.data.entity.DataSourceTemplateEntity;
import com.data.rsync.data.entity.DataSourceDiagnoseReportEntity;
import com.data.rsync.data.service.DataSourceService;
import com.data.rsync.data.vo.DataSourceConnectionTestResponse;
import com.data.rsync.data.vo.DataSourceHealthCheckResponse;
import com.data.rsync.data.vo.DataSourceBatchHealthCheckResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 数据源管理控制器
 */
@RestController
@RequestMapping("/data-source")
public class DataSourceController {

    @Resource
    private DataSourceService dataSourceService;

    /**
     * 获取数据源列表
     * @return 数据源列表
     */
    @GetMapping
    public Response<List<DataSourceEntity>> getList() {
        try {
            List<DataSourceEntity> dataSources = dataSourceService.getAllDataSources();
            return Response.success(dataSources);
        } catch (Exception e) {
            return Response.failure(500, "获取数据源列表失败");
        }
    }

    /**
     * 获取数据源详情
     * @param id 数据源ID
     * @return 数据源详情
     */
    @GetMapping("/{id}")
    public Response<DataSourceEntity> getDetail(@PathVariable Long id) {
        try {
            DataSourceEntity dataSource = dataSourceService.getDataSourceById(id);
            if (dataSource == null) {
                return Response.failure(404, "数据源不存在");
            }
            return Response.success(dataSource);
        } catch (Exception e) {
            return Response.failure(500, "获取数据源详情失败");
        }
    }

    /**
     * 创建数据源
     * @param dataSource 数据源信息
     * @return 创建结果
     */
    @PostMapping
    public Response<DataSourceEntity> create(@RequestBody DataSourceEntity dataSource) {
        try {
            DataSourceEntity createdDataSource = dataSourceService.createDataSource(dataSource);
            return Response.success("数据源创建成功", createdDataSource);
        } catch (Exception e) {
            return Response.failure(500, "创建数据源失败");
        }
    }

    /**
     * 更新数据源
     * @param id 数据源ID
     * @param dataSource 数据源信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Response<DataSourceEntity> update(@PathVariable Long id, @RequestBody DataSourceEntity dataSource) {
        try {
            dataSource.setId(id);
            DataSourceEntity updatedDataSource = dataSourceService.updateDataSource(dataSource);
            return Response.success("数据源更新成功", updatedDataSource);
        } catch (Exception e) {
            return Response.failure(500, "更新数据源失败");
        }
    }

    /**
     * 删除数据源
     * @param id 数据源ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Response<String> delete(@PathVariable Long id) {
        try {
            dataSourceService.deleteDataSource(id);
            return Response.success("数据源删除成功");
        } catch (Exception e) {
            return Response.failure(500, "删除数据源失败");
        }
    }

    /**
     * 测试数据源连接
     * @param id 数据源ID
     * @return 测试结果
     */
    @PostMapping("/{id}/test-connection")
    public Response<Boolean> testDataSourceConnection(@PathVariable Long id) {
        try {
            boolean success = dataSourceService.testDataSourceConnection(id);
            return Response.success(success);
        } catch (Exception e) {
            return Response.failure(500, "测试数据源连接失败");
        }
    }

    /**
     * 测试新数据源连接
     * @param dataSource 数据源信息
     * @return 测试结果
     */
    @PostMapping("/test-connection")
    public Response<Map<String, Object>> testNewDataSourceConnection(@RequestBody DataSourceEntity dataSource) {
        try {
            DataSourceConnectionTestResponse result = dataSourceService.testDataSourceConnection(dataSource);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "连接成功");
            response.put("responseTimeMs", result.getResponseTimeMs());
            return Response.success(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return Response.failure(500, e.getMessage(), response);
        }
    }

    /**
     * 启用/禁用数据源
     * @param id 数据源ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @PutMapping("/{id}/enable")
    public Response<DataSourceEntity> enable(@PathVariable Long id, @RequestParam boolean enabled) {
        try {
            DataSourceEntity dataSource = dataSourceService.getDataSourceById(id);
            if (dataSource == null) {
                return Response.failure(404, "数据源不存在");
            }
            dataSource.setEnabled(enabled);
            DataSourceEntity updatedDataSource = dataSourceService.updateDataSource(dataSource);
            return Response.success(updatedDataSource);
        } catch (Exception e) {
            return Response.failure(500, "启用/禁用数据源失败");
        }
    }

    /**
     * 检查数据源健康状态
     * @param id 数据源ID
     * @return 健康状态
     */
    @GetMapping("/{id}/health")
    public Response<DataSourceHealthCheckResponse> checkHealth(@PathVariable Long id) {
        try {
            String status = dataSourceService.checkDataSourceHealth(id);
            DataSourceHealthCheckResponse result = new DataSourceHealthCheckResponse(
                    "HEALTHY".equals(status),
                    status,
                    0
            );
            return Response.success(result);
        } catch (Exception e) {
            return Response.failure(500, "检查数据源健康状态失败");
        }
    }

    /**
     * 批量检查数据源健康状态
     * @return 健康状态列表
     */
    @PostMapping("/batch-check-health")
    public Response<List<DataSourceBatchHealthCheckResponse.DataSourceHealthCheckItem>> batchCheckHealth() {
        try {
            List<DataSourceBatchHealthCheckResponse.DataSourceHealthCheckItem> results = dataSourceService.batchCheckDataSourceHealth();
            return Response.success(results);
        } catch (Exception e) {
            return Response.failure(500, "批量检查数据源健康状态失败");
        }
    }

    /**
     * 根据类型获取数据源
     * @param type 数据源类型
     * @return 数据源列表
     */
    @GetMapping("/type/{type}")
    public Response<List<DataSourceEntity>> getDataSourcesByType(@PathVariable String type) {
        try {
            List<DataSourceEntity> dataSources = dataSourceService.getDataSourcesByType(type);
            return Response.success(dataSources);
        } catch (Exception e) {
            return Response.failure(500, "获取数据源列表失败");
        }
    }

    /**
     * 根据启用状态获取数据源
     * @param enabled 是否启用
     * @return 数据源列表
     */
    @GetMapping("/enabled/{enabled}")
    public Response<List<DataSourceEntity>> getDataSourcesByEnabled(@PathVariable boolean enabled) {
        try {
            List<DataSourceEntity> dataSources = dataSourceService.getDataSourcesByEnabled(enabled);
            return Response.success(dataSources);
        } catch (Exception e) {
            return Response.failure(500, "获取数据源列表失败");
        }
    }

    // 模板相关接口

    /**
     * 获取数据源模板列表
     * @return 模板列表
     */
    @GetMapping("/templates")
    public Response<List<DataSourceTemplateEntity>> getTemplates() {
        try {
            List<DataSourceTemplateEntity> templates = dataSourceService.getAllDataSourceTemplates();
            return Response.success(templates);
        } catch (Exception e) {
            return Response.failure(500, "获取数据源模板列表失败");
        }
    }

    /**
     * 根据类型获取数据源模板
     * @param type 数据源类型
     * @return 模板列表
     */
    @GetMapping("/templates/type/{type}")
    public Response<List<DataSourceTemplateEntity>> getTemplatesByType(@PathVariable String type) {
        try {
            List<DataSourceTemplateEntity> templates = dataSourceService.getDataSourceTemplatesByType(type);
            return Response.success(templates);
        } catch (Exception e) {
            return Response.failure(500, "获取数据源模板列表失败");
        }
    }

    /**
     * 获取系统模板
     * @return 系统模板列表
     */
    @GetMapping("/templates/system")
    public Response<List<DataSourceTemplateEntity>> getSystemTemplates() {
        try {
            List<DataSourceTemplateEntity> templates = dataSourceService.getSystemDataSourceTemplates();
            return Response.success(templates);
        } catch (Exception e) {
            return Response.failure(500, "获取系统模板列表失败");
        }
    }

    /**
     * 创建数据源模板
     * @param template 模板信息
     * @return 创建结果
     */
    @PostMapping("/templates")
    public Response<DataSourceTemplateEntity> createTemplate(@RequestBody DataSourceTemplateEntity template) {
        try {
            DataSourceTemplateEntity createdTemplate = dataSourceService.createDataSourceTemplate(template);
            return Response.success("模板创建成功", createdTemplate);
        } catch (Exception e) {
            return Response.failure(500, "创建数据源模板失败");
        }
    }

    /**
     * 更新数据源模板
     * @param id 模板ID
     * @param template 模板信息
     * @return 更新结果
     */
    @PutMapping("/templates/{id}")
    public Response<DataSourceTemplateEntity> updateTemplate(@PathVariable Long id, @RequestBody DataSourceTemplateEntity template) {
        try {
            template.setId(id);
            DataSourceTemplateEntity updatedTemplate = dataSourceService.updateDataSourceTemplate(template);
            return Response.success("模板更新成功", updatedTemplate);
        } catch (Exception e) {
            return Response.failure(500, "更新数据源模板失败");
        }
    }

    /**
     * 删除数据源模板
     * @param id 模板ID
     * @return 删除结果
     */
    @DeleteMapping("/templates/{id}")
    public Response<String> deleteTemplate(@PathVariable Long id) {
        try {
            dataSourceService.deleteDataSourceTemplate(id);
            return Response.success("模板删除成功");
        } catch (Exception e) {
            return Response.failure(500, "删除数据源模板失败");
        }
    }

    /**
     * 初始化系统模板
     * @return 初始化结果
     */
    @PostMapping("/templates/init-system")
    public Response<String> initSystemTemplates() {
        try {
            dataSourceService.initSystemDataSourceTemplates();
            return Response.success("系统模板初始化成功");
        } catch (Exception e) {
            return Response.failure(500, "初始化系统模板失败");
        }
    }

    // 诊断相关接口

    /**
     * 诊断数据源
     * @param id 数据源ID
     * @return 诊断报告
     */
    @PostMapping("/{id}/diagnose")
    public Response<DataSourceDiagnoseReportEntity> diagnose(@PathVariable Long id) {
        try {
            DataSourceDiagnoseReportEntity report = dataSourceService.diagnoseDataSource(id);
            return Response.success(report);
        } catch (Exception e) {
            return Response.failure(500, "诊断数据源失败");
        }
    }

    /**
     * 获取数据源诊断报告列表
     * @param id 数据源ID
     * @return 诊断报告列表
     */
    @GetMapping("/{id}/diagnose-reports")
    public Response<List<DataSourceDiagnoseReportEntity>> getDiagnoseReports(@PathVariable Long id) {
        try {
            List<DataSourceDiagnoseReportEntity> reports = dataSourceService.getDataSourceDiagnoseReports(id);
            return Response.success(reports);
        } catch (Exception e) {
            return Response.failure(500, "获取诊断报告列表失败");
        }
    }

    /**
     * 获取数据源最新诊断报告
     * @param id 数据源ID
     * @return 最新诊断报告
     */
    @GetMapping("/{id}/latest-diagnose-report")
    public Response<DataSourceDiagnoseReportEntity> getLatestDiagnoseReport(@PathVariable Long id) {
        try {
            DataSourceDiagnoseReportEntity report = dataSourceService.getLatestDataSourceDiagnoseReport(id);
            if (report == null) {
                return Response.failure(404, "诊断报告不存在");
            }
            return Response.success(report);
        } catch (Exception e) {
            return Response.failure(500, "获取最新诊断报告失败");
        }
    }
}
