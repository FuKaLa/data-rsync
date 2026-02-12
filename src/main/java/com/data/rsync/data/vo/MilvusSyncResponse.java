package com.data.rsync.data.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Milvus同步数据响应类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MilvusSyncResponse {
    private boolean success;
    private long totalCount;
    private long syncedCount;
    private long failedCount;
    private long syncTimeMs;
    private String message;
    private String collectionName;
    
    // 可以添加额外的方法，如果需要
}
