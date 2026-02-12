package com.data.rsync.task.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 向量化预览响应对象
 */
@Data
public class VectorizationPreviewResponse {
    private boolean success;
    private String message;
    private float[] vector;
    private int dimension;
    private String algorithm;
    private String sourceData;
    private LocalDateTime previewTime;
}
