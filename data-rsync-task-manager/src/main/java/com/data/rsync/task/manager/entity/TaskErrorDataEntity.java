package com.data.rsync.task.manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 任务错误数据实体类
 */
@Data
@ToString
@Entity
@Table(name = "task_error_data")
public class TaskErrorDataEntity {

    /**
     * 错误数据ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * 源数据（JSON格式）
     */
    @Column(name = "source_data", columnDefinition = "TEXT", nullable = false)
    private String sourceData;

    /**
     * 错误原因
     */
    @Column(name = "error_message", columnDefinition = "TEXT", nullable = false)
    private String errorMessage;

    /**
     * 错误类型
     */
    @Column(name = "error_type", nullable = false)
    private String errorType;

    /**
     * 同步环节
     */
    @Column(name = "sync_stage", nullable = false)
    private String syncStage;

    /**
     * 错误时间
     */
    @Column(name = "error_time", nullable = false)
    private LocalDateTime errorTime;

    /**
     * 处理状态：PENDING, PROCESSING, SUCCESS, FAILED
     */
    @Column(name = "process_status", nullable = false)
    private String processStatus;

    /**
     * 重试次数
     */
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    /**
     * 最后重试时间
     */
    @Column(name = "last_retry_time")
    private LocalDateTime lastRetryTime;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 初始化创建时间和更新时间
     */
    @PrePersist
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
    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

}
