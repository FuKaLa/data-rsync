package com.data.rsync.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 任务节点实体类
 */
@Data
@ToString
@TableName("task_node")
public class TaskNodeEntity {

    /**
     * 节点ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField(value = "task_id", exist = true)
    private Long taskId;

    /**
     * 节点类型
     */
    @TableField(value = "node_type", exist = true)
    private String nodeType;

    /**
     * 节点标签
     */
    @TableField(value = "node_label", exist = true)
    private String nodeLabel;

    /**
     * 节点配置（JSON格式）
     */
    @TableField(value = "node_config", exist = true)
    private String nodeConfig;

    /**
     * 节点位置X坐标
     */
    @TableField(value = "position_x", exist = true)
    private Integer positionX;

    /**
     * 节点位置Y坐标
     */
    @TableField(value = "position_y", exist = true)
    private Integer positionY;

    /**
     * 节点状态
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
