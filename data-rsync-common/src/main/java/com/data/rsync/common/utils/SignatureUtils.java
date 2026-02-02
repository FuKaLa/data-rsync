package com.data.rsync.common.utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * API签名验证工具类
 */
public class SignatureUtils {

    /**
     * 生成API签名
     * @param params 请求参数
     * @param secretKey 密钥
     * @return 签名
     */
    public static String generateSignature(Map<String, Object> params, String secretKey) {
        // 1. 过滤空值参数
        Map<String, Object> filteredParams = params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().toString().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        // 2. 按参数名排序
        List<String> sortedKeys = new ArrayList<>(filteredParams.keySet());
        Collections.sort(sortedKeys);
        
        // 3. 构建签名字符串
        StringBuilder sb = new StringBuilder();
        for (String key : sortedKeys) {
            sb.append(key).append("=")
              .append(filteredParams.get(key).toString())
              .append("&");
        }
        
        // 4. 添加密钥
        sb.append("secretKey=").append(secretKey);
        
        // 5. 计算MD5值
        return md5(sb.toString());
    }

    /**
     * 验证API签名
     * @param params 请求参数
     * @param signature 签名
     * @param secretKey 密钥
     * @return 是否验证通过
     */
    public static boolean verifySignature(Map<String, Object> params, String signature, String secretKey) {
        // 1. 移除签名参数
        Map<String, Object> paramsWithoutSignature = new HashMap<>(params);
        paramsWithoutSignature.remove("signature");
        
        // 2. 生成签名
        String generatedSignature = generateSignature(paramsWithoutSignature, secretKey);
        
        // 3. 验证签名
        return generatedSignature.equals(signature);
    }

    /**
     * 计算MD5值
     * @param str 字符串
     * @return MD5值
     */
    private static String md5(String str) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            LogUtils.error("Failed to calculate MD5: {}", e, str);
            return null;
        }
    }

    /**
     * 生成时间戳
     * @return 时间戳
     */
    public static long generateTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 验证时间戳是否有效
     * @param timestamp 时间戳
     * @param timeout 超时时间（秒）
     * @return 是否有效
     */
    public static boolean verifyTimestamp(long timestamp, long timeout) {
        long currentTimestamp = generateTimestamp();
        return Math.abs(currentTimestamp - timestamp) <= timeout;
    }
}
