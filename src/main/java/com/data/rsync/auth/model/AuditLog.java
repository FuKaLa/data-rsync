package com.data.rsync.auth.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 操作审计日志模型
 */
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getBeforeData() {
        return beforeData;
    }

    public void setBeforeData(String beforeData) {
        this.beforeData = beforeData;
    }

    public String getAfterData() {
        return afterData;
    }

    public void setAfterData(String afterData) {
        this.afterData = afterData;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", operationType='" + operationType + '\'' +
                ", module='" + module + '\'' +
                ", description='" + description + '\'' +
                ", objectId=" + objectId +
                ", objectName='" + objectName + '\'' +
                ", beforeData='" + beforeData + '\'' +
                ", afterData='" + afterData + '\'' +
                ", ip='" + ip + '\'' +
                ", browser='" + browser + '\'' +
                ", os='" + os + '\'' +
                ", result='" + result + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", operationTime=" + operationTime +
                '}';
    }

}
