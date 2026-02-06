package com.data.rsync.common.service;

import com.data.rsync.common.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据一致性校验服务
 * 确保源数据和目标数据的一致性
 */
@Service
@Slf4j
public class DataConsistencyService {

    /**
     * 数据一致性校验结果
     */
    public static class ConsistencyCheckResult {
        private boolean consistent;
        private long sourceCount;
        private long targetCount;
        private int sampleCheckPassed;
        private int sampleCheckTotal;
        private String errorMessage;
        private List<String> discrepancies;
        private long checkTimeMs;

        // Getters and setters
        public boolean isConsistent() { return consistent; }
        public void setConsistent(boolean consistent) { this.consistent = consistent; }
        public long getSourceCount() { return sourceCount; }
        public void setSourceCount(long sourceCount) { this.sourceCount = sourceCount; }
        public long getTargetCount() { return targetCount; }
        public void setTargetCount(long targetCount) { this.targetCount = targetCount; }
        public int getSampleCheckPassed() { return sampleCheckPassed; }
        public void setSampleCheckPassed(int sampleCheckPassed) { this.sampleCheckPassed = sampleCheckPassed; }
        public int getSampleCheckTotal() { return sampleCheckTotal; }
        public void setSampleCheckTotal(int sampleCheckTotal) { this.sampleCheckTotal = sampleCheckTotal; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public List<String> getDiscrepancies() { return discrepancies; }
        public void setDiscrepancies(List<String> discrepancies) { this.discrepancies = discrepancies; }
        public long getCheckTimeMs() { return checkTimeMs; }
        public void setCheckTimeMs(long checkTimeMs) { this.checkTimeMs = checkTimeMs; }

        @Override
        public String toString() {
            return "ConsistencyCheckResult{" +
                    "consistent=" + consistent +
                    ", sourceCount=" + sourceCount +
                    ", targetCount=" + targetCount +
                    ", sampleCheckPassed=" + sampleCheckPassed +
                    ", sampleCheckTotal=" + sampleCheckTotal +
                    ", errorMessage='" + errorMessage + '\'' +
                    ", discrepancies=" + discrepancies +
                    ", checkTimeMs=" + checkTimeMs +
                    '}';
        }
    }

    /**
     * 执行数据一致性校验
     * @param task 任务
     * @param sourceCount 源数据数量
     * @param targetCount 目标数据数量
     * @param sourceSampleData 源数据样本
     * @param targetSampleData 目标数据样本
     * @return 校验结果
     */
    public ConsistencyCheckResult checkDataConsistency(Task task, long sourceCount, long targetCount, 
                                                     List<Map<String, Object>> sourceSampleData, 
                                                     List<Map<String, Object>> targetSampleData) {
        log.info("Executing data consistency check for task: {}, sourceCount: {}, targetCount: {}", 
                task.getName(), sourceCount, targetCount);
        
        long startTime = System.currentTimeMillis();
        ConsistencyCheckResult result = new ConsistencyCheckResult();
        result.setSourceCount(sourceCount);
        result.setTargetCount(targetCount);
        result.setSampleCheckTotal(sourceSampleData != null ? sourceSampleData.size() : 0);
        result.setDiscrepancies(new ArrayList<>());

        try {
            // 1. 数量一致性校验
            checkCountConsistency(result);

            // 2. 样本数据一致性校验
            if (sourceSampleData != null && !sourceSampleData.isEmpty()) {
                checkSampleDataConsistency(result, sourceSampleData, targetSampleData);
            }

            // 3. 综合判断一致性
            if (result.getDiscrepancies().isEmpty()) {
                result.setConsistent(true);
            } else {
                result.setConsistent(false);
            }

            long endTime = System.currentTimeMillis();
            result.setCheckTimeMs(endTime - startTime);

            log.info("Data consistency check completed for task: {}, result: {}", task.getName(), result);
            return result;
        } catch (Exception e) {
            log.error("Failed to check data consistency for task {}: {}", task.getName(), e.getMessage(), e);
            result.setConsistent(false);
            result.setErrorMessage("Check failed: " + e.getMessage());
            result.setCheckTimeMs(System.currentTimeMillis() - startTime);
            return result;
        }
    }

    /**
     * 校验数量一致性
     * @param result 校验结果
     */
    private void checkCountConsistency(ConsistencyCheckResult result) {
        if (result.getSourceCount() != result.getTargetCount()) {
            String discrepancy = "Count mismatch: source=" + result.getSourceCount() + ", target=" + result.getTargetCount();
            result.getDiscrepancies().add(discrepancy);
            log.warn(discrepancy);
        }
    }

    /**
     * 校验样本数据一致性
     * @param result 校验结果
     * @param sourceSampleData 源数据样本
     * @param targetSampleData 目标数据样本
     */
    private void checkSampleDataConsistency(ConsistencyCheckResult result, 
                                           List<Map<String, Object>> sourceSampleData, 
                                           List<Map<String, Object>> targetSampleData) {
        if (targetSampleData == null || targetSampleData.isEmpty()) {
            String discrepancy = "Target sample data is empty";
            result.getDiscrepancies().add(discrepancy);
            log.warn(discrepancy);
            return;
        }

        int passed = 0;
        for (Map<String, Object> sourceData : sourceSampleData) {
            if (isDataPresentInTarget(sourceData, targetSampleData)) {
                passed++;
            } else {
                String discrepancy = "Source data not found in target: " + sourceData;
                result.getDiscrepancies().add(discrepancy);
                log.warn(discrepancy);
            }
        }

        result.setSampleCheckPassed(passed);
        log.info("Sample check: {}/{} passed", passed, sourceSampleData.size());
    }

    /**
     * 检查源数据是否存在于目标数据中
     * @param sourceData 源数据
     * @param targetSampleData 目标数据样本
     * @return 是否存在
     */
    private boolean isDataPresentInTarget(Map<String, Object> sourceData, List<Map<String, Object>> targetSampleData) {
        // 提取源数据的主键或唯一标识
        Object sourceId = sourceData.get("id");
        if (sourceId != null) {
            // 根据ID查找
            for (Map<String, Object> targetData : targetSampleData) {
                Object targetId = targetData.get("id");
                if (sourceId.equals(targetId)) {
                    // 进一步检查字段一致性
                    return checkFieldConsistency(sourceData, targetData);
                }
            }
        } else {
            // 根据所有字段查找
            for (Map<String, Object> targetData : targetSampleData) {
                if (checkFieldConsistency(sourceData, targetData)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查字段一致性
     * @param sourceData 源数据
     * @param targetData 目标数据
     * @return 字段是否一致
     */
    private boolean checkFieldConsistency(Map<String, Object> sourceData, Map<String, Object> targetData) {
        for (Map.Entry<String, Object> entry : sourceData.entrySet()) {
            String key = entry.getKey();
            if ("vector".equals(key)) {
                // 跳过向量字段的比较
                continue;
            }
            Object sourceValue = entry.getValue();
            Object targetValue = targetData.get(key);
            if (!equals(sourceValue, targetValue)) {
                log.debug("Field mismatch: key={}, source={}, target={}", key, sourceValue, targetValue);
                return false;
            }
        }
        return true;
    }

    /**
     * 比较两个对象是否相等
     * @param obj1 对象1
     * @param obj2 对象2
     * @return 是否相等
     */
    private boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }

    /**
     * 生成一致性校验报告
     * @param result 校验结果
     * @return 校验报告
     */
    public String generateConsistencyReport(ConsistencyCheckResult result) {
        StringBuilder report = new StringBuilder();
        report.append("===== Data Consistency Check Report =====\n");
        report.append("Consistent: " + result.isConsistent() + "\n");
        report.append("Source Count: " + result.getSourceCount() + "\n");
        report.append("Target Count: " + result.getTargetCount() + "\n");
        report.append("Sample Check: " + result.getSampleCheckPassed() + "/" + result.getSampleCheckTotal() + " passed\n");
        report.append("Check Time: " + result.getCheckTimeMs() + "ms\n");
        
        if (!result.getDiscrepancies().isEmpty()) {
            report.append("Discrepancies:\n");
            for (String discrepancy : result.getDiscrepancies()) {
                report.append("- " + discrepancy + "\n");
            }
        }
        
        if (result.getErrorMessage() != null) {
            report.append("Error: " + result.getErrorMessage() + "\n");
        }
        
        report.append("=====================================\n");
        return report.toString();
    }

    /**
     * 执行快速一致性检查（仅数量检查）
     * @param sourceCount 源数据数量
     * @param targetCount 目标数据数量
     * @return 是否一致
     */
    public boolean quickCheck(long sourceCount, long targetCount) {
        return sourceCount == targetCount;
    }

    /**
     * 执行深度一致性检查（数量+样本检查）
     * @param sourceCount 源数据数量
     * @param targetCount 目标数据数量
     * @param sourceSampleData 源数据样本
     * @param targetSampleData 目标数据样本
     * @return 是否一致
     */
    public boolean deepCheck(long sourceCount, long targetCount, 
                            List<Map<String, Object>> sourceSampleData, 
                            List<Map<String, Object>> targetSampleData) {
        Task dummyTask = new Task();
        dummyTask.setName("Deep Check Task");
        ConsistencyCheckResult result = checkDataConsistency(dummyTask, sourceCount, targetCount, sourceSampleData, targetSampleData);
        return result.isConsistent();
    }
}
