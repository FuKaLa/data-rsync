package com.data.rsync.common.exception;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ValidationException extends DataRsyncException {

    public static final String ERROR_CODE = "VALIDATION_ERROR";

    public ValidationException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public ValidationException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }
}
