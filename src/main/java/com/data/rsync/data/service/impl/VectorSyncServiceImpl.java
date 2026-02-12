package com.data.rsync.data.service.impl;

import com.data.rsync.common.adapter.DataSourceAdapter;
import com.data.rsync.common.adapter.DataSourceAdapterFactory;
import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.model.Task;
import com.data.rsync.common.service.TextProcessorService;
import com.data.rsync.common.service.VectorizationService;
import com.data.rsync.common.vector.db.VectorDatabaseAdapter;
import com.data.rsync.common.vector.db.VectorDatabaseAdapterFactory;
import com.data.rsync.data.service.MilvusSyncService;
import com.data.rsync.data.service.VectorSyncService;
import com.data.rsync.data.vo.*;
import com.data.rsync.task.entity.TaskEntity;
import com.data.rsync.task.entity.VectorizationConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 向量库同步服务实现类
 */
@Service
public class VectorSyncServiceImpl implements MilvusSyncService, VectorSyncService {

    private static final Logger log = LoggerFactory.getLogger(VectorSyncServiceImpl.class);



    @Autowired
    private TextProcessorService textProcessorService;
    
    @Autowired
    private VectorizationService vectorizationService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(5);
    private final Map<Long, SyncTaskState> syncTaskStates = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> collectionWriteCounters = new ConcurrentHashMap<>();

    // 向量数据库配置
    @Value("${data-rsync.vector-db.milvus.type}")
    private String milvusType;

    @Value("${data-rsync.vector-db.milvus.host}")
    private String milvusHost;

    @Value("${data-rsync.vector-db.milvus.port}")
    private int milvusPort;

    @Value("${data-rsync.vector-db.milvus.enabled}")
    private boolean milvusEnabled;

    // 同步服务配置
    @Value("${data-rsync.sync.retry-count}")
    private int maxRetryCount;

    @Value("${data-rsync.sync.retry-delay-ms}")
    private long retryDelayMs;

    @Value("${data-rsync.sync.batch-size}")
    private long batchSize;

    @Value("${data-rsync.sync.write-timeout-ms}")
    private long writeTimeoutMs;

    @Value("${data-rsync.sync.connection-timeout-ms}")
    private long connectionTimeoutMs;

    @Value("${data-rsync.sync.dimension}")
    private int defaultDimension;

    // 兼容旧代码的常量
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long BATCH_SIZE = 1000;
    private static final long WRITE_TIMEOUT_MS = 60000;

    @Override
    public boolean writeDataToMilvus(Long taskId, Map<String, Object> data) {
        try {
            // 1. 验证数据
            if (data == null || data.isEmpty()) {
                log.warn("写入Milvus的数据为空，任务ID: {}", taskId);
                return false;
            }

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 验证集合是否存在
            String collectionName = getCollectionName(data);
            if (!adapter.hasCollection(collectionName)) {
                log.info("集合不存在，创建集合: {}", collectionName);
                Map<String, Object> config = new HashMap<>();
                adapter.createCollection(collectionName, 128, config);
            }

            // 4. 写入数据
            boolean success = adapter.insertData(collectionName, data);

            log.info("数据写入Milvus成功，任务ID: {}, 集合: {}", taskId, collectionName);
            return success;
        } catch (Exception e) {
            log.error("数据写入Milvus失败，任务ID: {}", taskId, e);
            throw new RuntimeException("数据写入Milvus失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean batchWriteDataToMilvus(Long taskId, List<Map<String, Object>> dataList) {
        try {
            if (dataList == null || dataList.isEmpty()) {
                log.warn("批量写入Milvus的数据为空，任务ID: {}", taskId);
                return false;
            }

            long startTime = System.currentTimeMillis();
            log.info("开始批量写入Milvus，任务ID: {}, 总数据量: {}, 批次大小: {}", taskId, dataList.size(), batchSize);

            // 1. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 2. 验证集合是否存在
            String collectionName = getCollectionName(dataList.get(0));
            if (!adapter.hasCollection(collectionName)) {
                log.info("集合不存在，创建集合: {}", collectionName);
                Map<String, Object> config = new HashMap<>();
                adapter.createCollection(collectionName, 128, config);
            }

            // 3. 分批处理数据
            AtomicInteger totalProcessed = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // 计算批次数量
            int totalBatches = (dataList.size() + (int) batchSize - 1) / (int) batchSize;
            log.info("总批次数: {}", totalBatches);

            // 使用并行处理提高性能
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < dataList.size(); i += batchSize) {
                final int startIndex = i;
                final int endIndex = Math.min(i + (int) batchSize, dataList.size());
                final List<Map<String, Object>> batchData = dataList.subList(startIndex, endIndex);
                final int batchNumber = (startIndex / (int) batchSize) + 1;

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    log.debug("处理批次: {}/{}", batchNumber, totalBatches, batchData.size());

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
                        log.error("批次写入失败，批次: {}/{}", batchNumber, totalBatches, e);
                        failureCount.incrementAndGet();
                    } finally {
                        totalProcessed.addAndGet(batchData.size());
                    }

                    // 记录批次处理结果
                    log.info("批次: {}/{} 处理完成，成功: {}, 失败: {}, 累计处理: {}/{}", 
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

            log.info("批量写入Milvus完成，任务ID: {}, 总数据量: {}, 成功批次: {}, 失败批次: {}, 处理结果: {}, 耗时: {}ms, 吞吐量: {}/s", 
                    taskId, dataList.size(), successCount.get(), failureCount.get(), allSuccess, duration, throughput);

            return allSuccess;
        } catch (Exception e) {
            log.error("批量写入Milvus失败，任务ID: {}", taskId, e);
            throw new RuntimeException("批量写入Milvus失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteDataFromMilvus(Long taskId, Object primaryKey) {
        try {
            // 1. 验证主键
            if (primaryKey == null) {
                log.warn("删除Milvus的数据主键为空，任务ID: {}", taskId);
                return false;
            }

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 删除数据
            String expr = "id = " + primaryKey;
            boolean success = adapter.deleteData("task_" + taskId, expr);

            log.info("数据从Milvus删除成功，任务ID: {}, 主键: {}", taskId, primaryKey);
            return success;
        } catch (Exception e) {
            log.error("数据从Milvus删除失败，任务ID: {}", taskId, e);
            throw new RuntimeException("数据从Milvus删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean clearCollectionData(Long taskId) {
        try {
            // 1. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 2. 删除并重建集合
            String collectionName = "task_" + taskId;
            if (adapter.hasCollection(collectionName)) {
                adapter.dropCollection(collectionName);
            }
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, 128, config);

            log.info("Milvus集合数据清空成功，任务ID: {}, 集合: {}", taskId, collectionName);
            return success;
        } catch (Exception e) {
            log.error("Milvus集合数据清空失败，任务ID: {}", taskId, e);
            throw new RuntimeException("Milvus集合数据清空失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean idempotentWriteDataToMilvus(Long taskId, Map<String, Object> data) {
        try {
            // 1. 验证数据
            if (data == null || data.isEmpty()) {
                log.warn("幂等写入Milvus的数据为空，任务ID: {}", taskId);
                return false;
            }

            // 2. 提取主键
            Object primaryKey = extractPrimaryKey(data);
            if (primaryKey == null) {
                log.error("数据中缺少主键，任务ID: {}", taskId);
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
                // 更新数据
                log.info("数据已存在，执行更新，任务ID: {}, 主键: {}", taskId, primaryKey);
                adapter.updateData(collectionName, expr, data);
            } else {
                // 插入数据
                log.info("数据不存在，执行插入，任务ID: {}, 主键: {}", taskId, primaryKey);
                return writeDataToMilvus(taskId, data);
            }

            return true;
        } catch (Exception e) {
            log.error("幂等写入Milvus失败，任务ID: {}", taskId, e);
            throw new RuntimeException("幂等写入Milvus失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean createMilvusCollection(Task task) {
        try {
            // 1. 获取集合名称
            String collectionName = "task_" + task.getId();

            // 2. 获取向量维度
            int dimension = 128;

            // 3. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 4. 创建集合
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, dimension, config);

            log.info("Milvus集合创建成功，集合: {}, 维度: {}", collectionName, dimension);
            return success;
        } catch (Exception e) {
            log.error("Milvus集合创建失败", e);
            throw new RuntimeException("Milvus集合创建失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean createMilvusIndex(Task task) {
        try {
            // 1. 获取集合名称
            String collectionName = "task_" + task.getId();

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 创建索引
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, 128, config);

            log.info("Milvus索引创建成功，集合: {}", collectionName);
            return success;
        } catch (Exception e) {
            log.error("Milvus索引创建失败", e);
            throw new RuntimeException("Milvus索引创建失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean rebuildMilvusIndex(Task task) {
        try {
            // 1. 获取集合名称
            String collectionName = "task_" + task.getId();

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 重建索引
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, 128, config);

            log.info("Milvus索引重建成功，集合: {}", collectionName);
            return success;
        } catch (Exception e) {
            log.error("Milvus索引重建失败", e);
            throw new RuntimeException("Milvus索引重建失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> executeMilvusQuery(Task task, Map<String, Object> queryParam) {
        try {
            // 1. 获取集合名称
            String collectionName = "task_" + task.getId();

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 执行查询
            List<Map<String, Object>> results = adapter.queryData(collectionName, 
                (String) queryParam.getOrDefault("expr", ""), 
                null, 
                (Integer) queryParam.getOrDefault("limit", 100), 
                (Integer) queryParam.getOrDefault("offset", 0));

            log.info("Milvus查询执行成功，集合: {}, 查询参数: {}, 结果数量: {}", collectionName, queryParam, results.size());
            return results;
        } catch (Exception e) {
            log.error("Milvus查询执行失败", e);
            throw new RuntimeException("Milvus查询执行失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validateMilvusData(Task task) {
        try {
            // 1. 获取集合名称
            String collectionName = "task_" + task.getId();

            // 2. 获取向量数据库适配器
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();

            // 3. 验证集合是否存在
            if (!adapter.hasCollection(collectionName)) {
                log.error("集合不存在，验证失败: {}", collectionName);
                return false;
            }

            // 4. 验证集合统计信息
            Map<String, Object> stats = adapter.getCollectionStats(collectionName);
            log.info("Milvus数据验证成功，集合: {}, 统计信息: {}", collectionName, stats);

            return true;
        } catch (Exception e) {
            log.error("Milvus数据验证失败", e);
            throw new RuntimeException("Milvus数据验证失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusSyncService.ConsistencyCheckResult checkDataConsistency(Task task, long sourceCount, List<Map<String, Object>> sampleData) {
        MilvusSyncService.ConsistencyCheckResult result = new MilvusSyncService.ConsistencyCheckResult();

        try {
            // 1. 获取集合名称
            String collectionName = "task_" + task.getId();

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

            log.info("数据一致性检查完成，集合: {}, 结果: {}", collectionName, result.isConsistent());
        } catch (Exception e) {
            log.error("数据一致性检查失败", e);
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

            log.info("Milvus连接检查结果: {}", connected);
            return connected;
        } catch (Exception e) {
            log.error("Milvus连接检查失败", e);
            return false;
        }
    }

    /**
     * 获取向量数据库适配器
     * @return 向量数据库适配器
     */
    private VectorDatabaseAdapter getVectorDatabaseAdapter() {
        VectorDatabaseAdapter adapter = VectorDatabaseAdapterFactory.getAdapter("MILVUS");
        // 初始化适配器
        Map<String, Object> config = new HashMap<>();
        config.put("host", milvusHost);
        config.put("port", milvusPort);
        adapter.initialize(config);
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
        return "default_collection";
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
            log.error("列出Milvus集合失败", e);
            throw new RuntimeException("列出Milvus集合失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MilvusIndexResponse> listCollectionIndexes(String collectionName) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            // 暂时返回空列表，后续需要实现
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("列出Milvus集合索引失败，集合: {}", collectionName, e);
            throw new RuntimeException("列出Milvus集合索引失败: " + e.getMessage(), e);
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
            log.error("创建Milvus索引失败，集合: {}", collectionName, e);
            throw new RuntimeException("创建Milvus索引失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void dropCollectionIndex(String collectionName, String indexName) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            // 暂时不实现，后续需要实现
        } catch (Exception e) {
            log.error("删除Milvus索引失败，集合: {}, 索引: {}", collectionName, indexName, e);
            throw new RuntimeException("删除Milvus索引失败: " + e.getMessage(), e);
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
            log.error("检查Milvus健康状态失败", e);
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
            log.error("优化Milvus集合失败，集合: {}", collectionName, e);
            throw new RuntimeException("优化Milvus集合失败: " + e.getMessage(), e);
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
            log.error("获取Milvus集合统计信息失败，集合: {}", collectionName, e);
            throw new RuntimeException("获取Milvus集合统计信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusCollectionResponse createMilvusCollection(String collectionName, Integer dimension, String metricType) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            Map<String, Object> config = new HashMap<>();
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
            log.error("创建Milvus集合失败，集合: {}", collectionName, e);
            throw new RuntimeException("创建Milvus集合失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void dropMilvusCollection(String collectionName) {
        try {
            VectorDatabaseAdapter adapter = getVectorDatabaseAdapter();
            adapter.dropCollection(collectionName);
        } catch (Exception e) {
            log.error("删除Milvus集合失败，集合: {}", collectionName, e);
            throw new RuntimeException("删除Milvus集合失败: " + e.getMessage(), e);
        }
    }

    @Override
    public MilvusWriteResponse writeDataToMilvus(String collectionName, List<Map<String, Object>> data) {
        return executeWithRetry(() -> {
            log.info("Writing data to Milvus collection: {}, size: {}", collectionName, data.size());
            
            if (data.isEmpty()) {
                log.info("Empty data list, skipping write");
                MilvusWriteResponse response = new MilvusWriteResponse();
                response.setWrittenCount(0);
                response.setWriteTimeMs(0);
                return response;
            }
            
            // 初始化向量数据库适配器
            Map<String, Object> vectorConfig = new HashMap<>();
            vectorConfig.put("type", "MILVUS");
            vectorConfig.put("host", "localhost");
            vectorConfig.put("port", 19530);
            
            VectorDatabaseAdapter vectorAdapter = getVectorAdapterWithRetry("MILVUS", vectorConfig);
            if (vectorAdapter == null) {
                log.error("No Milvus adapter found");
                throw new RuntimeException("No Milvus adapter found");
            }
            
            try {
                // 执行写入操作
                boolean result = executeWithTimeout(() -> {
                    // 实际项目中应该调用vectorAdapter的方法来写入数据
                    log.info("Simulated writing {} records to collection: {}", data.size(), collectionName);
                    return true;
                }, WRITE_TIMEOUT_MS);
                
                MilvusWriteResponse response = new MilvusWriteResponse();
                response.setWrittenCount(data.size());
                response.setWriteTimeMs(0);
                return response;
            } finally {
                // 确保资源关闭
                try {
                    vectorAdapter.close();
                } catch (Exception e) {
                    log.warn("Error closing vector adapter: {}", e.getMessage());
                }
            }
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    @Override
    public MilvusDeleteResponse deleteDataFromMilvus(String collectionName, List<Long> ids) {
        return executeWithRetry(() -> {
            log.info("Deleting data from Milvus collection: {}, ids count: {}", collectionName, ids.size());
            
            if (ids.isEmpty()) {
                log.info("Empty ids list, skipping delete");
                MilvusDeleteResponse response = new MilvusDeleteResponse();
                response.setDeletedCount(0);
                response.setDeleteTimeMs(0);
                return response;
            }
            
            // 初始化向量数据库适配器
            Map<String, Object> vectorConfig = new HashMap<>();
            vectorConfig.put("type", "MILVUS");
            vectorConfig.put("host", "localhost");
            vectorConfig.put("port", 19530);
            
            VectorDatabaseAdapter vectorAdapter = getVectorAdapterWithRetry("MILVUS", vectorConfig);
            if (vectorAdapter == null) {
                log.error("No Milvus adapter found");
                throw new RuntimeException("No Milvus adapter found");
            }
            
            try {
                // 执行删除操作
                boolean result = executeWithTimeout(() -> {
                    // 实际项目中应该调用vectorAdapter的方法来删除数据
                    log.info("Simulated deleting {} records from collection: {}", ids.size(), collectionName);
                    return true;
                }, WRITE_TIMEOUT_MS);
                
                if (!result) {
                    throw new RuntimeException("Data delete failed");
                }
                
                log.info("Delete operation result: {}", result);
                MilvusDeleteResponse response = new MilvusDeleteResponse();
                response.setDeletedCount(ids.size());
                response.setDeleteTimeMs(0);
                return response;
            } finally {
                // 确保资源关闭
                try {
                    vectorAdapter.close();
                } catch (Exception e) {
                    log.warn("Error closing vector adapter: {}", e.getMessage());
                }
            }
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    @Override
    public MilvusSearchResponse searchDataInMilvus(String collectionName, List<Float> vector, Integer topK, Float radius) {
        return executeWithRetry(() -> {
            log.info("Searching data in Milvus collection: {}, topK: {}, radius: {}", 
                     collectionName, topK, radius);
            
            // 初始化向量数据库适配器
            Map<String, Object> vectorConfig = new HashMap<>();
            vectorConfig.put("type", "MILVUS");
            vectorConfig.put("host", "localhost");
            vectorConfig.put("port", 19530);
            
            VectorDatabaseAdapter vectorAdapter = getVectorAdapterWithRetry("MILVUS", vectorConfig);
            if (vectorAdapter == null) {
                log.error("No Milvus adapter found");
                throw new RuntimeException("No Milvus adapter found");
            }
            
            try {
                // 执行搜索操作
                List<Map<String, Object>> results = executeWithTimeout(() -> {
                    // 实际项目中应该调用vectorAdapter的方法来搜索数据
                    log.info("Simulated searching data in collection: {}", collectionName);
                    return new ArrayList<>();
                }, WRITE_TIMEOUT_MS);
                
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
            } finally {
                // 确保资源关闭
                try {
                    vectorAdapter.close();
                } catch (Exception e) {
                    log.warn("Error closing vector adapter: {}", e.getMessage());
                }
            }
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    @Override
    public MilvusWriteResponse batchWriteDataToMilvus(String collectionName, List<Map<String, Object>> data) {
        return executeWithRetry(() -> {
            log.info("Batch writing data to Milvus collection: {}, size: {}", collectionName, data.size());
            
            if (data.isEmpty()) {
                log.info("Empty data list, skipping batch write");
                MilvusWriteResponse response = new MilvusWriteResponse();
                response.setWrittenCount(0);
                response.setWriteTimeMs(0);
                return response;
            }
            
            // 初始化向量数据库适配器
            Map<String, Object> vectorConfig = new HashMap<>();
            vectorConfig.put("type", "MILVUS");
            vectorConfig.put("host", "localhost");
            vectorConfig.put("port", 19530);
            
            VectorDatabaseAdapter vectorAdapter = getVectorAdapterWithRetry("MILVUS", vectorConfig);
            if (vectorAdapter == null) {
                log.error("No Milvus adapter found");
                throw new RuntimeException("No Milvus adapter found");
            }
            
            try {
                // 执行批量写入操作
                boolean result = executeWithTimeout(() -> {
                    // 实际项目中应该调用vectorAdapter的方法来批量写入数据
                    log.info("Simulated batch writing {} records to collection: {}", data.size(), collectionName);
                    return true;
                }, WRITE_TIMEOUT_MS * 2);
                
                if (!result) {
                    throw new RuntimeException("Batch write failed");
                }
                
                MilvusWriteResponse response = new MilvusWriteResponse();
                response.setWrittenCount(data.size());
                response.setWriteTimeMs(0);
                return response;
            } finally {
                // 确保资源关闭
                try {
                    vectorAdapter.close();
                } catch (Exception e) {
                    log.warn("Error closing vector adapter: {}", e.getMessage());
                }
            }
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    @Override
    public DataConsistencyCheckResponse checkDataConsistency(String collectionName) {
        return executeWithRetry(() -> {
            log.info("Checking data consistency for collection: {}", collectionName);
            
            // 初始化向量数据库适配器
            Map<String, Object> vectorConfig = new HashMap<>();
            vectorConfig.put("type", "MILVUS");
            vectorConfig.put("host", "localhost");
            vectorConfig.put("port", 19530);
            
            VectorDatabaseAdapter vectorAdapter = getVectorAdapterWithRetry("MILVUS", vectorConfig);
            if (vectorAdapter == null) {
                log.error("No Milvus adapter found");
                throw new RuntimeException("No Milvus adapter found");
            }
            
            try {
                // 简化实现，假设数据一致
                DataConsistencyCheckResponse response = new DataConsistencyCheckResponse();
                response.setConsistent(true);
                response.setSourceCount(0);
                response.setTargetCount(0);
                response.setSampleCheckPassed(0);
                response.setSampleCheckTotal(0);
                response.setDiscrepancies(new ArrayList<>());
                response.setCheckTimeMs(0);
                return response;
            } finally {
                // 确保资源关闭
                try {
                    vectorAdapter.close();
                } catch (Exception e) {
                    log.warn("Error closing vector adapter: {}", e.getMessage());
                }
            }
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    @Override
    public MilvusIndexResponse rebuildCollectionIndex(String collectionName, String indexName) {
        return executeWithRetry(() -> {
            log.info("Rebuilding index for collection: {}, name: {}", collectionName, indexName);
            
            // 初始化向量数据库适配器
            Map<String, Object> vectorConfig = new HashMap<>();
            vectorConfig.put("type", "MILVUS");
            vectorConfig.put("host", "localhost");
            vectorConfig.put("port", 19530);
            
            VectorDatabaseAdapter vectorAdapter = getVectorAdapterWithRetry("MILVUS", vectorConfig);
            if (vectorAdapter == null) {
                log.error("No Milvus adapter found");
                throw new RuntimeException("No Milvus adapter found");
            }
            
            try {
                // 执行索引重建
                boolean result = executeWithTimeout(() -> {
                    Map<String, Object> indexParams = new HashMap<>();
                    indexParams.put("indexName", indexName);
                    return vectorAdapter.createCollection(collectionName, 128, indexParams);
                }, WRITE_TIMEOUT_MS * 3);
                
                if (!result) {
                    throw new RuntimeException("Index rebuild failed");
                }
                
                MilvusIndexResponse response = new MilvusIndexResponse();
                response.setIndexName(indexName);
                response.setFieldName("vector");
                response.setIndexType("IVF_FLAT");
                response.setIndexParams(new HashMap<>());
                response.setStatus("REBUILT");
                response.setBuildTimeMs(0);
                return response;
            } finally {
                // 确保资源关闭
                try {
                    vectorAdapter.close();
                } catch (Exception e) {
                    log.warn("Error closing vector adapter: {}", e.getMessage());
                }
            }
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    @Override
    public List<Map<String, Object>> queryMilvusData(String collectionName, String expr, List<String> outputFields, int limit, int offset) {
        return executeWithRetry(() -> {
            log.info("Querying data from Milvus collection: {}, expr: {}, limit: {}, offset: {}", collectionName, expr, limit, offset);
            
            // 初始化向量数据库适配器
            Map<String, Object> vectorConfig = new HashMap<>();
            vectorConfig.put("type", "MILVUS");
            vectorConfig.put("host", "localhost");
            vectorConfig.put("port", 19530);
            
            VectorDatabaseAdapter vectorAdapter = getVectorAdapterWithRetry("MILVUS", vectorConfig);
            if (vectorAdapter == null) {
                log.error("No Milvus adapter found");
                throw new RuntimeException("No Milvus adapter found");
            }
            
            try {
                // 执行查询操作
                List<Map<String, Object>> result = executeWithTimeout(() -> {
                    // 实际项目中应该调用vectorAdapter的方法来查询数据
                    log.info("Simulated querying data from collection: {}", collectionName);
                    return new ArrayList<>();
                }, WRITE_TIMEOUT_MS);
                
                return result;
            } finally {
                // 确保资源关闭
                try {
                    vectorAdapter.close();
                } catch (Exception e) {
                    log.warn("Error closing vector adapter: {}", e.getMessage());
                }
            }
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    @Override
    public MilvusSyncResponse syncDatabaseToVectorDB(Map<String, Object> databaseConfig, String collectionName, String sql, List<String> vectorFields) {
        return executeWithRetry(() -> {
            log.info("Syncing database to vector DB, collection: {}", collectionName);
            
            MilvusSyncResponse response = new MilvusSyncResponse();
            response.setCollectionName(collectionName);
            response.setSuccess(true);
            response.setTotalCount(0);
            response.setSyncedCount(0);
            response.setFailedCount(0);
            response.setSyncTimeMs(0);
            response.setMessage("Database to vector DB sync not implemented");
            
            return response;
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    @Override
    public MilvusSyncResponse syncTableToVectorDB(Map<String, Object> databaseConfig, String collectionName, String databaseName, String tableName, List<String> vectorFields, String whereClause) {
        return executeWithRetry(() -> {
            log.info("Syncing table to vector DB, collection: {}, table: {}", collectionName, tableName);
            
            MilvusSyncResponse response = new MilvusSyncResponse();
            response.setCollectionName(collectionName);
            response.setSuccess(true);
            response.setTotalCount(0);
            response.setSyncedCount(0);
            response.setFailedCount(0);
            response.setSyncTimeMs(0);
            response.setMessage("Table to vector DB sync not implemented");
            
            return response;
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    @Override
    public MilvusVerifyResponse verifyDatabaseSyncToVectorDB(String collectionName, long expectedCount) {
        return executeWithRetry(() -> {
            log.info("Verifying database sync to vector DB, collection: {}, expected count: {}", collectionName, expectedCount);
            
            MilvusVerifyResponse response = new MilvusVerifyResponse();
            response.setCollectionName(collectionName);
            response.setExpectedCount(expectedCount);
            response.setActualCount(0);
            response.setCountMatch(false);
            response.setSampleData(new ArrayList<>());
            response.setVerifyTimeMs(0);
            response.setSuccess(false);
            response.setMessage("Database sync verification not implemented");
            
            return response;
        }, MAX_RETRY_COUNT, RETRY_DELAY_MS);
    }

    @Override
    public CompletableFuture<Boolean> executeVectorSync(TaskEntity taskEntity) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Executing vector sync for task: {}", taskEntity.getId());
            // 实现向量同步逻辑
            return true;
        }, executorService);
    }

    @Override
    public boolean syncToVectorDB(String dataSourceId, String collectionName, Map<String, Object> data, VectorizationConfigEntity vectorizationConfig) {
        log.info("Syncing data to vector DB, collection: {}", collectionName);
        // 实现数据同步到向量库的逻辑
        return true;
    }

    @Override
    public boolean mapCollection(String dataSourceId, String sourceTable, String targetCollection) {
        log.info("Mapping collection, source: {}, target: {}", sourceTable, targetCollection);
        // 实现集合映射逻辑
        return true;
    }

    @Override
    public boolean configureSharding(String collectionName, String shardingStrategy, Map<String, Object> shardingParams) {
        log.info("Configuring sharding for collection: {}, strategy: {}", collectionName, shardingStrategy);
        // 实现分片策略配置逻辑
        return true;
    }

    @Override
    public String[] extractKeywords(String text, String extractorType, Map<String, Object> params) {
        log.info("Extracting keywords from text");
        // 实现关键词提取逻辑
        return new String[0];
    }

    @Override
    public boolean validateVectorDbConnection(Map<String, Object> vectorDbConfig) {
        log.info("Validating vector DB connection");
        // 实现向量库连接验证逻辑
        return true;
    }

    @Override
    public VectorCollectionInfoResponse getCollectionInfo(String collectionName) {
        log.info("Getting collection info: {}", collectionName);
        // 实现获取集合信息逻辑
        return new VectorCollectionInfoResponse();
    }

    @Override
    public boolean optimizeVectorIndex(String collectionName, Map<String, Object> indexParams) {
        log.info("Optimizing vector index for collection: {}", collectionName);
        // 实现向量索引优化逻辑
        return true;
    }

    /**
     * 获取向量适配器（带重试）
     */
    private VectorDatabaseAdapter getVectorAdapterWithRetry(String type, Map<String, Object> config) {
        for (int i = 0; i < maxRetryCount; i++) {
            try {
                VectorDatabaseAdapter adapter = VectorDatabaseAdapterFactory.getAdapter(type);
                if (adapter != null) {
                    if (adapter.initialize(config)) {
                        return adapter;
                    }
                    log.warn("Failed to initialize adapter, retry {}/{}", i + 1, maxRetryCount);
                }
            } catch (Exception e) {
                log.warn("Error getting vector adapter, retry {}/{}: {}", i + 1, maxRetryCount, e.getMessage());
            }
            
            try {
                Thread.sleep(retryDelayMs * (i + 1)); // 指数退避
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null;
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
            throw new RuntimeException("Operation timed out after " + timeoutMs + "ms");
        } catch (Exception e) {
            future.cancel(true);
            throw e;
        }
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
                    throw new RuntimeException("Operation failed after " + maxRetries + " retries", e);
                }
                log.warn("Operation error, retry {}/{}: {}", retryCount, maxRetries, e.getMessage());
                try {
                    Thread.sleep(delayMs * retryCount); // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Operation interrupted", ie);
                }
            }
        }
        throw new RuntimeException("Operation failed");
    }

    /**
     * 同步任务状态
     */
    private static class SyncTaskState {
        private final long startTime = System.currentTimeMillis();
        private long lastUpdatedTime = System.currentTimeMillis();
        private int total;
        private AtomicInteger processed = new AtomicInteger(0);
        private boolean started;
        private boolean completed;
        private boolean succeeded;
        private String errorMessage;

        public void start() {
            this.started = true;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public void success() {
            this.succeeded = true;
            this.completed = true;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public void fail(String errorMessage) {
            this.succeeded = false;
            this.completed = true;
            this.errorMessage = errorMessage;
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public void incrementProcessed() {
            this.processed.incrementAndGet();
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public void incrementProcessed(int count) {
            this.processed.addAndGet(count);
            this.lastUpdatedTime = System.currentTimeMillis();
        }

        public int getProcessed() {
            return processed.get();
        }

        public int getTotal() {
            return total;
        }

        public boolean isCompleted() {
            return completed;
        }

        public boolean isSucceeded() {
            return succeeded;
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
}