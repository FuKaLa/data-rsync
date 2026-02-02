package com.data.rsync.datasource.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 数据源模板配置实体类
 */
@Data
@ToString
@Entity
@Table(name = "data_source_template")
public class DataSourceTemplateEntity {

    /**
     * 模板ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模板名称
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * 数据源类型
     */
    @Column(name = "data_source_type", nullable = false)
    private String dataSourceType;

    /**
     * 驱动类
     */
    @Column(name = "driver_class")
    private String driverClass;

    /**
     * 日志监听方式
     */
    @Column(name = "log_monitor_type")
    private String logMonitorType;

    /**
     * 默认端口
     */
    @Column(name = "default_port")
    private Integer defaultPort;

    /**
     * 连接超时时间（毫秒）
     */
    @Column(name = "connection_timeout")
    private Integer connectionTimeout;

    /**
     * 描述
     */
    @Column(name = "description")
    private String description;

    /**
     * 是否为系统预设模板
     */
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem;

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
