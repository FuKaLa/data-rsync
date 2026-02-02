package com.data.rsync.common.vectorizer;

import com.data.rsync.common.vectorizer.impl.TextFeatureVectorizer;
import lombok.extern.slf4j.Slf4j;

/**
 * 向量化器初始化器
 * 用于在系统启动时注册所有的向量化器
 */
@Slf4j
public class VectorizerInitializer {

    /**
     * 初始化向量化器
     */
    public static void initialize() {
        log.info("Initializing vectorizers");

        // 注册默认的向量化器
        registerDefaultVectorizers();

        log.info("Vectorizers initialized successfully");
        log.info("Supported vectorizers: {}", VectorizerFactory.getSupportedVectorizers());
    }

    /**
     * 注册默认的向量化器
     */
    private static void registerDefaultVectorizers() {
        // 注册文本特征向量化器
        VectorizerFactory.registerVectorizer(new TextFeatureVectorizer());

        // TODO: 注册其他向量化器
        // VectorizerFactory.registerVectorizer(new BERTVectorizer());
        // VectorizerFactory.registerVectorizer(new Word2VecVectorizer());
        // VectorizerFactory.registerVectorizer(new FastTextVectorizer());
    }

}
