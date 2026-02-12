package com.data.rsync.common.strategy;

import java.util.HashMap;
import java.util.Map;

/**
 * 向量化策略工厂
 */
public class VectorizationStrategyFactory {

    private static final Map<String, VectorizationStrategy> strategyMap = new HashMap<>();

    static {
        // 初始化默认向量化策略
        // 这里可以添加默认的向量化策略实现
    }

    /**
     * 获取向量化策略
     * @param strategyName 策略名称
     * @return 向量化策略
     * @throws IllegalArgumentException 当策略名称不支持时抛出
     */
    public static VectorizationStrategy getStrategy(String strategyName) {
        VectorizationStrategy strategy = strategyMap.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported vectorization strategy: " + strategyName);
        }
        return strategy;
    }

    /**
     * 注册新的向量化策略
     * @param strategyName 策略名称
     * @param strategy 向量化策略
     */
    public static void registerStrategy(String strategyName, VectorizationStrategy strategy) {
        strategyMap.put(strategyName, strategy);
    }

    /**
     * 移除向量化策略
     * @param strategyName 策略名称
     */
    public static void removeStrategy(String strategyName) {
        strategyMap.remove(strategyName);
    }

    /**
     * 获取默认向量化策略
     * @return 默认向量化策略
     */
    public static VectorizationStrategy getDefaultStrategy() {
        // 如果没有注册策略，返回一个默认的实现
        if (strategyMap.isEmpty()) {
            return new DefaultVectorizationStrategy();
        }
        // 返回第一个注册的策略
        return strategyMap.values().iterator().next();
    }

    /**
     * 默认向量化策略实现
     */
    private static class DefaultVectorizationStrategy implements VectorizationStrategy {

        @Override
        public float[] vectorize(Map<String, Object> data) throws Exception {
            // 简单的默认实现，返回一个固定维度的随机向量
            int dimension = 128;
            float[] vector = new float[dimension];
            for (int i = 0; i < dimension; i++) {
                vector[i] = (float) Math.random();
            }
            return vector;
        }

        @Override
        public String getStrategyName() {
            return "default";
        }

        @Override
        public int getVectorDimension() {
            return 128;
        }

        @Override
        public boolean supports(Map<String, Object> data) {
            return true; // 默认支持所有数据
        }
    }
}
