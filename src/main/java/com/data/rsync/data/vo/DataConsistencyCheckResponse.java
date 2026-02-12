package com.data.rsync.data.vo;

import lombok.Data;

import java.util.List;

/**
 * 数据一致性检查响应类
 */
@Data
public class DataConsistencyCheckResponse {
    private boolean consistent;
    private long sourceCount;
    private long targetCount;
    private int sampleCheckPassed;
    private int sampleCheckTotal;
    private List<String> discrepancies;
    private long checkTimeMs;
}
