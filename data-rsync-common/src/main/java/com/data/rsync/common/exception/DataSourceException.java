package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * 数据源异常类
 */
public class DataSourceException extends DataRsyncException {

    public DataSourceException(String message) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.DATA_SOURCE_ERROR), message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.DATA_SOURCE_ERROR), message, cause);
    }

    public DataSourceException(String message, boolean retryable) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.DATA_SOURCE_ERROR), message, retryable);
    }

    public DataSourceException(String message, boolean retryable, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.DATA_SOURCE_ERROR), message, cause, retryable);
    }
}
