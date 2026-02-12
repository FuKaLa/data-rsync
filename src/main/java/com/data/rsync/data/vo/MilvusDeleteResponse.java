package com.data.rsync.data.vo;

import lombok.Data;

/**
 * Milvus删除数据响应类
 */
@Data
public class MilvusDeleteResponse {
    private int deletedCount;
    private long deleteTimeMs;
}
