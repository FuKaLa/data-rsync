package com.data.rsync.task.manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 向量化配置实体类
 */
@Data
@ToString
@Entity
@Table(name = "vectorization_config")
public class VectorizationConfigEntity {

    /**
     * 配置ID
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
     * 算法类型：FASTTEXT, OPENAI, BERT
     */
    @Column(name = "algorithm", nullable = false)
    private String algorithm;

    /**
     * 向量维度
     */
    @Column(name = "dimension", nullable = false)
    private Integer dimension;

    /**
     * 生成速率（tokens/秒）
     */
    @Column(name = "generation_rate")
    private Integer generationRate;

    /**
     * API密钥（用于OpenAI等需要密钥的算法）
     */
    @Column(name = "api_key")
    private String apiKey;

    /**
     * 模型名称
     */
    @Column(name = "model_name")
    private String modelName;

    /**
     * 字段映射配置（JSON格式）
     */
    @Column(name = "field_mappings", columnDefinition = "TEXT")
    private String fieldMappings;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

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
        if (enabled == null) {
            enabled = true;
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
