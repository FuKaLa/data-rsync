package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * 日志监听异常类
 */
public class LogListenerException extends DataRsyncException {

    public LogListenerException(String message) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.LOG_LISTENER_ERROR), message);
    }

    public LogListenerException(String message, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.LOG_LISTENER_ERROR), message, cause);
    }

    public LogListenerException(String message, boolean retryable) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.LOG_LISTENER_ERROR), message, retryable);
    }

    public LogListenerException(String message, boolean retryable, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.LOG_LISTENER_ERROR), message, cause, retryable);
    }
}
