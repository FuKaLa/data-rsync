package com.data.rsync.task.manager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 任务依赖实体类
 */
@Data
@ToString
@TableName("task_dependency")
public class TaskDependencyEntity {

    /**
     * 依赖ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField(value = "task_id", exist = true)
    private Long taskId;

    /**
     * 依赖任务ID
     */
    @TableField(value = "dependency_task_id", exist = true)
    private Long dependencyTaskId;

    /**
     * 依赖条件（JSON格式）
     */
    @TableField(value = "dependency_condition", exist = true)
    private String dependencyCondition;

    /**
     * 依赖状态
     */
    @TableField(value = "status", exist = true)
    private String status;

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
     * 初始化创建时间和更新时间
     */
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
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

}
