package com.data.rsync.task.manager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Milvus索引实体类
 */
@Data
@ToString
@Entity
@Table(name = "milvus_index")
public class MilvusIndexEntity {

    /**
     * 索引ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 集合名称
     */
    @Column(name = "collection_name", nullable = false)
    private String collectionName;

    /**
     * 索引名称
     */
    @Column(name = "index_name", nullable = false)
    private String indexName;

    /**
     * 索引类型：IVF_FLAT, IVF_SQ8, IVF_PQ, HNSW, FLAT, BIN_FLAT
     */
    @Column(name = "index_type", nullable = false)
    private String indexType;

    /**
     * 度量类型：L2, IP, COSINE
     */
    @Column(name = "metric_type", nullable = false)
    private String metricType;

    /**
     * 向量维度
     */
    @Column(name = "dimension", nullable = false)
    private Integer dimension;

    /**
     * nlist参数（用于IVF系列索引）
     */
    @Column(name = "nlist")
    private Integer nlist;

    /**
     * efConstruction参数（用于HNSW索引）
     */
    @Column(name = "ef_construction")
    private Integer efConstruction;

    /**
     * M参数（用于HNSW索引）
     */
    @Column(name = "m_param")
    private Integer mParam;

    /**
     * 索引状态：READY, BUILDING, FAILED
     */
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * 构建进度（百分比）
     */
    @Column(name = "progress")
    private Integer progress;

    /**
     * 内存占用（MB）
     */
    @Column(name = "memory_usage")
    private Integer memoryUsage;

    /**
     * 磁盘占用（MB）
     */
    @Column(name = "disk_usage")
    private Integer diskUsage;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

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
    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

}
