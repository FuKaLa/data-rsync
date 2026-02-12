package com.data.rsync.common.vector.db;

import com.data.rsync.common.vector.db.impl.MilvusVectorDatabaseAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 向量数据库适配器工厂
 * 管理不同类型的向量数据库适配器
 */
public class VectorDatabaseAdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(VectorDatabaseAdapterFactory.class);

    private static final Map<String, VectorDatabaseAdapter> adapterMap = new HashMap<>();

    static {
        // 初始化默认适配器
        initializeDefaultAdapters();
    }

    /**
     * 初始化默认适配器
     */
    private static void initializeDefaultAdapters() {
        try {
            // 注册Milvus适配器
            MilvusVectorDatabaseAdapter milvusAdapter = new MilvusVectorDatabaseAdapter();
            registerAdapter(milvusAdapter);
            log.info("Registered Milvus vector database adapter");
        } catch (Exception e) {
            log.error("Failed to register vector database adapters: {}", e.getMessage(), e);
        }
    }

    /**
     * 注册向量数据库适配器
     * @param adapter 向量数据库适配器
     */
    public static void registerAdapter(VectorDatabaseAdapter adapter) {
        String type = adapter.getVectorDbType();
        adapterMap.put(type, adapter);
        log.info("Registered vector database adapter for type: {}", type);
    }

    /**
     * 获取向量数据库适配器
     * @param vectorDbType 向量数据库类型
     * @return 向量数据库适配器
     */
    public static VectorDatabaseAdapter getAdapter(String vectorDbType) {
        VectorDatabaseAdapter adapter = adapterMap.get(vectorDbType);
        if (adapter == null) {
            // 尝试大小写不敏感匹配
            for (Map.Entry<String, VectorDatabaseAdapter> entry : adapterMap.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(vectorDbType)) {
                    adapter = entry.getValue();
                    break;
                }
            }
        }
        return adapter;
    }

    /**
     * 获取默认向量数据库适配器
     * @return 默认向量数据库适配器
     */
    public static VectorDatabaseAdapter getDefaultAdapter() {
        // 优先使用MILVUS适配器
        VectorDatabaseAdapter adapter = adapterMap.get("MILVUS");
        if (adapter == null) {
            // 如果没有MILVUS适配器，创建一个新的
            adapter = new MilvusVectorDatabaseAdapter();
            registerAdapter(adapter);
        }
        return adapter;
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
            log.warn("Vector database adapter not found for type: {}, using default adapter", vectorDbType);
            adapter = getDefaultAdapter();
        }
        boolean initialized = adapter.initialize(config);
        if (initialized) {
            log.info("Vector database adapter initialized successfully for type: {}", vectorDbType);
        } else {
            log.error("Failed to initialize vector database adapter for type: {}", vectorDbType);
        }
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

