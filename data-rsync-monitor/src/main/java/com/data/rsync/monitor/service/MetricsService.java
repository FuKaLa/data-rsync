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

}
