package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * 数据源异常类
 */
public class DataSourceException extends DataRsyncException {

    public static final String ERROR_TYPE = "DATA_SOURCE_ERROR";

    public DataSourceException(String message) {
        super(DataRsyncConstants.ErrorCode.DATA_SOURCE_ERROR, message, ERROR_TYPE, false);
    }

    public DataSourceException(String message, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.DATA_SOURCE_ERROR, message, ERROR_TYPE, false, cause);
    }

    public DataSourceException(String message, boolean retryable) {
        super(DataRsyncConstants.ErrorCode.DATA_SOURCE_ERROR, message, ERROR_TYPE, retryable);
    }

    public DataSourceException(String message, boolean retryable, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.DATA_SOURCE_ERROR, message, ERROR_TYPE, retryable, cause);
    }
}
