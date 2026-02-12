package com.data.rsync.data.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Milvus验证数据响应类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MilvusVerifyResponse {
    private boolean success;
    private long expectedCount;
    private long actualCount;
    private boolean countMatch;
    private List<String> sampleData;
    private long verifyTimeMs;
    private String message;
    private String collectionName;
    
    // 可以添加额外的方法，如果需要
}
