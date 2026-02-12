package com.data.rsync.task.vo;

import lombok.Data;

/**
 * 向量化预览请求对象
 */
@Data
public class VectorizationPreviewRequest {
    private String algorithm;
    private Integer dimension;
    private String modelName;
    private String sourceData;
}
