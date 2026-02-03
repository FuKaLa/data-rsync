package com.data.rsync.datasource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 数据源模板配置实体类
 */
@Data
@ToString
@TableName("data_source_template")
public class DataSourceTemplateEntity {

    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    @TableField(value = "name", exist = true)
    private String name;

    /**
     * 数据源类型
     */
    @TableField(value = "data_source_type", exist = true)
    private String dataSourceType;

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
     * 默认端口
     */
    @TableField(value = "default_port", exist = true)
    private Integer defaultPort;

    /**
     * 连接超时时间（毫秒）
     */
    @TableField(value = "connection_timeout", exist = true)
    private Integer connectionTimeout;

    /**
     * 描述
     */
    @TableField(value = "description", exist = true)
    private String description;

    /**
     * 是否为系统预设模板
     */
    @TableField(value = "is_system", exist = true)
    private Boolean isSystem;

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

}
