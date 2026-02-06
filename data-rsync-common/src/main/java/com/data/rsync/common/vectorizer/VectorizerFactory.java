package com.data.rsync.common.vectorizer;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量化器工厂
 * 用于管理和加载不同的向量化器
 */
@Slf4j
public class VectorizerFactory {

    /**
     * 向量化器映射
     */
    private static final Map<String, Vectorizer> vectorizerMap = new ConcurrentHashMap<>();

    /**
     * 注册向量化器
     * @param vectorizer 向量化器
     */
    public static void registerVectorizer(Vectorizer vectorizer) {
        if (vectorizer == null) {
            throw new IllegalArgumentException("Vectorizer cannot be null");
        }

        String name = vectorizer.getName();
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Vectorizer name cannot be null or empty");
        }

        vectorizerMap.put(name, vectorizer);
        log.info("Registered vectorizer: {}", name);
    }

    /**
     * 获取向量化器
     * @param name 向量化器名称
     * @return 向量化器
     */
    public static Vectorizer getVectorizer(String name) {
        log.debug("Getting vectorizer: {}", name);
        Vectorizer vectorizer = vectorizerMap.get(name);
        if (vectorizer == null) {
            log.error("Unsupported vectorizer: {}", name);
            throw new UnsupportedOperationException("Unsupported vectorizer: " + name);
        }
        log.debug("Retrieved vectorizer: {}", name);
        return vectorizer;
    }

    /**
     * 检查是否支持该向量化器
     * @param name 向量化器名称
     * @return 是否支持
     */
    public static boolean supports(String name) {
        return vectorizerMap.containsKey(name);
    }

    /**
     * 获取所有支持的向量化器
     * @return 向量化器名称列表
     */
    public static java.util.Set<String> getSupportedVectorizers() {
        return vectorizerMap.keySet();
    }

    /**
     * 初始化向量化器
     * @param name 向量化器名称
     * @param config 配置参数
     * @return 初始化后的向量化器
     */
    public static Vectorizer initializeVectorizer(String name, Map<String, Object> config) {
        Vectorizer vectorizer = getVectorizer(name);
        vectorizer.initialize(config);
        return vectorizer;
    }

    /**
     * 关闭所有向量化器
     */
    public static void closeAllVectorizers() {
        for (Vectorizer vectorizer : vectorizerMap.values()) {
            try {
                vectorizer.close();
            } catch (Exception e) {
                log.error("Failed to close vectorizer: {}", e.getMessage(), e);
            }
        }
        vectorizerMap.clear();
        log.info("Closed all vectorizers");
    }

}
