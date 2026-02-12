package com.data.rsync.common.exception;

import com.data.rsync.common.constants.DataRsyncConstants;

/**
 * 任务管理异常类
 */
public class TaskManagerException extends DataRsyncException {

    public TaskManagerException(String message) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.TASK_MANAGER_ERROR), message);
    }

    public TaskManagerException(String message, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.TASK_MANAGER_ERROR), message, cause);
    }

    public TaskManagerException(String message, boolean retryable) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.TASK_MANAGER_ERROR), message, retryable);
    }

    public TaskManagerException(String message, boolean retryable, Throwable cause) {
        super(Integer.toString(DataRsyncConstants.ErrorCode.TASK_MANAGER_ERROR), message, cause, retryable);
    }
}
