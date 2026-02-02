package com.data.rsync.task.manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 任务依赖实体类
 */
@Data
@ToString
@Entity
@Table(name = "task_dependency")
public class TaskDependencyEntity {

    /**
     * 依赖ID
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
     * 依赖任务ID
     */
    @Column(name = "dependency_task_id", nullable = false)
    private Long dependencyTaskId;

    /**
     * 依赖条件（JSON格式）
     */
    @Column(name = "dependency_condition", columnDefinition = "TEXT")
    private String dependencyCondition;

    /**
     * 依赖状态
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
