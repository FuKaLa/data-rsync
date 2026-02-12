package com.data.rsync.common.utils;

import com.data.rsync.common.exception.DataRsyncException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class ExceptionUtils {

    private static final Logger log = LoggerFactory.getLogger(ExceptionUtils.class);

    /**
     * 带重试机制的方法执行
     * @param supplier 执行的方法
     * @param maxRetries 最大重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @param <T> 返回类型
     * @return 执行结果
     * @throws Exception 异常
     */
    public static <T> T executeWithRetry(Supplier<T> supplier, int maxRetries, long retryInterval) throws Exception {
        int retries = 0;
        while (true) {
            try {
                return supplier.get();
            } catch (DataRsyncException e) {
                if (e.isRetryable() && retries < maxRetries) {
                    retries++;
                    log.warn("Operation failed, retrying {} of {}: {}", retries, maxRetries, e.getMessage());
                    Thread.sleep(retryInterval);
                } else {
                    throw e;
                }
            } catch (Exception e) {
                // 非自定义异常，默认不重试
                throw e;
            }
        }
    }

    /**
     * 带重试机制的方法执行（无返回值）
     * @param runnable 执行的方法
     * @param maxRetries 最大重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @throws Exception 异常
     */
    public static void executeWithRetry(Runnable runnable, int maxRetries, long retryInterval) throws Exception {
        executeWithRetry(() -> {
            runnable.run();
            return null;
        }, maxRetries, retryInterval);
    }

    /**
     * 带兜底逻辑的方法执行
     * @param supplier 执行的方法
     * @param fallback 兜底逻辑
     * @param <T> 返回类型
     * @return 执行结果或兜底结果
     */
    public static <T> T executeWithFallback(Supplier<T> supplier, Supplier<T> fallback) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("Operation failed, using fallback: {}", e.getMessage());
            return fallback.get();
        }
    }

    /**
     * 带兜底逻辑的方法执行（无返回值）
     * @param runnable 执行的方法
     * @param fallback 兜底逻辑
     */
    public static void executeWithFallback(Runnable runnable, Runnable fallback) {
        try {
            runnable.run();
        } catch (Exception e) {
            log.warn("Operation failed, using fallback: {}", e.getMessage());
            fallback.run();
        }
    }
}
