package com.data.rsync.data.service.impl;

import com.data.rsync.common.config.DataRsyncProperties;
import com.data.rsync.common.model.Task;
import com.data.rsync.common.vectorizer.Vectorizer;
import com.data.rsync.common.vectorizer.VectorizerFactory;
import com.data.rsync.data.service.DataProcessService;
import com.data.rsync.common.exception.DataProcessException;
import com.data.rsync.common.utils.LogUtils;
import com.data.rsync.common.utils.ThreadPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据处理服务实现类
 */
@Service
public class DataProcessServiceImpl implements DataProcessService {



    @Autowired
    private DataRsyncProperties dataRsyncProperties;

    // 线程池用于并行处理数据
    private ExecutorService executorService;

    // 线程池监控和统计
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicLong totalTasks = new AtomicLong(0);
    private final AtomicLong failedTasks = new AtomicLong(0);
    private final AtomicLong successfulTasks = new AtomicLong(0);
    private final ConcurrentMap<Long, ProcessTaskState> processTaskStates = new ConcurrentHashMap<>();
    
    // 重试配置
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1000;

    // 构造函数
    public DataProcessServiceImpl() {
    }
    
    // 初始化方法，在依赖注入后执行
    @PostConstruct
    public void init() {
        // 从配置中获取线程池参数
        int corePoolSize = dataRsyncProperties.getData().getProcess().getThreadPool().getCoreSize();
        int maxPoolSize = dataRsyncProperties.getData().getProcess().getThreadPool().getMaxSize();
        int keepAliveSeconds = dataRsyncProperties.getData().getProcess().getThreadPool().getKeepAliveSeconds();
        int queueCapacity = dataRsyncProperties.getData().getProcess().getThreadPool().getQueueCapacity();
        
        // 使用线程池工具类创建线程池
        this.executorService = ThreadPoolUtil.createThreadPool(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                queueCapacity,
                "data-process"
        );
        
        LogUtils.info("数据处理线程池初始化完成，核心线程数: {}, 最大线程数: {}", corePoolSize, maxPoolSize);
    }
    
    /**
     * 处理任务状态
     */
    private static class ProcessTaskState {
        private final long startTime = System.currentTimeMillis();
        private long lastUpdatedTime = System.currentTimeMillis();
        private int total;
        private AtomicInteger processed = new AtomicInteger(0);
        private AtomicInteger succeeded = new AtomicInteger(0);
        private AtomicInteger failed = new AtomicInteger(0);
        private boolean started;
        private boolean completed;
        private String errorMessage;

        public void start() {
            this.started = true;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public void success() {
            this.succeeded.incrementAndGet();
            this.processed.incrementAndGet();
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public void fail(String errorMessage) {
            this.failed.incrementAndGet();
            this.processed.incrementAndGet();
            this.errorMessage = errorMessage;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public void complete() {
            this.completed = true;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public int getProcessed() {
            return processed.get();
        }

        public int getSucceeded() {
            return succeeded.get();
        }

        public int getFailed() {
            return failed.get();
        }

        public int getTotal() {
            return total;
        }

        public boolean isCompleted() {
            return completed;
        }

        public boolean isStarted() {
            return started;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getLastUpdatedTime() {
            return lastUpdatedTime;
        }

        public int getProgress() {
            return total > 0 ? (int) ((processed.get() * 100.0) / total) : 0;
        }
    }

    /**
     * 销毁线程池
     */
    @PreDestroy
    public void destroy() {
        ThreadPoolUtil.shutdownExecutorService(executorService);
        LogUtils.info("数据处理线程池已关闭");
    }

    /**
     * 获取批量大小
     */
    private int getBatchSize() {
        return dataRsyncProperties.getData().getProcess().getBatchSize();
    }

    /**
     * 获取超时时间（秒）
     */
    private int getTimeoutSeconds() {
        return dataRsyncProperties.getData().getProcess().getTimeoutSeconds();
    }

    @Override
    public boolean processDataChange(Long taskId, Map<String, Object> dataChange) {
        return executeWithRetry(() -> {
            // 初始化处理任务状态
            ProcessTaskState taskState = processTaskStates.computeIfAbsent(taskId, id -> new ProcessTaskState());
            taskState.start();
            
            try {
                activeTasks.incrementAndGet();
                totalTasks.incrementAndGet();
                
                // 1. 验证数据变更
                if (dataChange == null || dataChange.isEmpty()) {
                    LogUtils.warn("数据变更为空，任务ID: {}", taskId);
                    taskState.fail("数据变更为空");
                    return false;
                }

                // 2. 执行数据转换
                Map<String, Object> transformedData = executeDataTransform(new Task(), dataChange);

                // 3. 执行数据清洗
                Map<String, Object> cleanedData = executeDataCleaning(new Task(), transformedData);

                // 4. 生成向量
                float[] vector = generateVector(new Task(), cleanedData);

                // 5. 验证处理结果
                if (vector == null || vector.length == 0) {
                    LogUtils.error("向量生成失败，任务ID: {}", taskId);
                    taskState.fail("向量生成失败");
                    return false;
                }

                taskState.success();
                taskState.complete();
                successfulTasks.incrementAndGet();
                LogUtils.info("数据变更处理成功，任务ID: {}, 数据大小: {}", taskId, dataChange.size());
                return true;
            } catch (Exception e) {
                LogUtils.error("数据变更处理失败，任务ID: {}", taskId, e);
                failedTasks.incrementAndGet();
                taskState.fail(e.getMessage());
                taskState.complete();
                throw new DataProcessException("数据变更处理失败: " + e.getMessage(), e);
            } finally {
                activeTasks.decrementAndGet();
            }
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }
    
    /**
     * 带重试的执行
     */
    private <T> T executeWithRetry(Callable<T> task, int maxRetries, long delayMs) {
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                return task.call();
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new DataProcessException("执行失败，已达到最大重试次数: " + maxRetries, e);
                }
                LogUtils.warn("执行失败，正在重试 {}/{}", retryCount, maxRetries, e);
                try {
                    Thread.sleep(delayMs * retryCount); // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new DataProcessException("重试被中断", ie);
                }
            }
        }
        throw new DataProcessException("执行失败，未知错误");
    }
    
    /**
     * 带超时的执行
     */
    private <T> T executeWithTimeout(Callable<T> task, long timeoutMs) throws Exception {
        Future<T> future = executorService.submit(task);
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new DataProcessException("操作超时: " + timeoutMs + "ms");
        } catch (Exception e) {
            future.cancel(true);
            throw e;
        }
    }

    @Override
    public Map<String, Object> executeDataTransform(Task task, Map<String, Object> data) {
        Map<String, Object> transformedData = new HashMap<>(data);

        try {
            // 1. 执行字段映射
            executeFieldMapping(transformedData);

            // 2. 执行类型转换
            executeTypeConversion(transformedData);

            // 3. 执行数据格式化
            executeDataFormatting(transformedData);

            // 4. 执行业务规则转换
            executeBusinessRuleTransform(transformedData);

            LogUtils.debug("数据转换成功，转换前大小: {}, 转换后大小: {}", data.size(), transformedData.size());
        } catch (Exception e) {
            LogUtils.error("数据转换失败", e);
            throw new DataProcessException("数据转换失败: " + e.getMessage(), e);
        }

        return transformedData;
    }

    @Override
    public float[] generateVector(Task task, Map<String, Object> data) {
        try {
            // 1. 选择向量化算法
            String algorithm = "FASTTEXT";
            int dimension = dataRsyncProperties.getVectorDb().getMilvus().getVector().getDimension();

            // 2. 获取向量化器
            Vectorizer vectorizer = VectorizerFactory.getVectorizer(algorithm);

            // 3. 准备向量化数据
            String text = prepareVectorizationText(data);

            // 4. 执行向量化
            float[] vector = vectorizer.vectorize(text);

            LogUtils.debug("向量生成成功，算法: {}, 维度: {}, 文本长度: {}", algorithm, dimension, text.length());
            return vector;
        } catch (Exception e) {
            LogUtils.error("向量生成失败", e);
            throw new DataProcessException("向量生成失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> executeDataCleaning(Task task, Map<String, Object> data) {
        Map<String, Object> cleanedData = new HashMap<>(data);

        try {
            // 1. 移除空值
            removeNullValues(cleanedData);

            // 2. 移除重复值
            removeDuplicateValues(cleanedData);

            // 3. 执行数据标准化
            executeDataStandardization(cleanedData);

            // 4. 执行数据验证
            validateData(cleanedData);

            LogUtils.debug("数据清洗成功，清洗前大小: {}, 清洗后大小: {}", data.size(), cleanedData.size());
        } catch (Exception e) {
            LogUtils.error("数据清洗失败", e);
            throw new DataProcessException("数据清洗失败: " + e.getMessage(), e);
        }

        return cleanedData;
    }

    @Override
    public boolean batchProcessData(Long taskId, List<Map<String, Object>> dataList) {
        return executeWithRetry(() -> {
            // 初始化处理任务状态
            ProcessTaskState taskState = processTaskStates.computeIfAbsent(taskId, id -> new ProcessTaskState());
            taskState.start();
            taskState.setTotal(dataList.size());
            
            try {
                activeTasks.incrementAndGet();
                totalTasks.incrementAndGet();
                
                if (dataList == null || dataList.isEmpty()) {
                    LogUtils.warn("批量处理数据为空，任务ID: {}", taskId);
                    taskState.fail("批量处理数据为空");
                    taskState.complete();
                    return false;
                }

                // 获取批次大小
                int batchSize = getBatchSize();
                LogUtils.info("开始批量处理数据，任务ID: {}, 总数据量: {}, 批次大小: {}", taskId, dataList.size(), batchSize);

                // 分批处理数据
                AtomicInteger totalProcessed = new AtomicInteger(0);
                AtomicInteger successCount = new AtomicInteger(0);
                AtomicInteger failureCount = new AtomicInteger(0);

                // 计算批次数量
                int totalBatches = (dataList.size() + batchSize - 1) / batchSize;
                LogUtils.info("总批次数: {}", totalBatches);

        // 用于记录处理失败的数据，以便重试
        List<Map<String, Object>> failedData = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, dataList.size());
            List<Map<String, Object>> batchData = dataList.subList(i, endIndex);
            int batchNumber = (i / batchSize) + 1;

                    LogUtils.debug("处理批次: {}/{}, 批次数据量: {}", batchNumber, totalBatches, batchData.size());

                    // 并行处理当前批次
                    List<Future<Map.Entry<Boolean, Map<String, Object>>>> futures = new ArrayList<>();
                    for (Map<String, Object> data : batchData) {
                        futures.add(executorService.submit(() -> {
                            try {
                                boolean result = processDataChange(taskId, data);
                                return Map.entry(result, data);
                            } catch (Exception e) {
                                LogUtils.error("单条数据处理失败", e);
                                return Map.entry(false, data);
                            }
                        }));
                    }

                    // 收集当前批次的处理结果
                    int timeoutSeconds = getTimeoutSeconds();
                    for (Future<Map.Entry<Boolean, Map<String, Object>>> future : futures) {
                        try {
                            Map.Entry<Boolean, Map<String, Object>> resultEntry = future.get(timeoutSeconds, TimeUnit.SECONDS);
                            if (resultEntry.getKey()) {
                                successCount.incrementAndGet();
                                taskState.success();
                            } else {
                                failureCount.incrementAndGet();
                                failedData.add(resultEntry.getValue());
                                taskState.fail("数据处理失败");
                            }
                        } catch (Exception e) {
                            LogUtils.error("获取处理结果失败", e);
                            failureCount.incrementAndGet();
                            taskState.fail("获取处理结果失败: " + e.getMessage());
                        } finally {
                            totalProcessed.incrementAndGet();
                        }
                    }

                    // 记录批次处理结果
                    LogUtils.info("批次: {}/{} 处理完成，成功: {}, 失败: {}", batchNumber, totalBatches, successCount.get(), failureCount.get());
                }

                // 尝试重试处理失败的数据
                if (!failedData.isEmpty()) {
                    LogUtils.info("开始重试处理失败的数据，任务ID: {}, 失败数据量: {}", taskId, failedData.size());
                    int retrySuccessCount = 0;
                    
                    for (Map<String, Object> data : failedData) {
                        try {
                            boolean retryResult = processDataChange(taskId, data);
                            if (retryResult) {
                                retrySuccessCount++;
                                successCount.incrementAndGet();
                                failureCount.decrementAndGet();
                                taskState.success();
                            }
                        } catch (Exception e) {
                            LogUtils.error("重试处理数据失败", e);
                        }
                    }
                    
                    LogUtils.info("重试处理完成，任务ID: {}, 成功: {}, 失败: {}", taskId, retrySuccessCount, failedData.size() - retrySuccessCount);
                }

                // 计算总体处理结果
                boolean allSuccess = failureCount.get() == 0;
                boolean partialSuccess = successCount.get() > 0;
                
                if (allSuccess) {
                    taskState.complete();
                    successfulTasks.incrementAndGet();
                } else if (partialSuccess) {
                    taskState.complete();
                    // 部分成功，记录失败情况
                    LogUtils.warn("批量处理数据部分成功，任务ID: {}, 成功: {}, 失败: {}", taskId, successCount.get(), failureCount.get());
                } else {
                    taskState.complete();
                    failedTasks.incrementAndGet();
                }
                
                LogUtils.info("批量处理数据完成，任务ID: {}, 总数据量: {}, 成功: {}, 失败: {}, 处理结果: {}", 
                        taskId, dataList.size(), successCount.get(), failureCount.get(), allSuccess);

                return allSuccess;
            } catch (Exception e) {
                LogUtils.error("批量处理数据失败，任务ID: {}", taskId, e);
                taskState.fail(e.getMessage());
                taskState.complete();
                failedTasks.incrementAndGet();
                throw new DataProcessException("批量处理数据失败: " + e.getMessage(), e);
            } finally {
                activeTasks.decrementAndGet();
            }
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    @Override
    public String getProcessStatus(Long taskId) {
        ProcessTaskState taskState = processTaskStates.get(taskId);
        if (taskState == null) {
            return "NOT_FOUND";
        }
        
        if (!taskState.isStarted()) {
            return "NOT_STARTED";
        }
        
        if (taskState.isCompleted()) {
            return taskState.getFailed() > 0 ? "COMPLETED_WITH_ERRORS" : "COMPLETED";
        }
        
        return "PROCESSING";
    }

    // 内部方法，用于获取详细状态
    public Map<String, Object> getProcessDetailedStatus(Long taskId) {
        ProcessTaskState taskState = processTaskStates.get(taskId);
        if (taskState == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("status", "NOT_FOUND");
            result.put("progress", 0);
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("status", getProcessStatus(taskId));
        result.put("progress", taskState.getProgress());
        result.put("total", taskState.getTotal());
        result.put("processed", taskState.getProcessed());
        result.put("succeeded", taskState.getSucceeded());
        result.put("failed", taskState.getFailed());
        result.put("startTime", taskState.getStartTime());
        result.put("lastUpdatedTime", taskState.getLastUpdatedTime());
        result.put("errorMessage", taskState.getErrorMessage());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @Override
    public String checkProcessHealth(Long taskId) {
        try {
            // 检查线程池健康状态
            ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
            int activeCount = executor.getActiveCount();
            int queueSize = executor.getQueue().size();
            int poolSize = executor.getPoolSize();
            int maxPoolSize = executor.getMaximumPoolSize();
            
            // 检查任务状态
            ProcessTaskState taskState = processTaskStates.get(taskId);
            boolean taskHealthy = taskState == null || taskState.isCompleted() || 
                (System.currentTimeMillis() - taskState.getLastUpdatedTime() < 5 * 60 * 1000); // 5分钟内有更新
            
            // 检查系统资源
            Runtime runtime = Runtime.getRuntime();
            long freeMemory = runtime.freeMemory();
            long totalMemory = runtime.totalMemory();
            double memoryUsage = (1.0 - (double) freeMemory / totalMemory) * 100;
            
            // 获取队列容量
            int queueCapacity = dataRsyncProperties.getData().getProcess().getThreadPool().getQueueCapacity();
            
            // 综合判断健康状态
            if (activeCount >= maxPoolSize * 0.9) {
                return "POOL_OVERLOADED";
            }
            
            if (queueSize >= queueCapacity * 0.9) {
                return "QUEUE_FULL";
            }
            
            if (memoryUsage >= 90) {
                return "MEMORY_CRITICAL";
            }
            
            if (!taskHealthy) {
                return "TASK_STALLED";
            }
            
            return "HEALTHY";
        } catch (Exception e) {
            LogUtils.error("健康检查失败，任务ID: {}", taskId, e);
            return "HEALTH_CHECK_FAILED";
        }
    }

    @Override
    public boolean cleanProcessedRecords(Long taskId) {
        return executeWithRetry(() -> {
            try {
                // 清理任务状态
                ProcessTaskState removedState = processTaskStates.remove(taskId);
                if (removedState != null) {
                    LogUtils.info("处理记录清理成功，任务ID: {}, 处理记录: {}", taskId, removedState.getProcessed());
                } else {
                    LogUtils.warn("处理记录不存在，任务ID: {}", taskId);
                }
                
                // 这里可以添加其他清理逻辑，如清理临时文件、数据库记录等
                
                return true;
            } catch (Exception e) {
                LogUtils.error("处理记录清理失败，任务ID: {}", taskId, e);
                throw new DataProcessException("处理记录清理失败: " + e.getMessage(), e);
            }
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    // 内部方法，用于获取处理统计信息
    public Map<String, Object> getProcessStatistics() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
        
        Map<String, Object> result = new HashMap<>();
        result.put("activeTasks", activeTasks.get());
        result.put("totalTasks", totalTasks.get());
        result.put("successfulTasks", successfulTasks.get());
        result.put("failedTasks", failedTasks.get());
        
        Map<String, Object> threadPoolInfo = new HashMap<>();
        threadPoolInfo.put("activeCount", executor.getActiveCount());
        threadPoolInfo.put("poolSize", executor.getPoolSize());
        threadPoolInfo.put("maxPoolSize", executor.getMaximumPoolSize());
        threadPoolInfo.put("queueSize", executor.getQueue().size());
        threadPoolInfo.put("completedTaskCount", executor.getCompletedTaskCount());
        
        result.put("threadPool", threadPoolInfo);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    // 内部方法，用于取消处理任务
    public boolean cancelProcessTask(Long taskId) {
        LogUtils.info("取消处理任务，任务ID: {}", taskId);
        
        ProcessTaskState taskState = processTaskStates.get(taskId);
        if (taskState != null && !taskState.isCompleted()) {
            // 实现真正的任务取消逻辑
            // 1. 标记任务状态为取消
            taskState.fail("Task canceled by user");
            taskState.complete();
            
            // 2. 这里可以添加更复杂的取消逻辑，例如：
            // - 中断正在执行的线程
            // - 从线程池中移除任务
            // - 清理相关资源
            
            LogUtils.info("处理任务已取消，任务ID: {}", taskId);
            return true;
        }
        
        LogUtils.info("处理任务不存在或已完成，任务ID: {}", taskId);
        return false;
    }

    /**
     * 执行字段映射
     * @param data 数据
     */
    private void executeFieldMapping(Map<String, Object> data) {
        // 示例：将old_field映射到new_field
        if (data.containsKey("old_field")) {
            data.put("new_field", data.get("old_field"));
            data.remove("old_field");
        }
    }

    /**
     * 执行类型转换
     * @param data 数据
     */
    private void executeTypeConversion(Map<String, Object> data) {
        // 示例：将字符串转换为数字
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                String strValue = (String) value;
                // 尝试转换为数字
                try {
                    if (strValue.contains(".")) {
                        data.put(key, Double.parseDouble(strValue));
                    } else {
                        data.put(key, Long.parseLong(strValue));
                    }
                } catch (NumberFormatException e) {
                    // 转换失败，保持原类型
                }
            }
        }
    }

    /**
     * 执行数据格式化
     * @param data 数据
     */
    private void executeDataFormatting(Map<String, Object> data) {
        // 示例：格式化日期
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Date) {
                // 格式化日期为字符串
                data.put(key, value.toString());
            }
        }
    }

    /**
     * 执行业务规则转换
     * @param data 数据
     */
    private void executeBusinessRuleTransform(Map<String, Object> data) {
        // 示例：根据业务规则转换数据
        if (data.containsKey("status")) {
            String status = data.get("status").toString();
            if ("ACTIVE".equals(status)) {
                data.put("status_code", 1);
            } else if ("INACTIVE".equals(status)) {
                data.put("status_code", 0);
            }
        }
    }

    /**
     * 移除空值
     * @param data 数据
     */
    private void removeNullValues(Map<String, Object> data) {
        Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() == null || "".equals(entry.getValue())) {
                iterator.remove();
            }
        }
    }

    /**
     * 移除重复值
     * @param data 数据
     */
    private void removeDuplicateValues(Map<String, Object> data) {
        // 实现更复杂的重复值检测逻辑
        // 1. 检查特定字段的重复值
        // 2. 检查不同字段之间的重复内容
        
        // 示例：检查ID相关字段的重复
        if (data.containsKey("id") && data.containsKey("primaryKey")) {
            if (data.get("id").equals(data.get("primaryKey"))) {
                data.remove("primaryKey");
                LogUtils.debug("移除重复字段: primaryKey");
            }
        }
        
        // 示例：检查名称字段的重复
        if (data.containsKey("name") && data.containsKey("title")) {
            if (data.get("name").equals(data.get("title"))) {
                data.remove("title");
                LogUtils.debug("移除重复字段: title");
            }
        }
        
        // 可以根据具体业务需求添加更多的重复值检测逻辑
    }

    /**
     * 执行数据标准化
     * @param data 数据
     */
    private void executeDataStandardization(Map<String, Object> data) {
        // 示例：标准化字符串大小写
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                String strValue = (String) value;
                // 标准化为小写
                data.put(key, strValue.toLowerCase());
            }
        }
    }

    /**
     * 验证数据
     * @param data 数据
     */
    private void validateData(Map<String, Object> data) {
        // 示例：验证必填字段
        List<String> requiredFields = Arrays.asList("id", "name", "status");
        for (String field : requiredFields) {
            if (!data.containsKey(field)) {
                LogUtils.warn("缺少必填字段: {}", field);
                // 可以选择抛出异常或设置默认值
            }
        }
    }

    /**
     * 准备向量化文本
     * @param data 数据
     * @return 向量化文本
     */
    private String prepareVectorizationText(Map<String, Object> data) {
        StringBuilder textBuilder = new StringBuilder();

        // 遍历数据，将所有字段值拼接为文本
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value != null) {
                textBuilder.append(key).append(": ").append(value).append(" ");
            }
        }

        return textBuilder.toString().trim();
    }
}
