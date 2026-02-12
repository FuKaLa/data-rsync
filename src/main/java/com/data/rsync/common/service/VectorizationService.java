package com.data.rsync.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 向量转换服务
 * 使用简单的实现来生成向量
 */
@Service
public class VectorizationService {

    private final int chunkSize;
    private final int chunkOverlap;
    private final Random random;

    /**
     * 构造函数，使用构造函数注入
     */
    public VectorizationService(
            @Value("${data-rsync.document.chunk.size:1000}") int chunkSize,
            @Value("${data-rsync.document.chunk.overlap:200}") int chunkOverlap) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.random = new Random();
    }

    /**
     * 将文本转换为向量
     * @param text 输入文本
     * @return 向量表示
     */
    public float[] vectorize(String text) {
        // 简单实现：基于文本内容生成随机向量
        // 在实际应用中，这里应该使用真实的嵌入模型
        float[] vector = new float[1024]; // 使用1024维度
        
        // 基于文本的哈希值生成确定性的随机向量
        long seed = text.hashCode();
        random.setSeed(seed);
        
        for (int i = 0; i < vector.length; i++) {
            vector[i] = random.nextFloat() * 2 - 1; // 生成[-1, 1]之间的随机数
        }
        
        return vector;
    }

    /**
     * 批量将文本转换为向量
     * @param texts 输入文本列表
     * @return 向量表示列表
     */
    public List<float[]> vectorizeBatch(List<String> texts) {
        List<float[]> vectors = new ArrayList<>();
        for (String text : texts) {
            vectors.add(vectorize(text));
        }
        return vectors;
    }

    /**
     * 对文本进行分块
     * @param text 输入文本
     * @return 分块后的文本列表
     */
    public List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        int textLength = text.length();
        int start = 0;
        
        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);
            chunks.add(text.substring(start, end));
            start += chunkSize - chunkOverlap;
        }
        
        return chunks;
    }
}
