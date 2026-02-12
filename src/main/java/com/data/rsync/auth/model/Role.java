package com.data.rsync.auth.model;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色模型
 */
@Data
@TableName("role")
public class Role {

    /**
     * 角色ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 角色名称
     */
    @TableField("name")
    private String name;

    /**
     * 角色编码
     */
    @TableField("code")
    private String code;

    /**
     * 角色描述
     */
    @TableField("description")
    private String description;

    /**
     * 状态：ENABLE, DISABLE
     */
    @TableField("status")
    private String status;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @TableField("deleted")
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 更新人
     */
    @TableField("update_by")
    private String updateBy;

    /**
     * 版本号
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 角色关联的权限
     */
    @TableField(exist = false)
    private List<Permission> permissions;

    /**
     * 构造方法
     */
    public Role() {
        this.tenantId = 0L;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "ENABLE";
        this.deleted = 0;
        this.version = 0;
    }

    /**
     * 设置角色启用状态
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.status = enabled ? "ENABLE" : "DISABLE";
    }

    /**
     * 获取角色启用状态
     * @return 是否启用
     */
    public boolean getEnabled() {
        return "ENABLE".equals(this.status);
    }

    // Builder模式实现
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Role role;

        private Builder() {
            role = new Role();
        }

        public Builder id(Long id) {
            role.id = id;
            return this;
        }

        public Builder tenantId(Long tenantId) {
            role.tenantId = tenantId;
            return this;
        }

        public Builder name(String name) {
            role.name = name;
            return this;
        }

        public Builder code(String code) {
            role.code = code;
            return this;
        }

        public Builder description(String description) {
            role.description = description;
            return this;
        }

        public Builder status(String status) {
            role.status = status;
            return this;
        }

        public Builder deleted(Integer deleted) {
            role.deleted = deleted;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            role.createTime = createTime;
            return this;
        }

        public Builder updateTime(LocalDateTime updateTime) {
            role.updateTime = updateTime;
            return this;
        }

        public Builder createBy(String createBy) {
            role.createBy = createBy;
            return this;
        }

        public Builder updateBy(String updateBy) {
            role.updateBy = updateBy;
            return this;
        }

        public Builder version(Integer version) {
            role.version = version;
            return this;
        }

        public Builder permissions(List<Permission> permissions) {
            role.permissions = permissions;
            return this;
        }

        public Role build() {
            return role;
        }
    }

}
