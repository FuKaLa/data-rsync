package com.data.rsync.common.vectorizer;

import java.util.Map;

/**
 * 向量化器接口
 * 定义了对数据进行向量化的方法
 */
public interface Vectorizer {

    /**
     * 获取向量化器名称
     * @return 向量化器名称
     */
    String getName();

    /**
     * 获取向量维度
     * @return 向量维度
     */
    int getVectorDimension();

    /**
     * 对文本进行向量化
     * @param text 文本
     * @return 向量
     */
    float[] vectorize(String text);

    /**
     * 对数据进行向量化
     * @param data 数据
     * @return 向量
     */
    float[] vectorize(Map<String, Object> data);

    /**
     * 初始化向量化器
     * @param config 配置参数
     */
    void initialize(Map<String, Object> config);

    /**
     * 关闭向量化器
     */
    void close();

    /**
     * 检查向量化器是否可用
     * @return 是否可用
     */
    boolean isAvailable();

}
