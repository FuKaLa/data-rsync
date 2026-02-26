package com.data.rsync.common.model;

import lombok.Data;
import lombok.ToString;

/**
 * 统一响应模型
 */
@Data
@ToString
public class Response<T> {

    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误详情
     */
    private String errorDetail;

    /**
     * 建议操作
     */
    private String suggestion;

    /**
     * 构造方法
     */
    public Response() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 构造方法
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     */
    public Response(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 构造方法
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     * @param errorCode 错误码
     * @param errorDetail 错误详情
     * @param suggestion 建议操作
     */
    public Response(int code, String message, T data, String errorCode, String errorDetail, String suggestion) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
        this.suggestion = suggestion;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应
     * @param data 数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Response<T> success(T data) {
        return new Response<>(200, "success", data);
    }

    /**
     * 成功响应
     * @param message 消息
     * @param data 数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Response<T> success(String message, T data) {
        return new Response<>(200, message, data);
    }

    /**
     * 失败响应
     * @param code 状态码
     * @param message 消息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Response<T> failure(int code, String message) {
        return new Response<>(code, message, null);
    }

    /**
     * 失败响应
     * @param message 消息
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Response<T> failure(String message) {
        return new Response<>(500, message, null);
    }

    /**
     * 失败响应
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Response<T> failure(int code, String message, T data) {
        return new Response<>(code, message, data);
    }

    /**
     * 失败响应
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     * @param errorCode 错误码
     * @param errorDetail 错误详情
     * @param suggestion 建议操作
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Response<T> failure(int code, String message, T data, String errorCode, String errorDetail, String suggestion) {
        return new Response<>(code, message, data, errorCode, errorDetail, suggestion);
    }

    /**
     * 失败响应
     * @param message 消息
     * @param errorCode 错误码
     * @param errorDetail 错误详情
     * @param suggestion 建议操作
     * @param <T> 数据类型
     * @return 响应对象
     */
    public static <T> Response<T> failure(String message, String errorCode, String errorDetail, String suggestion) {
        return new Response<>(500, message, null, errorCode, errorDetail, suggestion);
    }

}
