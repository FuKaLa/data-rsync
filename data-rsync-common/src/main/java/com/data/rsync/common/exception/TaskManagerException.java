package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * 任务管理异常类
 */
public class TaskManagerException extends DataRsyncException {

    public static final String ERROR_TYPE = "TASK_MANAGER_ERROR";

    public TaskManagerException(String message) {
        super(DataRsyncConstants.ErrorCode.TASK_MANAGER_ERROR, message, ERROR_TYPE, false);
    }

    public TaskManagerException(String message, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.TASK_MANAGER_ERROR, message, ERROR_TYPE, false, cause);
    }

    public TaskManagerException(String message, boolean retryable) {
        super(DataRsyncConstants.ErrorCode.TASK_MANAGER_ERROR, message, ERROR_TYPE, retryable);
    }

    public TaskManagerException(String message, boolean retryable, Throwable cause) {
        super(DataRsyncConstants.ErrorCode.TASK_MANAGER_ERROR, message, ERROR_TYPE, retryable, cause);
    }
}
