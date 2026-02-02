package com.data.rsync.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DataRsyncException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;
    private final boolean retryable;

    public DataRsyncException(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, false);
    }

    public DataRsyncException(String errorCode, String errorMessage, boolean retryable) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.retryable = retryable;
    }

    public DataRsyncException(String errorCode, String errorMessage, Throwable cause) {
        this(errorCode, errorMessage, cause, false);
    }

    public DataRsyncException(String errorCode, String errorMessage, Throwable cause, boolean retryable) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.retryable = retryable;
    }
}
