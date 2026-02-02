package com.data.rsync.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * 线程池管理器
 * 实现不同优先级任务的资源隔离
 */
@Component
@Slf4j
public class ThreadPoolManager {

    /**
     * 核心业务线程池（高优先级）
     */
    private final ExecutorService coreThreadPool;

    /**
     * 非核心业务线程池（中优先级）
     */
    private final ExecutorService nonCoreThreadPool;

    /**
     * 低优先级线程池
     */
    private final ExecutorService lowPriorityThreadPool;

    /**
     * 批量处理线程池
     */
    private final ExecutorService batchThreadPool;

    /**
     * 构造方法
     */
    public ThreadPoolManager() {
        // 核心业务线程池配置
        coreThreadPool = new ThreadPoolExecutor(
                10, // 核心线程数
                20, // 最大线程数
                60L, TimeUnit.SECONDS, // 线程存活时间
                new LinkedBlockingQueue<>(1000), // 阻塞队列
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );

        // 非核心业务线程池配置
        nonCoreThreadPool = new ThreadPoolExecutor(
                5, // 核心线程数
                10, // 最大线程数
                60L, TimeUnit.SECONDS, // 线程存活时间
                new LinkedBlockingQueue<>(500), // 阻塞队列
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );

        // 低优先级线程池配置
        lowPriorityThreadPool = new ThreadPoolExecutor(
                2, // 核心线程数
                5, // 最大线程数
                60L, TimeUnit.SECONDS, // 线程存活时间
                new LinkedBlockingQueue<>(200), // 阻塞队列
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );

        // 批量处理线程池配置
        batchThreadPool = new ThreadPoolExecutor(
                15, // 核心线程数
                30, // 最大线程数
                60L, TimeUnit.SECONDS, // 线程存活时间
                new LinkedBlockingQueue<>(2000), // 阻塞队列
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );

        log.info("ThreadPoolManager initialized successfully");
    }

    /**
     * 获取核心业务线程池
     * @return 核心业务线程池
     */
    public ExecutorService getCoreThreadPool() {
        return coreThreadPool;
    }

    /**
     * 获取非核心业务线程池
     * @return 非核心业务线程池
     */
    public ExecutorService getNonCoreThreadPool() {
        return nonCoreThreadPool;
    }

    /**
     * 获取低优先级线程池
     * @return 低优先级线程池
     */
    public ExecutorService getLowPriorityThreadPool() {
        return lowPriorityThreadPool;
    }

    /**
     * 获取批量处理线程池
     * @return 批量处理线程池
     */
    public ExecutorService getBatchThreadPool() {
        return batchThreadPool;
    }

    /**
     * 关闭所有线程池
     */
    public void shutdownAll() {
        log.info("Shutting down all thread pools");
        
        coreThreadPool.shutdown();
        nonCoreThreadPool.shutdown();
        lowPriorityThreadPool.shutdown();
        batchThreadPool.shutdown();

        try {
            if (!coreThreadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                coreThreadPool.shutdownNow();
            }
            if (!nonCoreThreadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                nonCoreThreadPool.shutdownNow();
            }
            if (!lowPriorityThreadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                lowPriorityThreadPool.shutdownNow();
            }
            if (!batchThreadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                batchThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while shutting down thread pools: {}", e.getMessage(), e);
            coreThreadPool.shutdownNow();
            nonCoreThreadPool.shutdownNow();
            lowPriorityThreadPool.shutdownNow();
            batchThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("All thread pools shut down successfully");
    }

    /**
     * 提交核心业务任务
     * @param task 任务
     * @return 任务执行结果
     */
    public Future<?> submitCoreTask(Runnable task) {
        return coreThreadPool.submit(task);
    }

    /**
     * 提交非核心业务任务
     * @param task 任务
     * @return 任务执行结果
     */
    public Future<?> submitNonCoreTask(Runnable task) {
        return nonCoreThreadPool.submit(task);
    }

    /**
     * 提交低优先级任务
     * @param task 任务
     * @return 任务执行结果
     */
    public Future<?> submitLowPriorityTask(Runnable task) {
        return lowPriorityThreadPool.submit(task);
    }

    /**
     * 提交批量处理任务
     * @param task 任务
     * @return 任务执行结果
     */
    public Future<?> submitBatchTask(Runnable task) {
        return batchThreadPool.submit(task);
    }

    /**
     * 获取线程池状态
     * @return 线程池状态
     */
    public String getThreadPoolStatus() {
        StringBuilder status = new StringBuilder();
        status.append("ThreadPoolManager status:\n");
        status.append("Core Thread Pool: ").append(getThreadPoolInfo(coreThreadPool)).append("\n");
        status.append("Non-Core Thread Pool: ").append(getThreadPoolInfo(nonCoreThreadPool)).append("\n");
        status.append("Low Priority Thread Pool: ").append(getThreadPoolInfo(lowPriorityThreadPool)).append("\n");
        status.append("Batch Thread Pool: ").append(getThreadPoolInfo(batchThreadPool)).append("\n");
        return status.toString();
    }

    /**
     * 获取线程池信息
     * @param executorService 线程池
     * @return 线程池信息
     */
    private String getThreadPoolInfo(ExecutorService executorService) {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor pool = (ThreadPoolExecutor) executorService;
            return String.format("coreSize=%d, maxSize=%d, activeCount=%d, queueSize=%d",
                    pool.getCorePoolSize(),
                    pool.getMaximumPoolSize(),
                    pool.getActiveCount(),
                    pool.getQueue().size());
        }
        return "Unknown"; 
    }
}
