package com.data.rsync.data.process.service.impl;

import com.data.rsync.common.constants.DataRsyncConstants;
import com.data.rsync.common.model.Task;
import com.data.rsync.common.vectorizer.Vectorizer;
import com.data.rsync.common.vectorizer.VectorizerFactory;
import com.data.rsync.data.process.service.DataProcessService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 数据处理服务实现类
 */
@Service
@Slf4j
public class DataProcessServiceImpl implements DataProcessService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 处理任务状态缓存
     */
    private final Map<Long, String> processStatusMap = new ConcurrentHashMap<>();

    /**
     * 向量化缓存
     */
    private final Map<String, float[]> vectorCache = new ConcurrentHashMap<>();

    /**
     * 执行器服务
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    /**
     * 缓存大小限制
     */
    private static final int CACHE_SIZE_LIMIT = 10000;

    /**
     * 处理数据变更
     * @param taskId 任务ID
     * @param dataChange 数据变更
     * @return 处理结果
     */
    @Override
    public boolean processDataChange(Long taskId, Map<String, Object> dataChange) {
        log.info("Processing data change for task: {}", taskId);
        try {
            // 1. 检查任务状态
            if (!processStatusMap.containsKey(taskId)) {
                processStatusMap.put(taskId, "RUNNING");
                redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.DATA_PROCESS_PREFIX + taskId, "RUNNING");
            }

            // 2. 从Redis获取任务配置（实际项目中可能需要从任务管理服务获取）
            String taskConfigStr = redisTemplate.opsForValue().get(DataRsyncConstants.RedisKey.TASK_CONFIG_PREFIX + taskId);
            Task task = null;
            if (taskConfigStr != null) {
                // 实际项目中需要反序列化taskConfigStr为Task对象
                task = new Task();
                // 模拟任务配置
                String config = "{\"cleaningRules\": \"remove_empty,trim_whitespace,validate_format\", \"transformationRules\": \"field_mapping,type_conversion,value_normalization\", \"vectorizationRules\": \"text_feature,use_all_fields\", \"vectorizerName\": \"text_feature\"}";
                task.setConfig(config);
            }

            // 3. 执行数据清洗
            // 从任务配置中获取清洗规则
            Map<String, Object> cleanedData = executeDataCleaning(task, dataChange);

            // 4. 执行数据转换
            // 从任务配置中获取转换规则
            Map<String, Object> transformedData = executeDataTransform(task, cleanedData);

            // 5. 生成向量
            // 从任务配置中获取向量化规则
            float[] vector = generateVector(task, transformedData);

            // 6. 构建处理结果
            Map<String, Object> processedData = new HashMap<>(transformedData);
            processedData.put("vector", vector);

            // 7. 发送处理结果到 Kafka
            sendProcessedDataToKafka(taskId, processedData);

            log.info("Processed data change for task: {}", taskId);
            return true;
        } catch (Exception e) {
            log.error("Failed to process data change for task {}: {}", taskId, e.getMessage(), e);
            processStatusMap.put(taskId, "FAILED");
            redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.DATA_PROCESS_PREFIX + taskId, "FAILED");
            return false;
        }
    }

    /**
     * 执行数据转换
     * @param task 任务
     * @param data 原始数据
     * @return 转换后的数据
     */
    @Override
    public Map<String, Object> executeDataTransform(Task task, Map<String, Object> data) {
        log.info("Executing data transform");
        try {
            // 实现具体的数据转换逻辑
            // 1. 字段映射
            // 2. 类型转换
            // 3. 重命名字段
            
            // 获取转换规则
            Set<String> transformationRules = new HashSet<>();
            if (task != null && task.getConfig() != null) {
                // 实际项目中需要解析task.getConfig()为Map
                // 这里简单示例：直接使用默认规则
                transformationRules.addAll(Arrays.asList("field_mapping", "type_conversion", "value_normalization"));
            }
            
            // 如果没有配置转换规则，使用默认规则
            if (transformationRules.isEmpty()) {
                transformationRules.addAll(Arrays.asList("field_mapping", "type_conversion", "value_normalization"));
            }
            
            log.info("Applying transformation rules: {}", transformationRules);

            // 执行数据转换
            Map<String, Object> transformedData = new HashMap<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 1. 字段映射
                if (transformationRules.contains("field_mapping")) {
                    // 实际项目中可能需要从配置中获取字段映射规则
                    // 这里简单示例：如果字段名为"user_name"，映射为"username"
                    if ("user_name".equals(key)) {
                        key = "username";
                    } else if ("user_age".equals(key)) {
                        key = "age";
                    } else if ("user_email".equals(key)) {
                        key = "email";
                    }
                }
                
                // 2. 类型转换
                if (transformationRules.contains("type_conversion")) {
                    // 实际项目中可能需要从配置中获取类型转换规则
                    // 这里简单示例：将字符串类型的数字转换为整数
                    if (value instanceof String) {
                        String strValue = (String) value;
                        if (strValue.matches("\\d+")) {
                            try {
                                value = Integer.parseInt(strValue);
                            } catch (NumberFormatException e) {
                                // 转换失败，保持原值
                            }
                        } else if (strValue.matches("\\d+\\.\\d+")) {
                            try {
                                value = Double.parseDouble(strValue);
                            } catch (NumberFormatException e) {
                                // 转换失败，保持原值
                            }
                        } else if ("true".equalsIgnoreCase(strValue) || "false".equalsIgnoreCase(strValue)) {
                            value = Boolean.parseBoolean(strValue);
                        }
                    }
                }
                
                // 3. 重命名字段
                if (transformationRules.contains("value_normalization")) {
                    // 实际项目中可能需要从配置中获取值规范化规则
                    // 这里简单示例：对字符串值进行trim操作
                    if (value instanceof String) {
                        value = ((String) value).trim();
                    }
                }
                
                transformedData.put(key, value);
            }

            log.info("Executed data transform, transformed {} fields", transformedData.size());
            return transformedData;
        } catch (Exception e) {
            log.error("Failed to execute data transform: {}", e.getMessage(), e);
            return data; // 转换失败返回原始数据
        }
    }

    /**
     * 生成向量
     * @param task 任务
     * @param data 原始数据
     * @return 向量
     */
    @Override
    public float[] generateVector(Task task, Map<String, Object> data) {
        log.info("Generating vector");
        try {
            // 1. 提取文本特征
            String text = extractTextFeatures(data);
            
            // 2. 生成缓存键
            String cacheKey = generateCacheKey(text);
            
            // 3. 检查缓存
            if (vectorCache.containsKey(cacheKey)) {
                log.debug("Vector found in cache for key: {}", cacheKey);
                return vectorCache.get(cacheKey);
            }
            
            // 4. 获取向量化器
            // 从任务配置中获取向量化器名称，默认为 text_feature
            String vectorizerName = "text_feature";
            if (task != null && task.getConfig() != null) {
                // 实际项目中需要解析task.getConfig()为Map
                // 这里简单示例：使用默认向量化器
                log.info("Using default vectorizer: {}", vectorizerName);
            }
            
            // 5. 使用向量化器生成向量
            Vectorizer vectorizer = VectorizerFactory.getVectorizer(vectorizerName);
            float[] vector = vectorizer.vectorize(data);

            // 6. 更新缓存
            updateVectorCache(cacheKey, vector);

            log.info("Generated vector with dimension: {} using vectorizer: {}", vector.length, vectorizerName);
            return vector;
        } catch (Exception e) {
            log.error("Failed to generate vector: {}", e.getMessage(), e);
            return new float[0]; // 生成失败返回空向量
        }
    }

    /**
     * 生成缓存键
     * @param text 文本特征
     * @return 缓存键
     */
    private String generateCacheKey(String text) {
        // 使用文本的哈希值作为缓存键
        return String.valueOf(text.hashCode());
    }

    /**
     * 更新向量缓存
     * @param key 缓存键
     * @param vector 向量
     */
    private void updateVectorCache(String key, float[] vector) {
        // 检查缓存大小
        if (vectorCache.size() >= CACHE_SIZE_LIMIT) {
            // 移除最旧的缓存项（简单实现，实际应用中可使用LRU缓存）
            String oldestKey = vectorCache.keySet().iterator().next();
            vectorCache.remove(oldestKey);
        }
        // 添加新的缓存项
        vectorCache.put(key, vector);
    }

    /**
     * 提取文本特征
     * @param data 原始数据
     * @return 文本特征
     */
    private String extractTextFeatures(Map<String, Object> data) {
        StringBuilder textBuilder = new StringBuilder();
        
        // 遍历数据中的所有字段，提取文本特征
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // 跳过向量字段
            if ("vector".equals(key)) {
                continue;
            }
            
            // 将字段值转换为文本
            if (value != null) {
                textBuilder.append(key).append(": ").append(value.toString()).append(" ");
            }
        }
        
        return textBuilder.toString().trim();
    }

    /**
     * 执行数据清洗
     * @param task 任务
     * @param data 原始数据
     * @return 清洗后的数据
     */
    @Override
    public Map<String, Object> executeDataCleaning(Task task, Map<String, Object> data) {
        log.info("Executing data cleaning");
        try {
            // 实现具体的数据清洗逻辑
            // 1. 空值处理
            // 2. 重复数据去重
            // 3. 数据格式校验
            
            // 获取清洗规则
            Set<String> cleaningRules = new HashSet<>();
            if (task != null && task.getConfig() != null) {
                // 实际项目中需要解析task.getConfig()为Map
                // 这里简单示例：直接使用默认规则
                cleaningRules.addAll(Arrays.asList("remove_empty", "trim_whitespace", "validate_format"));
            }
            
            // 如果没有配置清洗规则，使用默认规则
            if (cleaningRules.isEmpty()) {
                cleaningRules.addAll(Arrays.asList("remove_empty", "trim_whitespace", "validate_format"));
            }
            
            log.info("Applying cleaning rules: {}", cleaningRules);

            // 执行数据清洗
            Map<String, Object> cleanedData = new HashMap<>();
            Set<String> processedKeys = new HashSet<>(); // 用于去重
            
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 2. 重复数据去重
                if (cleaningRules.contains("remove_duplicates")) {
                    if (processedKeys.contains(key)) {
                        log.debug("Removing duplicate key: {}", key);
                        continue;
                    }
                    processedKeys.add(key);
                }
                
                // 1. 空值处理
                if (value == null) {
                    // 根据配置处理空值
                    String nullHandlingStrategy = "empty_string"; // 默认策略：使用空字符串
                    // 实际项目中需要从task.getConfig()中获取nullHandlingStrategy
                    
                    log.debug("Handling null value for key {} using strategy: {}", key, nullHandlingStrategy);
                    
                    switch (nullHandlingStrategy) {
                        case "empty_string":
                            value = "";
                            break;
                        case "zero":
                            value = 0;
                            break;
                        case "null":
                            value = null;
                            break;
                        case "default_value":
                            // 实际项目中可能需要从配置中获取默认值
                            value = "default";
                            break;
                        default:
                            value = "";
                    }
                } else {
                    // 3. 数据格式校验和清洗
                    if (cleaningRules.contains("trim_whitespace") && value instanceof String) {
                        value = ((String) value).trim();
                    }
                    
                    if (cleaningRules.contains("validate_format")) {
                        // 实际项目中可能需要从配置中获取格式校验规则
                        // 这里简单示例：校验邮箱格式
                        if ("email".equals(key) && value instanceof String) {
                            String email = (String) value;
                            if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
                                log.warn("Invalid email format for key {}: {}", key, email);
                                // 实际项目中可能需要根据配置决定如何处理无效格式
                                // 这里简单示例：标记为无效邮箱
                                value = "invalid_email:" + email;
                            }
                        }
                    }
                }
                
                cleanedData.put(key, value);
            }

            log.info("Executed data cleaning, cleaned {} fields", cleanedData.size());
            return cleanedData;
        } catch (Exception e) {
            log.error("Failed to execute data cleaning: {}", e.getMessage(), e);
            return data; // 清洗失败返回原始数据
        }
    }

    /**
     * 批量处理数据
     * @param taskId 任务ID
     * @param dataList 数据列表
     * @return 处理结果
     */
    @Override
    public boolean batchProcessData(Long taskId, List<Map<String, Object>> dataList) {
        log.info("Batch processing data for task {}: {}", taskId, dataList.size());
        try {
            // 1. 检查任务状态
            if (!processStatusMap.containsKey(taskId)) {
                processStatusMap.put(taskId, "RUNNING");
                redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.DATA_PROCESS_PREFIX + taskId, "RUNNING");
            }

            // 2. 并行处理数据
            int batchSize = 1000;
            List<List<Map<String, Object>>> batches = splitIntoBatches(dataList, batchSize);
            CountDownLatch latch = new CountDownLatch(batches.size());
            List<Map<String, Object>> allProcessedData = Collections.synchronizedList(new ArrayList<>());

            for (List<Map<String, Object>> batch : batches) {
                executorService.submit(() -> {
                    try {
                        for (Map<String, Object> data : batch) {
                            // 执行数据清洗
                            Map<String, Object> cleanedData = executeDataCleaning(null, data);
                            // 执行数据转换
                            Map<String, Object> transformedData = executeDataTransform(null, cleanedData);
                            // 生成向量
                            float[] vector = generateVector(null, transformedData);
                            // 构建处理结果
                            Map<String, Object> processedData = new HashMap<>(transformedData);
                            processedData.put("vector", vector);
                            // 添加到结果列表
                            allProcessedData.add(processedData);
                        }
                    } catch (Exception e) {
                        log.error("Failed to process batch data: {}", e.getMessage(), e);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 等待所有批次处理完成
            latch.await();

            // 3. 批量发送处理结果到 Kafka
            if (!allProcessedData.isEmpty()) {
                batchSendProcessedDataToKafka(taskId, allProcessedData);
            }

            log.info("Batch processed {} data items for task: {}", dataList.size(), taskId);
            return true;
        } catch (Exception e) {
            log.error("Failed to batch process data for task {}: {}", taskId, e.getMessage(), e);
            processStatusMap.put(taskId, "FAILED");
            redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.DATA_PROCESS_PREFIX + taskId, "FAILED");
            return false;
        }
    }

    /**
     * 将列表分割为多个批次
     * @param list 原始列表
     * @param batchSize 批次大小
     * @return 批次列表
     */
    private <T> List<List<T>> splitIntoBatches(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, list.size());
            batches.add(list.subList(i, endIndex));
        }
        return batches;
    }

    /**
     * 获取处理状态
     * @param taskId 任务ID
     * @return 处理状态
     */
    @Override
    public String getProcessStatus(Long taskId) {
        log.info("Getting process status for task: {}", taskId);
        try {
            // 1. 从缓存获取状态
            String status = processStatusMap.get(taskId);
            if (status != null) {
                return status;
            }

            // 2. 从 Redis 获取状态
            status = redisTemplate.opsForValue().get(DataRsyncConstants.RedisKey.DATA_PROCESS_PREFIX + taskId);
            if (status != null) {
                processStatusMap.put(taskId, status);
                return status;
            }

            return "IDLE";
        } catch (Exception e) {
            log.error("Failed to get process status for task {}: {}", taskId, e.getMessage(), e);
            return "UNKNOWN";
        }
    }

    /**
     * 检查处理健康状态
     * @param taskId 任务ID
     * @return 健康状态
     */
    @Override
    public String checkProcessHealth(Long taskId) {
        log.info("Checking process health for task: {}", taskId);
        try {
            // 1. 获取处理状态
            String status = getProcessStatus(taskId);

            // 2. 检查处理进程
            // 实现具体的健康检查逻辑
            
            // 2.1 检查执行器服务状态
            boolean executorServiceHealthy = true;
            if (executorService.isShutdown() || executorService.isTerminated()) {
                executorServiceHealthy = false;
                log.warn("Executor service is not healthy: shutdown={}, terminated={}", 
                         executorService.isShutdown(), executorService.isTerminated());
            }
            
            // 2.2 检查缓存状态
            boolean cacheHealthy = true;
            if (vectorCache.size() > CACHE_SIZE_LIMIT * 0.9) {
                cacheHealthy = false;
                log.warn("Cache is nearly full: current size={}, limit={}", 
                         vectorCache.size(), CACHE_SIZE_LIMIT);
            }
            
            // 2.3 检查Redis连接状态
            boolean redisHealthy = true;
            try {
                redisTemplate.opsForValue().get("health_check");
            } catch (Exception e) {
                redisHealthy = false;
                log.warn("Redis connection is not healthy: {}", e.getMessage());
            }
            
            // 2.4 检查Kafka连接状态
            boolean kafkaHealthy = true;
            try {
                kafkaTemplate.send("health_check", "health_check").get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                kafkaHealthy = false;
                log.warn("Kafka connection is not healthy: {}", e.getMessage());
            }
            
            // 2.5 综合判断健康状态
            if (!executorServiceHealthy || !cacheHealthy || !redisHealthy || !kafkaHealthy) {
                log.warn("Process health check failed: executorService={}, cache={}, redis={}, kafka={}",
                         executorServiceHealthy, cacheHealthy, redisHealthy, kafkaHealthy);
                return "UNHEALTHY";
            }
            
            log.debug("Process health check passed: executorService={}, cache={}, redis={}, kafka={}",
                      executorServiceHealthy, cacheHealthy, redisHealthy, kafkaHealthy);

            // 3. 返回健康状态
            if ("RUNNING".equals(status)) {
                return "HEALTHY";
            } else if ("IDLE".equals(status)) {
                return "IDLE";
            } else {
                return "UNHEALTHY";
            }
        } catch (Exception e) {
            log.error("Failed to check process health for task {}: {}", taskId, e.getMessage(), e);
            return "UNKNOWN";
        }
    }

    /**
     * 发送处理后的数据到 Kafka
     * @param taskId 任务ID
     * @param processedData 处理后的数据
     */
    private void sendProcessedDataToKafka(Long taskId, Map<String, Object> processedData) {
        try {
            String topic = DataRsyncConstants.KafkaTopic.DATA_PROCESSED_TOPIC;
            
            // 构建消息
            Map<String, Object> message = new HashMap<>();
            message.put("taskId", taskId);
            message.put("data", processedData);
            message.put("timestamp", System.currentTimeMillis());
            
            // 生成消息键
            String key = taskId + "-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
            
            // 发送消息
            kafkaTemplate.send(topic, key, message.toString());
            log.debug("Sent processed data to Kafka topic {} for task: {}", topic, taskId);
        } catch (Exception e) {
            log.error("Failed to send processed data to Kafka for task {}: {}", taskId, e.getMessage(), e);
        }
    }

    /**
     * 批量发送处理后的数据到 Kafka
     * @param taskId 任务ID
     * @param processedDataList 处理后的数据列表
     */
    private void batchSendProcessedDataToKafka(Long taskId, List<Map<String, Object>> processedDataList) {
        try {
            String topic = DataRsyncConstants.KafkaTopic.DATA_PROCESSED_TOPIC;
            
            for (Map<String, Object> processedData : processedDataList) {
                // 构建消息
                Map<String, Object> message = new HashMap<>();
                message.put("taskId", taskId);
                message.put("data", processedData);
                message.put("timestamp", System.currentTimeMillis());
                
                // 生成消息键
                String key = taskId + "-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
                
                // 发送消息
                kafkaTemplate.send(topic, key, message.toString());
            }
            
            log.debug("Batch sent {} processed data items to Kafka for task: {}", processedDataList.size(), taskId);
        } catch (Exception e) {
            log.error("Failed to batch send processed data to Kafka for task {}: {}", taskId, e.getMessage(), e);
        }
    }

} 
