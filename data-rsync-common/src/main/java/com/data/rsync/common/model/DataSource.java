package com.data.rsync.common.model;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 数据源模型
 */
@Data
@ToString
public class DataSource {

    /**
     * 数据源ID
     */
    private Long id;

    /**
     * 数据源名称
     */
    private String name;

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
     * 状态：启用/禁用
     */
    private Boolean enabled;

    /**
     * 健康状态
     */
    private String healthStatus;

    /**
     * 描述
     */
    private String description;

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

}
