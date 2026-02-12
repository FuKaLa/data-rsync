package com.data.rsync.task.vo;

import lombok.Data;

/**
 * 批量重试错误数据响应对象
 */
@Data
public class BatchRetryResponse {
    private int totalCount;
    private int successCount;
    private int failureCount;
    private String message;
}
