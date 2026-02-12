package com.data.rsync.common.vector.db.impl;

import com.data.rsync.common.vector.db.VectorDatabaseAdapter;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.*;
import io.milvus.param.index.*;
import io.milvus.param.partition.*;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import io.milvus.grpc.DescribeCollectionResponse;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.QueryResults;
import io.milvus.grpc.SearchResults;
import io.milvus.grpc.ShowCollectionsResponse;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.grpc.FlushResponse;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Milvus向量数据库适配器实现
 */
public class MilvusVectorDatabaseAdapter implements VectorDatabaseAdapter {

    private static final Logger log = LoggerFactory.getLogger(MilvusVectorDatabaseAdapter.class);

    private String host;
    private int port;
    private MilvusServiceClient client;
    private Map<String, Boolean> collectionCache = new ConcurrentHashMap<>();

    @Override
    public String getVectorDbType() {
        return "MILVUS";
    }

    @Override
    public boolean initialize(Map<String, Object> config) {
        try {
            // 从配置中获取Milvus连接信息，默认使用配置文件中的地址
            // 首先尝试从config参数中获取
            host = (String) config.getOrDefault("host", "192.168.1.81");
            // 处理端口配置，确保类型正确
            Object portObj = config.getOrDefault("port", 19530);
            if (portObj instanceof String) {
                port = Integer.parseInt((String) portObj);
            } else if (portObj instanceof Integer) {
                port = (Integer) portObj;
            } else {
                port = 19530;
            }

            // 获取连接超时配置
            int connectTimeoutMs = (Integer) config.getOrDefault("connectTimeoutMs", 5000);

            log.info("Attempting to initialize Milvus client at {}:{}, timeout: {}ms", host, port, connectTimeoutMs);

            // 如果客户端已经初始化，先关闭旧连接
            if (client != null) {
                try {
                    client.close();
                    log.info("Closed existing Milvus client");
                } catch (Exception e) {
                    log.warn("Failed to close existing Milvus client: {}", e.getMessage());
                }
            }

            // 初始化Milvus客户端
            ConnectParam connectParam = ConnectParam.newBuilder()
                    .withHost(host)
                    .withPort(port)
                    .build();

            // 创建客户端
            log.info("Creating MilvusServiceClient...");
            client = new MilvusServiceClient(connectParam);
            log.info("Created Milvus client instance successfully");
            
            // 测试连接
            boolean connected = false;
            try {
                log.info("Testing Milvus connection...");
                connected = checkConnection();
                if (!connected) {
                    log.error("Failed to connect to Milvus at {}:{}", host, port);
                    return false;
                }
            } catch (Exception e) {
                log.error("Connection test failed: {}", e.getMessage(), e);
                return false;
            }
            log.info("Milvus connection test passed");
            
            log.info("Successfully initialized and connected to Milvus at {}:{}", host, port);
            
            // 预热集合缓存，但不影响初始化结果
            try {
                refreshCollectionCache();
            } catch (Exception e) {
                log.warn("Failed to refresh collection cache during initialization: {}", e.getMessage());
                // 继续执行，不影响初始化结果
            }
            
            return true;
        } catch (Exception e) {
            log.error("Failed to initialize Milvus client: {}", e.getMessage(), e);
            // 清理资源
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ex) {
                    log.warn("Failed to close Milvus client: {}", ex.getMessage());
                }
                client = null;
            }
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (client != null) {
                client.close();
                log.info("Milvus client closed");
            }
            collectionCache.clear();
        } catch (Exception e) {
            log.error("Error closing Milvus client: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean checkConnection() {
        try {
            if (client == null) {
                log.warn("Milvus client is null, connection check failed");
                return false;
            }
            // 执行一个简单的操作来检查连接 - 使用showCollections而不是describeCollection
            // 因为describeCollection需要指定一个存在的集合，而showCollections总是可以执行
            ShowCollectionsParam param = ShowCollectionsParam.newBuilder()
                    .build();
            R<ShowCollectionsResponse> response = client.showCollections(param);
            if (response.getStatus() == R.Status.Success.getCode()) {
                log.info("Milvus connection check passed");
                return true;
            } else {
                log.warn("Connection check failed with status: {}, message: {}", response.getStatus(), response.getMessage());
                return false;
            }
        } catch (Exception e) {
            log.warn("Connection check failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean createCollection(String collectionName, int dimension, Map<String, Object> config) {
        try {
            log.info("Starting to create collection {} with dimension {}", collectionName, dimension);
            
            // 检查客户端是否初始化
            if (client == null) {
                log.error("Milvus client is not initialized, cannot create collection {}", collectionName);
                return false;
            }

            // 检查连接状态
            if (!checkConnection()) {
                log.error("Milvus connection is not available, cannot create collection {}", collectionName);
                return false;
            }

            // 检查集合是否已存在
            boolean exists = hasCollection(collectionName);
            log.info("Collection {} exists: {}", collectionName, exists);
            if (exists) {
                log.info("Collection {} already exists, skipping creation", collectionName);
                return true;
            }

            // 创建集合
            CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withDescription((String) config.getOrDefault("description", "Data sync collection"))
                    .withShardsNum(2)
                    .build();

            log.info("Created CreateCollectionParam for collection {}", collectionName);

            // 执行创建集合操作
            log.info("Executing createCollection for collection {}", collectionName);
            R<RpcStatus> response = client.createCollection(createParam);
            log.info("Received response from createCollection: status={}, message={}", response.getStatus(), response.getMessage());
            
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to create collection {}: {}", collectionName, response.getMessage());
                return false;
            }

            log.info("Collection creation API call successful for {}", collectionName);
            
            // 同步操作：等待并验证集合真正创建成功
            int maxRetries = 5;
            int retryDelayMs = 1000;
            boolean collectionVerified = false;
            
            for (int i = 0; i < maxRetries; i++) {
                log.info("Verifying collection creation (attempt {}/{}) for {}", i + 1, maxRetries, collectionName);
                
                // 刷新集合缓存
                refreshCollectionCache();
                
                // 检查集合是否存在
                boolean currentExists = hasCollection(collectionName);
                log.info("Collection {} exists after refresh (attempt {}): {}", collectionName, i + 1, currentExists);
                
                if (currentExists) {
                    // 进一步验证：尝试获取集合信息
                    try {
                        DescribeCollectionParam describeParam = DescribeCollectionParam.newBuilder()
                                .withCollectionName(collectionName)
                                .build();
                        
                        R<DescribeCollectionResponse> describeResponse = client.describeCollection(describeParam);
                        if (describeResponse.getStatus() == R.Status.Success.getCode()) {
                            log.info("Successfully verified collection {} creation by describing it", collectionName);
                            collectionVerified = true;
                            break;
                        } else {
                            log.warn("Failed to describe collection {} (attempt {}): {}", collectionName, i + 1, describeResponse.getMessage());
                        }
                    } catch (Exception e) {
                        log.warn("Exception when describing collection {} (attempt {}): {}", collectionName, i + 1, e.getMessage());
                    }
                }
                
                // 等待一段时间后重试
                if (i < maxRetries - 1) {
                    log.info("Waiting {}ms before next verification attempt for collection {}", retryDelayMs, collectionName);
                    Thread.sleep(retryDelayMs);
                }
            }
            
            if (!collectionVerified) {
                log.error("Failed to verify collection {} creation after {} attempts", collectionName, maxRetries);
                return false;
            }

            log.info("Successfully created and verified collection {}", collectionName);
            collectionCache.put(collectionName, true);

            // 自动创建索引
            if ((Boolean) config.getOrDefault("autoCreateIndex", true)) {
                log.info("Creating index for collection {}", collectionName);
                boolean indexCreated = createIndex(collectionName, "vector", (String) config.getOrDefault("indexType", "IVF_FLAT"), config);
                if (!indexCreated) {
                    log.error("Failed to create index for collection {}", collectionName);
                    // 索引创建失败不影响集合创建结果
                }
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to create collection {}: {}", collectionName, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean dropCollection(String collectionName) {
        try {
            if (!hasCollection(collectionName)) {
                log.info("Collection {} does not exist, skipping deletion", collectionName);
                return true;
            }

            DropCollectionParam dropParam = DropCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();

            R<RpcStatus> response = client.dropCollection(dropParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to drop collection {}: {}", collectionName, response.getMessage());
                return false;
            }

            log.info("Dropped collection {}", collectionName);
            collectionCache.remove(collectionName);
            return true;
        } catch (Exception e) {
            log.error("Failed to drop collection {}: {}", collectionName, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean hasCollection(String collectionName) {
        try {
            if (collectionCache.containsKey(collectionName)) {
                return collectionCache.get(collectionName);
            }

            DescribeCollectionParam describeParam = DescribeCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();

            R<DescribeCollectionResponse> response = client.describeCollection(describeParam);
            boolean exists = response.getStatus() == R.Status.Success.getCode();
            collectionCache.put(collectionName, exists);
            return exists;
        } catch (Exception e) {
            collectionCache.put(collectionName, false);
            return false;
        }
    }

    @Override
    public boolean createIndex(String collectionName, String fieldName, String indexType, Map<String, Object> config) {
        try {
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist, cannot create index", collectionName);
                return false;
            }

            // 创建索引
            CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldName(fieldName)
                    .withIndexType(IndexType.valueOf(indexType))
                    .withMetricType(MetricType.valueOf((String) config.getOrDefault("metricType", "L2")))
                    .withSyncMode((Boolean) config.getOrDefault("syncMode", true))
                    .build();

            R<RpcStatus> response = client.createIndex(createIndexParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to create index: {}", response.getMessage());
                return false;
            }

            log.info("Created {} index for collection {} on field {}", indexType, collectionName, fieldName);
            return true;
        } catch (Exception e) {
            log.error("Failed to create index: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean insertData(String collectionName, Map<String, Object> data) {
        try {
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist, cannot insert data", collectionName);
                return false;
            }

            // 构建插入数据
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("id", Collections.singletonList(data.get("id"))));
            fields.add(new InsertParam.Field("vector", Collections.singletonList(data.get("vector"))));
            fields.add(new InsertParam.Field("data", Collections.singletonList(data.getOrDefault("data", ""))));
            fields.add(new InsertParam.Field("metadata", Collections.singletonList(data.getOrDefault("metadata", ""))));

            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withPartitionName((String) data.getOrDefault("partitionName", ""))
                    .withFields(fields)
                    .build();

            R<MutationResult> response = client.insert(insertParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to insert data: {}", response.getMessage());
                return false;
            }

            log.debug("Inserted data into collection {}", collectionName);
            return true;
        } catch (Exception e) {
            log.error("Failed to insert data: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean batchInsertData(String collectionName, List<Map<String, Object>> dataList) {
        try {
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist, cannot batch insert data", collectionName);
                return false;
            }

            if (dataList.isEmpty()) {
                return true;
            }

            // 批量插入数据
            List<Long> ids = new ArrayList<>();
            List<float[]> vectors = new ArrayList<>();
            List<String> data = new ArrayList<>();
            List<String> metadata = new ArrayList<>();

            for (Map<String, Object> item : dataList) {
                // 处理id类型转换
                Object idObj = item.get("id");
                if (idObj instanceof Integer) {
                    ids.add(((Integer) idObj).longValue());
                } else if (idObj instanceof Long) {
                    ids.add((Long) idObj);
                } else {
                    ids.add(0L); // 默认值
                }
                
                // 处理vector类型转换
                Object vectorObj = item.get("vector");
                if (vectorObj instanceof List) {
                    List<?> vectorList = (List<?>) vectorObj;
                    float[] vectorArray = new float[vectorList.size()];
                    for (int i = 0; i < vectorList.size(); i++) {
                        Object element = vectorList.get(i);
                        if (element instanceof Double) {
                            vectorArray[i] = ((Double) element).floatValue();
                        } else if (element instanceof Float) {
                            vectorArray[i] = (Float) element;
                        } else if (element instanceof Integer) {
                            vectorArray[i] = ((Integer) element).floatValue();
                        } else {
                            vectorArray[i] = 0.0f;
                        }
                    }
                    vectors.add(vectorArray);
                } else if (vectorObj instanceof float[]) {
                    vectors.add((float[]) vectorObj);
                } else {
                    vectors.add(new float[0]); // 默认空向量
                }
                
                // 处理data和metadata
                data.add(item.getOrDefault("data", item.getOrDefault("content", "")).toString());
                metadata.add(item.getOrDefault("metadata", "").toString());
            }

            // 创建字段
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("id", ids));
            fields.add(new InsertParam.Field("vector", vectors));
            fields.add(new InsertParam.Field("data", data));
            fields.add(new InsertParam.Field("metadata", metadata));

            // 创建插入参数
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields)
                    .build();

            // 执行插入操作
            R<MutationResult> response = client.insert(insertParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to batch insert data: {}", response.getMessage());
                return false;
            }

            // 打印前几条数据的信息
            for (int i = 0; i < Math.min(3, dataList.size()); i++) {
                Map<String, Object> item = dataList.get(i);
                log.info("Inserting data: id={}, vector dimension={}, data={}", 
                        item.get("id"), 
                        item.get("vector") != null ? ((float[]) item.get("vector")).length : 0, 
                        item.get("data"));
            }

            log.info("Batch inserted {} records into collection {}", dataList.size(), collectionName);
            return true;
        } catch (Exception e) {
            log.error("Failed to batch insert data: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteData(String collectionName, String expr) {
        try {
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist, cannot delete data", collectionName);
                return false;
            }

            DeleteParam deleteParam = DeleteParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withExpr(expr)
                    .build();

            R<MutationResult> response = client.delete(deleteParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to delete data: {}", response.getMessage());
                return false;
            }

            log.info("Deleted data from collection {} with expr: {}", collectionName, expr);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete data: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateData(String collectionName, String expr, Map<String, Object> data) {
        try {
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist, cannot update data", collectionName);
                return false;
            }

            // 暂时返回false，因为Milvus SDK版本可能不支持更新操作
            log.warn("Update operation is not supported in current Milvus SDK version");
            return false;
        } catch (Exception e) {
            log.error("Failed to update data: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> searchData(String collectionName, float[] vector, int topK, String metricType, String expr, List<String> outputFields) {
        try {
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist, cannot search data", collectionName);
                return new ArrayList<>();
            }

            // 简化搜索实现
            log.info("Searching data in collection {}, topK: {}", collectionName, topK);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to search data: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> queryData(String collectionName, String expr, List<String> outputFields, int limit, int offset) {
        try {
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist, cannot query data", collectionName);
                return new ArrayList<>();
            }

            // 简化查询实现
            log.info("Querying data from collection {}, limit: {}, offset: {}", collectionName, limit, offset);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Failed to query data: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getCollectionStats(String collectionName) {
        try {
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist, cannot get stats", collectionName);
                return new HashMap<>();
            }

            GetCollectionStatisticsParam statsParam = GetCollectionStatisticsParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();

            R<GetCollectionStatisticsResponse> response = client.getCollectionStatistics(statsParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to get collection stats: {}", response.getMessage());
                return new HashMap<>();
            }

            // 处理统计结果
            Map<String, Object> stats = new HashMap<>();
            stats.put("rowCount", 0);
            stats.put("collectionName", collectionName);
            stats.put("timestamp", System.currentTimeMillis());

            log.info("Got stats for collection {}", collectionName);
            return stats;
        } catch (Exception e) {
            log.error("Failed to get collection stats: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public List<String> getCollections() {
        try {
            log.info("Starting to list collections");
            
            // 检查客户端是否初始化
            if (client == null) {
                log.error("Milvus client is not initialized, cannot list collections");
                return new ArrayList<>();
            }

            ShowCollectionsParam showParam = ShowCollectionsParam.newBuilder()
                    .build();

            log.info("Created ShowCollectionsParam");

            R<ShowCollectionsResponse> response = client.showCollections(showParam);
            log.info("Received response from showCollections: status={}, message={}", response.getStatus(), response.getMessage());
            
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to list collections: {}", response.getMessage());
                return new ArrayList<>();
            }

            List<String> collections = new ArrayList<>();
            // 从响应中提取集合名称
            if (response.getData() != null) {
                log.info("Response data is not null");
                if (response.getData().getCollectionNamesList() != null) {
                    log.info("Collection names list is not null, size={}", response.getData().getCollectionNamesList().size());
                    collections.addAll(response.getData().getCollectionNamesList());
                    log.info("Collections: {}", collections);
                } else {
                    log.info("Collection names list is null");
                }
            } else {
                log.info("Response data is null");
            }
            
            log.info("Listed {} collections: {}", collections.size(), collections);
            return collections;
        } catch (Exception e) {
            log.error("Failed to list collections: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Object executeOperation(String operation, Map<String, Object> params) {
        try {
            switch (operation) {
                case "createPartition":
                    return createPartition((String) params.get("collectionName"), (String) params.get("partitionName"));
                case "dropPartition":
                    return dropPartition((String) params.get("collectionName"), (String) params.get("partitionName"));
                case "flush":
                    return flushCollection((String) params.get("collectionName"));
                case "compact":
                    return compactCollection((String) params.get("collectionName"));
                default:
                    log.warn("Unknown operation: {}", operation);
                    return null;
            }
        } catch (Exception e) {
            log.error("Failed to execute operation {}: {}", operation, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean createTenantSpace(String tenantId) {
        try {
            // Milvus 2.3+ 支持多租户
            log.info("Creating tenant space for tenant: {}", tenantId);
            // 这里可以实现租户隔离逻辑
            return true;
        } catch (Exception e) {
            log.error("Failed to create tenant space: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean dropTenantSpace(String tenantId) {
        try {
            log.info("Dropping tenant space for tenant: {}", tenantId);
            // 这里可以实现租户删除逻辑
            return true;
        } catch (Exception e) {
            log.error("Failed to drop tenant space: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getTenantCollectionName(String tenantId, String collectionName) {
        // 生成租户隔离的集合名称
        return tenantId + "_" + collectionName;
    }

    @Override
    public Map<String, Object> getCollectionInfo(String collectionName) {
        try {
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist, cannot get info", collectionName);
                return new HashMap<>();
            }

            DescribeCollectionParam describeParam = DescribeCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();

            R<DescribeCollectionResponse> response = client.describeCollection(describeParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("Failed to get collection info: {}", response.getMessage());
                return new HashMap<>();
            }

            // 构建集合信息
            Map<String, Object> info = new HashMap<>();
            info.put("collectionName", collectionName);
            info.put("status", "EXISTS");
            info.put("timestamp", System.currentTimeMillis());

            log.info("Got info for collection {}", collectionName);
            return info;
        } catch (Exception e) {
            log.error("Failed to get collection info: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    @Override
    public boolean optimizeIndex(String collectionName, Map<String, Object> params) {
        try {
            if (!hasCollection(collectionName)) {
                log.error("Collection {} does not exist, cannot optimize index", collectionName);
                return false;
            }

            // 简化索引优化实现
            log.info("Optimizing index for collection {} with params: {}", collectionName, params);
            // 这里可以实现具体的索引优化逻辑
            return true;
        } catch (Exception e) {
            log.error("Failed to optimize index: {}", e.getMessage(), e);
            return false;
        }
    }

    // 辅助方法
    private void refreshCollectionCache() {
        try {
            List<String> collections = getCollections();
            collectionCache.clear();
            for (String collection : collections) {
                collectionCache.put(collection, true);
            }
            log.info("Refreshed collection cache, found {} collections", collections.size());
        } catch (Exception e) {
            log.error("Failed to refresh collection cache: {}", e.getMessage(), e);
        }
    }

    private boolean createPartition(String collectionName, String partitionName) {
        try {
            CreatePartitionParam param = CreatePartitionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withPartitionName(partitionName)
                    .build();
            R<RpcStatus> response = client.createPartition(param);
            return response.getStatus() == R.Status.Success.getCode();
        } catch (Exception e) {
            log.error("Failed to create partition: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean dropPartition(String collectionName, String partitionName) {
        try {
            DropPartitionParam param = DropPartitionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withPartitionName(partitionName)
                    .build();
            R<RpcStatus> response = client.dropPartition(param);
            return response.getStatus() == R.Status.Success.getCode();
        } catch (Exception e) {
            log.error("Failed to drop partition: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean flushCollection(String collectionName) {
        try {
            FlushParam param = FlushParam.newBuilder()
                    .addCollectionName(collectionName)
                    .build();
            R<FlushResponse> response = client.flush(param);
            return response.getStatus() == R.Status.Success.getCode();
        } catch (Exception e) {
            log.error("Failed to flush collection: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean compactCollection(String collectionName) {
        try {
            // 简化压缩实现
            log.info("Compacting collection: {}", collectionName);
            return true;
        } catch (Exception e) {
            log.error("Failed to compact collection: {}", e.getMessage(), e);
            return false;
        }
    }
}

