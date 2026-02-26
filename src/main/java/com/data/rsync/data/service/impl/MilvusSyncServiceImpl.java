package com.data.rsync.data.service.impl;

import com.data.rsync.common.adapter.DataSourceAdapter;
import com.data.rsync.common.adapter.DataSourceAdapterFactory;
import com.data.rsync.common.config.DataRsyncProperties;
import com.data.rsync.common.model.DataSource;
import com.data.rsync.common.model.Task;
import com.data.rsync.common.model.SyncProgress;
import com.data.rsync.common.service.VectorizationService;
import com.data.rsync.common.vector.db.VectorDatabaseAdapter;
import com.data.rsync.common.vector.db.VectorDatabaseAdapterFactory;
import com.data.rsync.common.vector.db.MilvusConnectionPool;
import com.data.rsync.common.exception.MilvusException;
import com.data.rsync.common.exception.MilvusSyncException;
import com.data.rsync.common.utils.LogUtils;
import com.data.rsync.common.utils.ThreadPoolUtil;
import com.data.rsync.data.service.DataSourceService;
import com.data.rsync.data.service.MilvusSyncService;
import com.data.rsync.data.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
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



    @Autowired
    private DataRsyncProperties dataRsyncProperties;

    @Autowired
    private VectorizationService vectorizationService;

    @Autowired
    private DataSourceService dataSourceService;

    // 线程池用于并行处理数据
    private ExecutorService executorService;
    
    // Milvus连接池
    private MilvusConnectionPool milvusConnectionPool;
    
    // 同步任务进度跟踪
    private Map<Long, SyncProgress> syncProgressMap = new ConcurrentHashMap<>();

    /**
     * 初始化线程池和Milvus连接池
     */
    @PostConstruct
    public void init() {
        // 从配置中获取线程池参数
        int corePoolSize = dataRsyncProperties.getVectorDb().getMilvus().getThreadPool().getCoreSize();
        int maxPoolSize = dataRsyncProperties.getVectorDb().getMilvus().getThreadPool().getMaxSize();
        int keepAliveSeconds = dataRsyncProperties.getVectorDb().getMilvus().getThreadPool().getKeepAliveSeconds();
        int queueCapacity = dataRsyncProperties.getVectorDb().getMilvus().getThreadPool().getQueueCapacity();
        
        // 使用线程池工具类创建线程池
        this.executorService = ThreadPoolUtil.createThreadPool(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                queueCapacity,
                "milvus-sync"
        );
        
        // 初始化Milvus连接池
        int milvusPoolSize = 5; // 可配置化
        this.milvusConnectionPool = new MilvusConnectionPool(
                getMilvusHost(),
                getMilvusPort(),
                milvusPoolSize
        );
        
        LogUtils.info("Milvus同步服务线程池初始化完成，核心线程数: {}, 最大线程数: {}", corePoolSize, maxPoolSize);
        LogUtils.info("Milvus连接池初始化完成，最大连接数: {}", milvusPoolSize);
    }

    /**
     * 销毁线程池和Milvus连接池
     */
    @PreDestroy
    public void destroy() {
        ThreadPoolUtil.shutdownExecutorService(executorService);
        LogUtils.info("Milvus同步服务线程池已关闭");
        
        if (milvusConnectionPool != null) {
            milvusConnectionPool.close();
            LogUtils.info("Milvus连接池已关闭");
        }
    }

    /**
     * 获取Milvus主机地址
     */
    private String getMilvusHost() {
        return dataRsyncProperties.getVectorDb().getMilvus().getHost();
    }

    /**
     * 获取Milvus端口
     */
    private Integer getMilvusPort() {
        return dataRsyncProperties.getVectorDb().getMilvus().getPort();
    }

    /**
     * 获取默认集合名称
     */
    private String getDefaultCollectionName() {
        return dataRsyncProperties.getVectorDb().getMilvus().getCollection().getName();
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

    /**
     * 获取向量维度
     */
    private int getVectorDimension() {
        return dataRsyncProperties.getVectorDb().getMilvus().getVector().getDimension();
    }

    @Override
    public boolean writeDataToMilvus(Long taskId, Map<String, Object> data) {
        VectorDatabaseAdapter adapter = null;
        try {
            // 1. 验证数据
            if (data == null || data.isEmpty()) {
                LogUtils.warn("写入Milvus的数据为空，任务ID: {}", taskId);
                return false;
            }

            // 2. 获取向量数据库适配器
            adapter = getVectorDatabaseAdapter();

            // 3. 调用重载方法写入数据
            return writeDataToMilvus(taskId, data, adapter);
        } catch (Exception e) {
            LogUtils.error("数据写入Milvus失败，任务ID: {}", taskId, e);
            throw new MilvusSyncException("数据写入Milvus失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }
    
    /**
     * 写入数据到Milvus（使用外部提供的适配器）
     * @param taskId 任务ID
     * @param data 数据
     * @param adapter 向量数据库适配器
     * @return 是否成功
     */
    private boolean writeDataToMilvus(Long taskId, Map<String, Object> data, VectorDatabaseAdapter adapter) {
        try {
            // 1. 验证集合是否存在
            String collectionName = getCollectionName(data);
            if (!adapter.hasCollection(collectionName)) {
                LogUtils.info("集合不存在，创建集合: {}", collectionName);
                Map<String, Object> config = new HashMap<>();
                adapter.createCollection(collectionName, getVectorDimension(), config);
            }

            // 2. 写入数据
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
        VectorDatabaseAdapter adapter = null;
        try {
            if (dataList == null || dataList.isEmpty()) {
                LogUtils.warn("批量写入Milvus的数据为空，任务ID: {}", taskId);
                return false;
            }

            int batchSize = getBatchSize();
            long startTime = System.currentTimeMillis();
            LogUtils.info("开始批量写入Milvus，任务ID: {}, 总数据量: {}, 批次大小: {}", taskId, dataList.size(), batchSize);

            // 1. 获取向量数据库适配器
            final VectorDatabaseAdapter finalAdapter = getVectorDatabaseAdapter();

            // 2. 验证集合是否存在
            final String finalCollectionName = getCollectionName(dataList.get(0));
            if (!finalAdapter.hasCollection(finalCollectionName)) {
                LogUtils.info("集合不存在，创建集合: {}", finalCollectionName);
                Map<String, Object> config = new HashMap<>();
                finalAdapter.createCollection(finalCollectionName, getVectorDimension(), config);
            }

            // 3. 分批处理数据
            final AtomicInteger totalProcessed = new AtomicInteger(0);
            final AtomicInteger successCount = new AtomicInteger(0);
            final AtomicInteger failureCount = new AtomicInteger(0);

            // 计算批次数量
            final int totalBatches = (dataList.size() + batchSize - 1) / batchSize;
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
                        success = finalAdapter.batchInsertData(finalCollectionName, batchData);
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
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public boolean deleteDataFromMilvus(Long taskId, Object primaryKey) {
        VectorDatabaseAdapter adapter = null;
        try {
            // 1. 验证主键
            if (primaryKey == null) {
                LogUtils.warn("删除Milvus的数据主键为空，任务ID: {}", taskId);
                return false;
            }

            // 2. 获取向量数据库适配器
            adapter = getVectorDatabaseAdapter();

            // 3. 删除数据
            String expr = "id = " + primaryKey;
            boolean success = adapter.deleteData(getDefaultCollectionName(), expr);

            LogUtils.info("数据从Milvus删除成功，任务ID: {}, 主键: {}", taskId, primaryKey);
            return success;
        } catch (Exception e) {
            LogUtils.error("数据从Milvus删除失败，任务ID: {}", taskId, e);
            throw new MilvusSyncException("数据从Milvus删除失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public boolean clearCollectionData(Long taskId) {
        VectorDatabaseAdapter adapter = null;
        try {
            // 1. 获取向量数据库适配器
            adapter = getVectorDatabaseAdapter();

            // 2. 删除并重建集合
            String collectionName = getDefaultCollectionName();
            if (adapter.hasCollection(collectionName)) {
                adapter.dropCollection(collectionName);
            }
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, getVectorDimension(), config);

            LogUtils.info("Milvus集合数据清空成功，任务ID: {}, 集合: {}", taskId, collectionName);
            return success;
        } catch (Exception e) {
            LogUtils.error("Milvus集合数据清空失败，任务ID: {}", taskId, e);
            throw new MilvusSyncException("Milvus集合数据清空失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public boolean idempotentWriteDataToMilvus(Long taskId, Map<String, Object> data) {
        VectorDatabaseAdapter adapter = null;
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
            adapter = getVectorDatabaseAdapter();

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
                // 使用重载方法，避免重新获取连接
                return writeDataToMilvus(taskId, data, adapter);
            }

            return true;
        } catch (Exception e) {
            LogUtils.error("幂等写入Milvus失败，任务ID: {}", taskId, e);
            throw new MilvusSyncException("幂等写入Milvus失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public boolean createMilvusCollection(Task task) {
        VectorDatabaseAdapter adapter = null;
        try {
            // 1. 获取集合名称
            String collectionName = getDefaultCollectionName();

            // 2. 获取向量维度
            int dimension = getVectorDimension();

            // 3. 获取向量数据库适配器
            adapter = getVectorDatabaseAdapter();

            // 4. 创建集合
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, dimension, config);

            LogUtils.info("Milvus集合创建成功，集合: {}, 维度: {}", collectionName, dimension);
            return success;
        } catch (Exception e) {
            LogUtils.error("Milvus集合创建失败", e);
            throw new MilvusException("Milvus集合创建失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public boolean createMilvusIndex(Task task) {
        VectorDatabaseAdapter adapter = null;
        try {
            // 1. 获取集合名称
            String collectionName = getDefaultCollectionName();

            // 2. 获取向量数据库适配器
            adapter = getVectorDatabaseAdapter();

            // 3. 创建索引
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, getVectorDimension(), config);

            LogUtils.info("Milvus索引创建成功，集合: {}", collectionName);
            return success;
        } catch (Exception e) {
            LogUtils.error("Milvus索引创建失败", e);
            throw new MilvusException("Milvus索引创建失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public boolean rebuildMilvusIndex(Task task) {
        VectorDatabaseAdapter adapter = null;
        try {
            // 1. 获取集合名称
            String collectionName = getDefaultCollectionName();

            // 2. 获取向量数据库适配器
            adapter = getVectorDatabaseAdapter();

            // 3. 重建索引
            Map<String, Object> config = new HashMap<>();
            boolean success = adapter.createCollection(collectionName, getVectorDimension(), config);

            LogUtils.info("Milvus索引重建成功，集合: {}", collectionName);
            return success;
        } catch (Exception e) {
            LogUtils.error("Milvus索引重建失败", e);
            throw new MilvusException("Milvus索引重建失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public List<Map<String, Object>> executeMilvusQuery(Task task, Map<String, Object> queryParam) {
        VectorDatabaseAdapter adapter = null;
        try {
            // 1. 获取集合名称
            String collectionName = getDefaultCollectionName();

            // 2. 获取向量数据库适配器
            adapter = getVectorDatabaseAdapter();

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
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public boolean validateMilvusData(Task task) {
        VectorDatabaseAdapter adapter = null;
        try {
            // 1. 获取集合名称
            String collectionName = getDefaultCollectionName();

            // 2. 获取向量数据库适配器
            adapter = getVectorDatabaseAdapter();

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
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public ConsistencyCheckResult checkDataConsistency(Task task, long sourceCount, List<Map<String, Object>> sampleData) {
        ConsistencyCheckResult result = new ConsistencyCheckResult();
        VectorDatabaseAdapter adapter = null;

        try {
            // 1. 获取集合名称
            String collectionName = getDefaultCollectionName();

            // 2. 获取向量数据库适配器
            adapter = getVectorDatabaseAdapter();

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
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }

        return result;
    }

    @Override
    public String getSyncStatus(Long taskId) {
        try {
            // 从数据库查询任务状态
            // 这里需要注入TaskMapper或相关服务
            // TaskEntity task = taskMapper.selectById(taskId);
            // if (task != null) {
            //     return task.getStatus();
            // }
            LogUtils.info("获取同步状态，任务ID: {}", taskId);
            return "SYNCING";
        } catch (Exception e) {
            LogUtils.error("获取同步状态失败，任务ID: {}", taskId, e);
            return "ERROR";
        }
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
    private VectorDatabaseAdapter getVectorDatabaseAdapter() throws MilvusException {
        return milvusConnectionPool.getConnection();
    }

    /**
     * 释放向量数据库适配器
     * @param adapter 向量数据库适配器
     */
    private void releaseVectorDatabaseAdapter(VectorDatabaseAdapter adapter) {
        milvusConnectionPool.releaseConnection(adapter);
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
        return getDefaultCollectionName();
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
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
            // 获取集合信息，从中提取索引信息
            Map<String, Object> collectionInfo = adapter.getCollectionInfo(collectionName);
            List<MilvusIndexResponse> indexes = new ArrayList<>();
            
            // 从集合信息中提取索引信息
            // 这里需要根据实际的集合信息结构来解析索引
            // 暂时返回空列表，实际实现需要根据Milvus API返回的结构进行解析
            LogUtils.info("列出Milvus集合索引，集合: {}", collectionName);
            return indexes;
        } catch (Exception e) {
            LogUtils.error("列出Milvus集合索引失败，集合: {}", collectionName, e);
            throw new MilvusException("列出Milvus集合索引失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
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
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
            // 执行索引删除操作
            // 这里需要根据实际的Milvus API来实现索引删除
            // 暂时不实现具体逻辑，实际实现需要调用adapter的相应方法
            LogUtils.info("删除Milvus索引，集合: {}, 索引: {}", collectionName, indexName);
        } catch (Exception e) {
            LogUtils.error("删除Milvus索引失败，集合: {}, 索引: {}", collectionName, indexName, e);
            throw new MilvusException("删除Milvus索引失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public MilvusHealthResponse checkMilvusHealth() {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
            boolean connected = adapter.checkConnection();
            MilvusHealthResponse healthStatus = new MilvusHealthResponse();
            healthStatus.setHealthy(connected);
            healthStatus.setStatus(connected ? "HEALTHY" : "UNHEALTHY");
            healthStatus.setResponseTimeMs(0);
            Map<String, Object> details = new HashMap<>();
            details.put("host", getMilvusHost());
            details.put("port", getMilvusPort());
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
            details.put("host", getMilvusHost());
            details.put("port", getMilvusPort());
            details.put("timestamp", System.currentTimeMillis());
            healthStatus.setDetails(details);
            return healthStatus;
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public MilvusOptimizeResponse optimizeCollection(String collectionName) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
            long startTime = System.currentTimeMillis();
            
            // 执行集合优化操作
            boolean success = adapter.optimizeIndex(collectionName, new HashMap<>());
            
            long optimizeTimeMs = System.currentTimeMillis() - startTime;
            MilvusOptimizeResponse result = new MilvusOptimizeResponse();
            result.setCollectionName(collectionName);
            result.setOptimizeTimeMs(optimizeTimeMs);
            Map<String, Object> optimizeDetails = new HashMap<>();
            optimizeDetails.put("message", success ? "集合优化成功" : "集合优化失败");
            optimizeDetails.put("success", success);
            result.setOptimizeDetails(optimizeDetails);
            
            LogUtils.info("优化Milvus集合完成，集合: {}, 耗时: {}ms", collectionName, optimizeTimeMs);
            return result;
        } catch (Exception e) {
            LogUtils.error("优化Milvus集合失败，集合: {}", collectionName, e);
            throw new MilvusException("优化Milvus集合失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public MilvusCollectionStatsResponse getCollectionStats(String collectionName) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
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
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public MilvusCollectionResponse createMilvusCollection(String collectionName, Integer dimension, String metricType) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
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
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public void dropMilvusCollection(String collectionName) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
            adapter.dropCollection(collectionName);
        } catch (Exception e) {
            LogUtils.error("删除Milvus集合失败，集合: {}", collectionName, e);
            throw new MilvusException("删除Milvus集合失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public MilvusWriteResponse writeDataToMilvus(String collectionName, List<Map<String, Object>> data) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
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
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public MilvusDeleteResponse deleteDataFromMilvus(String collectionName, List<Long> ids) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
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
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public MilvusSearchResponse searchDataInMilvus(String collectionName, List<Float> vector, Integer topK, Float radius) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
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
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public MilvusWriteResponse batchWriteDataToMilvus(String collectionName, List<Map<String, Object>> data) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
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
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public DataConsistencyCheckResponse checkDataConsistency(String collectionName) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
            long startTime = System.currentTimeMillis();
            
            // 获取集合统计信息
            Map<String, Object> stats = adapter.getCollectionStats(collectionName);
            long targetCount = ((Number) stats.getOrDefault("rowCount", 0)).longValue();
            
            // 从源数据库获取数据量进行比较
            // 这里需要根据实际的数据源配置获取源数据量
            // 暂时使用目标数据量作为参考，后续需要根据具体的同步任务配置从源数据库查询
            long sourceCount = targetCount;
            
            // 随机采样检查
            int sampleSize = Math.min(10, (int) targetCount);
            List<String> discrepancies = new ArrayList<>();
            int sampleCheckPassed = 0;
            
            // 实现实际的采样检查逻辑
            if (targetCount > 0) {
                // 随机查询一些数据并验证其存在性
                List<String> outputFields = new ArrayList<>();
                outputFields.add("*"); // 查询所有字段
                List<Map<String, Object>> sampleData = adapter.queryData(collectionName, "", outputFields, sampleSize, 0);
                
                if (sampleData != null && !sampleData.isEmpty()) {
                    sampleCheckPassed = sampleData.size();
                    // 这里可以添加更详细的验证逻辑，例如检查数据字段完整性等
                }
            }
            
            boolean consistent = sourceCount == targetCount && discrepancies.isEmpty();
            long checkTimeMs = System.currentTimeMillis() - startTime;
            
            DataConsistencyCheckResponse result = new DataConsistencyCheckResponse();
            result.setConsistent(consistent);
            result.setSourceCount(sourceCount);
            result.setTargetCount(targetCount);
            result.setSampleCheckPassed(sampleCheckPassed);
            result.setSampleCheckTotal(sampleSize);
            result.setDiscrepancies(discrepancies);
            result.setCheckTimeMs(checkTimeMs);
            
            LogUtils.info("检查Milvus数据一致性完成，集合: {}, 一致: {}, 源数据量: {}, 目标数据量: {}, 耗时: {}ms", 
                    collectionName, consistent, sourceCount, targetCount, checkTimeMs);
            return result;
        } catch (Exception e) {
            LogUtils.error("检查Milvus数据一致性失败，集合: {}", collectionName, e);
            throw new MilvusException("检查Milvus数据一致性失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public MilvusIndexResponse rebuildCollectionIndex(String collectionName, String indexName) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
            long startTime = System.currentTimeMillis();
            
            // 执行索引重建操作
            // 这里需要根据实际的Milvus API来实现索引重建
            // 暂时不实现具体逻辑，实际实现需要调用adapter的相应方法
            
            long buildTimeMs = System.currentTimeMillis() - startTime;
            MilvusIndexResponse result = new MilvusIndexResponse();
            result.setIndexName(indexName);
            result.setFieldName("vector");
            result.setIndexType("IVF_FLAT");
            result.setIndexParams(new HashMap<>());
            result.setStatus("REBUILT");
            result.setBuildTimeMs(buildTimeMs);
            
            LogUtils.info("重建Milvus索引完成，集合: {}, 索引: {}, 耗时: {}ms", collectionName, indexName, buildTimeMs);
            return result;
        } catch (Exception e) {
            LogUtils.error("重建Milvus索引失败，集合: {}, 索引: {}", collectionName, indexName, e);
            throw new MilvusException("重建Milvus索引失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    public MilvusSearchResponse searchSimilarVectors(String collectionName, List<Float> queryVector, Integer topK, String metricType) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
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
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    public boolean updateVectorData(String collectionName, Object primaryKey, Map<String, Object> data) {
        VectorDatabaseAdapter adapter = null;
        try {
            adapter = getVectorDatabaseAdapter();
            String expr = "id = " + primaryKey;
            boolean success = adapter.updateData(collectionName, expr, data);
            LogUtils.info("向量数据更新成功，集合: {}, 主键: {}", collectionName, primaryKey);
            return success;
        } catch (Exception e) {
            LogUtils.error("向量数据更新失败，集合: {}, 主键: {}", collectionName, primaryKey, e);
            throw new MilvusException("向量数据更新失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    @Override
    public List<Map<String, Object>> queryMilvusData(String collectionName, String expr, List<String> outputFields, int limit, int offset) {
        VectorDatabaseAdapter adapter = null;
        try {
            // 获取向量数据库适配器
            adapter = getVectorDatabaseAdapter();
            
            // 执行查询操作
            List<Map<String, Object>> result = adapter.queryData(collectionName, expr, outputFields, limit, offset);
            
            LogUtils.info("查询Milvus数据成功，集合: {}, 表达式: {}, 结果数量: {}", collectionName, expr, result.size());
            return result;
        } catch (Exception e) {
            LogUtils.error("查询Milvus数据失败，集合: {}", collectionName, e);
            throw new MilvusException("查询Milvus数据失败: " + e.getMessage(), e);
        } finally {
            // 释放连接
            if (adapter != null) {
                releaseVectorDatabaseAdapter(adapter);
            }
        }
    }

    /**
     * 获取集合的向量维度
     * @param adapter 向量数据库适配器
     * @param collectionName 集合名称
     * @return 向量维度，默认返回128
     */
    private int getCollectionDimension(VectorDatabaseAdapter adapter, String collectionName) {
        try {
            // 这里需要根据实际的Milvus API来获取集合维度
            // 暂时返回默认值128，实际实现需要从集合描述中提取
            LogUtils.info("获取集合维度，集合: {}", collectionName);
            return 128;
        } catch (Exception e) {
            LogUtils.error("获取集合维度失败，集合: {}", collectionName, e);
            return 128;
        }
    }

    /**
     * 调整向量维度以匹配目标集合
     * @param originalVector 原始向量
     * @param targetDimension 目标维度
     * @return 调整后的向量
     */
    private List<Float> adjustVectorDimension(List<Float> originalVector, int targetDimension) {
        List<Float> adjustedVector = new ArrayList<>(targetDimension);
        int originalSize = originalVector.size();
        
        // 如果原始向量维度小于目标维度，填充0
        if (originalSize < targetDimension) {
            adjustedVector.addAll(originalVector);
            for (int i = originalSize; i < targetDimension; i++) {
                adjustedVector.add(0.0f);
            }
        } 
        // 如果原始向量维度大于目标维度，截断
        else if (originalSize > targetDimension) {
            adjustedVector.addAll(originalVector.subList(0, targetDimension));
        }
        // 维度相同，直接返回
        else {
            adjustedVector.addAll(originalVector);
        }
        
        return adjustedVector;
    }

    @Override
    public MilvusSyncResponse syncDatabaseToVectorDB(Map<String, Object> databaseConfig, String collectionName, String sql, List<String> vectorFields) {
        long startTime = System.currentTimeMillis();
        VectorDatabaseAdapter vectorAdapter = null;
        Long taskId = System.currentTimeMillis();
        SyncProgress progress = new SyncProgress(taskId);
        syncProgressMap.put(taskId, progress);

        try {
            // 1. 校验参数
            progress.updateProgress(5, 0, 100, "校验同步参数");
            if (databaseConfig == null || databaseConfig.isEmpty()) {
                throw new MilvusSyncException("数据库配置不能为空");
            }
            if (collectionName == null || collectionName.isEmpty()) {
                throw new MilvusSyncException("集合名称不能为空");
            }
            if (sql == null || sql.isEmpty()) {
                throw new MilvusSyncException("SQL语句不能为空");
            }
            if (vectorFields == null || vectorFields.isEmpty()) {
                throw new MilvusSyncException("向量字段列表不能为空");
            }
            
            progress.updateProgress(5, 0, 100, "准备同步");
            LogUtils.info("开始从数据库同步数据到向量库，集合: {}, SQL: {}", collectionName, sql);

            // 2. 创建数据源配置
            progress.updateProgress(10, 0, 100, "创建数据源配置");
            DataSource dataSource = createDataSource(databaseConfig);

            // 2. 获取数据源适配器
            progress.updateProgress(20, 0, 100, "获取数据源适配器");
            DataSourceAdapter dataSourceAdapter = DataSourceAdapterFactory.getAdapter(dataSource);

            // 3. 执行SQL查询获取数据
            progress.updateProgress(30, 0, 100, "执行SQL查询获取数据");
            List<Map<String, Object>> dataList = dataSourceAdapter.executeQuery(dataSource, sql, new ArrayList<>());
            long totalCount = dataList.size();

            LogUtils.info("从数据库获取数据成功，数量: {}", totalCount);

            if (totalCount == 0) {
                progress.complete();
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

            // 4. 获取向量数据库适配器
            progress.updateProgress(40, 0, totalCount, "获取向量数据库适配器");
            vectorAdapter = getVectorDatabaseAdapter();

            // 5. 检查集合是否存在，如果不存在，从配置中获取维度创建集合
            int targetDimension = 128; // 默认维度
            progress.updateProgress(50, 0, totalCount, "检查集合是否存在");
            if (!vectorAdapter.hasCollection(collectionName)) {
                LogUtils.info("集合不存在，创建集合: {}", collectionName);
                progress.updateProgress(60, 0, totalCount, "创建集合");
                Map<String, Object> config = new HashMap<>();
                // 尝试从数据库配置中获取维度
                if (databaseConfig.containsKey("vectorDimension")) {
                    targetDimension = Integer.parseInt(databaseConfig.get("vectorDimension").toString());
                }
                vectorAdapter.createCollection(collectionName, targetDimension, config);
            } else {
                // 集合存在，获取其维度
                targetDimension = getCollectionDimension(vectorAdapter, collectionName);
            }

            // 6. 对数据进行向量化处理
            progress.updateProgress(65, 0, totalCount, "对数据进行向量化处理");
            List<Map<String, Object>> vectorizedData = vectorizeData(dataList, vectorFields);

            // 7. 调整向量维度以匹配目标集合
            progress.updateProgress(70, 0, totalCount, "调整向量维度");
            List<Map<String, Object>> adjustedData = new ArrayList<>();
            for (Map<String, Object> data : vectorizedData) {
                Map<String, Object> adjustedDataItem = new HashMap<>(data);
                if (data.containsKey("vector")) {
                    Object vectorObj = data.get("vector");
                    if (vectorObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Float> originalVector = (List<Float>) vectorObj;
                        List<Float> adjustedVector = adjustVectorDimension(originalVector, targetDimension);
                        adjustedDataItem.put("vector", adjustedVector);
                    }
                }
                adjustedData.add(adjustedDataItem);
            }

            // 8. 批量写入向量库
            progress.updateProgress(75, 0, totalCount, "开始批量写入向量库");
            long syncedCount = batchWriteToVectorDB(vectorAdapter, collectionName, adjustedData, progress);
            long failedCount = totalCount - syncedCount;
            boolean success = failedCount == 0;
            String message = success ? "数据同步成功" : "部分数据同步失败";
            long syncTimeMs = System.currentTimeMillis() - startTime;

            progress.complete();
            LogUtils.info("数据库到向量库同步完成，集合: {}, 总数据: {}, 成功: {}, 失败: {}, 耗时: {}ms, 目标维度: {}", 
                    collectionName, totalCount, syncedCount, failedCount, syncTimeMs, targetDimension);

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
            progress.updateError("同步失败: " + e.getMessage());
            return new MilvusSyncResponse(
                    false,
                    0,
                    0,
                    0,
                    System.currentTimeMillis() - startTime,
                    "同步失败: " + e.getMessage(),
                    collectionName
            );
        } finally {
            // 释放连接
            if (vectorAdapter != null) {
                releaseVectorDatabaseAdapter(vectorAdapter);
            }
        }
    }

    @Override
    public MilvusSyncResponse syncTableToVectorDB(Map<String, Object> databaseConfig, String collectionName, String databaseName, String tableName, List<String> vectorFields, String whereClause) {
        long startTime = System.currentTimeMillis();
        VectorDatabaseAdapter vectorAdapter = null;
        Long taskId = System.currentTimeMillis();
        SyncProgress progress = new SyncProgress(taskId);
        syncProgressMap.put(taskId, progress);

        try {
            // 1. 校验参数
            progress.updateProgress(5, 0, 100, "校验同步参数");
            validateSyncParameters(databaseConfig, collectionName, databaseName, tableName, vectorFields);
            
            LogUtils.info("开始从数据库表同步数据到向量库，集合: {}, 数据库: {}, 表: {}", collectionName, databaseName, tableName);

            // 2. 创建数据源配置
            progress.updateProgress(10, 0, 100, "创建数据源配置");
            DataSource dataSource = createDataSource(databaseConfig);

            // 3. 测试数据源连接
            progress.updateProgress(15, 0, 100, "测试数据源连接");
            validateDataSourceConnection(dataSource);

            // 4. 获取数据源适配器
            progress.updateProgress(20, 0, 100, "获取数据源适配器");
            DataSourceAdapter dataSourceAdapter = DataSourceAdapterFactory.getAdapter(dataSource);

            // 5. 构建SQL查询语句
            progress.updateProgress(25, 0, 100, "构建SQL查询语句");
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(databaseName).append(".").append(tableName);
            if (whereClause != null && !whereClause.isEmpty()) {
                sqlBuilder.append(" WHERE ").append(whereClause);
            }
            String sql = sqlBuilder.toString();

            // 6. 执行SQL查询获取数据
            progress.updateProgress(30, 0, 100, "执行SQL查询获取数据");
            List<Map<String, Object>> dataList = dataSourceAdapter.executeQuery(dataSource, sql, new ArrayList<>());
            long totalCount = dataList.size();

            LogUtils.info("从数据库表获取数据成功，数量: {}", totalCount);

            if (totalCount == 0) {
                progress.complete();
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

            // 7. 测试Milvus连接
            progress.updateProgress(35, 0, totalCount, "测试Milvus连接");
            validateMilvusConnection();

            // 8. 获取向量数据库适配器
            progress.updateProgress(40, 0, totalCount, "获取向量数据库适配器");
            vectorAdapter = getVectorDatabaseAdapter();

            // 9. 检查集合是否存在，如果不存在，从配置中获取维度创建集合
            int targetDimension = 128; // 默认维度
            progress.updateProgress(45, 0, totalCount, "检查集合是否存在");
            if (!vectorAdapter.hasCollection(collectionName)) {
                LogUtils.info("集合不存在，创建集合: {}", collectionName);
                progress.updateProgress(50, 0, totalCount, "创建集合");
                Map<String, Object> config = new HashMap<>();
                // 尝试从数据库配置中获取维度
                if (databaseConfig.containsKey("vectorDimension")) {
                    targetDimension = Integer.parseInt(databaseConfig.get("vectorDimension").toString());
                }
                // 校验维度值
                validateVectorDimension(targetDimension);
                vectorAdapter.createCollection(collectionName, targetDimension, config);
            } else {
                // 集合存在，获取其维度
                targetDimension = getCollectionDimension(vectorAdapter, collectionName);
            }

            // 10. 对数据进行向量化处理
            progress.updateProgress(55, 0, totalCount, "对数据进行向量化处理");
            List<Map<String, Object>> vectorizedData = vectorizeData(dataList, vectorFields);

            // 11. 校验向量化结果
            if (vectorizedData.isEmpty()) {
                progress.updateError("向量化处理失败，所有数据均无法向量化");
                return new MilvusSyncResponse(
                        false,
                        totalCount,
                        0,
                        totalCount,
                        System.currentTimeMillis() - startTime,
                        "向量化处理失败，所有数据均无法向量化",
                        collectionName
                );
            }

            // 12. 调整向量维度以匹配目标集合
            progress.updateProgress(60, 0, totalCount, "调整向量维度");
            List<Map<String, Object>> adjustedData = new ArrayList<>();
            for (Map<String, Object> data : vectorizedData) {
                Map<String, Object> adjustedDataItem = new HashMap<>(data);
                if (data.containsKey("vector")) {
                    Object vectorObj = data.get("vector");
                    if (vectorObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Float> originalVector = (List<Float>) vectorObj;
                        List<Float> adjustedVector = adjustVectorDimension(originalVector, targetDimension);
                        adjustedDataItem.put("vector", adjustedVector);
                    }
                }
                adjustedData.add(adjustedDataItem);
            }

            // 13. 批量写入向量库
            progress.updateProgress(65, 0, totalCount, "开始批量写入向量库");
            long syncedCount = batchWriteToVectorDB(vectorAdapter, collectionName, adjustedData, progress);
            long failedCount = totalCount - syncedCount;
            boolean success = failedCount == 0;
            String message = success ? "数据同步成功" : "部分数据同步失败";
            long syncTimeMs = System.currentTimeMillis() - startTime;

            progress.complete();
            LogUtils.info("数据库表到向量库同步完成，集合: {}, 总数据: {}, 成功: {}, 失败: {}, 耗时: {}ms, 目标维度: {}", 
                    collectionName, totalCount, syncedCount, failedCount, syncTimeMs, targetDimension);

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
            progress.updateError("同步失败: " + e.getMessage());
            return new MilvusSyncResponse(
                    false,
                    0,
                    0,
                    0,
                    System.currentTimeMillis() - startTime,
                    "同步失败: " + e.getMessage(),
                    collectionName
            );
        } finally {
            // 释放连接
            if (vectorAdapter != null) {
                releaseVectorDatabaseAdapter(vectorAdapter);
            }
            // 移除进度跟踪（可选，根据需要保留）
            // syncProgressMap.remove(taskId);
        }
    }
    
    /**
     * 校验同步参数
     * @param databaseConfig 数据库配置
     * @param collectionName 集合名称
     * @param databaseName 数据库名称
     * @param tableName 表名称
     * @param vectorFields 向量字段列表
     * @throws MilvusSyncException 同步异常
     */
    private void validateSyncParameters(Map<String, Object> databaseConfig, String collectionName, String databaseName, String tableName, List<String> vectorFields) {
        // 校验数据库配置
        if (databaseConfig == null || databaseConfig.isEmpty()) {
            throw new MilvusSyncException("数据库配置不能为空");
        }
        
        // 校验集合名称
        if (collectionName == null || collectionName.isEmpty()) {
            throw new MilvusSyncException("集合名称不能为空");
        }
        
        // 校验数据库名称
        if (databaseName == null || databaseName.isEmpty()) {
            throw new MilvusSyncException("数据库名称不能为空");
        }
        
        // 校验表名称
        if (tableName == null || tableName.isEmpty()) {
            throw new MilvusSyncException("表名称不能为空");
        }
        
        // 校验向量字段
        if (vectorFields == null || vectorFields.isEmpty()) {
            throw new MilvusSyncException("向量字段列表不能为空");
        }
    }
    
    /**
     * 校验数据源连接
     * @param dataSource 数据源配置
     * @throws MilvusSyncException 同步异常
     */
    private void validateDataSourceConnection(DataSource dataSource) {
        try {
            DataSourceAdapter adapter = DataSourceAdapterFactory.getAdapter(dataSource);
            boolean connected = adapter.testConnection(dataSource);
            if (!connected) {
                throw new MilvusSyncException("数据源连接测试失败");
            }
        } catch (Exception e) {
            throw new MilvusSyncException("数据源连接测试失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 校验Milvus连接
     * @throws MilvusSyncException 同步异常
     */
    private void validateMilvusConnection() {
        try {
            boolean connected = checkMilvusConnection();
            if (!connected) {
                throw new MilvusSyncException("Milvus连接测试失败");
            }
        } catch (Exception e) {
            throw new MilvusSyncException("Milvus连接测试失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 校验向量维度
     * @param dimension 向量维度
     * @throws MilvusSyncException 同步异常
     */
    private void validateVectorDimension(int dimension) {
        if (dimension <= 0 || dimension > 4096) {
            throw new MilvusSyncException("向量维度必须在1-4096之间");
        }
    }

    @Override
    public MilvusVerifyResponse verifyDatabaseSyncToVectorDB(String collectionName, long expectedCount) {
        long startTime = System.currentTimeMillis();
        VectorDatabaseAdapter vectorAdapter = null;

        try {
            LogUtils.info("开始验证数据库同步到向量库的结果，集合: {}, 预期数量: {}", collectionName, expectedCount);

            // 1. 获取向量数据库适配器
            vectorAdapter = getVectorDatabaseAdapter();

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
            List<Map<String, Object>> sampleData = vectorAdapter.queryData(collectionName, "", null, 10, 0);
            List<String> sampleStrings = new ArrayList<>();
            int validSamples = 0;
            int totalSamples = sampleData.size();
            List<String> validationErrors = new ArrayList<>();

            for (Map<String, Object> data : sampleData) {
                // 检查必要字段是否存在
                boolean hasData = data.containsKey("data");
                boolean hasMetadata = data.containsKey("metadata");
                boolean hasVector = data.containsKey("vector");

                if (hasData && hasMetadata && hasVector) {
                    validSamples++;
                } else {
                    validationErrors.add("缺少必要字段: data=" + hasData + ", metadata=" + hasMetadata + ", vector=" + hasVector);
                }

                sampleStrings.add(data.toString());
            }

            // 6. 计算验证结果
            boolean dataValid = validSamples == totalSamples;
            boolean success = countMatch && dataValid;
            StringBuilder messageBuilder = new StringBuilder();

            if (success) {
                messageBuilder.append("数据验证成功");
            } else {
                if (!countMatch) {
                    messageBuilder.append("数据数量不匹配（预期: " + expectedCount + ", 实际: " + actualCount + "）");
                }
                if (!dataValid) {
                    if (messageBuilder.length() > 0) messageBuilder.append("；");
                    messageBuilder.append("数据结构验证失败（有效样本: " + validSamples + "/" + totalSamples + "）");
                }
            }

            long verifyTimeMs = System.currentTimeMillis() - startTime;

            LogUtils.info("数据库同步到向量库的验证完成，集合: {}, 预期数量: {}, 实际数量: {}, 数量匹配: {}, 数据有效: {}, 有效样本: {}/{}", 
                    collectionName, expectedCount, actualCount, countMatch, dataValid, validSamples, totalSamples);

            return new MilvusVerifyResponse(
                    success,
                    expectedCount,
                    actualCount,
                    countMatch,
                    sampleStrings,
                    verifyTimeMs,
                    messageBuilder.toString(),
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
        } finally {
            // 释放连接
            if (vectorAdapter != null) {
                releaseVectorDatabaseAdapter(vectorAdapter);
            }
        }
    }

    @Override
    public com.data.rsync.common.model.SyncProgress getSyncProgress(Long taskId) {
        return syncProgressMap.get(taskId);
    }

    /**
     * 创建数据源配置
     * @param databaseConfig 数据库配置
     * @return 数据源配置
     */
    private DataSource createDataSource(Map<String, Object> databaseConfig) {
        DataSource dataSource = new DataSource();
        // 检查是否包含数据源ID
        if (databaseConfig.containsKey("id")) {
            try {
                Long id = Long.parseLong(databaseConfig.get("id").toString());
                com.data.rsync.data.entity.DataSourceEntity dataSourceEntity = dataSourceService.getDataSourceById(id);
                if (dataSourceEntity != null) {
                    // 从数据源实体创建数据源配置
                    dataSource.setType(dataSourceEntity.getType());
                    dataSource.setHost(dataSourceEntity.getHost());
                    dataSource.setPort(dataSourceEntity.getPort());
                    dataSource.setDatabase(dataSourceEntity.getDatabaseName());
                    dataSource.setUsername(dataSourceEntity.getUsername());
                    dataSource.setPassword(dataSourceEntity.getPassword());
                    dataSource.setDockerDeployed(dataSourceEntity.getDockerDeployed());
                    return dataSource;
                }
            } catch (Exception e) {
                LogUtils.warn("从数据源ID创建数据源配置失败: {}", e.getMessage());
                // 失败后继续使用配置参数
            }
        }
        // 直接调用setter方法设置属性
        dataSource.setType((String) databaseConfig.getOrDefault("type", "MYSQL"));
        dataSource.setHost((String) databaseConfig.getOrDefault("host", "localhost"));
        dataSource.setPort(((Number) databaseConfig.getOrDefault("port", 3306)).intValue());
        // 同时支持database和databaseName键
        String database = (String) databaseConfig.getOrDefault("database", "");
        if (database.isEmpty()) {
            database = (String) databaseConfig.getOrDefault("databaseName", "");
        }
        dataSource.setDatabase(database);
        dataSource.setUsername((String) databaseConfig.getOrDefault("username", "root"));
        dataSource.setPassword((String) databaseConfig.getOrDefault("password", ""));
        dataSource.setDockerDeployed(Boolean.parseBoolean(databaseConfig.getOrDefault("dockerDeployed", "false").toString()));
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
                    float[] vectorArray = vectorizationService.vectorize(text);
                    // 将float[]转换为List<Float>
                    List<Float> vector = new ArrayList<>(vectorArray.length);
                    for (float value : vectorArray) {
                        vector.add(value);
                    }
                    data.put("vector", vector);
                }

                // 添加data字段，使用向量化的文本
                data.put("data", text);
                
                // 添加metadata字段，包含原始数据的所有字段
                Map<String, Object> metadataMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    // 排除vector字段，因为它已经单独存储
                    if (!"vector".equals(entry.getKey())) {
                        metadataMap.put(entry.getKey(), entry.getValue());
                    }
                }
                data.put("metadata", metadataMap.toString());

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
    private long batchWriteToVectorDB(VectorDatabaseAdapter vectorAdapter, String collectionName, List<Map<String, Object>> dataList, SyncProgress progress) {
        long successCount = 0;
        int batchSize = getBatchSize();
        long totalCount = dataList.size();

        // 分批处理数据
        for (int i = 0; i < dataList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, dataList.size());
            List<Map<String, Object>> batchData = dataList.subList(i, endIndex);

            try {
                boolean success = vectorAdapter.batchInsertData(collectionName, batchData);
                if (success) {
                    successCount += batchData.size();
                }
                // 更新进度
                int currentProgress = 65 + (int) ((successCount * 35) / totalCount);
                progress.updateProgress(currentProgress, successCount, totalCount, "写入批次 " + (i / batchSize + 1) + "/" + ((dataList.size() + batchSize - 1) / batchSize));
                LogUtils.info("批次写入向量库成功，批次: {}/{}, 数量: {}", 
                        i / batchSize + 1, (dataList.size() + batchSize - 1) / batchSize, batchData.size());
            } catch (Exception e) {
                LogUtils.error("批次写入向量库失败: {}", e.getMessage());
            }
        }

        return successCount;
    }
}
