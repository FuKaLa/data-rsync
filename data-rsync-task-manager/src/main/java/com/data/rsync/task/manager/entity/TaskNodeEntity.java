package com.data.rsync.task.manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 任务节点实体类
 */
@Data
@ToString
@Entity
@Table(name = "task_node")
public class TaskNodeEntity {

    /**
     * 节点ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * 节点类型
     */
    @Column(name = "node_type", nullable = false)
    private String nodeType;

    /**
     * 节点标签
     */
    @Column(name = "node_label", nullable = false)
    private String nodeLabel;

    /**
     * 节点配置（JSON格式）
     */
    @Column(name = "node_config", columnDefinition = "TEXT")
    private String nodeConfig;

    /**
     * 节点位置X坐标
     */
    @Column(name = "position_x", nullable = false)
    private Integer positionX;

    /**
     * 节点位置Y坐标
     */
    @Column(name = "position_y", nullable = false)
    private Integer positionY;

    /**
     * 节点状态
     */
    @Column(name = "status")
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
     * 初始化创建时间和更新时间
     */
    @PrePersist
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
    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

}
