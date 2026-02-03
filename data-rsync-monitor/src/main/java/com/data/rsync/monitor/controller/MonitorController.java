package com.data.rsync.monitor.controller;

import com.data.rsync.common.model.Response;
import com.data.rsync.monitor.service.AlertService;
import com.data.rsync.monitor.service.MetricsService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 监控服务控制器
 */
@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Resource
    private MetricsService metricsService;

    @Resource
    private AlertService alertService;

    /**
     * 获取所有指标
     * @return 所有指标
     */
    @GetMapping("/metrics")
    public Response<Map<String, Object>> getAllMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectAllMetrics();
            return Response.success(metrics);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 获取 JVM 指标
     * @return JVM 指标
     */
    @GetMapping("/metrics/jvm")
    public Response<Map<String, Object>> getJvmMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectJvmMetrics();
            return Response.success(metrics);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 获取系统指标
     * @return 系统指标
     */
    @GetMapping("/metrics/system")
    public Response<Map<String, Object>> getSystemMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectSystemMetrics();
            return Response.success(metrics);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 获取数据源指标
     * @return 数据源指标
     */
    @GetMapping("/metrics/datasource")
    public Response<Map<String, Object>> getDatasourceMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectDatasourceMetrics();
            return Response.success(metrics);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 获取任务指标
     * @return 任务指标
     */
    @GetMapping("/metrics/task")
    public Response<Map<String, Object>> getTaskMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectTaskMetrics();
            return Response.success(metrics);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 获取 Milvus 指标
     * @return Milvus 指标
     */
    @GetMapping("/metrics/milvus")
    public Response<Map<String, Object>> getMilvusMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectMilvusMetrics();
            return Response.success(metrics);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 检查指标阈值
     * @param metricName 指标名称
     * @param metricValue 指标值
     * @return 检查结果
     */
    @GetMapping("/alert/check")
    public Response<Map<String, Object>> checkThreshold(@RequestParam String metricName, @RequestParam double metricValue) {
        try {
            Map<String, Object> result = alertService.checkThreshold(metricName, metricValue);
            return Response.success(result);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
        }
    }

    /**
     * 发送告警
     * @param severity 告警级别
     * @param message 告警消息
     * @param metrics 相关指标
     * @return 发送结果
     */
    @PostMapping("/alert/send")
    public Response<Boolean> sendAlert(@RequestParam String severity, @RequestParam String message, @RequestBody(required = false) Map<String, Object> metrics) {
        try {
            boolean result = alertService.sendAlert(severity, message, metrics);
            return Response.success(result ? "告警发送成功" : "告警发送失败", result);
        } catch (Exception e) {
            return Response.failure(500, e.getMessage());
        }
    }

}
