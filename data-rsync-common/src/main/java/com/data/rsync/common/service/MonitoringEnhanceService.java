package com.data.rsync.common.service;

import com.data.rsync.common.utils.MilvusUtils;
import io.milvus.client.MilvusClient;
import io.milvus.param.*;
import io.milvus.param.collection.GetCollectionStatisticsParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控指标增强服务
 * 实现更详细的业务维度指标采集
 */
@Service
@Slf4j
public class MonitoringEnhanceService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 同步任务统计
     */
    private final Map<Long, SyncTaskStats> syncTaskStatsMap = new ConcurrentHashMap<>();

    /**
     * 全局同步统计
     */
    private final GlobalSyncStats globalSyncStats = new GlobalSyncStats();

    /**
     * 记录同步任务开始
     * @param taskId 任务ID
     */
    public void recordSyncTaskStart(Long taskId) {
        SyncTaskStats stats = syncTaskStatsMap.computeIfAbsent(taskId, id -> new SyncTaskStats());
        stats.start();
        globalSyncStats.incrementTaskCount();
        log.debug("Started sync task: {}", taskId);
    }

    /**
     * 记录同步任务完成
     * @param taskId 任务ID
     * @param success 是否成功
     * @param recordsProcessed 处理记录数
     * @param duration 耗时（毫秒）
     */
    public void recordSyncTaskComplete(Long taskId, boolean success, long recordsProcessed, long duration) {
        SyncTaskStats stats = syncTaskStatsMap.get(taskId);
        if (stats != null) {
            stats.complete(success, recordsProcessed, duration);
            globalSyncStats.updateTaskStats(success, recordsProcessed, duration);
            log.debug("Completed sync task {}: success={}, records={}, duration={}ms", 
                    taskId, success, recordsProcessed, duration);
        }
    }

    /**
     * 记录同步错误
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    public void recordSyncError(Long taskId, String errorMessage) {
        SyncTaskStats stats = syncTaskStatsMap.get(taskId);
        if (stats != null) {
            stats.incrementErrorCount();
            globalSyncStats.incrementErrorCount();
            log.debug("Recorded error for sync task {}: {}", taskId, errorMessage);
        }
    }

    /**
     * 记录数据延迟
     * @param taskId 任务ID
     * @param delayMs 延迟（毫秒）
     */
    public void recordDataDelay(Long taskId, long delayMs) {
        SyncTaskStats stats = syncTaskStatsMap.get(taskId);
        if (stats != null) {
            stats.updateDataDelay(delayMs);
            globalSyncStats.updateDataDelay(delayMs);
            log.debug("Recorded data delay for task {}: {}ms", taskId, delayMs);
        }
    }

    /**
     * 记录Milvus写入
     * @param taskId 任务ID
     * @param count 写入数量
     * @param latencyMs 延迟（毫秒）
     * @param success 是否成功
     */
    public void recordMilvusWrite(Long taskId, int count, long latencyMs, boolean success) {
        SyncTaskStats stats = syncTaskStatsMap.get(taskId);
        if (stats != null) {
            stats.updateMilvusWrite(count, latencyMs, success);
            globalSyncStats.updateMilvusWrite(count, latencyMs, success);
            log.debug("Recorded Milvus write for task {}: count={}, latency={}ms, success={}", 
                    taskId, count, latencyMs, success);
        }
    }

    /**
     * 获取同步任务指标
     * @param taskId 任务ID
     * @return 同步任务指标
     */
    public Map<String, Object> getSyncTaskMetrics(Long taskId) {
        Map<String, Object> metrics = new HashMap<>();
        SyncTaskStats stats = syncTaskStatsMap.get(taskId);
        if (stats != null) {
            metrics.put("task_id", taskId);
            metrics.put("status", stats.getStatus());
            metrics.put("total_executions", stats.getTotalExecutions());
            metrics.put("success_count", stats.getSuccessCount());
            metrics.put("error_count", stats.getErrorCount());
            metrics.put("total_records", stats.getTotalRecords());
            metrics.put("avg_duration_ms", stats.getAvgDurationMs());
            metrics.put("avg_records_per_second", stats.getAvgRecordsPerSecond());
            metrics.put("avg_data_delay_ms", stats.getAvgDataDelayMs());
            metrics.put("milvus_write_qps", stats.getMilvusWriteQPS());
            metrics.put("milvus_write_success_rate", stats.getMilvusWriteSuccessRate());
            metrics.put("milvus_write_avg_latency_ms", stats.getMilvusWriteAvgLatencyMs());
        }
        return metrics;
    }

    /**
     * 获取全局同步指标
     * @return 全局同步指标
     */
    public Map<String, Object> getGlobalSyncMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("total_tasks", globalSyncStats.getTotalTasks());
        metrics.put("success_rate", globalSyncStats.getSuccessRate());
        metrics.put("total_records", globalSyncStats.getTotalRecords());
        metrics.put("avg_task_duration_ms", globalSyncStats.getAvgTaskDurationMs());
        metrics.put("avg_records_per_second", globalSyncStats.getAvgRecordsPerSecond());
        metrics.put("error_count", globalSyncStats.getErrorCount());
        metrics.put("error_rate", globalSyncStats.getErrorRate());
        metrics.put("avg_data_delay_ms", globalSyncStats.getAvgDataDelayMs());
        metrics.put("milvus_write_qps", globalSyncStats.getMilvusWriteQPS());
        metrics.put("milvus_write_success_rate", globalSyncStats.getMilvusWriteSuccessRate());
        metrics.put("milvus_write_avg_latency_ms", globalSyncStats.getMilvusWriteAvgLatencyMs());
        return metrics;
    }

    /**
     * 获取Milvus专项指标
     * @return Milvus专项指标
     */
    public Map<String, Object> getMilvusSpecialMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            MilvusClient milvusClient = MilvusUtils.createMilvusClient();
            try {
                // TODO: 实现具体的Milvus指标采集
                // 这里可以通过Milvus的API获取更详细的指标
                metrics.put("milvus.collection.count", 0);
                metrics.put("milvus.index.count", 0);
                metrics.put("milvus.vector.count", 0);
                metrics.put("milvus.query.latency", 0);
                metrics.put("milvus.insert.latency", 0);
                metrics.put("milvus.search.latency", 0);
                metrics.put("milvus.delete.latency", 0);
                metrics.put("milvus.flush.count", 0);
                metrics.put("milvus.compaction.count", 0);
            } finally {
                MilvusUtils.closeMilvusClient(milvusClient);
            }
        } catch (Exception e) {
            log.error("Failed to collect Milvus metrics: {}", e.getMessage(), e);
            metrics.put("milvus.error", e.getMessage());
        }
        
        return metrics;
    }

    /**
     * 同步任务统计类
     */
    private static class SyncTaskStats {
        private final AtomicLong totalExecutions = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong totalRecords = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicLong totalDataDelay = new AtomicLong(0);
        private final AtomicLong dataDelayCount = new AtomicLong(0);
        private final AtomicLong milvusWriteCount = new AtomicLong(0);
        private final AtomicLong milvusWriteSuccessCount = new AtomicLong(0);
        private final AtomicLong milvusWriteTotalLatency = new AtomicLong(0);
        private final AtomicLong milvusWriteTotalCount = new AtomicLong(0);
        private volatile boolean running = false;
        private volatile long startTime = 0;

        public void start() {
            running = true;
            startTime = System.currentTimeMillis();
            totalExecutions.incrementAndGet();
        }

        public void complete(boolean success, long recordsProcessed, long duration) {
            running = false;
            if (success) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
            totalRecords.addAndGet(recordsProcessed);
            totalDuration.addAndGet(duration);
        }

        public void incrementErrorCount() {
            errorCount.incrementAndGet();
        }

        public void updateDataDelay(long delayMs) {
            totalDataDelay.addAndGet(delayMs);
            dataDelayCount.incrementAndGet();
        }

        public void updateMilvusWrite(int count, long latencyMs, boolean success) {
            milvusWriteCount.incrementAndGet();
            if (success) {
                milvusWriteSuccessCount.incrementAndGet();
            }
            milvusWriteTotalLatency.addAndGet(latencyMs);
            milvusWriteTotalCount.addAndGet(count);
        }

        public String getStatus() {
            return running ? "RUNNING" : "COMPLETED";
        }

        public long getTotalExecutions() { return totalExecutions.get(); }
        public long getSuccessCount() { return successCount.get(); }
        public long getErrorCount() { return errorCount.get(); }
        public long getTotalRecords() { return totalRecords.get(); }
        public double getAvgDurationMs() {
            long executions = totalExecutions.get();
            return executions > 0 ? (double) totalDuration.get() / executions : 0;
        }
        public double getAvgRecordsPerSecond() {
            long duration = totalDuration.get();
            return duration > 0 ? (double) totalRecords.get() * 1000 / duration : 0;
        }
        public double getAvgDataDelayMs() {
            long count = dataDelayCount.get();
            return count > 0 ? (double) totalDataDelay.get() / count : 0;
        }
        public double getMilvusWriteQPS() {
            long duration = totalDuration.get();
            return duration > 0 ? (double) milvusWriteTotalCount.get() * 1000 / duration : 0;
        }
        public double getMilvusWriteSuccessRate() {
            long count = milvusWriteCount.get();
            return count > 0 ? (double) milvusWriteSuccessCount.get() / count * 100 : 100;
        }
        public double getMilvusWriteAvgLatencyMs() {
            long count = milvusWriteCount.get();
            return count > 0 ? (double) milvusWriteTotalLatency.get() / count : 0;
        }
    }

    /**
     * 全局同步统计类
     */
    private static class GlobalSyncStats {
        private final AtomicLong totalTasks = new AtomicLong(0);
        private final AtomicLong successTasks = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong totalRecords = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicLong totalDataDelay = new AtomicLong(0);
        private final AtomicLong dataDelayCount = new AtomicLong(0);
        private final AtomicLong milvusWriteCount = new AtomicLong(0);
        private final AtomicLong milvusWriteSuccessCount = new AtomicLong(0);
        private final AtomicLong milvusWriteTotalLatency = new AtomicLong(0);
        private final AtomicLong milvusWriteTotalCount = new AtomicLong(0);

        public void incrementTaskCount() {
            totalTasks.incrementAndGet();
        }

        public void updateTaskStats(boolean success, long recordsProcessed, long duration) {
            if (success) {
                successTasks.incrementAndGet();
            }
            totalRecords.addAndGet(recordsProcessed);
            totalDuration.addAndGet(duration);
        }

        public void incrementErrorCount() {
            errorCount.incrementAndGet();
        }

        public void updateDataDelay(long delayMs) {
            totalDataDelay.addAndGet(delayMs);
            dataDelayCount.incrementAndGet();
        }

        public void updateMilvusWrite(int count, long latencyMs, boolean success) {
            milvusWriteCount.incrementAndGet();
            if (success) {
                milvusWriteSuccessCount.incrementAndGet();
            }
            milvusWriteTotalLatency.addAndGet(latencyMs);
            milvusWriteTotalCount.addAndGet(count);
        }

        public long getTotalTasks() { return totalTasks.get(); }
        public double getSuccessRate() {
            long tasks = totalTasks.get();
            return tasks > 0 ? (double) successTasks.get() / tasks * 100 : 0;
        }
        public long getErrorCount() { return errorCount.get(); }
        public double getErrorRate() {
            long tasks = totalTasks.get();
            return tasks > 0 ? (double) errorCount.get() / tasks * 100 : 0;
        }
        public long getTotalRecords() { return totalRecords.get(); }
        public double getAvgTaskDurationMs() {
            long tasks = totalTasks.get();
            return tasks > 0 ? (double) totalDuration.get() / tasks : 0;
        }
        public double getAvgRecordsPerSecond() {
            long duration = totalDuration.get();
            return duration > 0 ? (double) totalRecords.get() * 1000 / duration : 0;
        }
        public double getAvgDataDelayMs() {
            long count = dataDelayCount.get();
            return count > 0 ? (double) totalDataDelay.get() / count : 0;
        }
        public double getMilvusWriteQPS() {
            long duration = totalDuration.get();
            return duration > 0 ? (double) milvusWriteTotalCount.get() * 1000 / duration : 0;
        }
        public double getMilvusWriteSuccessRate() {
            long count = milvusWriteCount.get();
            return count > 0 ? (double) milvusWriteSuccessCount.get() / count * 100 : 100;
        }
        public double getMilvusWriteAvgLatencyMs() {
            long count = milvusWriteCount.get();
            return count > 0 ? (double) milvusWriteTotalLatency.get() / count : 0;
        }
    }
}
