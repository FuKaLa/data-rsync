package com.data.rsync.auth.model;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户模型
 */
@Data
@TableName("user")
public class User {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 租户ID
     */
    @TableField(exist = false)
    private Long tenantId;

    /**
     * 用户名
     */
    @TableField("username")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;

    /**
     * 密码
     */
    @TableField("password")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
    private String password;

    /**
     * 姓名
     */
    @TableField("name")
    @NotBlank(message = "姓名不能为空")
    @Size(min = 1, max = 100, message = "姓名长度必须在1-100之间")
    private String name;

    /**
     * 邮箱
     */
    @TableField("email")
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 状态：ENABLE, DISABLE
     */
    @TableField("status")
    private String status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

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
     * 用户关联的角色
     */
    @TableField(exist = false)
    private List<Role> roles;

    /**
     * 构造方法
     */
    public User() {
        this.tenantId = 0L;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "ENABLE";
        this.deleted = 0;
        this.version = 0;
    }

    // Builder模式实现
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User user;

        private Builder() {
            user = new User();
        }

        public Builder id(Long id) {
            user.id = id;
            return this;
        }

        public Builder tenantId(Long tenantId) {
            user.tenantId = tenantId;
            return this;
        }

        public Builder username(String username) {
            user.username = username;
            return this;
        }

        public Builder password(String password) {
            user.password = password;
            return this;
        }

        public Builder name(String name) {
            user.name = name;
            return this;
        }

        public Builder email(String email) {
            user.email = email;
            return this;
        }

        public Builder phone(String phone) {
            user.phone = phone;
            return this;
        }

        public Builder status(String status) {
            user.status = status;
            return this;
        }

        public Builder lastLoginTime(LocalDateTime lastLoginTime) {
            user.lastLoginTime = lastLoginTime;
            return this;
        }

        public Builder lastLoginIp(String lastLoginIp) {
            user.lastLoginIp = lastLoginIp;
            return this;
        }

        public Builder deleted(Integer deleted) {
            user.deleted = deleted;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            user.createTime = createTime;
            return this;
        }

        public Builder updateTime(LocalDateTime updateTime) {
            user.updateTime = updateTime;
            return this;
        }

        public Builder createBy(String createBy) {
            user.createBy = createBy;
            return this;
        }

        public Builder updateBy(String updateBy) {
            user.updateBy = updateBy;
            return this;
        }

        public Builder version(Integer version) {
            user.version = version;
            return this;
        }

        public Builder roles(List<Role> roles) {
            user.roles = roles;
            return this;
        }

        public User build() {
            return user;
        }
    }

}
