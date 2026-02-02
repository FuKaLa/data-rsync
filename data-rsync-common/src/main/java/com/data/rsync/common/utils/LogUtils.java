package com.data.rsync.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * 日志工具类
 */
@Slf4j
public class LogUtils {

    public static final String TRACE_ID = "traceId";
    public static final String TASK_ID = "taskId";
    public static final String DATA_SOURCE_ID = "dataSourceId";
    public static final String OPERATOR = "operator";

    /**
     * 生成并设置链路ID
     * @return 链路ID
     */
    public static String setTraceId() {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID, traceId);
        return traceId;
    }

    /**
     * 设置链路ID
     * @param traceId 链路ID
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    /**
     * 设置任务ID
     * @param taskId 任务ID
     */
    public static void setTaskId(Long taskId) {
        if (taskId != null) {
            MDC.put(TASK_ID, taskId.toString());
        }
    }

    /**
     * 设置数据源ID
     * @param dataSourceId 数据源ID
     */
    public static void setDataSourceId(Long dataSourceId) {
        if (dataSourceId != null) {
            MDC.put(DATA_SOURCE_ID, dataSourceId.toString());
        }
    }

    /**
     * 设置操作人
     * @param operator 操作人
     */
    public static void setOperator(String operator) {
        if (operator != null) {
            MDC.put(OPERATOR, operator);
        }
    }

    /**
     * 清除所有MDC值
     */
    public static void clearMdc() {
        MDC.clear();
    }

    /**
     * 清除指定的MDC值
     * @param key 键
     */
    public static void removeMdc(String key) {
        MDC.remove(key);
    }

    /**
     * 获取链路ID
     * @return 链路ID
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 打印调试日志
     * @param message 消息
     * @param args 参数
     */
    public static void debug(String message, Object... args) {
        log.debug(message, args);
    }

    /**
     * 打印信息日志
     * @param message 消息
     * @param args 参数
     */
    public static void info(String message, Object... args) {
        log.info(message, args);
    }

    /**
     * 打印警告日志
     * @param message 消息
     * @param args 参数
     */
    public static void warn(String message, Object... args) {
        log.warn(message, args);
    }

    /**
     * 打印错误日志
     * @param message 消息
     * @param args 参数
     */
    public static void error(String message, Object... args) {
        log.error(message, args);
    }

    /**
     * 打印错误日志（带异常）
     * @param message 消息
     * @param e 异常
     * @param args 参数
     */
    public static void error(String message, Throwable e, Object... args) {
        log.error(message, args, e);
    }

    /**
     * 打印调试日志（带上下文）
     * @param context 上下文信息
     * @param message 消息
     * @param args 参数
     */
    public static void debugWithContext(String context, String message, Object... args) {
        log.debug("[{}] {}", context, message, args);
    }

    /**
     * 打印信息日志（带上下文）
     * @param context 上下文信息
     * @param message 消息
     * @param args 参数
     */
    public static void infoWithContext(String context, String message, Object... args) {
        log.info("[{}] {}", context, message, args);
    }

    /**
     * 打印警告日志（带上下文）
     * @param context 上下文信息
     * @param message 消息
     * @param args 参数
     */
    public static void warnWithContext(String context, String message, Object... args) {
        log.warn("[{}] {}", context, message, args);
    }

    /**
     * 打印错误日志（带上下文）
     * @param context 上下文信息
     * @param message 消息
     * @param args 参数
     */
    public static void errorWithContext(String context, String message, Object... args) {
        log.error("[{}] {}", context, message, args);
    }

    /**
     * 打印错误日志（带上下文和异常）
     * @param context 上下文信息
     * @param message 消息
     * @param e 异常
     * @param args 参数
     */
    public static void errorWithContext(String context, String message, Throwable e, Object... args) {
        log.error("[{}] {}", context, message, args, e);
    }
}
