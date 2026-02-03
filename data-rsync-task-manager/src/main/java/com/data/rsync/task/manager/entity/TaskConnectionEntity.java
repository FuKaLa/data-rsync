package com.data.rsync.task.manager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 任务连接实体类
 */
@Data
@ToString
@TableName("task_connection")
public class TaskConnectionEntity {

    /**
     * 连接ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField(value = "task_id", exist = true)
    private Long taskId;

    /**
     * 源节点ID
     */
    @TableField(value = "source_node_id", exist = true)
    private Long sourceNodeId;

    /**
     * 目标节点ID
     */
    @TableField(value = "target_node_id", exist = true)
    private Long targetNodeId;

    /**
     * 源节点句柄ID
     */
    @TableField(value = "source_handle_id", exist = true)
    private String sourceHandleId;

    /**
     * 目标节点句柄ID
     */
    @TableField(value = "target_handle_id", exist = true)
    private String targetHandleId;

    /**
     * 连接状态
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
