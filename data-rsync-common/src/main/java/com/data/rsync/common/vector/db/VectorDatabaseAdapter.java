package com.data.rsync.common.vector.db;

import com.data.rsync.common.model.Task;

import java.util.List;
import java.util.Map;

/**
 * 向量数据库适配器接口
 * 隔离具体的向量数据库实现
 */
public interface VectorDatabaseAdapter {

    /**
     * 获取向量数据库类型
     * @return 向量数据库类型
     */
    String getVectorDbType();

    /**
     * 初始化向量数据库连接
     * @param config 连接配置
     * @return 初始化是否成功
     */
    boolean initialize(Map<String, Object> config);

    /**
     * 关闭向量数据库连接
     */
    void close();

    /**
     * 检查连接状态
     * @return 连接是否可用
     */
    boolean checkConnection();

    /**
     * 创建集合
     * @param collectionName 集合名称
     * @param dimension 向量维度
     * @param config 集合配置
     * @return 创建是否成功
     */
    boolean createCollection(String collectionName, int dimension, Map<String, Object> config);

    /**
     * 删除集合
     * @param collectionName 集合名称
     * @return 删除是否成功
     */
    boolean dropCollection(String collectionName);

    /**
     * 检查集合是否存在
     * @param collectionName 集合名称
     * @return 集合是否存在
     */
    boolean hasCollection(String collectionName);

    /**
     * 创建索引
     * @param collectionName 集合名称
     * @param fieldName 字段名称
     * @param indexType 索引类型
     * @param config 索引配置
     * @return 创建是否成功
     */
    boolean createIndex(String collectionName, String fieldName, String indexType, Map<String, Object> config);

    /**
     * 插入数据
     * @param collectionName 集合名称
     * @param data 数据
     * @return 插入是否成功
     */
    boolean insertData(String collectionName, Map<String, Object> data);

    /**
     * 批量插入数据
     * @param collectionName 集合名称
     * @param dataList 数据列表
     * @return 插入是否成功
     */
    boolean batchInsertData(String collectionName, List<Map<String, Object>> dataList);

    /**
     * 删除数据
     * @param collectionName 集合名称
     * @param expr 删除条件
     * @return 删除是否成功
     */
    boolean deleteData(String collectionName, String expr);

    /**
     * 更新数据
     * @param collectionName 集合名称
     * @param expr 更新条件
     * @param data 更新数据
     * @return 更新是否成功
     */
    boolean updateData(String collectionName, String expr, Map<String, Object> data);

    /**
     * 搜索数据
     * @param collectionName 集合名称
     * @param vector 搜索向量
     * @param topK 返回数量
     * @param metricType 距离度量类型
     * @param expr 过滤条件
     * @param outputFields 输出字段
     * @return 搜索结果
     */
    List<Map<String, Object>> searchData(String collectionName, float[] vector, int topK, String metricType, String expr, List<String> outputFields);

    /**
     * 查询数据
     * @param collectionName 集合名称
     * @param expr 查询条件
     * @param outputFields 输出字段
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 查询结果
     */
    List<Map<String, Object>> queryData(String collectionName, String expr, List<String> outputFields, int limit, int offset);

    /**
     * 获取集合统计信息
     * @param collectionName 集合名称
     * @return 统计信息
     */
    Map<String, Object> getCollectionStats(String collectionName);

    /**
     * 获取集合列表
     * @return 集合列表
     */
    List<String> getCollections();

    /**
     * 执行向量数据库特定操作
     * @param operation 操作名称
     * @param params 操作参数
     * @return 操作结果
     */
    Object executeOperation(String operation, Map<String, Object> params);

    /**
     * 为租户创建隔离空间
     * @param tenantId 租户ID
     * @return 创建是否成功
     */
    boolean createTenantSpace(String tenantId);

    /**
     * 为租户删除隔离空间
     * @param tenantId 租户ID
     * @return 删除是否成功
     */
    boolean dropTenantSpace(String tenantId);

    /**
     * 为租户获取隔离的集合名称
     * @param tenantId 租户ID
     * @param collectionName 原始集合名称
     * @return 租户隔离的集合名称
     */
    String getTenantCollectionName(String tenantId, String collectionName);

}
