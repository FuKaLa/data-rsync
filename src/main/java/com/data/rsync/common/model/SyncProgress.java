package com.data.rsync.common.model;

import lombok.Data;

/**
 * 同步进度模型
 */
@Data
public class SyncProgress {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 同步状态
     */
    private String status;

    /**
     * 进度百分比
     */
    private int progress;

    /**
     * 已处理记录数
     */
    private long processedCount;

    /**
     * 总记录数
     */
    private long totalCount;

    /**
     * 当前操作
     */
    private String currentOperation;

    /**
     * 预计剩余时间（秒）
     */
    private int estimatedTimeRemaining;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 最后更新时间
     */
    private long lastUpdateTime;

    /**
     * 构造方法
     * @param taskId 任务ID
     */
    public SyncProgress(Long taskId) {
        this.taskId = taskId;
        this.status = "PENDING";
        this.progress = 0;
        this.processedCount = 0;
        this.totalCount = 0;
        this.currentOperation = "准备同步";
        this.estimatedTimeRemaining = 0;
        this.startTime = System.currentTimeMillis();
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * 更新进度
     * @param progress 进度百分比
     * @param processedCount 已处理记录数
     * @param totalCount 总记录数
     * @param currentOperation 当前操作
     */
    public void updateProgress(int progress, long processedCount, long totalCount, String currentOperation) {
        this.progress = progress;
        this.processedCount = processedCount;
        this.totalCount = totalCount;
        this.currentOperation = currentOperation;
        this.lastUpdateTime = System.currentTimeMillis();
        
        // 简单估算剩余时间
        if (progress > 0) {
            long elapsedTime = (this.lastUpdateTime - this.startTime) / 1000;
            this.estimatedTimeRemaining = (int) ((elapsedTime * (100 - progress)) / progress);
        }
    }

    /**
     * 更新状态
     * @param status 状态
     */
    public void updateStatus(String status) {
        this.status = status;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * 更新错误信息
     * @param errorMessage 错误信息
     */
    public void updateError(String errorMessage) {
        this.errorMessage = errorMessage;
        this.status = "FAILED";
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * 完成同步
     */
    public void complete() {
        this.status = "COMPLETED";
        this.progress = 100;
        this.currentOperation = "同步完成";
        this.estimatedTimeRemaining = 0;
        this.lastUpdateTime = System.currentTimeMillis();
    }

}