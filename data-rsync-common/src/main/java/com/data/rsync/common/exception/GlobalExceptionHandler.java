package com.data.rsync.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataRsyncException.class)
    public ResponseEntity<Map<String, Object>> handleDataRsyncException(DataRsyncException e) {
        log.error("DataRsyncException occurred: [{}] {}", e.getErrorCode(), e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", e.getErrorCode());
        response.put("message", e.getMessage());
        response.put("retryable", e.isRetryable());
        
        HttpStatus status = getHttpStatus(e.getErrorCode());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation exception occurred: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", "VALIDATION_ERROR");
        response.put("message", "参数验证失败");
        response.put("retryable", false);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", "SYSTEM_ERROR");
        response.put("message", "系统内部错误");
        response.put("retryable", false);
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus getHttpStatus(String errorCode) {
        switch (errorCode) {
            case "VALIDATION_ERROR":
                return HttpStatus.BAD_REQUEST;
            case "DATABASE_ERROR":
            case "MILVUS_ERROR":
            case "KAFKA_ERROR":
                return HttpStatus.SERVICE_UNAVAILABLE;
            case "TASK_ERROR":
                return HttpStatus.CONFLICT;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
