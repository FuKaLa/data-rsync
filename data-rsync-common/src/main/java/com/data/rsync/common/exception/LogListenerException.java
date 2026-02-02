package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * 日志监听异常类
 */
public class LogListenerException extends DataRsyncException {

    public static final String ERROR_TYPE = "LOG_LISTENER_ERROR";

    public LogListenerException(String message) {
        super(DataRsyncConstants.ErrorCode.LOG_LISTENER_ERROR, message, ERROR_TYPE, false);
    }

    public LogListenerException(String message, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.LOG_LISTENER_ERROR, message, ERROR_TYPE, false, cause);
    }

    public LogListenerException(String message, boolean retryable) {
        super(DataRsyncConstants.ErrorCode.LOG_LISTENER_ERROR, message, ERROR_TYPE, retryable);
    }

    public LogListenerException(String message, boolean retryable, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.LOG_LISTENER_ERROR, message, ERROR_TYPE, retryable, cause);
    }
}
