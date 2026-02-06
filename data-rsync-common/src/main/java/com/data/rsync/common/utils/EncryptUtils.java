package com.data.rsync.common.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * 加密工具类
 */
public class EncryptUtils {

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 密码变换
     */
    private static final String CIPHER_TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 密钥 (16字节，符合AES-128要求)
     */
    private static final String SECRET_KEY = "abcdefghigklmnop"; // 16字节

    /**
     * 字符编码
     */
    private static final String CHARSET = "UTF-8";

    /**
     * AES 加密
     * @param plainText 明文
     * @return 密文
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            // 使用明确的字符编码
            byte[] keyBytes = SECRET_KEY.getBytes(CHARSET);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] plainBytes = plainText.getBytes(CHARSET);
            byte[] encryptedBytes = cipher.doFinal(plainBytes);
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            // 打印详细的错误信息
            System.err.println("Encryption failed: " + e.getMessage());
            e.printStackTrace();
            // 为了避免整个系统崩溃，返回原始文本作为降级方案
            return plainText;
        }
    }

    /**
     * AES 解密
     * @param cipherText 密文
     * @return 明文
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        
        try {
            // 使用明确的字符编码
            byte[] keyBytes = SECRET_KEY.getBytes(CHARSET);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] cipherBytes = Base64.getDecoder().decode(cipherText);
            byte[] decryptedBytes = cipher.doFinal(cipherBytes);
            return new String(decryptedBytes, CHARSET);
        } catch (Exception e) {
            // 打印详细的错误信息
            System.err.println("Decryption failed: " + e.getMessage());
            e.printStackTrace();
            // 为了避免整个系统崩溃，返回原始文本作为降级方案
            return cipherText;
        }
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
