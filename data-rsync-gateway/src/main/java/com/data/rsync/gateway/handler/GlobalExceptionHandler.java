package com.data.rsync.gateway.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 处理网关层面的异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理所有异常
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleException(Exception e, ServerWebExchange exchange) {
        logger.error("Gateway exception: {}", e.getMessage(), e);

        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("message", "Gateway internal error");
        errorResponse.put("path", exchange.getRequest().getURI().getPath());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    /**
     * 处理超时异常
     */
    @ExceptionHandler(TimeoutException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleTimeoutException(TimeoutException e, ServerWebExchange exchange) {
        logger.warn("Gateway timeout: {}", e.getMessage());

        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", HttpStatus.GATEWAY_TIMEOUT.value());
        errorResponse.put("message", "Service timeout");
        errorResponse.put("path", exchange.getRequest().getURI().getPath());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return Mono.just(ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse));
    }

    /**
     * 处理服务不可用异常
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleServiceUnavailableException(ServiceUnavailableException e, ServerWebExchange exchange) {
        logger.warn("Service unavailable: {}", e.getMessage());

        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", HttpStatus.SERVICE_UNAVAILABLE.value());
        errorResponse.put("message", "Service unavailable");
        errorResponse.put("path", exchange.getRequest().getURI().getPath());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse));
    }

    /**
     * 自定义超时异常类
     */
    public static class TimeoutException extends RuntimeException {
        public TimeoutException(String message) {
            super(message);
        }
    }

    /**
     * 自定义服务不可用异常类
     */
    public static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) {
            super(message);
        }
    }
}
