package com.data.rsync.datasource.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 数据源配置实体类
 */
@Data
@ToString
@Entity
@Table(name = "data_source")
public class DataSourceEntity {

    /**
     * 数据源ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 数据源名称
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * 数据源类型
     */
    @Column(name = "type", nullable = false)
    private String type;

    /**
     * 主机地址
     */
    @Column(name = "host", nullable = false)
    private String host;

    /**
     * 端口
     */
    @Column(name = "port", nullable = false)
    private Integer port;

    /**
     * 数据库名称
     */
    @Column(name = "database_name")
    private String databaseName;

    /**
     * 用户名
     */
    @Column(name = "username", nullable = false)
    private String username;

    /**
     * 密码（加密存储）
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 连接URL
     */
    @Column(name = "url", nullable = false)
    private String url;

    /**
     * 状态：启用/禁用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    /**
     * 健康状态
     */
    @Column(name = "health_status")
    private String healthStatus;

    /**
     * 描述
     */
    @Column(name = "description")
    private String description;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @Column(name = "create_by")
    private String createBy;

    /**
     * 更新人
     */
    @Column(name = "update_by")
    private String updateBy;

    /**
     * 版本号
     */
    @Version
    private Integer version;

    /**
     * 初始化创建时间和更新时间
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) {
            createTime = now;
        }
        if (updateTime == null) {
            updateTime = now;
        }
    }

    /**
     * 更新时更新时间
     */
    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

}
