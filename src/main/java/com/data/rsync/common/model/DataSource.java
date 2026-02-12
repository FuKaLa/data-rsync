package com.data.rsync.common.model;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 数据源模型
 */
@Data
@ToString
@TableName("data_source")
public class DataSource {

    /**
     * 数据源ID
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据源分组
     */
    private String dataSourceGroup;

    /**
     * 数据源类型
     */
    private String type;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 连接URL
     */
    private String url;

    /**
     * 驱动类
     */
    private String driverClass;

    /**
     * 日志监听方式
     */
    private String logMonitorType;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectionTimeout;

    /**
     * 连接池配置（JSON格式）
     */
    private String connectionPoolConfig;

    /**
     * 状态：启用/禁用
     */
    private Boolean enabled;

    /**
     * 健康状态
     */
    private String healthStatus;

    /**
     * 最近一次连接失败原因
     */
    private String lastFailureReason;

    /**
     * 心跳检测耗时（毫秒）
     */
    private Integer heartbeatTime;

    /**
     * 最近一次心跳检测时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 连续失败次数
     */
    private Integer failureCount;

    /**
     * 描述
     */
    private String description;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    private Integer deleted;

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
     * 获取数据库名称（别名方法）
     * @return 数据库名称
     */
    public String getDatabase() {
        return databaseName;
    }

    /**
     * 设置数据库名称（别名方法）
     * @param database 数据库名称
     */
    public void setDatabase(String database) {
        this.databaseName = database;
    }

    /**
     * 构造方法
     */
    public DataSource() {
        this.tenantId = 0L;
        this.enabled = true;
        this.failureCount = 0;
        this.deleted = 0;
        this.version = 0;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

}
