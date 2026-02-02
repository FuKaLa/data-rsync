package com.data.rsync.common.feign;

import com.data.rsync.common.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Milvus同步服务Feign客户端降级实现
 */
@Component
@Slf4j
public class MilvusSyncFeignFallback implements MilvusSyncFeignClient {

    @Override
    public boolean writeDataToMilvus(Long taskId, Map<String, Object> data) {
        log.warn("MilvusSync service fallback: writeDataToMilvus, taskId={}", taskId);
        return false;
    }

    @Override
    public boolean batchWriteDataToMilvus(Long taskId, List<Map<String, Object>> dataList) {
        log.warn("MilvusSync service fallback: batchWriteDataToMilvus, taskId={}", taskId);
        return false;
    }

    @Override
    public boolean deleteDataFromMilvus(Long taskId, Object primaryKey) {
        log.warn("MilvusSync service fallback: deleteDataFromMilvus, taskId={}", taskId);
        return false;
    }

    @Override
    public boolean createMilvusCollection(Task task) {
        log.warn("MilvusSync service fallback: createMilvusCollection");
        return false;
    }

    @Override
    public boolean createMilvusIndex(Task task) {
        log.warn("MilvusSync service fallback: createMilvusIndex");
        return false;
    }

    @Override
    public Map<String, Object> checkDataConsistency(Task task, long sourceCount, List<Map<String, Object>> sampleData) {
        log.warn("MilvusSync service fallback: checkDataConsistency");
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("consistent", false);
        result.put("errorMessage", "Service fallback");
        return result;
    }

    @Override
    public String getSyncStatus(Long taskId) {
        log.warn("MilvusSync service fallback: getSyncStatus, taskId={}", taskId);
        return "UNKNOWN";
    }

    @Override
    public boolean checkMilvusConnection() {
        log.warn("MilvusSync service fallback: checkMilvusConnection");
        return false;
    }
}
