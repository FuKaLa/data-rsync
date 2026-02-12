package com.data.rsync.monitor.controller;

import com.data.rsync.common.model.Response;
import com.data.rsync.monitor.service.AlertService;
import com.data.rsync.monitor.service.MetricsService;
import com.data.rsync.monitor.vo.MetricsResponse;
import com.data.rsync.monitor.vo.ThresholdCheckResponse;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 监控服务控制器
 */
@RestController
@RequestMapping("/monitor")
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
    public Response<MetricsResponse> getAllMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectAllMetrics();
            MetricsResponse response = new MetricsResponse();
            response.setTimestamp(System.currentTimeMillis());
            response.setMetrics(metrics);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取指标失败");
        }
    }

    /**
     * 获取 JVM 指标
     * @return JVM 指标
     */
    @GetMapping("/metrics/jvm")
    public Response<MetricsResponse> getJvmMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectJvmMetrics();
            MetricsResponse response = new MetricsResponse();
            response.setTimestamp(System.currentTimeMillis());
            response.setMetrics(metrics);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取JVM指标失败");
        }
    }

    /**
     * 获取系统指标
     * @return 系统指标
     */
    @GetMapping("/metrics/system")
    public Response<MetricsResponse> getSystemMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectSystemMetrics();
            MetricsResponse response = new MetricsResponse();
            response.setTimestamp(System.currentTimeMillis());
            response.setMetrics(metrics);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取系统指标失败");
        }
    }

    /**
     * 获取数据源指标
     * @return 数据源指标
     */
    @GetMapping("/metrics/datasource")
    public Response<MetricsResponse> getDatasourceMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectDatasourceMetrics();
            MetricsResponse response = new MetricsResponse();
            response.setTimestamp(System.currentTimeMillis());
            response.setMetrics(metrics);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取数据源指标失败");
        }
    }

    /**
     * 获取任务指标
     * @return 任务指标
     */
    @GetMapping("/metrics/task")
    public Response<MetricsResponse> getTaskMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectTaskMetrics();
            MetricsResponse response = new MetricsResponse();
            response.setTimestamp(System.currentTimeMillis());
            response.setMetrics(metrics);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取任务指标失败");
        }
    }

    /**
     * 获取 Milvus 指标
     * @return Milvus 指标
     */
    @GetMapping("/metrics/milvus")
    public Response<MetricsResponse> getMilvusMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectMilvusMetrics();
            MetricsResponse response = new MetricsResponse();
            response.setTimestamp(System.currentTimeMillis());
            response.setMetrics(metrics);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取Milvus指标失败");
        }
    }

    /**
     * 获取 API 性能指标
     * @return API 性能指标
     */
    @GetMapping("/metrics/api")
    public Response<MetricsResponse> getApiMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectApiMetrics();
            MetricsResponse response = new MetricsResponse();
            response.setTimestamp(System.currentTimeMillis());
            response.setMetrics(metrics);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取API指标失败");
        }
    }

    /**
     * 获取 Redis 缓存指标
     * @return Redis 缓存指标
     */
    @GetMapping("/metrics/redis")
    public Response<MetricsResponse> getRedisMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectRedisMetrics();
            MetricsResponse response = new MetricsResponse();
            response.setTimestamp(System.currentTimeMillis());
            response.setMetrics(metrics);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取Redis指标失败");
        }
    }

    /**
     * 获取线程池指标
     * @return 线程池指标
     */
    @GetMapping("/metrics/thread-pool")
    public Response<MetricsResponse> getThreadPoolMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectThreadPoolMetrics();
            MetricsResponse response = new MetricsResponse();
            response.setTimestamp(System.currentTimeMillis());
            response.setMetrics(metrics);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取线程池指标失败");
        }
    }

    /**
     * 获取错误统计指标
     * @return 错误统计指标
     */
    @GetMapping("/metrics/error")
    public Response<MetricsResponse> getErrorMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectErrorMetrics();
            MetricsResponse response = new MetricsResponse();
            response.setTimestamp(System.currentTimeMillis());
            response.setMetrics(metrics);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取错误指标失败");
        }
    }

    /**
     * 获取业务流程监控指标
     * @return 业务流程监控指标
     */
    @GetMapping("/metrics/business-process")
    public Response<MetricsResponse> getBusinessProcessMetrics() {
        try {
            Map<String, Object> metrics = metricsService.collectBusinessProcessMetrics();
            MetricsResponse response = new MetricsResponse();
            response.setTimestamp(System.currentTimeMillis());
            response.setMetrics(metrics);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取业务流程指标失败");
        }
    }

    /**
     * 检查指标阈值
     * @param metricName 指标名称
     * @param metricValue 指标值
     * @return 检查结果
     */
    @GetMapping("/alert/check")
    public Response<com.data.rsync.monitor.vo.ThresholdCheckResult> checkThreshold(@RequestParam String metricName, @RequestParam double metricValue) {
        try {
            com.data.rsync.monitor.vo.ThresholdCheckResult result = alertService.checkThreshold(metricName, metricValue);
            return Response.success(result);
        } catch (Exception e) {
            return Response.failure(500, "检查阈值失败");
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
            return Response.failure(500, "发送告警失败");
        }
    }

}
