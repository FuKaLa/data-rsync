package com.data.rsync.task.vo;

import lombok.Data;

/**
 * 任务触发响应对象
 */
@Data
public class TaskTriggerResponse {
    private boolean success;
    private String taskId;
    private String message;
    private String executionId;
}
