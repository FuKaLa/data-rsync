package com.data.rsync.task.manager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 任务错误数据实体类
 */
@Data
@ToString
@TableName("task_error_data")
public class TaskErrorDataEntity {

    /**
     * 错误数据ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField(value = "task_id", exist = true)
    private Long taskId;

    /**
     * 源数据（JSON格式）
     */
    @TableField(value = "source_data", exist = true)
    private String sourceData;

    /**
     * 错误原因
     */
    @TableField(value = "error_message", exist = true)
    private String errorMessage;

    /**
     * 错误类型
     */
    @TableField(value = "error_type", exist = true)
    private String errorType;

    /**
     * 同步环节
     */
    @TableField(value = "sync_stage", exist = true)
    private String syncStage;

    /**
     * 错误时间
     */
    @TableField(value = "error_time", exist = true)
    private LocalDateTime errorTime;

    /**
     * 处理状态：PENDING, PROCESSING, SUCCESS, FAILED
     */
    @TableField(value = "process_status", exist = true)
    private String processStatus;

    /**
     * 重试次数
     */
    @TableField(value = "retry_count", exist = true)
    private Integer retryCount;

    /**
     * 最后重试时间
     */
    @TableField(value = "last_retry_time", exist = true)
    private LocalDateTime lastRetryTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", exist = true)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", exist = true)
    private LocalDateTime updateTime;

    /**
     * 初始化创建时间和更新时间
     */
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        if (updateTime == null) {
            updateTime = now;
        }
        if (errorTime == null) {
            errorTime = now;
        }
        if (processStatus == null) {
            processStatus = "PENDING";
        }
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    /**
     * 更新时更新时间
     */
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

}
