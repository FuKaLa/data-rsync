package com.data.rsync.data.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据源配置实体类
 */
@Data
@TableName("data_source")
public class DataSourceEntity {

    /**
     * 数据源ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField(value = "tenant_id", exist = true)
    private Long tenantId;

    /**
     * 数据源名称
     */
    @TableField(value = "name", exist = true)
    private String name;

    /**
     * 数据源分组
     */
    @TableField(value = "data_source_group", exist = true)
    private String dataSourceGroup;

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
     * 连接池配置（JSON格式）
     */
    @TableField(value = "connection_pool_config", exist = true)
    private String connectionPoolConfig;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField(value = "deleted", exist = true)
    private Integer deleted;

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

    /**
     * 构造方法
     */
    public DataSourceEntity() {
        this.tenantId = 0L;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.enabled = true;
        this.deleted = 0;
        this.version = 0;
        this.failureCount = 0;
    }

    /**
     * 全参数构造方法
     */
    public DataSourceEntity(Long id, Long tenantId, String name, String dataSourceGroup, String type, String host, Integer port, String databaseName, String username, String password, String url, Boolean enabled, String healthStatus, String lastFailureReason, Integer heartbeatTime, LocalDateTime lastHeartbeatTime, Integer failureCount, String description, String driverClass, String logMonitorType, Integer connectionTimeout, String connectionPoolConfig, Integer deleted, LocalDateTime createTime, LocalDateTime updateTime, String createBy, String updateBy, Integer version) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.dataSourceGroup = dataSourceGroup;
        this.type = type;
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.url = url;
        this.enabled = enabled;
        this.healthStatus = healthStatus;
        this.lastFailureReason = lastFailureReason;
        this.heartbeatTime = heartbeatTime;
        this.lastHeartbeatTime = lastHeartbeatTime;
        this.failureCount = failureCount;
        this.description = description;
        this.driverClass = driverClass;
        this.logMonitorType = logMonitorType;
        this.connectionTimeout = connectionTimeout;
        this.connectionPoolConfig = connectionPoolConfig;
        this.deleted = deleted;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.createBy = createBy;
        this.updateBy = updateBy;
        this.version = version;
    }

    @Override
    public String toString() {
        return "DataSourceEntity{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", name='" + name + '\'' +
                ", dataSourceGroup='" + dataSourceGroup + '\'' +
                ", type='" + type + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", databaseName='" + databaseName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", url='" + url + '\'' +
                ", enabled=" + enabled +
                ", healthStatus='" + healthStatus + '\'' +
                ", lastFailureReason='" + lastFailureReason + '\'' +
                ", heartbeatTime=" + heartbeatTime +
                ", lastHeartbeatTime=" + lastHeartbeatTime +
                ", failureCount=" + failureCount +
                ", description='" + description + '\'' +
                ", driverClass='" + driverClass + '\'' +
                ", logMonitorType='" + logMonitorType + '\'' +
                ", connectionTimeout=" + connectionTimeout +
                ", connectionPoolConfig='" + connectionPoolConfig + '\'' +
                ", deleted=" + deleted +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createBy='" + createBy + '\'' +
                ", updateBy='" + updateBy + '\'' +
                ", version=" + version +
                '}';
    }

}
