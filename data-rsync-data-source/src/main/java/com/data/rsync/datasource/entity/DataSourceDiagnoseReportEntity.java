package com.data.rsync.datasource.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 数据源诊断报告实体类
 */
@Data
@ToString
@Entity
@Table(name = "data_source_diagnose_report")
public class DataSourceDiagnoseReportEntity {

    /**
     * 报告ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 数据源ID
     */
    @Column(name = "data_source_id", nullable = false)
    private Long dataSourceId;

    /**
     * 整体诊断结果
     */
    @Column(name = "overall_status", nullable = false)
    private String overallStatus;

    /**
     * 网络连通性诊断结果
     */
    @Column(name = "network_status")
    private String networkStatus;

    /**
     * 网络诊断详情
     */
    @Column(name = "network_message")
    private String networkMessage;

    /**
     * 账号权限诊断结果
     */
    @Column(name = "authentication_status")
    private String authenticationStatus;

    /**
     * 账号权限诊断详情
     */
    @Column(name = "authentication_message")
    private String authenticationMessage;

    /**
     * 日志监听端口诊断结果
     */
    @Column(name = "log_monitor_status")
    private String logMonitorStatus;

    /**
     * 日志监听端口诊断详情
     */
    @Column(name = "log_monitor_message")
    private String logMonitorMessage;

    /**
     * 数据库连接诊断结果
     */
    @Column(name = "connection_status")
    private String connectionStatus;

    /**
     * 数据库连接诊断详情
     */
    @Column(name = "connection_message")
    private String connectionMessage;

    /**
     * 诊断时间
     */
    @Column(name = "diagnose_time", nullable = false)
    private LocalDateTime diagnoseTime;

    /**
     * 诊断耗时（毫秒）
     */
    @Column(name = "diagnose_duration")
    private Integer diagnoseDuration;

    /**
     * 初始化诊断时间
     */
    @PrePersist
    public void prePersist() {
        if (diagnoseTime == null) {
            diagnoseTime = LocalDateTime.now();
        }
    }

}
