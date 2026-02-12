package com.data.rsync.task.entity;

import lombok.Data;
import javax.validation.constraints.*;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 任务实体类
 */
@Data
@TableName("task")
public class TaskEntity {

    /**
     * 任务ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id", exist = true)
    private Long tenantId;

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过100个字符")
    @TableField(value = "name", exist = true)
    private String name;

    /**
     * 任务分组
     */
    @Size(max = 100, message = "任务分组长度不能超过100个字符")
    @TableField(value = "task_group", exist = true)
    private String taskGroup;

    /**
     * 任务类型：FULL_SYNC, INCREMENTAL_SYNC, FULL_AND_INCREMENTAL
     */
    @NotBlank(message = "任务类型不能为空")
    @Pattern(regexp = "^(FULL_SYNC|INCREMENTAL_SYNC|FULL_AND_INCREMENTAL)$", message = "任务类型必须是 FULL_SYNC, INCREMENTAL_SYNC 或 FULL_AND_INCREMENTAL")
    @TableField(value = "type", exist = true)
    private String type;

    /**
     * 同步策略：INSERT_ONLY, UPDATE_IF_EXISTS, UPSERT, DELETE_IF_NOT_EXISTS
     */
    @Pattern(regexp = "^(INSERT_ONLY|UPDATE_IF_EXISTS|UPSERT|DELETE_IF_NOT_EXISTS)$", message = "同步策略必须是 INSERT_ONLY, UPDATE_IF_EXISTS, UPSERT 或 DELETE_IF_NOT_EXISTS")
    @TableField(value = "sync_strategy", exist = true)
    private String syncStrategy;

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
     * 时间间隔（秒）
     */
    @Min(value = 1, message = "时间间隔不能小于1")
    @Max(value = 86400, message = "时间间隔不能大于86400")
    @TableField(exist = false)
    private Integer syncInterval;

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
     * 错误阈值
     */
    @Min(value = 0, message = "错误阈值不能小于0")
    @TableField(value = "error_threshold", exist = true)
    private Integer errorThreshold;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField(value = "deleted", exist = true)
    private Integer deleted;

    /**
     * 创建人
     */
    @TableField(value = "create_by", exist = true)
    private String createBy;

    /**
     * 更新人
     */
    @TableField(value = "update_by", exist = true)
    private String updateBy;

    /**
     * 版本号
     */
    @TableField(value = "version", exist = true)
    private Integer version;

    /**
     * 构造方法
     */
    public TaskEntity() {
        this.tenantId = 0L;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "PENDING";
        this.enabled = true;
        this.execCount = 0;
        this.concurrency = 1;
        this.batchSize = 1000;
        this.retryCount = 3;
        this.timeoutSeconds = 3600;
        this.syncInterval = 300; // 默认5分钟
        this.errorThreshold = 100;
        this.deleted = 0;
        this.version = 0;
        // 设置默认的数据源ID为1
        this.dataSourceId = 1L;
    }

    /**
     * 更新前方法
     */
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    // 新增方法：获取集合名称
    public String getCollectionName() {
        return tableName; // 假设集合名称与表名相同
    }

    // 新增方法：获取同步参数
    public String getSyncParams() {
        return config; // 假设同步参数存储在config字段中
    }

}
