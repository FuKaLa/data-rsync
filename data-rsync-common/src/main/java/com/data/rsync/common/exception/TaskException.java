package com.data.rsync.common.exception;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class TaskException extends DataRsyncException {

    public static final String ERROR_CODE = "TASK_ERROR";

    public TaskException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public TaskException(String errorMessage, boolean retryable) {
        super(ERROR_CODE, errorMessage, retryable);
    }

    public TaskException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }

    public TaskException(String errorMessage, Throwable cause, boolean retryable) {
        super(ERROR_CODE, errorMessage, cause, retryable);
    }
}
