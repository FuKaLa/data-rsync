package com.data.rsync.common.service.impl;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文本处理服务实现类
 */
@Service
public class TextProcessorServiceImpl implements com.data.rsync.common.service.TextProcessorService {

    private static final Logger log = LoggerFactory.getLogger(TextProcessorServiceImpl.class);

    private final JiebaSegmenter segmenter = new JiebaSegmenter();
    private final Tika tika = new Tika();

    @Override
    public List<String> segment(String text) {
        try {
            if (text == null || text.isEmpty()) {
                return Collections.emptyList();
            }

            List<SegToken> tokens = segmenter.process(text, JiebaSegmenter.SegMode.SEARCH);
            return tokens.stream()
                    .map(token -> token.word)
                    .filter(word -> word.length() > 1) // 过滤单个字符
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to segment text: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> extractKeywords(String text, int topN) {
        try {
            if (text == null || text.isEmpty()) {
                return Collections.emptyList();
            }

            // 使用分词结果计算词频
            List<String> words = segment(text);
            Map<String, Integer> wordFreq = new HashMap<>();

            for (String word : words) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }

            // 按词频排序并提取topN
            return wordFreq.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(topN)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to extract keywords: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public String summarize(String text, int sentenceCount) {
        try {
            if (text == null || text.isEmpty()) {
                return "";
            }

            // 简单的摘要算法：按句子长度和关键词密度排序
            String[] sentences = text.split("[。！？.!?]");
            List<String> validSentences = Arrays.stream(sentences)
                    .filter(s -> s.trim().length() > 10) // 过滤短句子
                    .collect(Collectors.toList());

            if (validSentences.isEmpty()) {
                return text;
            }

            // 计算每个句子的得分
            Map<String, Double> sentenceScores = new HashMap<>();
            List<String> keywords = extractKeywords(text, 20);

            for (String sentence : validSentences) {
                double score = calculateSentenceScore(sentence, keywords);
                sentenceScores.put(sentence, score);
            }

            // 按得分排序并提取
            List<String> topSentences = sentenceScores.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(sentenceCount)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // 按原始顺序排列
            List<String> orderedSentences = new ArrayList<>();
            for (String sentence : validSentences) {
                if (topSentences.contains(sentence)) {
                    orderedSentences.add(sentence);
                    if (orderedSentences.size() >= sentenceCount) {
                        break;
                    }
                }
            }

            return String.join("。", orderedSentences) + "。";
        } catch (Exception e) {
            log.error("Failed to summarize text: {}", e.getMessage(), e);
            return text;
        }
    }

    @Override
    public Map<String, Object> classify(String text) {
        try {
            if (text == null || text.isEmpty()) {
                return Collections.emptyMap();
            }

            // 简单的文本分类实现
            Map<String, Object> result = new HashMap<>();
            List<String> keywords = extractKeywords(text, 10);

            // 基于关键词的简单分类
            String category = "其他";
            if (keywords.contains("技术") || keywords.contains("代码") || keywords.contains("系统")) {
                category = "技术";
            } else if (keywords.contains("业务") || keywords.contains("公司") || keywords.contains("市场")) {
                category = "商业";
            } else if (keywords.contains("用户") || keywords.contains("产品") || keywords.contains("服务")) {
                category = "用户服务";
            }

            result.put("category", category);
            result.put("confidence", 0.7); // 模拟置信度
            result.put("keywords", keywords);

            return result;
        } catch (Exception e) {
            log.error("Failed to classify text: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, Object> sentimentAnalysis(String text) {
        try {
            if (text == null || text.isEmpty()) {
                return Collections.emptyMap();
            }

            // 简单的情感分析实现
            Map<String, Object> result = new HashMap<>();

            // 情感词词典
            Set<String> positiveWords = new HashSet<>(Arrays.asList(
                    "好", "优秀", "棒", "满意", "喜欢", "成功", "高兴", "开心", "快乐", "幸福"
            ));

            Set<String> negativeWords = new HashSet<>(Arrays.asList(
                    "坏", "差", "糟糕", "不满意", "讨厌", "失败", "难过", "伤心", "痛苦", "不幸"
            ));

            List<String> words = segment(text);
            int positiveCount = 0;
            int negativeCount = 0;

            for (String word : words) {
                if (positiveWords.contains(word)) {
                    positiveCount++;
                } else if (negativeWords.contains(word)) {
                    negativeCount++;
                }
            }

            double score = (positiveCount - negativeCount) / (double) Math.max(1, words.size());
            String sentiment;

            if (score > 0.1) {
                sentiment = "积极";
            } else if (score < -0.1) {
                sentiment = "消极";
            } else {
                sentiment = "中性";
            }

            result.put("sentiment", sentiment);
            result.put("score", score);
            result.put("positiveCount", positiveCount);
            result.put("negativeCount", negativeCount);

            return result;
        } catch (Exception e) {
            log.error("Failed to analyze sentiment: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, List<String>> extractEntities(String text) {
        try {
            if (text == null || text.isEmpty()) {
                return Collections.emptyMap();
            }

            // 简单的实体提取实现
            Map<String, List<String>> entities = new HashMap<>();
            entities.put("person", new ArrayList<>());
            entities.put("organization", new ArrayList<>());
            entities.put("location", new ArrayList<>());

            // 这里应该使用更复杂的命名实体识别算法
            // 暂时返回空结果
            return entities;
        } catch (Exception e) {
            log.error("Failed to extract entities: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public double calculateSimilarity(String text1, String text2) {
        try {
            if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
                return 0.0;
            }

            // 使用余弦相似度计算
            List<String> words1 = segment(text1);
            List<String> words2 = segment(text2);

            // 构建词频向量
            Map<String, Integer> freq1 = new HashMap<>();
            Map<String, Integer> freq2 = new HashMap<>();

            for (String word : words1) {
                freq1.put(word, freq1.getOrDefault(word, 0) + 1);
            }

            for (String word : words2) {
                freq2.put(word, freq2.getOrDefault(word, 0) + 1);
            }

            // 计算交集和并集
            Set<String> allWords = new HashSet<>();
            allWords.addAll(freq1.keySet());
            allWords.addAll(freq2.keySet());

            // 计算余弦相似度
            double dotProduct = 0;
            double norm1 = 0;
            double norm2 = 0;

            for (String word : allWords) {
                int f1 = freq1.getOrDefault(word, 0);
                int f2 = freq2.getOrDefault(word, 0);
                dotProduct += f1 * f2;
                norm1 += f1 * f1;
                norm2 += f2 * f2;
            }

            if (norm1 == 0 || norm2 == 0) {
                return 0.0;
            }

            return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        } catch (Exception e) {
            log.error("Failed to calculate similarity: {}", e.getMessage(), e);
            return 0.0;
        }
    }

    @Override
    public Map<String, Object> processFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                log.error("File not found: {}", filePath);
                return Collections.emptyMap();
            }

            Map<String, Object> result = new HashMap<>();

            // 使用Tika提取文件内容
            Metadata metadata = new Metadata();
            BodyContentHandler handler = new BodyContentHandler(-1); // -1表示无限制
            AutoDetectParser parser = new AutoDetectParser();
            ParseContext context = new ParseContext();

            try (InputStream stream = new FileInputStream(file)) {
                parser.parse(stream, handler, metadata, context);
            }

            String content = handler.toString();
            result.put("content", content);
            result.put("metadata", extractMetadata(metadata));
            result.put("keywords", extractKeywords(content, 10));
            result.put("summary", summarize(content, 3));

            return result;
        } catch (IOException | TikaException | SAXException e) {
            log.error("Failed to process file: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public List<Map<String, Object>> batchProcess(List<String> texts, List<String> operations) {
        try {
            List<Map<String, Object>> results = new ArrayList<>();

            for (String text : texts) {
                Map<String, Object> result = new HashMap<>();
                result.put("text", text);

                for (String operation : operations) {
                    switch (operation) {
                        case "segment":
                            result.put("segment", segment(text));
                            break;
                        case "keywords":
                            result.put("keywords", extractKeywords(text, 10));
                            break;
                        case "summarize":
                            result.put("summary", summarize(text, 3));
                            break;
                        case "classify":
                            result.put("classify", classify(text));
                            break;
                        case "sentiment":
                            result.put("sentiment", sentimentAnalysis(text));
                            break;
                        default:
                            break;
                    }
                }

                results.add(result);
            }

            return results;
        } catch (Exception e) {
            log.error("Failed to batch process texts: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // 辅助方法
    private double calculateSentenceScore(String sentence, List<String> keywords) {
        if (sentence == null || sentence.isEmpty()) {
            return 0;
        }

        double lengthScore = Math.min(sentence.length() / 50.0, 1.0); // 句子长度得分
        double keywordScore = 0;

        // 计算关键词匹配得分
        for (String keyword : keywords) {
            if (sentence.contains(keyword)) {
                keywordScore++;
            }
        }
        keywordScore = Math.min(keywordScore / 5.0, 1.0); // 关键词得分

        return lengthScore * 0.4 + keywordScore * 0.6; // 加权得分
    }

    private Map<String, String> extractMetadata(Metadata metadata) {
        Map<String, String> result = new HashMap<>();
        for (String name : metadata.names()) {
            result.put(name, metadata.get(name));
        }
        return result;
    }

}
