package com.data.rsync.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 权限模型
 */
@Data
@ToString
@Entity
@Table(name = "permission")
public class Permission {

    /**
     * 权限ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 权限名称
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * 权限编码
     */
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    /**
     * 权限类型：MENU, BUTTON, API, DATA
     */
    @Column(name = "type", nullable = false)
    private String type;

    /**
     * 权限描述
     */
    @Column(name = "description")
    private String description;

    /**
     * 资源路径
     */
    @Column(name = "resource_path")
    private String resourcePath;

    /**
     * 父权限ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 排序
     */
    @Column(name = "sort")
    private Integer sort;

    /**
     * 状态：ENABLE, DISABLE
     */
    @Column(name = "status", nullable = false)
    private String status;

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
     * 预更新方法
     */
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public Permission() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "ENABLE";
    }

}
