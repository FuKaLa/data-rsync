package com.data.rsync.auth.vo;

import lombok.Data;

/**
 * 系统信息响应对象
 */
@Data
public class SystemInfoResponse {
    private String javaVersion;
    private String osName;
    private String osVersion;
    private int availableProcessors;
    private String maxMemory;
    private String totalMemory;
    private String freeMemory;
    private String systemTime;
}
