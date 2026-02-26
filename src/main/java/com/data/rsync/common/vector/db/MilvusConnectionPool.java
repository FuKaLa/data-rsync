package com.data.rsync.common.vector.db;

import com.data.rsync.common.exception.MilvusException;
import com.data.rsync.common.utils.LogUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Milvus连接池，用于管理Milvus客户端连接的创建、获取和释放
 */
public class MilvusConnectionPool {

    private final List<VectorDatabaseAdapter> connections;
    private final Queue<VectorDatabaseAdapter> availableConnections;
    private final int maxPoolSize;
    private final String host;
    private final Integer port;
    private final ReentrantLock lock;

    /**
     * 构造函数
     * @param host Milvus主机地址
     * @param port Milvus端口
     * @param maxPoolSize 最大连接数
     */
    public MilvusConnectionPool(String host, Integer port, int maxPoolSize) {
        this.host = host;
        this.port = port;
        this.maxPoolSize = maxPoolSize;
        this.connections = new ArrayList<>(maxPoolSize);
        this.availableConnections = new LinkedList<>();
        this.lock = new ReentrantLock();
        initializePool();
    }

    /**
     * 初始化连接池
     */
    private void initializePool() {
        for (int i = 0; i < maxPoolSize; i++) {
            VectorDatabaseAdapter client = createConnection();
            if (client != null) {
                connections.add(client);
                availableConnections.offer(client);
            }
        }
        LogUtils.info("Milvus连接池初始化完成，连接数: {}", connections.size());
    }

    /**
     * 创建Milvus连接
     * @return Milvus客户端适配器
     */
    private VectorDatabaseAdapter createConnection() {
        try {
            VectorDatabaseAdapter adapter = VectorDatabaseAdapterFactory.getAdapter("MILVUS");
            if (adapter == null) {
                throw new MilvusException("Failed to get Milvus vector database adapter");
            }
            
            // 初始化适配器
            java.util.Map<String, Object> config = new java.util.HashMap<>();
            config.put("host", host);
            config.put("port", port);
            
            boolean initialized = adapter.initialize(config);
            if (!initialized) {
                throw new MilvusException("Failed to initialize Milvus vector database adapter");
            }
            
            LogUtils.debug("创建Milvus连接成功");
            return adapter;
        } catch (Exception e) {
            LogUtils.error("创建Milvus连接失败", e);
            return null;
        }
    }

    /**
     * 获取Milvus连接
     * @return Milvus客户端适配器
     * @throws MilvusException 连接获取失败异常
     */
    public VectorDatabaseAdapter getConnection() throws MilvusException {
        lock.lock();
        try {
            while (availableConnections.isEmpty()) {
                if (connections.size() < maxPoolSize) {
                    // 创建新连接
                    VectorDatabaseAdapter client = createConnection();
                    if (client != null) {
                        connections.add(client);
                        availableConnections.offer(client);
                        LogUtils.info("Milvus连接池创建新连接，当前连接数: {}", connections.size());
                    } else {
                        // 创建失败，等待
                        try {
                            lock.unlock();
                            Thread.sleep(1000);
                            lock.lock();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new MilvusException("获取Milvus连接被中断", e);
                        }
                    }
                } else {
                    // 池已满，等待
                    try {
                        lock.unlock();
                        Thread.sleep(1000);
                        lock.lock();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new MilvusException("获取Milvus连接被中断", e);
                    }
                }
            }
            return availableConnections.poll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 释放Milvus连接
     * @param adapter Milvus客户端适配器
     */
    public void releaseConnection(VectorDatabaseAdapter adapter) {
        if (adapter != null) {
            lock.lock();
            try {
                if (connections.contains(adapter)) {
                    availableConnections.offer(adapter);
                    LogUtils.debug("Milvus连接已释放");
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 关闭连接池
     */
    public void close() {
        lock.lock();
        try {
            for (VectorDatabaseAdapter adapter : connections) {
                try {
                    // 关闭连接
                    adapter.close();
                    LogUtils.debug("Milvus连接已关闭");
                } catch (Exception e) {
                    LogUtils.error("关闭Milvus连接失败", e);
                }
            }
            connections.clear();
            availableConnections.clear();
            LogUtils.info("Milvus连接池已关闭");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前连接数
     * @return 当前连接数
     */
    public int getCurrentConnectionCount() {
        lock.lock();
        try {
            return connections.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取可用连接数
     * @return 可用连接数
     */
    public int getAvailableConnectionCount() {
        lock.lock();
        try {
            return availableConnections.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取最大连接数
     * @return 最大连接数
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
}
