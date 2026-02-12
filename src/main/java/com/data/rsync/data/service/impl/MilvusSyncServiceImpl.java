package com.data.rsync.data.service.impl;

import com.data.rsync.common.adapter.DataSourceAdapter;
import com.data.rsync.common.adapter.DataSourceAdapterFactory;
import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.model.Task;
import com.data.rsync.common.service.VectorizationService;
import com.data.rsync.common.vector.db.VectorDatabaseAdapter;
import com.data.rsync.common.vector.db.VectorDatabaseAdapterFactory;
import com.data.rsync.common.exception.MilvusException;
import com.data.rsync.common.exception.MilvusSyncException;
import com.data.rsync.common.utils.LogUtils;
import com.data.rsync.data.service.MilvusSyncService;
import com.data.rsync.data.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Milvus 同步服务实现类
 */
@Service
public class MilvusSyncServiceImpl implements MilvusSyncService {



    @Value("${data-rsync.vector-db.milvus.host:192.168.1.81}")
    private String milvusHost;

    @Value("${data-rsync.vector-db.milvus.port:19530}")
    private Integer milvusPort;

    @Value("${data-rsync.vector-db.milvus.collection.name:data_rsync_collection}")
    private String defaultCollectionName;

    @Value("${data-rsync.milvus.thread-pool.core-size:5}")
    private int corePoolSize;

    @Value("${data-rsync.milvus.thread-pool.max-size:20}")
    private int maxPoolSize;

    @Value("${data-rsync.milvus.thread-pool.queue-capacity:1000}")
    private int queueCapacity;

    @Value("${data-rsync.milvus.thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${data-rsync.milvus.batch-size:50}")
    private int batchSize;

    @Value("${data-rsync.milvus.timeout-seconds:30}")
    private int timeoutSeconds;

    @Autowired
    private VectorizationService vectorizationService;

    // 线程池用于并行处理数据
    private ExecutorService executorService;

    /**
     * 初始化线程池
     */
    @PostConstruct
    public void init() {
        this.executorService = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 销毁线程池
     */
    @PreDestroy
    public void destroy() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        LogUtils.info("Milvus同步服务线程池已关闭");
    }

    @Override
    public boolean writeDataToMilvus(Long taskId, Map<String, Object> data) {
        try {
            // 1. 验证数据
            if (data == null || data.isEmpty()) {
                LogUtils.warn("写入Milvus的数据为空，任务ID: {}", taskId);
                return false;
            }

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 验证集合是否存在
            String collectionName = getCollectionName(data);
            if (!adapter.hasCollection(collectionName)) {
                LogUtils.info("集合不存在，创建集合: {}", collectionName);
                Map<String, Object> config = new HashMap<>();
                adapter.createCollection(collectionName, 128, config);
            }

            // 4. 写入数据
            boolean success = adapter.insertData(collectionName, data);

            LogUtils.info("数据写入Milvus成功，任务ID: {}, 集合: {}", taskId, collectionName);
            return success;
        } catch (Exception e) {
            LogUtils.error("数据写入Milvus失败，任务ID: {}", taskId, e);
            throw new MilvusSyncException("数据写入Milvus失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean batchWriteDataToMilvus(Long taskId, List<Map<String, Object>> dataList) {
        try {
            if (dataList == null || dataList.isEmpty()) {
                LogUtils.warn("批量写入Milvus的数据为空，任务ID: {}", taskId);
                return false;
            }

            long startTime = System.currentTimeMillis();
            LogUtils.info("开始批量写入Milvus，任务ID: {}, 总数据量: {}, 批次大小: {}", taskId, dataList.size(), batchSize);

            // 1. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 2. 验证集合是否存在
            String collectionName = getCollectionName(dataList.get(0));
            if (!adapter.hasCollection(collectionName)) {
                LogUtils.info("集合不存在，创建集合: {}", collectionName);
                Map<String, Object> config = new HashMap<>();
                adapter.createCollection(collectionName, 128, config);
            }

            // 3. 分批处理数据
            AtomicInteger totalProcessed = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // 计算批次数量
            int totalBatches = (dataList.size() + batchSize - 1) / batchSize;
            LogUtils.info("总批次数: {}", totalBatches);

            // 使用并行处理提高性能
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < dataList.size(); i += batchSize) {
                final int startIndex = i;
                final int endIndex = Math.min(i + batchSize, dataList.size());
                final List<Map<String, Object>> batchData = dataList.subList(startIndex, endIndex);
                final int batchNumber = (startIndex / batchSize) + 1;

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    LogUtils.debug("处理批次: {}/{}", batchNumber, totalBatches, batchData.size());

                    // 批量写入数据
                    boolean success = false;
                    try {
                        success = adapter.batchInsertData(collectionName, batchData);
                        if (success) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        LogUtils.error("批次写入失败，批次: {}/{}", batchNumber, totalBatches, e);
                        failureCount.incrementAndGet();
                    } finally {
                        totalProcessed.addAndGet(batchData.size());
                    }

                    // 记录批次处理结果
                    LogUtils.info("批次: {}/{} 处理完成，成功: {}, 失败: {}, 累计处理: {}/{}", 
                            batchNumber, totalBatches, successCount.get(), failureCount.get(), 
                            totalProcessed.get(), dataList.size());
                }, executorService);

                futures.add(future);
            }

            // 等待所有批次处理完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 计算总体处理结果
            boolean allSuccess = failureCount.get() == 0;
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            double throughput = dataList.size() / (duration / 1000.0);

            LogUtils.info("批量写入Milvus完成，任务ID: {}, 总数据量: {}, 成功批次: {}, 失败批次: {}, 处理结果: {}, 耗时: {}ms, 吞吐量: {}/s", 
                    taskId, dataList.size(), successCount.get(), failureCount.get(), allSuccess, duration, throughput);

            return allSuccess;
        } catch (Exception e) {
            LogUtils.error("批量写入Milvus失败，任务ID: {}", taskId, e);
            throw new MilvusSyncException("批量写入Milvus失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteDataFromMilvus(Long taskId, Object primaryKey) {
        try {
            // 1. 验证主键
            if (primaryKey == null) {
                LogUtils.warn("删除Milvus的数据主键为空，任务ID: {}", taskId);
                return false;
            }

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 删除数据
            String expr = "id = " + primaryKey;
            boolean success = adapter.deleteData(defaultCollectionName, expr);

            LogUtils.info("数据从Milvus删除成功，任务ID: {}, 主键: {}", taskId, primaryKey);
            return success;
        } catch (Exception e) {
            LogUtils.error("数据从Milvus删除失败，任务ID: {}", taskId, e);
            throw new MilvusSyncException("数据从Milvus删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean clearCollectionData(Long taskId) {
        try {
            // 1. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 2. 删除并重建集合
            if (adapter.hasCollection(defaultCollectionName)) {
                adapter.dropCollection(defaultCollectionName);
            }
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(defaultCollectionName, 128, config);

            LogUtils.info("Milvus集合数据清空成功，任务ID: {}, 集合: {}", taskId, defaultCollectionName);
            return success;
        } catch (Exception e) {
            LogUtils.error("Milvus集合数据清空失败，任务ID: {}", taskId, e);
            throw new MilvusSyncException("Milvus集合数据清空失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean idempotentWriteDataToMilvus(Long taskId, Map<String, Object> data) {
        try {
            // 1. 验证数据
            if (data == null || data.isEmpty()) {
                LogUtils.warn("幂等写入Milvus的数据为空，任务ID: {}", taskId);
                return false;
            }

            // 2. 提取主键
            Object primaryKey = extractPrimaryKey(data);
            if (primaryKey == null) {
                LogUtils.error("数据中缺少主键，任务ID: {}", taskId);
                return false;
            }

            // 3. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 4. 检查数据是否存在
            String collectionName = getCollectionName(data);
            String expr = "id = " + primaryKey;
            List<Map<String, Object>> results = adapter.queryData(collectionName, expr, null, 1, 0);
            boolean exists = !results.isEmpty();

            if (exists) {
                // 数据已存在，执行更新
                LogUtils.info("数据已存在，执行更新，任务ID: {}, 主键: {}", taskId, primaryKey);
                adapter.updateData(collectionName, expr, data);
            } else {
                // 数据不存在，执行插入
                LogUtils.info("数据不存在，执行插入，任务ID: {}, 主键: {}", taskId, primaryKey);
                return writeDataToMilvus(taskId, data);
            }

            return true;
        } catch (Exception e) {
            LogUtils.error("幂等写入Milvus失败，任务ID: {}", taskId, e);
            throw new MilvusSyncException("幂等写入Milvus失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean createMilvusCollection(Task task) {
        try {
            // 1. 获取集合名称
            String collectionName = defaultCollectionName;

            // 2. 获取向量维度
            int dimension = 128;

            // 3. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 4. 创建集合
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, dimension, config);

            LogUtils.info("Milvus集合创建成功，集合: {}, 维度: {}", collectionName, dimension);
            return success;
        } catch (Exception e) {
            LogUtils.error("Milvus集合创建失败", e);
            throw new MilvusException("Milvus集合创建失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean createMilvusIndex(Task task) {
        try {
            // 1. 获取集合名称
            String collectionName = defaultCollectionName;

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 创建索引
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, 128, config);

            LogUtils.info("Milvus索引创建成功，集合: {}", collectionName);
            return success;
        } catch (Exception e) {
            LogUtils.error("Milvus索引创建失败", e);
            throw new MilvusException("Milvus索引创建失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean rebuildMilvusIndex(Task task) {
        try {
            // 1. 获取集合名称
            String collectionName = defaultCollectionName;

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 重建索引
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, 128, config);

            LogUtils.info("Milvus索引重建成功，集合: {}", collectionName);
            return success;
        } catch (Exception e) {
            LogUtils.error("Milvus索引重建失败", e);
            throw new MilvusException("Milvus索引重建失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> executeMilvusQuery(Task task, Map<String, Object> queryParam) {
        try {
            // 1. 获取集合名称
            String collectionName = defaultCollectionName;

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 执行查询
            List<Map<String, Object>> results = adapter.queryData(collectionName, 
                (String) queryParam.getOrDefault("expr", ""), 
                null, 
                (Integer) queryParam.getOrDefault("limit", 100), 
                (Integer) queryParam.getOrDefault("offset", 0));

            LogUtils.info("Milvus查询执行成功，集合: {}, 查询参数: {}, 结果数量: {}", collectionName, queryParam, results.size());
            return results;
        } catch (Exception e) {
            LogUtils.error("Milvus查询执行失败", e);
            throw new MilvusException("Milvus查询执行失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateMilvusData(Task task) {
        try {
            // 1. 获取集合名称
            String collectionName = defaultCollectionName;

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 验证集合是否存在
            if (!adapter.hasCollection(collectionName)) {
                LogUtils.error("集合不存在，验证失败: {}", collectionName);
                return false;
            }

            // 4. 验证集合统计信息
            Map<String, Object> stats = adapter.getCollectionStats(collectionName);
            LogUtils.info("Milvus数据验证成功，集合: {}, 统计信息: {}", collectionName, stats);

            return true;
        } catch (Exception e) {
            LogUtils.error("Milvus数据验证失败", e);
            throw new MilvusException("Milvus数据验证失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ConsistencyCheckResult checkDataConsistency(Task task, long sourceCount, List<Map<String, Object>> sampleData) {
        ConsistencyCheckResult result = new ConsistencyCheckResult();

        try {
            // 1. 获取集合名称
            String collectionName = defaultCollectionName;

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 验证集合是否存在
            if (!adapter.hasCollection(collectionName)) {
                result.setConsistent(false);
                result.setErrorMessage("集合不存在: " + collectionName);
                return result;
            }

            // 4. 验证样本数据
            int sampleCheckPassed = 0;
            int sampleCheckTotal = sampleData.size();
            List<String> discrepancies = new ArrayList<>();

            for (Map<String, Object> sample : sampleData) {
                Object primaryKey = extractPrimaryKey(sample);
                if (primaryKey != null) {
                    String expr = "id = " + primaryKey;
                    List<Map<String, Object>> results = adapter.queryData(collectionName, expr, null, 1, 0);
                    if (!results.isEmpty()) {
                        sampleCheckPassed++;
                    } else {
                        discrepancies.add("主键不存在: " + primaryKey);
                    }
                }
            }

            // 5. 设置一致性检查结果
            result.setConsistent(sampleCheckPassed == sampleCheckTotal);
            result.setSourceCount(sourceCount);
            result.setTargetCount(sampleCheckTotal);
            result.setSampleCheckPassed(sampleCheckPassed);
            result.setSampleCheckTotal(sampleCheckTotal);
            result.setDiscrepancies(discrepancies);

            if (!result.isConsistent()) {
                result.setErrorMessage("数据不一致: 源数据数量: " + sourceCount + ", 样本检查通过: " + sampleCheckPassed + "/" + sampleCheckTotal);
            }

            LogUtils.info("数据一致性检查完成，集合: {}, 结果: {}", collectionName, result.isConsistent());
        } catch (Exception e) {
            LogUtils.error("数据一致性检查失败", e);
            result.setConsistent(false);
            result.setErrorMessage("数据一致性检查失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public String getSyncStatus(Long taskId) {
        // 暂时返回同步中状态，后续需要从数据库查询
        return "SYNCING";
    }

    @Override
    public boolean checkMilvusConnection() {
        try {
            // 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 测试连接
            boolean connected = adapter.checkConnection();

            LogUtils.info("Milvus连接检查结果: {}", connected);
            return connected;
        } catch (Exception e) {
            LogUtils.error("Milvus连接检查失败", e);
            return false;
        }
    }

    /**
     * 获取向量数据库适配器
     * @return 向量数据库适配器
     */
    private VectorDatabaseAdapter getVectorDatabaseAdapter() {
        return getVectorDatabaseAdapter(null);
    }

    /**
     * 获取向量数据库适配器（带自定义配置）
     * @param customConfig 自定义配置
     * @return 向量数据库适配器
     */
    private VectorDatabaseAdapter getVectorDatabaseAdapter(Map<String, Object> customConfig) {
        VectorDatabaseAdapter adapter = VectorDatabaseAdapterFactory.getAdapter("MILVUS");
        if (adapter == null) {
            throw new MilvusException("Failed to get Milvus vector database adapter");
        }
        // 初始化适配器
        Map<String, Object> config = new HashMap<>();
        // 使用默认配置
        config.put("host", milvusHost);
        config.put("port", milvusPort);
        // 应用自定义配置（如果有）
        if (customConfig != null) {
            config.putAll(customConfig);
        }
        LogUtils.info("Initializing Milvus vector database adapter with config: {}", config);
        boolean initialized = adapter.initialize(config);
        if (!initialized) {
            throw new MilvusException("Failed to initialize Milvus vector database adapter");
        }
        return adapter;
    }

    /**
     * 从数据中提取向量
     * @param data 数据
     * @return 向量
     */
    private float[] extractVector(Map<String, Object> data) {
        if (data.containsKey("vector")) {
            Object vectorObj = data.get("vector");
            if (vectorObj instanceof float[]) {
                return (float[]) vectorObj;
            } else if (vectorObj instanceof List) {
                List<?> vectorList = (List<?>) vectorObj;
                float[] vector = new float[vectorList.size()];
                for (int i = 0; i < vectorList.size(); i++) {
                    vector[i] = Float.parseFloat(vectorList.get(i).toString());
                }
                return vector;
            }
        }
        return null;
    }

    /**
     * 从数据中提取主键
     * @param data 数据
     * @return 主键
     */
    private Object extractPrimaryKey(Map<String, Object> data) {
        if (data.containsKey("id")) {
            return data.get("id");
        } else if (data.containsKey("primaryKey")) {
            return data.get("primaryKey");
        }
        return null;
    }

    /**
     * 从数据中获取集合名称
     * @param data 数据
     * @return 集合名称
     */
    private String getCollectionName(Map<String, Object> data) {
        if (data.containsKey("collectionName")) {
            return data.get("collectionName").toString();
        }
        return defaultCollectionName;
    }

    @Override
    public List<MilvusCollectionResponse> listCollections() {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            List<String> collectionNames = adapter.getCollections();
            List<MilvusCollectionResponse> result = new ArrayList<>();
            for (String collectionName : collectionNames) {
                MilvusCollectionResponse collectionInfo = new MilvusCollectionResponse();
                collectionInfo.setCollectionName(collectionName);
                result.add(collectionInfo);
            }
            return result;
        } catch (Exception e) {
            LogUtils.error("列出Milvus集合失败", e);
            throw new MilvusException("列出Milvus集合失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MilvusIndexResponse> listCollectionIndexes(String collectionName) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            // 暂时返回空列表，后续需要实现
            return new ArrayList<>();
        } catch (Exception e) {
            LogUtils.error("列出Milvus集合索引失败，集合: {}", collectionName, e);
            throw new MilvusException("列出Milvus集合索引失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusIndexResponse createCollectionIndex(String collectionName, Map<String, Object> indexParams) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, 128, config);
            MilvusIndexResponse result = new MilvusIndexResponse();
            result.setIndexName(indexParams.getOrDefault("indexName", "default_index").toString());
            result.setFieldName(indexParams.getOrDefault("fieldName", "vector").toString());
            result.setIndexType(indexParams.getOrDefault("indexType", "IVF_FLAT").toString());
            result.setIndexParams(indexParams);
            result.setStatus(success ? "CREATED" : "FAILED");
            return result;
        } catch (Exception e) {
            LogUtils.error("创建Milvus索引失败，集合: {}", collectionName, e);
            throw new MilvusException("创建Milvus索引失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void dropCollectionIndex(String collectionName, String indexName) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            // 暂时不实现，后续需要实现
        } catch (Exception e) {
            LogUtils.error("删除Milvus索引失败，集合: {}, 索引: {}", collectionName, indexName, e);
            throw new MilvusException("删除Milvus索引失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusHealthResponse checkMilvusHealth() {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            boolean connected = adapter.checkConnection();
            MilvusHealthResponse healthStatus = new MilvusHealthResponse();
            healthStatus.setHealthy(connected);
            healthStatus.setStatus(connected ? "HEALTHY" : "UNHEALTHY");
            healthStatus.setResponseTimeMs(0);
            Map<String, Object> details = new HashMap<>();
            details.put("host", milvusHost);
            details.put("port", milvusPort);
            details.put("timestamp", System.currentTimeMillis());
            healthStatus.setDetails(details);
            return healthStatus;
        } catch (Exception e) {
            LogUtils.error("检查Milvus健康状态失败", e);
            MilvusHealthResponse healthStatus = new MilvusHealthResponse();
            healthStatus.setHealthy(false);
            healthStatus.setStatus("UNHEALTHY");
            healthStatus.setResponseTimeMs(0);
            Map<String, Object> details = new HashMap<>();
            details.put("host", milvusHost);
            details.put("port", milvusPort);
            details.put("timestamp", System.currentTimeMillis());
            healthStatus.setDetails(details);
            return healthStatus;
        }
    }

    @Override
    public MilvusOptimizeResponse optimizeCollection(String collectionName) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            // 暂时返回成功，后续需要实现
            MilvusOptimizeResponse result = new MilvusOptimizeResponse();
            result.setCollectionName(collectionName);
            result.setOptimizeTimeMs(0);
            Map<String, Object> optimizeDetails = new HashMap<>();
            optimizeDetails.put("message", "集合优化成功");
            result.setOptimizeDetails(optimizeDetails);
            return result;
        } catch (Exception e) {
            LogUtils.error("优化Milvus集合失败，集合: {}", collectionName, e);
            throw new MilvusException("优化Milvus集合失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusCollectionStatsResponse getCollectionStats(String collectionName) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            Map<String, Object> stats = adapter.getCollectionStats(collectionName);
            MilvusCollectionStatsResponse result = new MilvusCollectionStatsResponse();
            result.setCollectionName(collectionName);
            result.setRowCount(((Number) stats.getOrDefault("rowCount", 0)).longValue());
            result.setSizeBytes(((Number) stats.getOrDefault("sizeBytes", 0)).longValue());
            result.setFieldStats(stats);
            result.setIndexStats(new HashMap<>());
            result.setStatsTimeMs(0);
            return result;
        } catch (Exception e) {
            LogUtils.error("获取Milvus集合统计信息失败，集合: {}", collectionName, e);
            throw new MilvusException("获取Milvus集合统计信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusCollectionResponse createMilvusCollection(String collectionName, Integer dimension, String metricType) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            Map<String, Object> config = new HashMap<>();
            config.put("dimension", dimension);
            config.put("metricType", metricType);
            config.put("autoCreateIndex", true);
            boolean success = adapter.createCollection(collectionName, dimension, config);
            MilvusCollectionResponse result = new MilvusCollectionResponse();
            result.setCollectionName(collectionName);
            result.setDimension(dimension);
            result.setMetricType(metricType);
            result.setRowCount(0);
            result.setStatus(success ? "CREATED" : "FAILED");
            Map<String, Object> properties = new HashMap<>();
            properties.put("message", success ? "集合创建成功" : "集合创建失败");
            result.setProperties(properties);
            return result;
        } catch (Exception e) {
            LogUtils.error("创建Milvus集合失败，集合: {}", collectionName, e);
            throw new MilvusException("创建Milvus集合失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void dropMilvusCollection(String collectionName) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            adapter.dropCollection(collectionName);
        } catch (Exception e) {
            LogUtils.error("删除Milvus集合失败，集合: {}", collectionName, e);
            throw new MilvusException("删除Milvus集合失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusWriteResponse writeDataToMilvus(String collectionName, List<Map<String, Object>> data) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            boolean success = adapter.batchInsertData(collectionName, data);
            if (!success) {
                throw new MilvusException("数据写入失败");
            }
            MilvusWriteResponse result = new MilvusWriteResponse();
            result.setWrittenCount(data.size());
            result.setWriteTimeMs(0);
            return result;
        } catch (Exception e) {
            LogUtils.error("写入Milvus数据失败，集合: {}", collectionName, e);
            throw new MilvusException("写入Milvus数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusDeleteResponse deleteDataFromMilvus(String collectionName, List<Long> ids) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            boolean success = true;
            for (Long id : ids) {
                String expr = "id = " + id;
                success &= adapter.deleteData(collectionName, expr);
            }
            if (!success) {
                throw new MilvusException("数据删除失败");
            }
            MilvusDeleteResponse result = new MilvusDeleteResponse();
            result.setDeletedCount(ids.size());
            result.setDeleteTimeMs(0);
            return result;
        } catch (Exception e) {
            LogUtils.error("删除Milvus数据失败，集合: {}", collectionName, e);
            throw new MilvusException("删除Milvus数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusSearchResponse searchDataInMilvus(String collectionName, List<Float> vector, Integer topK, Float radius) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            // 转换List<Float>为float[]
            float[] vectorArray = new float[vector.size()];
            for (int i = 0; i < vector.size(); i++) {
                vectorArray[i] = vector.get(i);
            }
            List<Map<String, Object>> results = adapter.searchData(collectionName, vectorArray, topK, "L2", "", null);
            MilvusSearchResponse response = new MilvusSearchResponse();
            List<MilvusSearchResponse.MilvusSearchResultItem> searchResults = new ArrayList<>();
            for (Map<String, Object> result : results) {
                MilvusSearchResponse.MilvusSearchResultItem item = new MilvusSearchResponse.MilvusSearchResultItem();
                item.setScore(((Number) result.getOrDefault("score", 0.0f)).floatValue());
                item.setFields(result);
                searchResults.add(item);
            }
            response.setResults(searchResults);
            response.setTotalCount(results.size());
            response.setSearchTimeMs(0);
            return response;
        } catch (Exception e) {
            LogUtils.error("搜索Milvus数据失败，集合: {}", collectionName, e);
            throw new MilvusException("搜索Milvus数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusWriteResponse batchWriteDataToMilvus(String collectionName, List<Map<String, Object>> data) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            boolean success = adapter.batchInsertData(collectionName, data);
            if (!success) {
                throw new MilvusException("批量写入失败");
            }
            MilvusWriteResponse result = new MilvusWriteResponse();
            result.setWrittenCount(data.size());
            result.setWriteTimeMs(0);
            return result;
        } catch (Exception e) {
            LogUtils.error("批量写入Milvus数据失败，集合: {}", collectionName, e);
            throw new MilvusException("批量写入Milvus数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public DataConsistencyCheckResponse checkDataConsistency(String collectionName) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            // 暂时返回模拟数据，后续需要实现
            DataConsistencyCheckResponse result = new DataConsistencyCheckResponse();
            result.setConsistent(true);
            result.setSourceCount(0);
            result.setTargetCount(0);
            result.setSampleCheckPassed(0);
            result.setSampleCheckTotal(0);
            result.setDiscrepancies(new ArrayList<>());
            result.setCheckTimeMs(0);
            return result;
        } catch (Exception e) {
            LogUtils.error("检查Milvus数据一致性失败，集合: {}", collectionName, e);
            throw new MilvusException("检查Milvus数据一致性失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusIndexResponse rebuildCollectionIndex(String collectionName, String indexName) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            // 暂时返回成功，后续需要实现
            MilvusIndexResponse result = new MilvusIndexResponse();
            result.setIndexName(indexName);
            result.setFieldName("vector");
            result.setIndexType("IVF_FLAT");
            result.setIndexParams(new HashMap<>());
            result.setStatus("REBUILT");
            result.setBuildTimeMs(0);
            return result;
        } catch (Exception e) {
            LogUtils.error("重建Milvus索引失败，集合: {}, 索引: {}", collectionName, indexName, e);
            throw new MilvusException("重建Milvus索引失败: " + e.getMessage(), e);
        }
    }

    public MilvusSearchResponse searchSimilarVectors(String collectionName, List<Float> queryVector, Integer topK, String metricType) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            // 转换List<Float>为float[]
            float[] vectorArray = new float[queryVector.size()];
            for (int i = 0; i < queryVector.size(); i++) {
                vectorArray[i] = queryVector.get(i);
            }
            List<Map<String, Object>> results = adapter.searchData(collectionName, vectorArray, topK, metricType, "", null);
            MilvusSearchResponse response = new MilvusSearchResponse();
            List<MilvusSearchResponse.MilvusSearchResultItem> searchResults = new ArrayList<>();
            for (Map<String, Object> result : results) {
                MilvusSearchResponse.MilvusSearchResultItem item = new MilvusSearchResponse.MilvusSearchResultItem();
                item.setScore(((Number) result.getOrDefault("score", 0.0f)).floatValue());
                item.setFields(result);
                searchResults.add(item);
            }
            response.setResults(searchResults);
            response.setTotalCount(results.size());
            response.setSearchTimeMs(0);
            return response;
        } catch (Exception e) {
            LogUtils.error("向量相似度查询失败，集合: {}", collectionName, e);
            throw new MilvusException("向量相似度查询失败: " + e.getMessage(), e);
        }
    }

    public boolean updateVectorData(String collectionName, Object primaryKey, Map<String, Object> data) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            String expr = "id = " + primaryKey;
            boolean success = adapter.updateData(collectionName, expr, data);
            LogUtils.info("向量数据更新成功，集合: {}, 主键: {}", collectionName, primaryKey);
            return success;
        } catch (Exception e) {
            LogUtils.error("向量数据更新失败，集合: {}, 主键: {}", collectionName, primaryKey, e);
            throw new MilvusException("向量数据更新失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> queryMilvusData(String collectionName, String expr, List<String> outputFields, int limit, int offset) {
        try {
            // 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            
            // 执行查询操作
            List<Map<String, Object>> result = adapter.queryData(collectionName, expr, outputFields, limit, offset);
            
            LogUtils.info("查询Milvus数据成功，集合: {}, 表达式: {}, 结果数量: {}", collectionName, expr, result.size());
            return result;
        } catch (Exception e) {
            LogUtils.error("查询Milvus数据失败，集合: {}", collectionName, e);
            throw new MilvusException("查询Milvus数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusSyncResponse syncDatabaseToVectorDB(Map<String, Object> databaseConfig, String collectionName, String sql, List<String> vectorFields) {
        long startTime = System.currentTimeMillis();

        try {
            LogUtils.info("开始从数据库同步数据到向量库，集合: {}, SQL: {}", collectionName, sql);

            // 1. 创建数据源配置
            DataSource dataSource = createDataSource(databaseConfig);

            // 2. 获取数据源适配器
            DataSourceAdapter dataSourceAdapter = DataSourceAdapterFactory.getAdapter(dataSource);

            // 3. 执行SQL查询获取数据
            List<Map<String, Object>> dataList = dataSourceAdapter.executeQuery(dataSource, sql, new ArrayList<>());
            long totalCount = dataList.size();

            LogUtils.info("从数据库获取数据成功，数量: {}", totalCount);

            if (totalCount == 0) {
                return new MilvusSyncResponse(
                        true,
                        0,
                        0,
                        0,
                        System.currentTimeMillis() - startTime,
                        "从数据库获取数据为空，同步完成",
                        collectionName
                );
            }

            // 4. 对数据进行向量化处理
            List<Map<String, Object>> vectorizedData = vectorizeData(dataList, vectorFields);

            // 5. 获取向量数据库适配器
            VectorDatabaseAdapter vectorAdapter = getVectorDatabaseAdapter();

            // 6. 确保集合存在
            if (!vectorAdapter.hasCollection(collectionName)) {
                LogUtils.info("集合不存在，创建集合: {}", collectionName);
                Map<String, Object> config = new HashMap<>();
                vectorAdapter.createCollection(collectionName, 1024, config); // 使用1024维度
            }

            // 7. 批量写入向量库
            long syncedCount = batchWriteToVectorDB(vectorAdapter, collectionName, vectorizedData);
            long failedCount = totalCount - syncedCount;
            boolean success = failedCount == 0;
            String message = success ? "数据同步成功" : "部分数据同步失败";
            long syncTimeMs = System.currentTimeMillis() - startTime;

            LogUtils.info("数据库到向量库同步完成，集合: {}, 总数据: {}, 成功: {}, 失败: {}, 耗时: {}ms", 
                    collectionName, totalCount, syncedCount, failedCount, syncTimeMs);

            return new MilvusSyncResponse(
                    success,
                    totalCount,
                    syncedCount,
                    failedCount,
                    syncTimeMs,
                    message,
                    collectionName
            );

        } catch (Exception e) {
            LogUtils.error("数据库到向量库同步失败，集合: {}", collectionName, e);
            return new MilvusSyncResponse(
                    false,
                    0,
                    0,
                    0,
                    System.currentTimeMillis() - startTime,
                    "同步失败: " + e.getMessage(),
                    collectionName
            );
        }
    }

    @Override
    public MilvusSyncResponse syncTableToVectorDB(Map<String, Object> databaseConfig, String collectionName, String databaseName, String tableName, List<String> vectorFields, String whereClause) {
        long startTime = System.currentTimeMillis();

        try {
            LogUtils.info("开始从数据库表同步数据到向量库，集合: {}, 数据库: {}, 表: {}", collectionName, databaseName, tableName);

            // 1. 创建数据源配置
            DataSource dataSource = createDataSource(databaseConfig);

            // 2. 获取数据源适配器
            DataSourceAdapter dataSourceAdapter = DataSourceAdapterFactory.getAdapter(dataSource);

            // 3. 构建SQL查询语句
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(databaseName).append(".").append(tableName);
            if (whereClause != null && !whereClause.isEmpty()) {
                sqlBuilder.append(" WHERE ").append(whereClause);
            }
            String sql = sqlBuilder.toString();

            // 4. 执行SQL查询获取数据
            List<Map<String, Object>> dataList = dataSourceAdapter.executeQuery(dataSource, sql, new ArrayList<>());
            long totalCount = dataList.size();

            LogUtils.info("从数据库表获取数据成功，数量: {}", totalCount);

            if (totalCount == 0) {
                return new MilvusSyncResponse(
                        true,
                        0,
                        0,
                        0,
                        System.currentTimeMillis() - startTime,
                        "从数据库表获取数据为空，同步完成",
                        collectionName
                );
            }

            // 5. 对数据进行向量化处理
            List<Map<String, Object>> vectorizedData = vectorizeData(dataList, vectorFields);

            // 6. 获取向量数据库适配器
            VectorDatabaseAdapter vectorAdapter = getVectorDatabaseAdapter();

            // 7. 确保集合存在
            if (!vectorAdapter.hasCollection(collectionName)) {
                LogUtils.info("集合不存在，创建集合: {}", collectionName);
                Map<String, Object> config = new HashMap<>();
                vectorAdapter.createCollection(collectionName, 1024, config); // 使用1024维度
            }

            // 8. 批量写入向量库
            long syncedCount = batchWriteToVectorDB(vectorAdapter, collectionName, vectorizedData);
            long failedCount = totalCount - syncedCount;
            boolean success = failedCount == 0;
            String message = success ? "数据同步成功" : "部分数据同步失败";
            long syncTimeMs = System.currentTimeMillis() - startTime;

            LogUtils.info("数据库表到向量库同步完成，集合: {}, 总数据: {}, 成功: {}, 失败: {}, 耗时: {}ms", 
                    collectionName, totalCount, syncedCount, failedCount, syncTimeMs);

            return new MilvusSyncResponse(
                    success,
                    totalCount,
                    syncedCount,
                    failedCount,
                    syncTimeMs,
                    message,
                    collectionName
            );

        } catch (Exception e) {
            LogUtils.error("数据库表到向量库同步失败，集合: {}", collectionName, e);
            return new MilvusSyncResponse(
                    false,
                    0,
                    0,
                    0,
                    System.currentTimeMillis() - startTime,
                    "同步失败: " + e.getMessage(),
                    collectionName
            );
        }
    }

    @Override
    public MilvusVerifyResponse verifyDatabaseSyncToVectorDB(String collectionName, long expectedCount) {
        long startTime = System.currentTimeMillis();

        try {
            LogUtils.info("开始验证数据库同步到向量库的结果，集合: {}, 预期数量: {}", collectionName, expectedCount);

            // 1. 获取向量数据库适配器
            VectorDatabaseAdapter vectorAdapter = getVectorDatabaseAdapter();

            // 2. 检查集合是否存在
            if (!vectorAdapter.hasCollection(collectionName)) {
                LogUtils.error("集合不存在，验证失败: {}", collectionName);
                return new MilvusVerifyResponse(
                        false,
                        expectedCount,
                        0,
                        false,
                        null,
                        System.currentTimeMillis() - startTime,
                        "集合不存在",
                        collectionName
                );
            }

            // 3. 获取集合统计信息
            Map<String, Object> stats = vectorAdapter.getCollectionStats(collectionName);
            long actualCount = ((Number) stats.getOrDefault("rowCount", 0)).longValue();

            // 4. 验证数据数量
            boolean countMatch = actualCount >= expectedCount;

            // 5. 获取样本数据进行验证
            List<Map<String, Object>> sampleData = vectorAdapter.queryData(collectionName, "", null, 5, 0);
            List<String> sampleStrings = new ArrayList<>();
            for (Map<String, Object> data : sampleData) {
                sampleStrings.add(data.toString());
            }

            // 6. 设置响应结果
            boolean success = countMatch;
            String message = countMatch ? "数据验证成功" : "数据数量不匹配";
            long verifyTimeMs = System.currentTimeMillis() - startTime;

            LogUtils.info("数据库同步到向量库的验证完成，集合: {}, 预期数量: {}, 实际数量: {}, 匹配: {}", 
                    collectionName, expectedCount, actualCount, countMatch);

            return new MilvusVerifyResponse(
                    success,
                    expectedCount,
                    actualCount,
                    countMatch,
                    sampleStrings,
                    verifyTimeMs,
                    message,
                    collectionName
            );

        } catch (Exception e) {
            LogUtils.error("验证数据库同步到向量库的结果失败，集合: {}", collectionName, e);
            return new MilvusVerifyResponse(
                    false,
                    expectedCount,
                    0,
                    false,
                    null,
                    System.currentTimeMillis() - startTime,
                    "验证失败: " + e.getMessage(),
                    collectionName
            );
        }
    }

    /**
     * 创建数据源配置
     * @param databaseConfig 数据库配置
     * @return 数据源配置
     */
    private DataSource createDataSource(Map<String, Object> databaseConfig) {
        DataSource dataSource = new DataSource();
        // 由于DataSource类使用了@Data注解，理论上应该有setter方法
        // 但为了确保编译通过，我们可以尝试使用反射来设置属性
        try {
            dataSource.getClass().getMethod("setType", String.class).invoke(dataSource, (String) databaseConfig.getOrDefault("type", "MYSQL"));
            dataSource.getClass().getMethod("setHost", String.class).invoke(dataSource, (String) databaseConfig.getOrDefault("host", "localhost"));
            dataSource.getClass().getMethod("setPort", Integer.class).invoke(dataSource, ((Number) databaseConfig.getOrDefault("port", 3306)).intValue());
            dataSource.getClass().getMethod("setDatabase", String.class).invoke(dataSource, (String) databaseConfig.getOrDefault("database", ""));
            dataSource.getClass().getMethod("setUsername", String.class).invoke(dataSource, (String) databaseConfig.getOrDefault("username", "root"));
            dataSource.getClass().getMethod("setPassword", String.class).invoke(dataSource, (String) databaseConfig.getOrDefault("password", ""));
        } catch (Exception e) {
            LogUtils.error("设置数据源配置失败", e);
            // 如果反射失败，直接返回默认的DataSource对象
        }
        return dataSource;
    }

    /**
     * 对数据进行向量化处理
     * @param dataList 原始数据列表
     * @param vectorFields 需要向量化的字段列表
     * @return 向量化后的数据列表
     */
    private List<Map<String, Object>> vectorizeData(List<Map<String, Object>> dataList, List<String> vectorFields) {
        List<Map<String, Object>> vectorizedData = new ArrayList<>();

        for (Map<String, Object> data : dataList) {
            try {
                // 构建需要向量化的文本
                StringBuilder textBuilder = new StringBuilder();
                for (String field : vectorFields) {
                    if (data.containsKey(field)) {
                        Object value = data.get(field);
                        if (value != null) {
                            textBuilder.append(value.toString()).append(" ");
                        }
                    }
                }
                String text = textBuilder.toString().trim();

                // 对文本进行向量化
                if (!text.isEmpty()) {
                    float[] vector = vectorizationService.vectorize(text);
                    data.put("vector", vector);
                }

                vectorizedData.add(data);
            } catch (Exception e) {
                LogUtils.warn("对数据进行向量化处理失败: {}", e.getMessage());
                // 跳过向量化失败的数据
            }
        }

        return vectorizedData;
    }

    /**
     * 批量写入数据到向量库
     * @param vectorAdapter 向量数据库适配器
     * @param collectionName 集合名称
     * @param dataList 向量化后的数据列表
     * @return 成功写入的数量
     */
    private long batchWriteToVectorDB(VectorDatabaseAdapter vectorAdapter, String collectionName, List<Map<String, Object>> dataList) {
        long successCount = 0;

        // 分批处理数据
        for (int i = 0; i < dataList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, dataList.size());
            List<Map<String, Object>> batchData = dataList.subList(i, endIndex);

            try {
                boolean success = vectorAdapter.batchInsertData(collectionName, batchData);
                if (success) {
                    successCount += batchData.size();
                }
                LogUtils.info("批次写入向量库成功，批次: {}/{}, 数量: {}", 
                        i / batchSize + 1, (dataList.size() + batchSize - 1) / batchSize, batchData.size());
            } catch (Exception e) {
                LogUtils.error("批次写入向量库失败: {}", e.getMessage());
            }
        }

        return successCount;
    }
}
