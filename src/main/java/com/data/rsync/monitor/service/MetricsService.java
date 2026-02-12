package com.data.rsync.monitor.service;

import java.util.Map;

/**
 * 指标采集服务
 */
public interface MetricsService {

    /**
     * 采集JVM指标
     * @return JVM指标
     */
    Map<String, Object> collectJvmMetrics();

    /**
     * 采集系统指标
     * @return 系统指标
     */
    Map<String, Object> collectSystemMetrics();

    /**
     * 采集数据源指标
     * @return 数据源指标
     */
    Map<String, Object> collectDatasourceMetrics();

    /**
     * 采集任务指标
     * @return 任务指标
     */
    Map<String, Object> collectTaskMetrics();

    /**
     * 采集Milvus指标
     * @return Milvus指标
     */
    Map<String, Object> collectMilvusMetrics();

    /**
     * 采集所有指标
     * @return 所有指标
     */
    Map<String, Object> collectAllMetrics();

    /**
     * 采集业务维度指标
     * @return 业务维度指标
     */
    Map<String, Object> collectBusinessMetrics();

    /**
     * 采集同步任务指标
     * @param taskId 任务ID
     * @return 同步任务指标
     */
    Map<String, Object> collectSyncTaskMetrics(Long taskId);

    /**
     * 采集数据延迟指标
     * @return 数据延迟指标
     */
    Map<String, Object> collectDataDelayMetrics();

    /**
     * 采集 Milvus 写入指标
     * @return Milvus 写入指标
     */
    Map<String, Object> collectMilvusWriteMetrics();

    /**
     * 采集 API 性能指标
     * @return API 性能指标
     */
    Map<String, Object> collectApiMetrics();

    /**
     * 采集 Redis 缓存指标
     * @return Redis 缓存指标
     */
    Map<String, Object> collectRedisMetrics();

    /**
     * 采集线程池指标
     * @return 线程池指标
     */
    Map<String, Object> collectThreadPoolMetrics();

    /**
     * 采集错误统计指标
     * @return 错误统计指标
     */
    Map<String, Object> collectErrorMetrics();

    /**
     * 采集业务流程监控指标
     * @return 业务流程监控指标
     */
    Map<String, Object> collectBusinessProcessMetrics();

}
