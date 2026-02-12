package com.data.rsync.auth.model;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;

/**
 * 权限模型
 */
@Data
@TableName("permission")
public class Permission {

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 权限名称
     */
    @TableField("name")
    private String name;

    /**
     * 权限编码
     */
    @TableField("code")
    private String code;

    /**
     * 权限类型：MENU, BUTTON, API, DATA
     */
    @TableField("type")
    private String type;

    /**
     * 权限描述
     */
    @TableField("description")
    private String description;

    /**
     * 资源路径
     */
    @TableField("resource_path")
    private String resourcePath;

    /**
     * 父权限ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

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
     * 构造方法
     */
    public Permission() {
        this.tenantId = 0L;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "ENABLE";
        this.deleted = 0;
        this.version = 0;
    }

    /**
     * 设置权限启用状态
     * @param enabled 是否启用
     */
    public void setEnabled(boolean enabled) {
        this.status = enabled ? "ENABLE" : "DISABLE";
    }

    /**
     * 获取权限启用状态
     * @return 是否启用
     */
    public boolean getEnabled() {
        return "ENABLE".equals(this.status);
    }

    // 新增方法：获取URL（映射到resourcePath）
    public String getUrl() {
        return resourcePath;
    }

    // 新增方法：获取Method（默认返回GET）
    public String getMethod() {
        return "GET";
    }

    // Builder模式实现
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Permission permission;

        private Builder() {
            permission = new Permission();
        }

        public Builder id(Long id) {
            permission.id = id;
            return this;
        }

        public Builder tenantId(Long tenantId) {
            permission.tenantId = tenantId;
            return this;
        }

        public Builder name(String name) {
            permission.name = name;
            return this;
        }

        public Builder code(String code) {
            permission.code = code;
            return this;
        }

        public Builder type(String type) {
            permission.type = type;
            return this;
        }

        public Builder description(String description) {
            permission.description = description;
            return this;
        }

        public Builder resourcePath(String resourcePath) {
            permission.resourcePath = resourcePath;
            return this;
        }

        public Builder parentId(Long parentId) {
            permission.parentId = parentId;
            return this;
        }

        public Builder sort(Integer sort) {
            permission.sort = sort;
            return this;
        }

        public Builder status(String status) {
            permission.status = status;
            return this;
        }

        public Builder deleted(Integer deleted) {
            permission.deleted = deleted;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            permission.createTime = createTime;
            return this;
        }

        public Builder updateTime(LocalDateTime updateTime) {
            permission.updateTime = updateTime;
            return this;
        }

        public Builder createBy(String createBy) {
            permission.createBy = createBy;
            return this;
        }

        public Builder updateBy(String updateBy) {
            permission.updateBy = updateBy;
            return this;
        }

        public Builder version(Integer version) {
            permission.version = version;
            return this;
        }

        public Permission build() {
            return permission;
        }
    }

}
