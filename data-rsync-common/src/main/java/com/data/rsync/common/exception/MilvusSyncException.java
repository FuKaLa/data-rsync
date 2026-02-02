package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * Milvus同步异常类
 */
public class MilvusSyncException extends DataRsyncException {

    public static final String ERROR_TYPE = "MILVUS_SYNC_ERROR";

    public MilvusSyncException(String message) {
        super(DataRsyncConstants.ErrorCode.MILVUS_SYNC_ERROR, message, ERROR_TYPE, false);
    }

    public MilvusSyncException(String message, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.MILVUS_SYNC_ERROR, message, ERROR_TYPE, false, cause);
    }

    public MilvusSyncException(String message, boolean retryable) {
        super(DataRsyncConstants.ErrorCode.MILVUS_SYNC_ERROR, message, ERROR_TYPE, retryable);
    }

    public MilvusSyncException(String message, boolean retryable, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.MILVUS_SYNC_ERROR, message, ERROR_TYPE, retryable, cause);
    }
}
