package com.data.rsync.task.manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 任务连接实体类
 */
@Data
@ToString
@Entity
@Table(name = "task_connection")
public class TaskConnectionEntity {

    /**
     * 连接ID
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
     * 源节点ID
     */
    @Column(name = "source_node_id", nullable = false)
    private Long sourceNodeId;

    /**
     * 目标节点ID
     */
    @Column(name = "target_node_id", nullable = false)
    private Long targetNodeId;

    /**
     * 源节点句柄ID
     */
    @Column(name = "source_handle_id")
    private String sourceHandleId;

    /**
     * 目标节点句柄ID
     */
    @Column(name = "target_handle_id")
    private String targetHandleId;

    /**
     * 连接状态
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
