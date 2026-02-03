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
        try {
            VectorizerFactory.registerVectorizer(new TextFeatureVectorizer());
            log.info("Registered default TextFeature vectorizer");
        } catch (Exception e) {
            log.error("Failed to register default TextFeature vectorizer: {}", e.getMessage(), e);
        }

        // 注册其他向量化器
        // BERT 向量化器
        try {
            Class<?> bertVectorizerClass = Class.forName("com.data.rsync.common.vectorizer.impl.BERTVectorizer");
            Vectorizer bertVectorizer = (Vectorizer) bertVectorizerClass.getDeclaredConstructor().newInstance();
            VectorizerFactory.registerVectorizer(bertVectorizer);
            log.info("Registered default BERT vectorizer");
        } catch (ClassNotFoundException e) {
            log.debug("BERT vectorizer not found, skipping registration");
        } catch (Exception e) {
            log.error("Failed to register default BERT vectorizer: {}", e.getMessage(), e);
        }

        // Word2Vec 向量化器
        try {
            Class<?> word2VecVectorizerClass = Class.forName("com.data.rsync.common.vectorizer.impl.Word2VecVectorizer");
            Vectorizer word2VecVectorizer = (Vectorizer) word2VecVectorizerClass.getDeclaredConstructor().newInstance();
            VectorizerFactory.registerVectorizer(word2VecVectorizer);
            log.info("Registered default Word2Vec vectorizer");
        } catch (ClassNotFoundException e) {
            log.debug("Word2Vec vectorizer not found, skipping registration");
        } catch (Exception e) {
            log.error("Failed to register default Word2Vec vectorizer: {}", e.getMessage(), e);
        }

        // FastText 向量化器
        try {
            Class<?> fastTextVectorizerClass = Class.forName("com.data.rsync.common.vectorizer.impl.FastTextVectorizer");
            Vectorizer fastTextVectorizer = (Vectorizer) fastTextVectorizerClass.getDeclaredConstructor().newInstance();
            VectorizerFactory.registerVectorizer(fastTextVectorizer);
            log.info("Registered default FastText vectorizer");
        } catch (ClassNotFoundException e) {
            log.debug("FastText vectorizer not found, skipping registration");
        } catch (Exception e) {
            log.error("Failed to register default FastText vectorizer: {}", e.getMessage(), e);
        }
    }

}
