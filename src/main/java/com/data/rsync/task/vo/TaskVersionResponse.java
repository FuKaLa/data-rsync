package com.data.rsync.task.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务版本响应对象
 */
@Data
public class TaskVersionResponse {
    private Long versionId;
    private Long taskId;
    private String versionName;
    private String description;
    private LocalDateTime createTime;
    private String createdBy;
    private String status;
}
