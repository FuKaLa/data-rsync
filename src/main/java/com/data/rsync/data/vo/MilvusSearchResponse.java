package com.data.rsync.data.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Milvus搜索结果响应类
 */
@Data
public class MilvusSearchResponse {
    private List<MilvusSearchResultItem> results;
    private int totalCount;
    private long searchTimeMs;

    /**
     * 搜索结果项
     */
    @Data
    public static class MilvusSearchResultItem {
        private float score;
        private Map<String, Object> fields;
    }
}
