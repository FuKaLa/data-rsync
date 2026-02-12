package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * 数据处理异常类
 */
public class DataProcessException extends DataRsyncException {

    public DataProcessException(String message) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.DATA_PROCESS_ERROR), message);
    }

    public DataProcessException(String message, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.DATA_PROCESS_ERROR), message, cause);
    }

    public DataProcessException(String message, boolean retryable) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.DATA_PROCESS_ERROR), message, retryable);
    }

    public DataProcessException(String message, boolean retryable, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.DATA_PROCESS_ERROR), message, cause, retryable);
    }
}
