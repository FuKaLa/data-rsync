package com.data.rsync.data.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 数据源诊断报告实体类
 */
@Data
@ToString
@TableName("data_source_diagnose_report")
public class DataSourceDiagnoseReportEntity {

    /**
     * 报告ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据源ID
     */
    @TableField(value = "data_source_id", exist = true)
    private Long dataSourceId;

    /**
     * 整体诊断结果
     */
    @TableField(value = "overall_status", exist = true)
    private String overallStatus;

    /**
     * 网络连通性诊断结果
     */
    @TableField(value = "network_status", exist = true)
    private String networkStatus;

    /**
     * 网络诊断详情
     */
    @TableField(value = "network_message", exist = true)
    private String networkMessage;

    /**
     * 账号权限诊断结果
     */
    @TableField(value = "authentication_status", exist = true)
    private String authenticationStatus;

    /**
     * 账号权限诊断详情
     */
    @TableField(value = "authentication_message", exist = true)
    private String authenticationMessage;

    /**
     * 日志监听端口诊断结果
     */
    @TableField(value = "log_monitor_status", exist = true)
    private String logMonitorStatus;

    /**
     * 日志监听端口诊断详情
     */
    @TableField(value = "log_monitor_message", exist = true)
    private String logMonitorMessage;

    /**
     * 数据库连接诊断结果
     */
    @TableField(value = "connection_status", exist = true)
    private String connectionStatus;

    /**
     * 数据库连接诊断详情
     */
    @TableField(value = "connection_message", exist = true)
    private String connectionMessage;

    /**
     * 诊断时间
     */
    @TableField(value = "diagnose_time", exist = true)
    private LocalDateTime diagnoseTime;

    /**
     * 诊断耗时（毫秒）
     */
    @TableField(value = "diagnose_duration", exist = true)
    private Integer diagnoseDuration;

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

}
