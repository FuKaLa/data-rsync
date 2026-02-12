package com.data.rsync.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigUtils {
    
    @Value("${milvus.host:localhost}")
    private String milvusHost;
    
    @Value("${milvus.port:19530}")
    private int milvusPort;
    
    @Value("${milvus.collection.name:data_rsync_collection}")
    private String milvusCollectionName;
    
    @Value("${milvus.collection.dimension:128}")
    private int milvusCollectionDimension;
    
    @Value("${data-rsync.sync.batch-size:1000}")
    private int syncBatchSize;
    
    @Value("${data-rsync.sync.thread-pool-size:10}")
    private int syncThreadPoolSize;
    
    public String getMilvusHost() {
        return milvusHost;
    }
    
    public int getMilvusPort() {
        return milvusPort;
    }
    
    public String getMilvusCollectionName() {
        return milvusCollectionName;
    }
    
    public int getMilvusCollectionDimension() {
        return milvusCollectionDimension;
    }
    
    public int getSyncBatchSize() {
        return syncBatchSize;
    }
    
    public int getSyncThreadPoolSize() {
        return syncThreadPoolSize;
    }
}
