package com.data.rsync.common.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 缓存工具类
 */
@Component
public class CacheUtils {

    private static RedisTemplate<String, Object> redisTemplate;

    public static void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        CacheUtils.redisTemplate = redisTemplate;
    }

    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     * @param expireTime 过期时间（秒）
     */
    public static void set(String key, Object value, long expireTime) {
        try {
            redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            LogUtils.error("Failed to set cache: {}", e, key);
        }
    }

    /**
     * 获取缓存
     * @param key 缓存键
     * @param <T> 缓存值类型
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            LogUtils.error("Failed to get cache: {}", e, key);
            return null;
        }
    }

    /**
     * 删除缓存
     * @param key 缓存键
     */
    public static void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            LogUtils.error("Failed to delete cache: {}", e, key);
        }
    }

    /**
     * 检查缓存是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    public static boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            LogUtils.error("Failed to check cache existence: {}", e, key);
            return false;
        }
    }

    /**
     * 设置缓存过期时间
     * @param key 缓存键
     * @param expireTime 过期时间（秒）
     */
    public static void expire(String key, long expireTime) {
        try {
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            LogUtils.error("Failed to set cache expire time: {}", e, key);
        }
    }

    /**
     * 生成缓存键
     * @param prefix 前缀
     * @param id ID
     * @return 缓存键
     */
    public static String generateCacheKey(String prefix, long id) {
        return prefix + ":" + id;
    }

    /**
     * 生成缓存键
     * @param prefix 前缀
     * @param params 参数
     * @return 缓存键
     */
    public static String generateCacheKey(String prefix, Object... params) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object param : params) {
            sb.append(":").append(param);
        }
        return sb.toString();
    }
}
