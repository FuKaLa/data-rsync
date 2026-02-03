package com.data.rsync.task.manager.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Milvus索引实体类
 */
@Data
@ToString
@TableName("milvus_index")
public class MilvusIndexEntity {

    /**
     * 索引ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 集合名称
     */
    @TableField(value = "collection_name", exist = true)
    private String collectionName;

    /**
     * 索引名称
     */
    @TableField(value = "index_name", exist = true)
    private String indexName;

    /**
     * 索引类型：IVF_FLAT, IVF_SQ8, IVF_PQ, HNSW, FLAT, BIN_FLAT
     */
    @TableField(value = "index_type", exist = true)
    private String indexType;

    /**
     * 度量类型：L2, IP, COSINE
     */
    @TableField(value = "metric_type", exist = true)
    private String metricType;

    /**
     * 向量维度
     */
    @TableField(value = "dimension", exist = true)
    private Integer dimension;

    /**
     * nlist参数（用于IVF系列索引）
     */
    @TableField(value = "nlist", exist = true)
    private Integer nlist;

    /**
     * efConstruction参数（用于HNSW索引）
     */
    @TableField(value = "ef_construction", exist = true)
    private Integer efConstruction;

    /**
     * M参数（用于HNSW索引）
     */
    @TableField(value = "m_param", exist = true)
    private Integer mParam;

    /**
     * 索引状态：READY, BUILDING, FAILED
     */
    @TableField(value = "status", exist = true)
    private String status;

    /**
     * 构建进度（百分比）
     */
    @TableField(value = "progress", exist = true)
    private Integer progress;

    /**
     * 内存占用（MB）
     */
    @TableField(value = "memory_usage", exist = true)
    private Integer memoryUsage;

    /**
     * 磁盘占用（MB）
     */
    @TableField(value = "disk_usage", exist = true)
    private Integer diskUsage;

    /**
     * 错误信息
     */
    @TableField(value = "error_message", exist = true)
    private String errorMessage;

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
        if (status == null) {
            status = "BUILDING";
        }
        if (progress == null) {
            progress = 0;
        }
    }

    /**
     * 更新时更新时间
     */
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

}
