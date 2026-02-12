package com.data.rsync.data.vo;

import lombok.Data;

/**
 * Milvus写入数据响应类
 */
@Data
public class MilvusWriteResponse {
    private int writtenCount;
    private long writeTimeMs;
}
