package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * 数据处理异常类
 */
public class DataProcessException extends DataRsyncException {

    public static final String ERROR_TYPE = "DATA_PROCESS_ERROR";

    public DataProcessException(String message) {
        super(DataRsyncConstants.ErrorCode.DATA_PROCESS_ERROR, message, ERROR_TYPE, false);
    }

    public DataProcessException(String message, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.DATA_PROCESS_ERROR, message, ERROR_TYPE, false, cause);
    }

    public DataProcessException(String message, boolean retryable) {
        super(DataRsyncConstants.ErrorCode.DATA_PROCESS_ERROR, message, ERROR_TYPE, retryable);
    }

    public DataProcessException(String message, boolean retryable, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.DATA_PROCESS_ERROR, message, ERROR_TYPE, retryable, cause);
    }
}
