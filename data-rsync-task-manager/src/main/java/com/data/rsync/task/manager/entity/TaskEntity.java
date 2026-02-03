package com.data.rsync.task.manager.entity;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 任务实体类
 */
@Data
@ToString
@TableName("task")
public class TaskEntity {

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过100个字符")
    @TableField(value = "name", exist = true)
    private String name;

    /**
     * 任务类型：FULL_SYNC, INCREMENTAL_SYNC, FULL_AND_INCREMENTAL
     */
    @NotBlank(message = "任务类型不能为空")
    @Pattern(regexp = "^(FULL_SYNC|INCREMENTAL_SYNC|FULL_AND_INCREMENTAL)$", message = "任务类型必须是 FULL_SYNC, INCREMENTAL_SYNC 或 FULL_AND_INCREMENTAL")
    @TableField(value = "type", exist = true)
    private String type;

    /**
     * 数据源ID
     */
    @NotNull(message = "数据源ID不能为空")
    @Positive(message = "数据源ID必须大于0")
    @TableField(value = "data_source_id", exist = true)
    private Long dataSourceId;

    /**
     * 数据库名称
     */
    @Size(max = 100, message = "数据库名称长度不能超过100个字符")
    @TableField(value = "database_name", exist = true)
    private String databaseName;

    /**
     * 表名
     */
    @Size(max = 100, message = "表名长度不能超过100个字符")
    @TableField(value = "table_name", exist = true)
    private String tableName;

    /**
     * 任务状态：PENDING, RUNNING, SUCCESS, FAILED, STOPPED
     */
    @Pattern(regexp = "^(PENDING|RUNNING|SUCCESS|FAILED|STOPPED|PAUSED|ROLLED_BACK)$", message = "任务状态必须是 PENDING, RUNNING, SUCCESS, FAILED, STOPPED, PAUSED 或 ROLLED_BACK")
    @TableField(value = "status", exist = true)
    private String status;

    /**
     * 任务配置（JSON格式）
     */
    @TableField(value = "config", exist = true)
    private String config;

    /**
     * 任务进度（百分比）
     */
    @Min(value = 0, message = "任务进度不能小于0")
    @Max(value = 100, message = "任务进度不能大于100")
    @TableField(value = "progress", exist = true)
    private Integer progress;

    /**
     * 开始时间
     */
    @TableField(value = "start_time", exist = true)
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField(value = "end_time", exist = true)
    private LocalDateTime endTime;

    /**
     * 错误信息
     */
    @TableField(value = "error_message", exist = true)
    private String errorMessage;

    /**
     * 断点续传位点
     */
    @TableField(value = "breakpoint", exist = true)
    private String breakpoint;

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
     * 启用状态
     */
    @TableField(value = "enabled", exist = true)
    private Boolean enabled;

    /**
     * 最后执行时间
     */
    @TableField(value = "last_exec_time", exist = true)
    private LocalDateTime lastExecTime;

    /**
     * 执行次数
     */
    @Min(value = 0, message = "执行次数不能小于0")
    @TableField(value = "exec_count", exist = true)
    private Integer execCount;

    /**
     * 下次执行时间
     */
    @TableField(value = "next_exec_time", exist = true)
    private LocalDateTime nextExecTime;

    /**
     * 并发度
     */
    @Min(value = 1, message = "并发度不能小于1")
    @Max(value = 100, message = "并发度不能大于100")
    @TableField(value = "concurrency", exist = true)
    private Integer concurrency;

    /**
     * 批量大小
     */
    @Min(value = 1, message = "批量大小不能小于1")
    @Max(value = 10000, message = "批量大小不能大于10000")
    @TableField(value = "batch_size", exist = true)
    private Integer batchSize;

    /**
     * 重试次数
     */
    @Min(value = 0, message = "重试次数不能小于0")
    @Max(value = 10, message = "重试次数不能大于10")
    @TableField(value = "retry_count", exist = true)
    private Integer retryCount;

    /**
     * 超时时间（秒）
     */
    @Min(value = 1, message = "超时时间不能小于1")
    @Max(value = 86400, message = "超时时间不能大于86400")
    @TableField(value = "timeout_seconds", exist = true)
    private Integer timeoutSeconds;

    /**
     * 调度类型：CRON, FIXED_RATE, FIXED_DELAY
     */
    @Pattern(regexp = "^(CRON|FIXED_RATE|FIXED_DELAY)$", message = "调度类型必须是 CRON, FIXED_RATE 或 FIXED_DELAY")
    @TableField(value = "schedule_type", exist = true)
    private String scheduleType;

    /**
     * 调度表达式（CRON表达式或时间间隔）
     */
    @Size(max = 255, message = "调度表达式长度不能超过255个字符")
    @TableField(value = "schedule_expression", exist = true)
    private String scheduleExpression;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @TableField(value = "remark", exist = true)
    private String remark;

    /**
     * 暂停时间
     */
    @TableField(value = "pause_time", exist = true)
    private LocalDateTime pauseTime;

    /**
     * 恢复时间
     */
    @TableField(value = "resume_time", exist = true)
    private LocalDateTime resumeTime;

    /**
     * 回滚点
     */
    @Size(max = 255, message = "回滚点长度不能超过255个字符")
    @TableField(value = "rollback_point", exist = true)
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
     * 更新前方法
     */
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

}
