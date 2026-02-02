package com.data.rsync.common.vector.db;

import com.data.rsync.common.vector.db.impl.MilvusVectorDatabaseAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * 向量数据库适配器工厂
 * 管理不同类型的向量数据库适配器
 */
public class VectorDatabaseAdapterFactory {

    private static final Map<String, VectorDatabaseAdapter> adapterMap = new HashMap<>();

    static {
        // 初始化默认适配器
        registerAdapter(new MilvusVectorDatabaseAdapter());
    }

    /**
     * 注册向量数据库适配器
     * @param adapter 向量数据库适配器
     */
    public static void registerAdapter(VectorDatabaseAdapter adapter) {
        adapterMap.put(adapter.getVectorDbType(), adapter);
    }

    /**
     * 获取向量数据库适配器
     * @param vectorDbType 向量数据库类型
     * @return 向量数据库适配器
     */
    public static VectorDatabaseAdapter getAdapter(String vectorDbType) {
        return adapterMap.get(vectorDbType);
    }

    /**
     * 获取默认向量数据库适配器
     * @return 默认向量数据库适配器
     */
    public static VectorDatabaseAdapter getDefaultAdapter() {
        return adapterMap.getOrDefault("milvus", new MilvusVectorDatabaseAdapter());
    }

    /**
     * 初始化向量数据库适配器
     * @param vectorDbType 向量数据库类型
     * @param config 配置信息
     * @return 初始化后的适配器
     */
    public static VectorDatabaseAdapter initializeAdapter(String vectorDbType, Map<String, Object> config) {
        VectorDatabaseAdapter adapter = getAdapter(vectorDbType);
        if (adapter == null) {
            adapter = getDefaultAdapter();
        }
        adapter.initialize(config);
        return adapter;
    }

    /**
     * 获取所有支持的向量数据库类型
     * @return 向量数据库类型列表
     */
    public static Map<String, VectorDatabaseAdapter> getSupportedAdapters() {
        return new HashMap<>(adapterMap);
    }

}
