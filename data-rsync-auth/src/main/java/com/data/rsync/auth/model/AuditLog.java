package com.data.rsync.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 操作审计日志模型
 */
@Data
@ToString
@Entity
@Table(name = "audit_log")
public class AuditLog {

    /**
     * 日志ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作用户ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 操作用户名
     */
    @Column(name = "username", nullable = false)
    private String username;

    /**
     * 操作类型：CREATE, UPDATE, DELETE, QUERY, LOGIN, LOGOUT, EXPORT, IMPORT
     */
    @Column(name = "operation_type", nullable = false)
    private String operationType;

    /**
     * 操作模块：USER, ROLE, PERMISSION, DATA_SOURCE, TASK, MONITOR, SYSTEM
     */
    @Column(name = "module", nullable = false)
    private String module;

    /**
     * 操作描述
     */
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * 操作对象
     */
    @Column(name = "object_id")
    private Long objectId;

    /**
     * 操作对象名称
     */
    @Column(name = "object_name")
    private String objectName;

    /**
     * 操作前数据
     */
    @Column(name = "before_data", columnDefinition = "TEXT")
    private String beforeData;

    /**
     * 操作后数据
     */
    @Column(name = "after_data", columnDefinition = "TEXT")
    private String afterData;

    /**
     * 操作IP
     */
    @Column(name = "ip", nullable = false)
    private String ip;

    /**
     * 操作浏览器
     */
    @Column(name = "browser")
    private String browser;

    /**
     * 操作系统
     */
    @Column(name = "os")
    private String os;

    /**
     * 操作结果：SUCCESS, FAIL
     */
    @Column(name = "result", nullable = false)
    private String result;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 操作时间
     */
    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    /**
     * 构造方法
     */
    public AuditLog() {
        this.operationTime = LocalDateTime.now();
        this.result = "SUCCESS";
    }

}
