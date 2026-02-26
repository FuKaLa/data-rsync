package com.data.rsync.data.controller;

import com.data.rsync.common.model.Response;
import com.data.rsync.data.service.MilvusSyncService;
import com.data.rsync.data.vo.MilvusCollectionResponse;
import com.data.rsync.data.vo.MilvusIndexResponse;
import com.data.rsync.data.vo.MilvusCreateIndexRequest;
import com.data.rsync.data.vo.MilvusHealthResponse;
import com.data.rsync.data.vo.MilvusOptimizeResponse;
import com.data.rsync.data.vo.MilvusCreateCollectionRequest;
import com.data.rsync.data.vo.MilvusSearchRequest;
import com.data.rsync.data.vo.MilvusCollectionStatsResponse;
import com.data.rsync.data.vo.MilvusSearchResponse;
import com.data.rsync.data.vo.MilvusWriteResponse;
import com.data.rsync.data.vo.DataConsistencyCheckResponse;
import com.data.rsync.data.vo.MilvusSyncResponse;
import com.data.rsync.data.vo.MilvusVerifyResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Milvus 管理控制器
 */
@RestController
@RequestMapping("/milvus")
public class MilvusController {

    @Resource
    @Qualifier("milvusSyncServiceImpl")
    private MilvusSyncService milvusSyncService;

    /**
     * 获取 Milvus 集合列表
     * @return 集合列表
     */
    @GetMapping("/collections")
    public Response<List<MilvusCollectionResponse>> getCollections() {
        try {
            List<MilvusCollectionResponse> collections = milvusSyncService.listCollections();
            return Response.success(collections);
        } catch (Exception e) {
            return Response.failure(500, "获取Milvus集合列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查 Milvus 连接状态
     * @return 连接状态
     */
    @GetMapping("/health")
    public Response<MilvusHealthResponse> getHealth() {
        try {
            MilvusHealthResponse response = milvusSyncService.checkMilvusHealth();
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取Milvus健康状态失败");
        }
    }

    /**
     * 获取 Milvus 集合索引
     * @param collectionName 集合名称
     * @return 索引列表
     */
    @GetMapping("/indexes/{collectionName}")
    public Response<List<MilvusIndexResponse>> getIndexes(@PathVariable String collectionName) {
        try {
            List<MilvusIndexResponse> indexes = milvusSyncService.listCollectionIndexes(collectionName);
            return Response.success(indexes);
        } catch (Exception e) {
            return Response.failure(500, "获取Milvus索引列表失败");
        }
    }

    /**
     * 创建 Milvus 索引
     * @param collectionName 集合名称
     * @param createIndexRequest 索引参数
     * @return 创建结果
     */
    @PostMapping("/indexes/{collectionName}")
    public Response<MilvusIndexResponse> createIndex(@PathVariable String collectionName, @RequestBody MilvusCreateIndexRequest createIndexRequest) {
        try {
            Map<String, Object> indexParams = new HashMap<>();
            indexParams.put("fieldName", createIndexRequest.getFieldName());
            indexParams.put("indexType", createIndexRequest.getIndexType());
            indexParams.put("metricType", createIndexRequest.getMetricType());
            indexParams.put("indexParams", createIndexRequest.getIndexParams());
            MilvusIndexResponse response = milvusSyncService.createCollectionIndex(collectionName, indexParams);
            return Response.success("索引创建成功", response);
        } catch (Exception e) {
            return Response.failure(500, "创建Milvus索引失败");
        }
    }

    /**
     * 删除 Milvus 索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     * @return 删除结果
     */
    @DeleteMapping("/indexes/{collectionName}/{indexName}")
    public Response<String> deleteIndex(@PathVariable String collectionName, @PathVariable String indexName) {
        try {
            milvusSyncService.dropCollectionIndex(collectionName, indexName);
            return Response.success("索引删除成功");
        } catch (Exception e) {
            return Response.failure(500, "删除Milvus索引失败");
        }
    }



    /**
     * 优化 Milvus 集合
     * @param collectionName 集合名称
     * @return 优化结果
     */
    @PostMapping("/optimize/{collectionName}")
    public Response<MilvusOptimizeResponse> optimizeCollection(@PathVariable String collectionName) {
        try {
            MilvusOptimizeResponse response = milvusSyncService.optimizeCollection(collectionName);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "优化Milvus集合失败");
        }
    }

    /**
     * 获取 Milvus 集合统计信息
     * @param collectionName 集合名称
     * @return 统计信息
     */
    @GetMapping("/collections/{collectionName}/stats")
    public Response<MilvusCollectionStatsResponse> getCollectionStats(@PathVariable String collectionName) {
        try {
            MilvusCollectionStatsResponse response = milvusSyncService.getCollectionStats(collectionName);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "获取Milvus集合统计信息失败");
        }
    }

    /**
     * 创建 Milvus 集合
     * @param createCollectionRequest 集合参数
     * @return 创建结果
     */
    @PostMapping("/collections")
    public Response<Boolean> createCollection(@RequestBody MilvusCreateCollectionRequest createCollectionRequest) {
        try {
            String collectionName = createCollectionRequest.getCollectionName();
            Integer dimension = createCollectionRequest.getDimension();
            String metricType = createCollectionRequest.getMetricType();
            
            milvusSyncService.createMilvusCollection(collectionName, dimension, metricType);
            return Response.success("集合创建成功", true);
        } catch (Exception e) {
            return Response.failure(500, "创建Milvus集合失败");
        }
    }

    /**
     * 删除 Milvus 集合
     * @param collectionName 集合名称
     * @return 删除结果
     */
    @DeleteMapping("/collections/{collectionName}")
    public Response<String> deleteCollection(@PathVariable String collectionName) {
        try {
            milvusSyncService.dropMilvusCollection(collectionName);
            return Response.success("集合删除成功");
        } catch (Exception e) {
            return Response.failure(500, "删除Milvus集合失败");
        }
    }

    /**
     * 向 Milvus 集合插入数据
     * @param collectionName 集合名称
     * @param data 数据
     * @return 插入结果
     */
    @PostMapping("/collections/{collectionName}/insert")
    public Response<Boolean> insertData(@PathVariable String collectionName, @RequestBody List<Map<String, Object>> data) {
        try {
            milvusSyncService.batchWriteDataToMilvus(collectionName, data);
            return Response.success("数据插入成功", true);
        } catch (Exception e) {
            return Response.failure(500, "插入数据到Milvus失败");
        }
    }

    /**
     * 从 Milvus 集合删除数据
     * @param collectionName 集合名称
     * @param ids 数据 ID 列表
     * @return 删除结果
     */
    @DeleteMapping("/collections/{collectionName}/delete")
    public Response<Boolean> deleteData(@PathVariable String collectionName, @RequestBody List<Long> ids) {
        try {
            milvusSyncService.deleteDataFromMilvus(collectionName, ids);
            return Response.success("数据删除成功", true);
        } catch (Exception e) {
            return Response.failure(500, "从Milvus删除数据失败");
        }
    }

    /**
     * 搜索 Milvus 集合数据
     * @param collectionName 集合名称
     * @param searchRequest 搜索参数
     * @return 搜索结果
     */
    @PostMapping("/collections/{collectionName}/search")
    public Response<MilvusSearchResponse> searchData(@PathVariable String collectionName, @RequestBody MilvusSearchRequest searchRequest) {
        try {
            List<Float> vector = searchRequest.getVector();
            Integer topK = searchRequest.getTopK();
            Float radius = searchRequest.getRadius();
            
            MilvusSearchResponse response = milvusSyncService.searchDataInMilvus(collectionName, vector, topK, radius);
            return Response.success(response);
        } catch (Exception e) {
            return Response.failure(500, "在Milvus中搜索数据失败");
        }
    }

    /**
     * 批量操作 Milvus 数据
     * @param batchParams 批量操作参数
     * @return 操作结果
     */
    @PostMapping("/batch")
    public Response<MilvusWriteResponse> batchOperation(@RequestBody Map<String, Object> batchParams) {
        try {
            String operation = (String) batchParams.get("operation");
            String collectionName = (String) batchParams.get("collectionName");
            // 安全地转换data类型
            Object dataObj = batchParams.get("data");
            List<Map<String, Object>> data = new ArrayList<>();
            if (dataObj instanceof List) {
                List<?> dataList = (List<?>) dataObj;
                for (Object item : dataList) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> dataMap = (Map<String, Object>) item;
                        data.add(dataMap);
                    }
                }
            }
            
            MilvusWriteResponse response = milvusSyncService.batchWriteDataToMilvus(collectionName, data);
            return Response.success("批量操作成功", response);
        } catch (Exception e) {
            return Response.failure(500, "Milvus批量操作失败");
        }
    }

    /**
     * 检查 Milvus 数据一致性
     * @param collectionName 集合名称
     * @return 一致性检查结果
     */
    @GetMapping("/consistency/{collectionName}")
    public Response<DataConsistencyCheckResponse> checkConsistency(@PathVariable String collectionName) {
        try {
            DataConsistencyCheckResponse response = milvusSyncService.checkDataConsistency(collectionName);
            return Response.success("数据一致性检查成功", response);
        } catch (Exception e) {
            return Response.failure(500, "检查Milvus数据一致性失败");
        }
    }

    /**
     * 重建 Milvus 集合索引
     * @param collectionName 集合名称
     * @param indexName 索引名称
     * @return 重建结果
     */
    @PostMapping("/indexes/{collectionName}/rebuild")
    public Response<Boolean> rebuildIndex(@PathVariable String collectionName, @RequestParam String indexName) {
        try {
            milvusSyncService.rebuildCollectionIndex(collectionName, indexName);
            return Response.success("索引重建成功", true);
        } catch (Exception e) {
            return Response.failure(500, "重建Milvus索引失败");
        }
    }

    /**
     * 查询 Milvus 数据
     * @param collectionName 集合名称
     * @param expr 查询表达式
     * @param outputFields 输出字段
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 查询结果
     */
    @GetMapping("/collections/{collectionName}/query")
    public Response<List<Map<String, Object>>> queryData(@PathVariable String collectionName,
                                                       @RequestParam(required = false) String expr,
                                                       @RequestParam(required = false) List<String> outputFields,
                                                       @RequestParam(defaultValue = "10") int limit,
                                                       @RequestParam(defaultValue = "0") int offset) {
        try {
            List<Map<String, Object>> result = milvusSyncService.queryMilvusData(collectionName, expr, outputFields, limit, offset);
            return Response.success(result);
        } catch (Exception e) {
            return Response.failure(500, "查询Milvus数据失败");
        }
    }

    /**
     * 从数据库同步数据到向量库
     * @param params 同步参数
     * @return 同步结果
     */
    @PostMapping("/sync/database")
    public Response<MilvusSyncResponse> syncDatabaseToVectorDB(@RequestBody Map<String, Object> params) {
        try {
            Map<String, Object> databaseConfig = (Map<String, Object>) params.get("databaseConfig");
            String collectionName = (String) params.get("collectionName");
            String sql = (String) params.get("sql");
            List<String> vectorFields = (List<String>) params.get("vectorFields");

            MilvusSyncResponse response = milvusSyncService.syncDatabaseToVectorDB(databaseConfig, collectionName, sql, vectorFields);
            return Response.success("数据库到向量库同步完成", response);
        } catch (Exception e) {
            return Response.failure(500, "数据库到向量库同步失败");
        }
    }

    /**
     * 从数据库表同步数据到向量库
     * @param params 同步参数
     * @return 同步结果
     */
    @PostMapping("/sync/table")
    public Response<MilvusSyncResponse> syncTableToVectorDB(@RequestBody Map<String, Object> params) {
        try {
            Map<String, Object> databaseConfig = (Map<String, Object>) params.get("databaseConfig");
            String collectionName = (String) params.get("collectionName");
            String databaseName = (String) params.get("databaseName");
            String tableName = (String) params.get("tableName");
            List<String> vectorFields = (List<String>) params.get("vectorFields");
            String whereClause = (String) params.getOrDefault("whereClause", "");

            MilvusSyncResponse response = milvusSyncService.syncTableToVectorDB(databaseConfig, collectionName, databaseName, tableName, vectorFields, whereClause);
            return Response.success("数据库表到向量库同步完成", response);
        } catch (Exception e) {
            return Response.failure(500, "数据库表到向量库同步失败");
        }
    }

    /**
     * 验证数据库同步到向量库的结果
     * @param collectionName 集合名称
     * @param expectedCount 预期数据数量
     * @return 验证结果
     */
    @GetMapping("/sync/verify")
    public Response<MilvusVerifyResponse> verifyDatabaseSyncToVectorDB(@RequestParam String collectionName, @RequestParam long expectedCount) {
        try {
            MilvusVerifyResponse response = milvusSyncService.verifyDatabaseSyncToVectorDB(collectionName, expectedCount);
            return Response.success("数据库同步到向量库的验证完成", response);
        } catch (Exception e) {
            return Response.failure(500, "数据库同步到向量库的验证失败");
        }
    }

    /**
     * 获取同步任务进度
     * @param taskId 任务ID
     * @return 同步进度
     */
    @GetMapping("/sync/progress")
    public Response<com.data.rsync.common.model.SyncProgress> getSyncProgress(@RequestParam Long taskId) {
        try {
            com.data.rsync.common.model.SyncProgress progress = milvusSyncService.getSyncProgress(taskId);
            if (progress == null) {
                return Response.failure(404, "任务不存在");
            }
            return Response.success(progress);
        } catch (Exception e) {
            return Response.failure(500, "获取同步进度失败");
        }
    }
}
