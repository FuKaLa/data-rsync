package com.data.rsync.auth.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 权限模型
 */
@Data
@ToString
@TableName("permission")
public class Permission {

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 构造方法
     */
    public Permission() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = "ENABLE";
    }

}
