package com.data.rsync.common.vectorizer.impl;

import com.data.rsync.common.vectorizer.Vectorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 文本特征向量化器
 * 基于文本的特征进行向量化
 */
public class TextFeatureVectorizer implements Vectorizer {

    private static final Logger log = LoggerFactory.getLogger(TextFeatureVectorizer.class);

    /**
     * 向量维度
     */
    private int vectorDimension = 128;

    /**
     * 向量化器名称
     */
    private static final String NAME = "text_feature";

    /**
     * 初始化状态
     */
    private boolean initialized = false;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getVectorDimension() {
        return vectorDimension;
    }

    @Override
    public float[] vectorize(String text) {
        if (!initialized) {
            throw new IllegalStateException("Vectorizer not initialized");
        }

        float[] vector = new float[vectorDimension];

        try {
            // 基于文本长度、字符分布等生成向量
            int textLength = text.length();
            float lengthFactor = Math.min(1.0f, (float) textLength / 1000.0f);

            // 生成向量值
            for (int i = 0; i < vector.length; i++) {
                // 基于文本特征生成更有意义的向量
                if (i < textLength % vector.length) {
                    vector[i] = (float) (text.charAt(i % textLength) / 255.0) * lengthFactor;
                } else {
                    vector[i] = (float) Math.sin(i) * lengthFactor * 0.5f;
                }
            }
        } catch (Exception e) {
            log.error("Failed to vectorize text: {}", e.getMessage(), e);
            return new float[vectorDimension];
        }

        return vector;
    }

    @Override
    public float[] vectorize(Map<String, Object> data) {
        if (!initialized) {
            throw new IllegalStateException("Vectorizer not initialized");
        }

        // 提取文本特征
        StringBuilder textBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                textBuilder.append(key).append(": ").append(value.toString()).append(" ");
            }
        }

        return vectorize(textBuilder.toString().trim());
    }

    @Override
    public void initialize(Map<String, Object> config) {
        log.info("Initializing TextFeatureVectorizer");

        // 从配置中读取向量维度
        if (config != null && config.containsKey("vectorDimension")) {
            try {
                vectorDimension = Integer.parseInt(config.get("vectorDimension").toString());
                log.info("Set vector dimension to: {}", vectorDimension);
            } catch (Exception e) {
                log.warn("Invalid vector dimension in config, using default: {}", vectorDimension);
            }
        }

        initialized = true;
        log.info("TextFeatureVectorizer initialized successfully");
    }

    @Override
    public void close() {
        log.info("Closing TextFeatureVectorizer");
        initialized = false;
    }

    @Override
    public boolean isAvailable() {
        return initialized;
    }

}
