package com.data.rsync.common.strategy;

import java.util.Map;

/**
 * 向量化策略接口
 */
public interface VectorizationStrategy {

    /**
     * 向量化数据
     * @param data 原始数据
     * @return 向量化结果（向量数组）
     * @throws Exception 异常
     */
    float[] vectorize(Map<String, Object> data) throws Exception;

    /**
     * 获取向量化策略名称
     * @return 策略名称
     */
    String getStrategyName();

    /**
     * 获取向量维度
     * @return 向量维度
     */
    int getVectorDimension();

    /**
     * 检查数据是否支持向量化
     * @param data 原始数据
     * @return 是否支持
     */
    boolean supports(Map<String, Object> data);
}
