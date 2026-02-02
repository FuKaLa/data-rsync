package com.data.rsync.common.exception;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class KafkaException extends DataRsyncException {

    public static final String ERROR_CODE = "KAFKA_ERROR";

    public KafkaException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public KafkaException(String errorMessage, boolean retryable) {
        super(ERROR_CODE, errorMessage, retryable);
    }

    public KafkaException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }

    public KafkaException(String errorMessage, Throwable cause, boolean retryable) {
        super(ERROR_CODE, errorMessage, cause, retryable);
    }
}
