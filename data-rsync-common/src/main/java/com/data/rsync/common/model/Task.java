package com.data.rsync.common.model;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 任务模型
 */
@Data
@ToString
public class Task {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务类型
     */
    private String type;

    /**
     * 数据源ID
     */
    private Long dataSourceId;

    /**
     * 数据源名称
     */
    private String dataSourceName;

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
     * 同步频率（Cron表达式）
     */
    private String syncFrequency;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 告警阈值
     */
    private Integer alertThreshold;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 同步进度
     */
    private Integer progress;

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
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    private Long duration;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 任务配置（JSON格式）
     */
    private String config;

    /**
     * 数据源类型
     */
    private String dataSourceType;

    /**
     * 数据源对象
     */
    private DataSource dataSource;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 主键
     */
    private String primaryKey;

    /**
     * 并发度
     */
    private Integer concurrency;

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

}
