package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * 监控异常类
 */
public class MonitorException extends DataRsyncException {

    public MonitorException(String message) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.MONITOR_ERROR), message);
    }

    public MonitorException(String message, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.MONITOR_ERROR), message, cause);
    }

    public MonitorException(String message, boolean retryable) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.MONITOR_ERROR), message, retryable);
    }

    public MonitorException(String message, boolean retryable, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.MONITOR_ERROR), message, cause, retryable);
    }
}
