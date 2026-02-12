package com.data.rsync.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 主键生成工具类
 * 确保在多次同步中生成一致的主键
 */
public class IdGeneratorUtils {

    private static final Logger log = LoggerFactory.getLogger(IdGeneratorUtils.class);

    /**
     * 生成基于数据内容的稳定哈希作为主键
     * @param data 数据
     * @return 稳定的主键ID
     */
    public static Long generateStableLongId(Map<String, Object> data) {
        try {
            // 构建数据指纹
            StringBuilder fingerprint = buildDataFingerprint(data);
            
            // 计算哈希值
            long hash = fingerprint.toString().hashCode();
            // 确保哈希值为正数
            return Math.abs(hash);
        } catch (Exception e) {
            log.error("Failed to generate stable long id: {}", e.getMessage(), e);
            // 生成基于时间戳的临时ID
            return System.currentTimeMillis();
        }
    }

    /**
     * 生成基于数据内容的稳定字符串作为记录标识
     * @param data 数据
     * @return 稳定的记录标识
     */
    public static String generateStableStringId(Map<String, Object> data) {
        try {
            // 构建数据指纹
            StringBuilder fingerprint = buildDataFingerprint(data);
            
            // 计算哈希值
            int hash = fingerprint.toString().hashCode();
            // 确保哈希值为正数并转换为十六进制字符串
            return Integer.toHexString(Math.abs(hash));
        } catch (Exception e) {
            log.error("Failed to generate stable string id: {}", e.getMessage(), e);
            // 生成基于时间戳的临时ID
            return "temp_" + System.currentTimeMillis() + "_" + Thread.currentThread().threadId();
        }
    }

    /**
     * 构建数据指纹
     * @param data 数据
     * @return 数据指纹
     */
    private static StringBuilder buildDataFingerprint(Map<String, Object> data) {
        StringBuilder fingerprint = new StringBuilder();
        
        // 按键排序，确保顺序一致
        List<String> sortedKeys = new ArrayList<>(data.keySet());
        Collections.sort(sortedKeys);
        
        for (String key : sortedKeys) {
            // 跳过向量字段和内部使用的字段
            if (!"vector".equals(key) && !"recordId".equals(key) && !"id".equals(key)) {
                Object value = data.get(key);
                if (value != null) {
                    // 处理不同类型的值
                    String valueStr = processValue(value);
                    fingerprint.append(key).append(":").append(valueStr).append("; ");
                }
            }
        }
        
        return fingerprint;
    }

    /**
     * 处理不同类型的值，确保生成一致的字符串表示
     * @param value 值
     * @return 处理后的值字符串
     */
    private static String processValue(Object value) {
        if (value == null) {
            return "null";
        }
        
        // 处理集合类型
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder listStr = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    listStr.append(", ");
                }
                listStr.append(processValue(list.get(i)));
            }
            listStr.append("]");
            return listStr.toString();
        }
        
        // 处理映射类型
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            StringBuilder mapStr = new StringBuilder("{");
            
            // 按键排序
            List<String> sortedKeys = new ArrayList<>(map.keySet());
            Collections.sort(sortedKeys);
            
            for (int i = 0; i < sortedKeys.size(); i++) {
                String key = sortedKeys.get(i);
                if (i > 0) {
                    mapStr.append(", ");
                }
                mapStr.append(key).append(":").append(processValue(map.get(key)));
            }
            mapStr.append("}");
            return mapStr.toString();
        }
        
        // 处理基本类型
        return value.toString();
    }

    /**
     * 合并多个字段生成复合主键
     * @param fields 字段值数组
     * @return 复合主键
     */
    public static Long generateCompositeId(Object... fields) {
        try {
            StringBuilder fingerprint = new StringBuilder();
            for (Object field : fields) {
                if (field != null) {
                    fingerprint.append(field.toString()).append("|");
                } else {
                    fingerprint.append("null|");
                }
            }
            long hash = fingerprint.toString().hashCode();
            return Math.abs(hash);
        } catch (Exception e) {
            log.error("Failed to generate composite id: {}", e.getMessage(), e);
            return System.currentTimeMillis();
        }
    }

    /**
     * 验证主键是否有效
     * @param id 主键
     * @return 是否有效
     */
    public static boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    /**
     * 验证记录标识是否有效
     * @param recordId 记录标识
     * @return 是否有效
     */
    public static boolean isValidRecordId(String recordId) {
        return recordId != null && !recordId.isEmpty() && !recordId.startsWith("temp_");
    }
}
