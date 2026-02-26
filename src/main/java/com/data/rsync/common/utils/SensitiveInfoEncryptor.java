package com.data.rsync.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 敏感信息加密工具
 * 用于加密和解密敏感信息
 */
@Component
public class SensitiveInfoEncryptor {

    private static final Logger log = LoggerFactory.getLogger(SensitiveInfoEncryptor.class);

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String SECRET_KEY = "DataRsyncSystem2"; // 16 bytes for AES-128

    private SecretKeySpec secretKeySpec;

    public SensitiveInfoEncryptor() {
        try {
            // 初始化密钥
            byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            log.info("SensitiveInfoEncryptor initialized");
        } catch (Exception e) {
            log.error("Failed to initialize SensitiveInfoEncryptor: {}", e.getMessage(), e);
        }
    }

    /**
     * 加密敏感信息
     * @param plaintext 明文
     * @return 密文
     */
    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Failed to encrypt sensitive info: {}", e.getMessage(), e);
            return plaintext;
        }
    }

    /**
     * 解密敏感信息
     * @param ciphertext 密文
     * @return 明文
     */
    public String decrypt(String ciphertext) {
        try {
            // Check if ciphertext is null or empty
            if (ciphertext == null || ciphertext.isEmpty()) {
                return ciphertext;
            }
            
            // Check if the input is Base64 encoded
            if (!isBase64Encoded(ciphertext)) {
                // If not Base64, it's probably plain text
                return ciphertext;
            }
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt sensitive info: {}", e.getMessage(), e);
            // If decryption fails, return the original text
            return ciphertext;
        }
    }
    
    /**
     * 检查字符串是否为Base64编码
     * @param input 输入字符串
     * @return 是否为Base64编码
     */
    private boolean isBase64Encoded(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 脱敏敏感信息
     * @param sensitiveInfo 敏感信息
     * @param type 敏感信息类型
     * @return 脱敏后的信息
     */
    public String maskSensitiveInfo(String sensitiveInfo, String type) {
        if (sensitiveInfo == null || sensitiveInfo.isEmpty()) {
            return sensitiveInfo;
        }

        switch (type.toLowerCase()) {
            case "phone":
                return maskPhone(sensitiveInfo);
            case "idcard":
                return maskIdCard(sensitiveInfo);
            case "email":
                return maskEmail(sensitiveInfo);
            case "password":
                return maskPassword(sensitiveInfo);
            case "bankcard":
                return maskBankCard(sensitiveInfo);
            default:
                return maskGeneric(sensitiveInfo);
        }
    }

    /**
     * 脱敏手机号
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    private String maskPhone(String phone) {
        if (phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 脱敏身份证号
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    private String maskIdCard(String idCard) {
        if (idCard.length() < 18) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }

    /**
     * 脱敏邮箱
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return email;
        }
        return email.substring(0, 2) + "****" + email.substring(atIndex);
    }

    /**
     * 脱敏密码
     * @param password 密码
     * @return 脱敏后的密码
     */
    private String maskPassword(String password) {
        return "******";
    }

    /**
     * 脱敏银行卡号
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    private String maskBankCard(String bankCard) {
        if (bankCard.length() < 16) {
            return bankCard;
        }
        return bankCard.substring(0, 4) + " **** **** " + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 通用脱敏
     * @param info 信息
     * @return 脱敏后的信息
     */
    private String maskGeneric(String info) {
        if (info.length() <= 4) {
            return "****";
        }
        int maskLength = Math.min(info.length() / 2, 6);
        String mask = "*".repeat(maskLength);
        int startIndex = info.length() / 4;
        int endIndex = startIndex + maskLength;
        if (endIndex >= info.length()) {
            endIndex = info.length() - 1;
        }
        return info.substring(0, startIndex) + mask + info.substring(endIndex);
    }

    /**
     * 生成随机密钥
     * @return 随机密钥
     */
    public String generateRandomKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(128, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("Failed to generate random key: {}", e.getMessage(), e);
            return SECRET_KEY;
        }
    }

    /**
     * 检查是否为敏感信息
     * @param info 信息
     * @return 是否为敏感信息
     */
    public boolean isSensitiveInfo(String info) {
        if (info == null) {
            return false;
        }
        // 简单的敏感信息检测
        return info.contains("password") || info.contains("token") || info.contains("secret") || info.contains("key");
    }
}
