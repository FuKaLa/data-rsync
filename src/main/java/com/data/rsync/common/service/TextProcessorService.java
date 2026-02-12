package com.data.rsync.common.service;

import java.util.List;
import java.util.Map;

/**
 * 文本处理服务接口
 */
public interface TextProcessorService {

    /**
     * 分词处理
     * @param text 待处理文本
     * @return 分词结果列表
     */
    List<String> segment(String text);

    /**
     * 提取关键词
     * @param text 待处理文本
     * @param topN 提取数量
     * @return 关键词列表
     */
    List<String> extractKeywords(String text, int topN);

    /**
     * 文本摘要
     * @param text 待处理文本
     * @param sentenceCount 摘要句子数量
     * @return 摘要文本
     */
    String summarize(String text, int sentenceCount);

    /**
     * 文本分类
     * @param text 待分类文本
     * @return 分类结果
     */
    Map<String, Object> classify(String text);

    /**
     * 情感分析
     * @param text 待分析文本
     * @return 情感分析结果
     */
    Map<String, Object> sentimentAnalysis(String text);

    /**
     * 提取实体
     * @param text 待处理文本
     * @return 实体列表
     */
    Map<String, List<String>> extractEntities(String text);

    /**
     * 文本相似度计算
     * @param text1 文本1
     * @param text2 文本2
     * @return 相似度分数
     */
    double calculateSimilarity(String text1, String text2);

    /**
     * 处理文件内容
     * @param filePath 文件路径
     * @return 文件内容处理结果
     */
    Map<String, Object> processFile(String filePath);

    /**
     * 批量文本处理
     * @param texts 文本列表
     * @param operations 操作类型列表
     * @return 批量处理结果
     */
    List<Map<String, Object>> batchProcess(List<String> texts, List<String> operations);

}
