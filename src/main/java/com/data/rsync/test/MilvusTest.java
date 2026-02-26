package com.data.rsync.test;

import com.data.rsync.common.vector.db.VectorDatabaseAdapter;
import com.data.rsync.common.vector.db.VectorDatabaseAdapterFactory;

import java.util.HashMap;
import java.util.Map;

public class MilvusTest {

    public static void main(String[] args) {
        // 获取Milvus向量数据库适配器
        VectorDatabaseAdapter adapter = VectorDatabaseAdapterFactory.getAdapter("MILVUS");
        
        // 初始化配置
        Map<String, Object> config = new HashMap<>();
        config.put("host", "192.168.1.95");
        config.put("port", 19530);
        
        // 初始化适配器
        boolean initialized = adapter.initialize(config);
        System.out.println("Adapter initialized: " + initialized);
        
        if (initialized) {
            // 测试创建集合
            String collectionName = "test_collection_" + System.currentTimeMillis();
            int dimension = 128;
            
            System.out.println("Creating collection: " + collectionName);
            boolean created = adapter.createCollection(collectionName, dimension, config);
            System.out.println("Collection created: " + created);
            
            if (created) {
                // 测试集合是否存在
                boolean exists = adapter.hasCollection(collectionName);
                System.out.println("Collection exists: " + exists);
                
                // 测试获取集合列表
                System.out.println("Collections: " + adapter.getCollections());
                
                // 测试删除集合
                boolean dropped = adapter.dropCollection(collectionName);
                System.out.println("Collection dropped: " + dropped);
            }
            
            // 关闭适配器
            adapter.close();
        }
    }
}