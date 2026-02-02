package com.data.rsync.task.manager.entity;

import lombok.Data;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务实体类
 */
@Data
@ToString
@Entity
@Table(name = "task")
public class TaskEntity {

    /**
     * 任务ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务名称
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * 任务类型：FULL_SYNC, INCREMENTAL_SYNC, FULL_AND_INCREMENTAL
     */
    @Column(name = "type", nullable = false)
    private String type;

    /**
     * 数据源ID
     */
    @Column(name = "data_source_id", nullable = false)
    private Long dataSourceId;

    /**
     * 数据库名称
     */
    @Column(name = "database_name")
    private String databaseName;

    /**
     * 表名
     */
    @Column(name = "table_name")
    private String tableName;

    /**
     * 任务状态：PENDING, RUNNING, SUCCESS, FAILED, STOPPED
     */
    @Column(name = "status")
    private String status;

    /**
     * 任务配置（JSON格式）
     */
    @Column(name = "config", columnDefinition = "TEXT")
    private String config;

    /**
     * 任务进度（百分比）
     */
    @Column(name = "progress")
    private Integer progress;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 断点续传位点
     */
    @Column(name = "breakpoint", columnDefinition = "TEXT")
    private String breakpoint;

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
     * 启用状态
     */
    @Column(name = "enabled")
    private Boolean enabled;

    /**
     * 最后执行时间
     */
    @Column(name = "last_exec_time")
    private LocalDateTime lastExecTime;

    /**
     * 执行次数
     */
    @Column(name = "exec_count")
    private Integer execCount;

    /**
     * 下次执行时间
     */
    @Column(name = "next_exec_time")
    private LocalDateTime nextExecTime;

    /**
     * 并发度
     */
    @Column(name = "concurrency")
    private Integer concurrency;

    /**
     * 批量大小
     */
    @Column(name = "batch_size")
    private Integer batchSize;

    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount;

    /**
     * 超时时间（秒）
     */
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    /**
     * 调度类型：CRON, FIXED_RATE, FIXED_DELAY
     */
    @Column(name = "schedule_type")
    private String scheduleType;

    /**
     * 调度表达式（CRON表达式或时间间隔）
     */
    @Column(name = "schedule_expression")
    private String scheduleExpression;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

    /**
     * 构造方法
     */
    public TaskEntity() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "PENDING";
        this.enabled = true;
        this.execCount = 0;
        this.concurrency = 1;
        this.batchSize = 1000;
        this.retryCount = 3;
        this.timeoutSeconds = 3600;
    }

    /**
     * 预更新方法
     */
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

}
