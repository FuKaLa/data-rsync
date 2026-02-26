package com.data.rsync.common.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程池工具类，统一管理线程池的初始化和销毁
 */
public class ThreadPoolUtil {

    /**
     * 创建线程池
     * @param corePoolSize 核心线程数
     * @param maxPoolSize 最大线程数
     * @param keepAliveSeconds 线程保持时间
     * @param queueCapacity 队列容量
     * @param threadNamePrefix 线程名称前缀
     * @return 线程池
     */
    public static ExecutorService createThreadPool(int corePoolSize, int maxPoolSize, int keepAliveSeconds, int queueCapacity, String threadNamePrefix) {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new NamedThreadFactory(threadNamePrefix),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 关闭线程池
     * @param executorService 线程池
     */
    public static void shutdownExecutorService(ExecutorService executorService) {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 命名线程工厂
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicLong counter = new AtomicLong(0);

        public NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(prefix + "-" + counter.incrementAndGet());
            return thread;
        }
    }
}
