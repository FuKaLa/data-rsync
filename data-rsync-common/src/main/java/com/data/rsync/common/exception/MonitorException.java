package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * 监控异常类
 */
public class MonitorException extends DataRsyncException {

    public static final String ERROR_TYPE = "MONITOR_ERROR";

    public MonitorException(String message) {
        super(DataRsyncConstants.ErrorCode.MONITOR_ERROR, message, ERROR_TYPE, false);
    }

    public MonitorException(String message, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.MONITOR_ERROR, message, ERROR_TYPE, false, cause);
    }

    public MonitorException(String message, boolean retryable) {
        super(DataRsyncConstants.ErrorCode.MONITOR_ERROR, message, ERROR_TYPE, retryable);
    }

    public MonitorException(String message, boolean retryable, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.MONITOR_ERROR, message, ERROR_TYPE, retryable, cause);
    }
}
