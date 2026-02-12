package com.data.rsync.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * 日志工具类
 * 
 * 日志级别使用规范：
 * 1. ERROR: 系统级错误，需要立即关注和处理的问题
 *    - 数据库连接失败
 *    - 服务启动失败
 *    - 关键业务流程异常
 *    - 外部系统调用失败
 *    - 包含异常堆栈的错误信息
 * 
 * 2. WARN: 警告信息，表示可能存在的问题，但不会立即影响系统运行
 *    - 参数验证失败
 *    - 配置项缺失使用默认值
 *    - 业务逻辑警告
 *    - 非关键操作失败
 * 
 * 3. INFO: 信息性消息，记录系统运行状态和关键业务操作
 *    - 服务启动/停止
 *    - 关键配置加载
 *    - 业务流程开始/结束
 *    - 定时任务执行
 *    - 重要的状态变更
 * 
 * 4. DEBUG: 调试信息，用于开发和故障排查
 *    - 详细的方法调用参数
 *    - 内部状态变更
 *    - 性能指标
 *    - 临时的调试信息
 * 
 * 5. TRACE: 最详细的日志，仅用于深度调试
 *    - 方法调用进入/退出
 *    - 循环迭代细节
 *    - 详细的网络请求/响应
 */
public class LogUtils {

    private static final Logger log = LoggerFactory.getLogger(LogUtils.class);

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
