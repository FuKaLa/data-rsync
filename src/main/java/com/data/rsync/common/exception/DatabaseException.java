package com.data.rsync.common.exception;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class DatabaseException extends DataRsyncException {

    public static final String ERROR_CODE = "DATABASE_ERROR";

    public DatabaseException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public DatabaseException(String errorMessage, boolean retryable) {
        super(ERROR_CODE, errorMessage, retryable);
    }

    public DatabaseException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }

    public DatabaseException(String errorMessage, Throwable cause, boolean retryable) {
        super(ERROR_CODE, errorMessage, cause, retryable);
    }
}
