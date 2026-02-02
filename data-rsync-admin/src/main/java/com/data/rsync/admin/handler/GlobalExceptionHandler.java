package com.data.rsync.admin.handler;

import com.data.rsync.common.exception.DataRsyncException;
import com.data.rsync.common.model.Response;
import com.data.rsync.common.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理数据同步系统自定义异常
     * @param e 自定义异常
     * @return 统一响应
     */
    @ExceptionHandler(DataRsyncException.class)
    public Response<?> handleDataRsyncException(DataRsyncException e) {
        log.error("DataRsyncException: [{}] {}", e.getErrorType(), e.getErrorMessage(), e);
        return Response.error(e.getErrorCode(), e.getErrorMessage());
    }

    /**
     * 处理参数验证异常
     * @param e 参数验证异常
     * @return 统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("MethodArgumentNotValidException: {}", errorMessage);
        return Response.error(400, errorMessage);
    }

    /**
     * 处理系统异常
     * @param e 系统异常
     * @return 统一响应
     */
    @ExceptionHandler(Exception.class)
    public Response<?> handleException(Exception e) {
        String errorMessage = ExceptionUtils.handleException(e);
        log.error("System Exception", e);
        return Response.error(500, errorMessage);
    }
}
