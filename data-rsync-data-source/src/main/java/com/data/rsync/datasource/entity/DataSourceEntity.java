package com.data.rsync.datasource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 数据源配置实体类
 */
@Data
@ToString
@TableName("data_source")
public class DataSourceEntity {

    /**
     * 数据源ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据源名称
     */
    @TableField(value = "name", exist = true)
    private String name;

    /**
     * 数据源类型
     */
    @TableField(value = "type", exist = true)
    private String type;

    /**
     * 主机地址
     */
    @TableField(value = "host", exist = true)
    private String host;

    /**
     * 端口
     */
    @TableField(value = "port", exist = true)
    private Integer port;

    /**
     * 数据库名称
     */
    @TableField(value = "database_name", exist = true)
    private String databaseName;

    /**
     * 用户名
     */
    @TableField(value = "username", exist = true)
    private String username;

    /**
     * 密码（加密存储）
     */
    @TableField(value = "password", exist = true)
    private String password;

    /**
     * 连接URL
     */
    @TableField(value = "url", exist = true)
    private String url;

    /**
     * 状态：启用/禁用
     */
    @TableField(value = "enabled", exist = true)
    private Boolean enabled;

    /**
     * 健康状态
     */
    @TableField(value = "health_status", exist = true)
    private String healthStatus;

    /**
     * 最近一次连接失败原因
     */
    @TableField(value = "last_failure_reason", exist = true)
    private String lastFailureReason;

    /**
     * 心跳检测耗时（毫秒）
     */
    @TableField(value = "heartbeat_time", exist = true)
    private Integer heartbeatTime;

    /**
     * 最近一次心跳检测时间
     */
    @TableField(value = "last_heartbeat_time", exist = true)
    private LocalDateTime lastHeartbeatTime;

    /**
     * 连续失败次数
     */
    @TableField(value = "failure_count", exist = true)
    private Integer failureCount;

    /**
     * 描述
     */
    @TableField(value = "description", exist = true)
    private String description;

    /**
     * 驱动类
     */
    @TableField(value = "driver_class", exist = true)
    private String driverClass;

    /**
     * 日志监听方式
     */
    @TableField(value = "log_monitor_type", exist = true)
    private String logMonitorType;

    /**
     * 连接超时时间（毫秒）
     */
    @TableField(value = "connection_timeout", exist = true)
    private Integer connectionTimeout;

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

}
