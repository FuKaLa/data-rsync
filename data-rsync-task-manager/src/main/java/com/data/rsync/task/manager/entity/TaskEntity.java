package com.data.rsync.task.manager.entity;

import jakarta.validation.constraints.*;
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
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * 任务类型：FULL_SYNC, INCREMENTAL_SYNC, FULL_AND_INCREMENTAL
     */
    @NotBlank(message = "任务类型不能为空")
    @Pattern(regexp = "^(FULL_SYNC|INCREMENTAL_SYNC|FULL_AND_INCREMENTAL)$", message = "任务类型必须是 FULL_SYNC, INCREMENTAL_SYNC 或 FULL_AND_INCREMENTAL")
    @Column(name = "type", nullable = false)
    private String type;

    /**
     * 数据源ID
     */
    @NotNull(message = "数据源ID不能为空")
    @Positive(message = "数据源ID必须大于0")
    @Column(name = "data_source_id", nullable = false)
    private Long dataSourceId;

    /**
     * 数据库名称
     */
    @Size(max = 100, message = "数据库名称长度不能超过100个字符")
    @Column(name = "database_name")
    private String databaseName;

    /**
     * 表名
     */
    @Size(max = 100, message = "表名长度不能超过100个字符")
    @Column(name = "table_name")
    private String tableName;

    /**
     * 任务状态：PENDING, RUNNING, SUCCESS, FAILED, STOPPED
     */
    @Pattern(regexp = "^(PENDING|RUNNING|SUCCESS|FAILED|STOPPED|PAUSED|ROLLED_BACK)$", message = "任务状态必须是 PENDING, RUNNING, SUCCESS, FAILED, STOPPED, PAUSED 或 ROLLED_BACK")
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
    @Min(value = 0, message = "任务进度不能小于0")
    @Max(value = 100, message = "任务进度不能大于100")
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
    @Min(value = 0, message = "执行次数不能小于0")
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
    @Min(value = 1, message = "并发度不能小于1")
    @Max(value = 100, message = "并发度不能大于100")
    @Column(name = "concurrency")
    private Integer concurrency;

    /**
     * 批量大小
     */
    @Min(value = 1, message = "批量大小不能小于1")
    @Max(value = 10000, message = "批量大小不能大于10000")
    @Column(name = "batch_size")
    private Integer batchSize;

    /**
     * 重试次数
     */
    @Min(value = 0, message = "重试次数不能小于0")
    @Max(value = 10, message = "重试次数不能大于10")
    @Column(name = "retry_count")
    private Integer retryCount;

    /**
     * 超时时间（秒）
     */
    @Min(value = 1, message = "超时时间不能小于1")
    @Max(value = 86400, message = "超时时间不能大于86400")
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    /**
     * 调度类型：CRON, FIXED_RATE, FIXED_DELAY
     */
    @Pattern(regexp = "^(CRON|FIXED_RATE|FIXED_DELAY)$", message = "调度类型必须是 CRON, FIXED_RATE 或 FIXED_DELAY")
    @Column(name = "schedule_type")
    private String scheduleType;

    /**
     * 调度表达式（CRON表达式或时间间隔）
     */
    @Size(max = 255, message = "调度表达式长度不能超过255个字符")
    @Column(name = "schedule_expression")
    private String scheduleExpression;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Column(name = "remark")
    private String remark;

    /**
     * 暂停时间
     */
    @Column(name = "pause_time")
    private LocalDateTime pauseTime;

    /**
     * 恢复时间
     */
    @Column(name = "resume_time")
    private LocalDateTime resumeTime;

    /**
     * 回滚点
     */
    @Size(max = 255, message = "回滚点长度不能超过255个字符")
    @Column(name = "rollback_point")
    private String rollbackPoint;

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
