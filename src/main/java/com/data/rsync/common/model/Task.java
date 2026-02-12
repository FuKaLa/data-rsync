package com.data.rsync.common.model;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 任务模型
 */
@Data
@TableName("task")
public class Task {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务分组
     */
    private String taskGroup;

    /**
     * 任务类型
     */
    private String type;

    /**
     * 同步策略
     */
    private String syncStrategy;

    /**
     * 数据源ID
     */
    private Long dataSourceId;

    /**
     * 数据源名称
     */
    private String dataSourceName;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 同步表/集合
     */
    private String syncTables;

    /**
     * Milvus 集合
     */
    private String milvusCollection;

    /**
     * 向量化规则
     */
    private String vectorizationRule;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 任务配置（JSON格式）
     */
    private String config;

    /**
     * 任务进度（百分比）
     */
    private Integer progress;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 断点续传位点
     */
    private String breakpoint;

    /**
     * 错误阈值
     */
    private Integer errorThreshold;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecTime;

    /**
     * 执行次数
     */
    private Integer execCount;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecTime;

    /**
     * 并发度
     */
    private Integer concurrency;

    /**
     * 批量大小
     */
    private Integer batchSize;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;

    /**
     * 调度类型：CRON, FIXED_RATE, FIXED_DELAY
     */
    private String scheduleType;

    /**
     * 调度表达式
     */
    private String scheduleExpression;

    /**
     * 备注
     */
    private String remark;

    /**
     * 暂停时间
     */
    private LocalDateTime pauseTime;

    /**
     * 恢复时间
     */
    private LocalDateTime resumeTime;

    /**
     * 回滚点
     */
    private String rollbackPoint;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer deleted;

    /**
     * 数据源类型
     */
    private String dataSourceType;

    /**
     * 数据源对象
     */
    private DataSource dataSource;

    /**
     * 主键
     */
    private String primaryKey;

    /**
     * 告警阈值
     */
    private Integer alertThreshold;

    /**
     * 已同步条数
     */
    private Long syncedCount;

    /**
     * 总条数
     */
    private Long totalCount;

    /**
     * 同步速率（条/秒）
     */
    private Double syncRate;

    /**
     * 延迟（毫秒）
     */
    private Long delay;

    /**
     * 耗时（毫秒）
     */
    private Long duration;

    /**
     * 扩展参数
     */
    private Map<String, Object> extParams;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 版本号
     */
    @Version
    private Integer version;

    /**
     * 构造方法
     */
    public Task() {
        this.tenantId = 0L;
        this.enabled = true;
        this.concurrency = 1;
        this.batchSize = 1000;
        this.retryCount = 3;
        this.timeoutSeconds = 3600;
        this.errorThreshold = 100;
        this.progress = 0;
        this.execCount = 0;
        this.deleted = 0;
        this.version = 0;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "PENDING";
    }

}
