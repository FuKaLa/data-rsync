package com.data.rsync.common.exception;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class MilvusException extends DataRsyncException {

    public static final String ERROR_CODE = "MILVUS_ERROR";

    public MilvusException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public MilvusException(String errorMessage, boolean retryable) {
        super(ERROR_CODE, errorMessage, retryable);
    }

    public MilvusException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }

    public MilvusException(String errorMessage, Throwable cause, boolean retryable) {
        super(ERROR_CODE, errorMessage, cause, retryable);
    }
}
