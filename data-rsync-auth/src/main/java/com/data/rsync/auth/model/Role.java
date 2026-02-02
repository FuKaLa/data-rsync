package com.data.rsync.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色模型
 */
@Data
@ToString
@Entity
@Table(name = "role")
public class Role {

    /**
     * 角色ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色名称
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * 角色编码
     */
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    /**
     * 角色描述
     */
    @Column(name = "description")
    private String description;

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
     * 角色关联的权限
     */
    @ManyToMany
    @JoinTable(name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private List<Permission> permissions;

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
    public Role() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "ENABLE";
    }

}
