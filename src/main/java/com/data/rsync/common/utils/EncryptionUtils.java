package com.data.rsync.common.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 加密工具类
 */
public class EncryptionUtils {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;

    /**
     * 加密数据
     * @param data 原始数据
     * @param key 加密密钥
     * @return 加密后的数据（Base64编码）
     * @throws Exception 异常
     */
    public static String encrypt(String data, String key) throws Exception {
        // 生成密钥
        SecretKey secretKey = generateKey(key);
        
        // 创建加密器
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        // 加密数据
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        
        // Base64编码
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * 解密数据
     * @param encryptedData 加密后的数据（Base64编码）
     * @param key 解密密钥
     * @return 原始数据
     * @throws Exception 异常
     */
    public static String decrypt(String encryptedData, String key) throws Exception {
        // 生成密钥
        SecretKey secretKey = generateKey(key);
        
        // 创建解密器
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        
        // Base64解码
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        
        // 解密数据
        byte[] decryptedData = cipher.doFinal(encryptedBytes);
        
        return new String(decryptedData);
    }

    /**
     * 生成密钥
     * @param key 原始密钥
     * @return 密钥对象
     * @throws Exception 异常
     */
    private static SecretKey generateKey(String key) throws Exception {
        // 使用SHA-256哈希获取固定长度的密钥
        byte[] keyBytes = key.getBytes();
        byte[] hash = java.security.MessageDigest.getInstance("SHA-256").digest(keyBytes);
        
        // 创建密钥规范
        return new SecretKeySpec(hash, ALGORITHM);
    }

    /**
     * 生成随机密钥
     * @return 随机密钥
     * @throws Exception 异常
     */
    public static String generateRandomKey() throws Exception {
        // 生成随机密钥
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        SecretKey secretKey = keyGenerator.generateKey();
        
        // Base64编码
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * 脱敏处理
     * @param data 原始数据
     * @param type 数据类型：phone, idCard, email, bankCard
     * @return 脱敏后的数据
     */
    public static String mask(String data, String type) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        
        switch (type) {
            case "phone":
                // 手机号脱敏：保留前3后4
                if (data.length() >= 7) {
                    return data.substring(0, 3) + "****" + data.substring(data.length() - 4);
                }
                break;
            case "idCard":
                // 身份证号脱敏：保留前6后4
                if (data.length() >= 10) {
                    return data.substring(0, 6) + "********" + data.substring(data.length() - 4);
                }
                break;
            case "email":
                // 邮箱脱敏：保留用户名前3和域名
                if (data.contains("@")) {
                    String[] parts = data.split("@");
                    if (parts[0].length() >= 3) {
                        return parts[0].substring(0, 3) + "****@" + parts[1];
                    }
                }
                break;
            case "bankCard":
                // 银行卡号脱敏：保留前4后4
                if (data.length() >= 8) {
                    return data.substring(0, 4) + "********" + data.substring(data.length() - 4);
                }
                break;
            default:
                // 默认脱敏：保留前2后2
                if (data.length() >= 4) {
                    return data.substring(0, 2) + "****" + data.substring(data.length() - 2);
                }
                break;
        }
        
        return data;
    }

    /**
     * MD5 加密
     * @param text 文本
     * @return MD5 哈希值
     */
    public static String md5(String text) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(text.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 encryption failed", e);
        }
    }
}
