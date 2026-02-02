package com.data.rsync.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * 内存管理器
 * 监控和管理内存使用，防止OOM异常
 */
@Component
@Slf4j
public class MemoryManager {

    private static final MemoryManager INSTANCE = new MemoryManager();
    private final MemoryMXBean memoryMXBean;

    // 内存阈值配置
    private static final double HEAP_MEMORY_THRESHOLD = 0.8; // 堆内存使用阈值
    private static final double NON_HEAP_MEMORY_THRESHOLD = 0.85; // 非堆内存使用阈值
    private static final long BATCH_PROCESSING_MEMORY_LIMIT = 512 * 1024 * 1024; // 批量处理内存限制（512MB）

    private MemoryManager() {
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        log.info("MemoryManager initialized");
    }

    /**
     * 获取内存管理器实例
     * @return 内存管理器实例
     */
    public static MemoryManager getInstance() {
        return INSTANCE;
    }

    /**
     * 检查内存使用情况
     * @return 是否内存充足
     */
    public boolean checkMemoryUsage() {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        double heapUsageRatio = (double) heapMemoryUsage.getUsed() / heapMemoryUsage.getMax();
        double nonHeapUsageRatio = (double) nonHeapMemoryUsage.getUsed() / nonHeapMemoryUsage.getMax();

        boolean heapMemorySufficient = heapUsageRatio < HEAP_MEMORY_THRESHOLD;
        boolean nonHeapMemorySufficient = nonHeapUsageRatio < NON_HEAP_MEMORY_THRESHOLD;

        if (!heapMemorySufficient) {
            log.warn("Heap memory usage high: {}/{} ({}%)", 
                    formatMemory(heapMemoryUsage.getUsed()), 
                    formatMemory(heapMemoryUsage.getMax()), 
                    (int) (heapUsageRatio * 100));
        }

        if (!nonHeapMemorySufficient) {
            log.warn("Non-heap memory usage high: {}/{} ({}%)", 
                    formatMemory(nonHeapMemoryUsage.getUsed()), 
                    formatMemory(nonHeapMemoryUsage.getMax()), 
                    (int) (nonHeapUsageRatio * 100));
        }

        return heapMemorySufficient && nonHeapMemorySufficient;
    }

    /**
     * 检查批量处理内存是否充足
     * @param estimatedSize 估计处理数据大小
     * @return 是否内存充足
     */
    public boolean checkBatchProcessingMemory(long estimatedSize) {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long availableHeapMemory = heapMemoryUsage.getMax() - heapMemoryUsage.getUsed();

        boolean memorySufficient = availableHeapMemory > estimatedSize && estimatedSize < BATCH_PROCESSING_MEMORY_LIMIT;

        if (!memorySufficient) {
            log.warn("Batch processing memory insufficient: available={}, estimated={}, limit={}", 
                    formatMemory(availableHeapMemory), 
                    formatMemory(estimatedSize), 
                    formatMemory(BATCH_PROCESSING_MEMORY_LIMIT));
        }

        return memorySufficient;
    }

    /**
     * 计算批量处理的合理批次大小
     * @param itemSize 单个数据项大小
     * @param maxItems 最大数据项数
     * @return 合理的批次大小
     */
    public int calculateBatchSize(long itemSize, int maxItems) {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        long availableHeapMemory = heapMemoryUsage.getMax() - heapMemoryUsage.getUsed();

        // 预留50%的可用内存
        long usableMemory = availableHeapMemory / 2;
        int batchSize = (int) (usableMemory / itemSize);

        // 确保批次大小在合理范围内
        batchSize = Math.max(1, Math.min(batchSize, maxItems));
        batchSize = Math.min(batchSize, 1000); // 最大批次大小限制

        log.debug("Calculated batch size: {} (itemSize={}, availableMemory={}, usableMemory={})", 
                batchSize, formatMemory(itemSize), formatMemory(availableHeapMemory), formatMemory(usableMemory));

        return batchSize;
    }

    /**
     * 触发垃圾回收
     */
    public void triggerGC() {
        log.info("Triggering garbage collection");
        System.gc();
        log.info("Garbage collection completed");
    }

    /**
     * 获取内存使用状态
     * @return 内存使用状态
     */
    public String getMemoryStatus() {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        StringBuilder status = new StringBuilder();
        status.append("Memory usage status:\n");
        status.append("Heap memory: ").append(formatMemory(heapMemoryUsage.getUsed())).append("/")
                .append(formatMemory(heapMemoryUsage.getMax())).append(" (").append((int) ((double) heapMemoryUsage.getUsed() / heapMemoryUsage.getMax() * 100))
                .append("%)\n");
        status.append("Non-heap memory: ").append(formatMemory(nonHeapMemoryUsage.getUsed())).append("/")
                .append(formatMemory(nonHeapMemoryUsage.getMax())).append(" (").append((int) ((double) nonHeapMemoryUsage.getUsed() / nonHeapMemoryUsage.getMax() * 100))
                .append("%)\n");
        status.append("Memory pools: \n");
        ManagementFactory.getMemoryPoolMXBeans().forEach(pool -> {
            MemoryUsage usage = pool.getUsage();
            status.append("  - ").append(pool.getName()).append(": ").append(formatMemory(usage.getUsed())).append("/")
                    .append(formatMemory(usage.getMax())).append("\n");
        });

        return status.toString();
    }

    /**
     * 格式化内存大小
     * @param bytes 字节数
     * @return 格式化后的内存大小
     */
    private String formatMemory(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return (bytes / (1024 * 1024)) + " MB";
        } else {
            return (bytes / (1024 * 1024 * 1024)) + " GB";
        }
    }

    /**
     * 监控内存使用并在必要时采取措施
     */
    public void monitorAndAct() {
        if (!checkMemoryUsage()) {
            // 内存使用过高，触发垃圾回收
            triggerGC();
            
            // 再次检查
            if (!checkMemoryUsage()) {
                log.error("Memory usage still high after GC: {}", getMemoryStatus());
                // 这里可以添加更复杂的处理逻辑，如暂停非核心任务等
            }
        }
    }

    /**
     * 获取堆内存使用百分比
     * @return 堆内存使用百分比
     */
    public double getHeapMemoryUsagePercentage() {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        return (double) heapMemoryUsage.getUsed() / heapMemoryUsage.getMax();
    }

    /**
     * 获取非堆内存使用百分比
     * @return 非堆内存使用百分比
     */
    public double getNonHeapMemoryUsagePercentage() {
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        return (double) nonHeapMemoryUsage.getUsed() / nonHeapMemoryUsage.getMax();
    }

    /**
     * 检查是否需要暂停同步任务
     * @return 是否需要暂停
     */
    public boolean needPauseSyncTasks() {
        return getHeapMemoryUsagePercentage() > HEAP_MEMORY_THRESHOLD;
    }
}
