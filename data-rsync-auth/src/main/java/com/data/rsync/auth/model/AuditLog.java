package com.data.rsync.auth.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 操作审计日志模型
 */
@Data
@ToString
@TableName("audit_log")
public class AuditLog {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作用户名
     */
    @TableField("username")
    private String username;

    /**
     * 操作类型：CREATE, UPDATE, DELETE, QUERY, LOGIN, LOGOUT, EXPORT, IMPORT
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 操作模块：USER, ROLE, PERMISSION, DATA_SOURCE, TASK, MONITOR, SYSTEM
     */
    @TableField("module")
    private String module;

    /**
     * 操作描述
     */
    @TableField("description")
    private String description;

    /**
     * 操作对象
     */
    @TableField("object_id")
    private Long objectId;

    /**
     * 操作对象名称
     */
    @TableField("object_name")
    private String objectName;

    /**
     * 操作前数据
     */
    @TableField("before_data")
    private String beforeData;

    /**
     * 操作后数据
     */
    @TableField("after_data")
    private String afterData;

    /**
     * 操作IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 操作浏览器
     */
    @TableField("browser")
    private String browser;

    /**
     * 操作系统
     */
    @TableField("os")
    private String os;

    /**
     * 操作结果：SUCCESS, FAIL
     */
    @TableField("result")
    private String result;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 操作时间
     */
    @TableField("operation_time")
    private LocalDateTime operationTime;

    /**
     * 构造方法
     */
    public AuditLog() {
        this.operationTime = LocalDateTime.now();
        this.result = "SUCCESS";
    }

}
