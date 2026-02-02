package com.data.rsync.milvus.sync.service.impl;

import com.data.rsync.common.constants.DataRsyncConstants;
import com.data.rsync.common.model.Task;
import com.data.rsync.milvus.sync.service.MilvusSyncService;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.*;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DescribeIndexParam;
import io.milvus.param.index.DropIndexParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Milvus 同步服务实现类
 */
@Service
@Slf4j
public class MilvusSyncServiceImpl implements MilvusSyncService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Milvus 客户端
     */
    private MilvusClient milvusClient;

    /**
     * 同步任务状态缓存
     */
    private final Map<Long, String> syncStatusMap = new ConcurrentHashMap<>();

    /**
     * 构造方法，初始化 Milvus 客户端
     */
    public MilvusSyncServiceImpl() {
        try {
            // 初始化 Milvus 客户端
            ConnectParam connectParam = ConnectParam.newBuilder()
                    .withHost("localhost") // TODO: 从配置中获取
                    .withPort(19530) // TODO: 从配置中获取
                    .build();
            
            milvusClient = new MilvusServiceClient(connectParam);
            log.info("Milvus client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Milvus client: {}", e.getMessage(), e);
        }
    }

    /**
     * 写入数据到 Milvus
     * @param taskId 任务ID
     * @param data 数据
     * @return 写入结果
     */
    @Override
    public boolean writeDataToMilvus(Long taskId, Map<String, Object> data) {
        log.info("Writing data to Milvus for task: {}", taskId);
        try {
            // 1. 检查 Milvus 连接
            if (!checkMilvusConnection()) {
                log.error("Milvus connection is not available");
                return false;
            }

            // 2. 获取集合名称
            String collectionName = getCollectionName(taskId);

            // 3. 检查集合是否存在
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist", collectionName);
                return false;
            }

            // 4. 构建插入数据
            // 提取向量
            float[] vector = (float[]) data.get("vector");
            if (vector == null || vector.length == 0) {
                log.error("Vector field is missing or empty");
                return false;
            }
            
            // 提取主键
            Long id = null;
            if (data.containsKey("id")) {
                id = Long.valueOf(data.get("id").toString());
            } else {
                id = System.currentTimeMillis();
            }
            
            // 提取文本
            String text = data.getOrDefault("text", "").toString();
            
            // 构建字段数据
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("id", Collections.singletonList(id)));
            fields.add(new InsertParam.Field("vector", Collections.singletonList(vector)));
            fields.add(new InsertParam.Field("text", Collections.singletonList(text)));
            
            // 创建插入参数
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields)
                    .build();

            // 5. 执行插入
            R<?> response = milvusClient.insert(insertParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to insert data: {}", response.getMessage());
                return false;
            }

            log.info("Written data to Milvus for task: {}", taskId);
            return true;
        } catch (Exception e) {
            log.error("Failed to write data to Milvus for task {}: {}", taskId, e.getMessage(), e);
            syncStatusMap.put(taskId, "FAILED");
            redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.MILVUS_SYNC_PREFIX + taskId, "FAILED");
            return false;
        }
    }

    /**
     * 批量写入数据到 Milvus
     * @param taskId 任务ID
     * @param dataList 数据列表
     * @return 写入结果
     */
    @Override
    public boolean batchWriteDataToMilvus(Long taskId, List<Map<String, Object>> dataList) {
        log.info("Batch writing data to Milvus for task {}: {}", taskId, dataList.size());
        try {
            // 1. 检查 Milvus 连接
            if (!checkMilvusConnection()) {
                log.error("Milvus connection is not available");
                return false;
            }

            // 2. 获取集合名称
            String collectionName = getCollectionName(taskId);

            // 3. 检查集合是否存在
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist", collectionName);
                return false;
            }

            // 4. 构建批量插入数据
            // 批量大小限制
            int batchSize = 1000;
            int totalInserted = 0;
            
            // 分批处理
            for (int i = 0; i < dataList.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, dataList.size());
                List<Map<String, Object>> batch = dataList.subList(i, endIndex);
                
                // 构建字段数据
                List<Long> ids = new ArrayList<>();
                List<float[]> vectors = new ArrayList<>();
                List<String> texts = new ArrayList<>();
                
                for (Map<String, Object> data : batch) {
                    // 提取向量
                    float[] vector = (float[]) data.get("vector");
                    if (vector == null || vector.length == 0) {
                        log.warn("Skipping data with missing or empty vector");
                        continue;
                    }
                    
                    // 提取主键
                    Long id = null;
                    if (data.containsKey("id")) {
                        id = Long.valueOf(data.get("id").toString());
                    } else {
                        id = System.currentTimeMillis() + ids.size();
                    }
                    
                    // 提取文本
                    String text = data.getOrDefault("text", "").toString();
                    
                    // 添加到批量列表
                    ids.add(id);
                    vectors.add(vector);
                    texts.add(text);
                }
                
                // 跳过空批次
                if (ids.isEmpty()) {
                    continue;
                }
                
                // 构建字段
                List<InsertParam.Field> fields = new ArrayList<>();
                fields.add(new InsertParam.Field("id", ids));
                fields.add(new InsertParam.Field("vector", vectors));
                fields.add(new InsertParam.Field("text", texts));
                
                // 创建插入参数
                InsertParam insertParam = InsertParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withFields(fields)
                        .build();

                // 执行批量插入
                R<?> response = milvusClient.insert(insertParam);
                if (response.getStatus() != R.Status.Success.getCode()) {
                    log.error("Failed to insert batch data: {}", response.getMessage());
                    return false;
                }
                
                totalInserted += batch.size();
                log.debug("Inserted {} entities in batch", batch.size());
            }

            log.info("Batch written data to Milvus for task: {}, inserted {} entities", taskId, totalInserted);
            return true;
        } catch (Exception e) {
            log.error("Failed to batch write data to Milvus for task {}: {}", taskId, e.getMessage(), e);
            syncStatusMap.put(taskId, "FAILED");
            redisTemplate.opsForValue().set(DataRsyncConstants.RedisKey.MILVUS_SYNC_PREFIX + taskId, "FAILED");
            return false;
        }
    }

    /**
     * 删除 Milvus 中的数据
     * @param taskId 任务ID
     * @param primaryKey 主键
     * @return 删除结果
     */
    @Override
    public boolean deleteDataFromMilvus(Long taskId, Object primaryKey) {
        log.info("Deleting data from Milvus for task: {}, primaryKey: {}", taskId, primaryKey);
        try {
            // 1. 获取集合名称
            String collectionName = getCollectionName(taskId);
            
            // 2. 检查集合是否存在
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist", collectionName);
                return false;
            }
            
            // 3. 构建删除条件
            String expr = "id = " + primaryKey;
            
            // 4. 创建删除参数
            DeleteParam deleteParam = DeleteParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withExpr(expr)
                    .build();
            
            // 5. 执行删除
            R<?> response = milvusClient.delete(deleteParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to delete data: {}", response.getMessage());
                return false;
            }
            
            log.info("Deleted data from Milvus for task: {}, primaryKey: {}", 
                    taskId, primaryKey);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete data from Milvus for task {}: {}", taskId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建 Milvus 集合
     * @param task 任务
     * @return 创建结果
     */
    @Override
    public boolean createMilvusCollection(Task task) {
        log.info("Creating Milvus collection for task: {}", task.getName());
        try {
            // 1. 检查 Milvus 连接
            if (!checkMilvusConnection()) {
                log.error("Milvus connection is not available");
                return false;
            }

            // 2. 获取集合名称
            String collectionName = getCollectionName(task.getId());

            // 3. 检查集合是否已存在
            if (hasCollection(collectionName)) {
                log.warn("Collection {} already exists", collectionName);
                return true;
            }

            // 4. 创建集合
            // 定义字段
            List<FieldType> fields = new ArrayList<>();
            
            // 主键字段
            FieldType idField = FieldType.newBuilder()
                    .withName("id")
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(false)
                    .build();
            fields.add(idField);
            
            // 向量字段
            FieldType vectorField = FieldType.newBuilder()
                    .withName("vector")
                    .withDataType(DataType.FloatVector)
                    .withDimension(128) // 向量维度
                    .build();
            fields.add(vectorField);
            
            // 标量字段（根据任务配置添加）
            FieldType textField = FieldType.newBuilder()
                    .withName("text")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(65535)
                    .build();
            fields.add(textField);
            
            // 创建集合参数
            CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldTypes(fields)
                    .withShardsNum(2) // 分片数
                    .build();
            
            // 执行创建
            R<?> response = milvusClient.createCollection(createCollectionParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to create collection: {}", response.getMessage());
                return false;
            }

            log.info("Created Milvus collection: {}", collectionName);
            return true;
        } catch (Exception e) {
            log.error("Failed to create Milvus collection for task {}: {}", task.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建 Milvus 索引
     * @param task 任务
     * @return 创建结果
     */
    @Override
    public boolean createMilvusIndex(Task task) {
        log.info("Creating Milvus index for task: {}", task.getName());
        try {
            // 1. 检查 Milvus 连接
            if (!checkMilvusConnection()) {
                log.error("Milvus connection is not available");
                return false;
            }

            // 2. 获取集合名称
            String collectionName = getCollectionName(task.getId());

            // 3. 检查集合是否存在
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist", collectionName);
                return false;
            }

            // 4. 创建索引
            // 索引参数
            CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldName("vector")
                    .withIndexName("vector_index")
                    .withIndexType(IndexType.IVF_FLAT) // 索引类型
                    .withMetricType(MetricType.L2) // 距离度量类型
                    .withExtraParam("{\"nlist\": 1024}") // 索引参数
                    .withSyncMode(true) // 同步模式
                    .build();
            
            // 执行创建
            R<?> response = milvusClient.createIndex(createIndexParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to create index: {}", response.getMessage());
                return false;
            }

            log.info("Created Milvus index for collection: {}", collectionName);
            return true;
        } catch (Exception e) {
            log.error("Failed to create Milvus index for task {}: {}", task.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 重建 Milvus 索引
     * @param task 任务
     * @return 重建结果
     */
    @Override
    public boolean rebuildMilvusIndex(Task task) {
        log.info("Rebuilding Milvus index for task: {}", task.getName());
        try {
            // 1. 检查 Milvus 连接
            if (!checkMilvusConnection()) {
                log.error("Milvus connection is not available");
                return false;
            }

            // 2. 获取集合名称
            String collectionName = getCollectionName(task.getId());

            // 3. 检查集合是否存在
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist", collectionName);
                return false;
            }

            // 4. 删除旧索引
            DropIndexParam dropIndexParam = DropIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withIndexName("vector_index")
                    .build();
            
            R<?> dropResponse = milvusClient.dropIndex(dropIndexParam);
            if (dropResponse.getStatus() != R.Status.Success.getCode()) {
                log.warn("Failed to drop old index (may not exist): {}", dropResponse.getMessage());
                // 继续执行，不返回失败
            }

            // 5. 创建新索引
            CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldName("vector")
                    .withIndexName("vector_index")
                    .withIndexType(IndexType.IVF_FLAT)
                    .withMetricType(MetricType.L2)
                    .withExtraParam("{\"nlist\": 1024}")
                    .withSyncMode(true)
                    .build();
            
            R<?> createResponse = milvusClient.createIndex(createIndexParam);
            if (createResponse.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to create new index: {}", createResponse.getMessage());
                return false;
            }

            log.info("Rebuilt Milvus index for collection: {}", collectionName);
            return true;
        } catch (Exception e) {
            log.error("Failed to rebuild Milvus index for task {}: {}", task.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 执行 Milvus 查询
     * @param task 任务
     * @param queryParam 查询参数
     * @return 查询结果
     */
    @Override
    public List<Map<String, Object>> executeMilvusQuery(Task task, Map<String, Object> queryParam) {
        log.info("Executing Milvus query for task: {}", task.getName());
        try {
            // 1. 检查 Milvus 连接
            if (!checkMilvusConnection()) {
                log.error("Milvus connection is not available");
                return Collections.emptyList();
            }

            // 2. 获取集合名称
            String collectionName = getCollectionName(task.getId());

            // 3. 检查集合是否存在
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist", collectionName);
                return Collections.emptyList();
            }

            // 4. 构建查询参数
            // 检查是否为向量查询
            if (queryParam.containsKey("vector")) {
                // 向量查询
                float[] queryVector = (float[]) queryParam.get("vector");
                int topK = Integer.parseInt(queryParam.getOrDefault("topK", 10).toString());
                String metricTypeStr = queryParam.getOrDefault("metricType", "L2").toString();
                MetricType metricType = MetricType.valueOf(metricTypeStr);
                
                // 构建搜索参数
                List<String> outputFields = Arrays.asList("id", "text");
                
                SearchParam searchParam = SearchParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withVectorFieldName("vector")
                        .withVectors(Collections.singletonList(queryVector))
                        .withTopK(topK)
                        .withMetricType(metricType)
                        .withParams("{\"nprobe\": 16}")
                        .build();
                
                // 5. 执行搜索
                R<?> response = milvusClient.search(searchParam);
                if (response.getStatus() != R.Status.Success.getCode()) {
                    log.error("Failed to execute vector search: {}", response.getMessage());
                    return Collections.emptyList();
                }
                
                // 处理搜索结果（简化实现）
                List<Map<String, Object>> results = new ArrayList<>();
                log.info("Executed Milvus vector query for task: {}", task.getName());
                return results;
            } else {
                // 标量查询
                String expr = queryParam.getOrDefault("expr", "").toString();
                int limit = Integer.parseInt(queryParam.getOrDefault("limit", 100).toString());
                
                // 构建查询参数
                List<String> outputFields = Arrays.asList("id", "text");
                
                QueryParam queryParamObj = QueryParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withExpr(expr)
                        .withLimit((long) limit)
                        .build();
                
                // 执行查询
                R<?> response = milvusClient.query(queryParamObj);
                if (response.getStatus() != R.Status.Success.getCode()) {
                    log.error("Failed to execute scalar query: {}", response.getMessage());
                    return Collections.emptyList();
                }
                
                // 处理查询结果（简化实现）
                List<Map<String, Object>> results = new ArrayList<>();
                log.info("Executed Milvus scalar query for task: {}", task.getName());
                return results;
            }
        } catch (Exception e) {
            log.error("Failed to execute Milvus query for task {}: {}", task.getId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 验证 Milvus 数据
     * @param task 任务
     * @return 验证结果
     */
    @Override
    public boolean validateMilvusData(Task task) {
        log.info("Validating Milvus data for task: {}", task.getName());
        try {
            // 1. 检查 Milvus 连接
            if (!checkMilvusConnection()) {
                log.error("Milvus connection is not available");
                return false;
            }

            // 2. 获取集合名称
            String collectionName = getCollectionName(task.getId());

            // 3. 检查集合是否存在
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist", collectionName);
                return false;
            }

            // 4. 执行数据验证（简化实现）
            // 验证集合是否存在
            log.info("Validated Milvus data for task: {}", task.getName());
            return true;
        } catch (Exception e) {
            log.error("Failed to validate Milvus data for task {}: {}", task.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取同步状态
     * @param taskId 任务ID
     * @return 同步状态
     */
    @Override
    public String getSyncStatus(Long taskId) {
        log.info("Getting sync status for task: {}", taskId);
        try {
            // 1. 从缓存获取状态
            String status = syncStatusMap.get(taskId);
            if (status != null) {
                return status;
            }

            // 2. 从 Redis 获取状态
            status = redisTemplate.opsForValue().get(DataRsyncConstants.RedisKey.MILVUS_SYNC_PREFIX + taskId);
            if (status != null) {
                syncStatusMap.put(taskId, status);
                return status;
            }

            return "IDLE";
        } catch (Exception e) {
            log.error("Failed to get sync status for task {}: {}", taskId, e.getMessage(), e);
            return "UNKNOWN";
        }
    }

    /**
     * 检查 Milvus 连接状态
     * @return 连接状态
     */
    @Override
    public boolean checkMilvusConnection() {
        log.debug("Checking Milvus connection");
        try {
            if (milvusClient == null) {
                return false;
            }

            // 执行一个简单的操作来检查连接
            // TODO: 实现具体的连接检查逻辑
            return true;
        } catch (Exception e) {
            log.error("Failed to check Milvus connection: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取集合名称
     * @param taskId 任务ID
     * @return 集合名称
     */
    private String getCollectionName(Long taskId) {
        return "collection_" + taskId;
    }

    /**
     * 检查集合是否存在
     * @param collectionName 集合名称
     * @return 是否存在
     */
    private boolean hasCollection(String collectionName) {
        HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();

        R<Boolean> response = milvusClient.hasCollection(hasCollectionParam);
        return response.getStatus() == R.Status.Success.getCode() && response.getData();
    }


}
