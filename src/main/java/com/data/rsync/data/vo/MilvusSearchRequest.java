package com.data.rsync.data.vo;

import lombok.Data;

import java.util.List;

/**
 * Milvus搜索请求对象
 */
@Data
public class MilvusSearchRequest {
    private List<Float> vector;
    private Integer topK;
    private Float radius;
}
