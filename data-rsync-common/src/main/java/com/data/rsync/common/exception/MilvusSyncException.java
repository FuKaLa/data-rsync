package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * Milvus同步异常类
 */
public class MilvusSyncException extends DataRsyncException {

    public MilvusSyncException(String message) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.MILVUS_SYNC_ERROR), message);
    }

    public MilvusSyncException(String message, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.MILVUS_SYNC_ERROR), message, cause);
    }

    public MilvusSyncException(String message, boolean retryable) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.MILVUS_SYNC_ERROR), message, retryable);
    }

    public MilvusSyncException(String message, boolean retryable, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.MILVUS_SYNC_ERROR), message, cause, retryable);
    }
}
