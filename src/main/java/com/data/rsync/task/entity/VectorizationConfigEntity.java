package com.data.rsync.task.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 向量化配置实体类
 */
@Data
@ToString
@TableName("vectorization_config")
public class VectorizationConfigEntity {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    @TableField(value = "task_id", exist = true)
    private Long taskId;

    /**
     * 算法类型：FASTTEXT, OPENAI, BERT
     */
    @TableField(value = "algorithm", exist = true)
    private String algorithm;

    /**
     * 向量维度
     */
    @TableField(value = "dimension", exist = true)
    private Integer dimension;

    /**
     * 生成速率（tokens/秒）
     */
    @TableField(value = "generation_rate", exist = true)
    private Integer generationRate;

    /**
     * API密钥（用于OpenAI等需要密钥的算法）
     */
    @TableField(value = "api_key", exist = true)
    private String apiKey;

    /**
     * 模型名称
     */
    @TableField(value = "model_name", exist = true)
    private String modelName;

    /**
     * 字段映射配置（JSON格式）
     */
    @TableField(value = "field_mappings", exist = true)
    private String fieldMappings;

    /**
     * 是否启用
     */
    @TableField(value = "enabled", exist = true)
    private Boolean enabled;

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
        if (enabled == null) {
            enabled = true;
        }
    }

    /**
     * 更新时更新时间
     */
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

    // 新增方法：获取索引类型
    public String getIndexType() {
        return "HNSW"; // 默认索引类型
    }

    // 新增方法：获取度量类型
    public String getMetricType() {
        return "COSINE"; // 默认度量类型
    }

    // 新增方法：获取分片数量
    public Integer getShardsNum() {
        return 1; // 默认分片数量
    }

    // 新增方法：是否自动创建索引
    public Boolean isAutoCreateIndex() {
        return true; // 默认自动创建索引
    }

}
