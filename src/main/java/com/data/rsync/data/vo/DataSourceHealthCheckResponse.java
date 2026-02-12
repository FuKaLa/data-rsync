package com.data.rsync.data.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 数据源健康检查响应类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataSourceHealthCheckResponse {
    private boolean healthy;
    private String status;
    private long responseTimeMs;
    
    // 可以添加额外的方法，如果需要
}
