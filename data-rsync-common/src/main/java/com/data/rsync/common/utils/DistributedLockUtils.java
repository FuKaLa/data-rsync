package com.data.rsync.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DistributedLockUtils {

    private static RedisTemplate<String, String> redisTemplate;

    public static void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        DistributedLockUtils.redisTemplate = redisTemplate;
    }

    /**
     * 获取分布式锁
     * @param lockKey 锁键
     * @param lockValue 锁值
     * @param expireTime 过期时间（秒）
     * @return 是否获取成功
     */
    public static boolean acquireLock(String lockKey, String lockValue, long expireTime) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, expireTime, TimeUnit.SECONDS);
            return result != null && result;
        } catch (Exception e) {
            log.error("Failed to acquire distributed lock: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁键
     * @param lockValue 锁值
     * @return 是否释放成功
     */
    public static boolean releaseLock(String lockKey, String lockValue) {
        try {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            RedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
            Long result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
            return result != null && result > 0;
        } catch (Exception e) {
            log.error("Failed to release distributed lock: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁键
     * @param lockValue 锁值
     * @param expireTime 过期时间（秒）
     * @param retryTimes 重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @return 是否获取成功
     */
    public static boolean tryAcquireLock(String lockKey, String lockValue, long expireTime, int retryTimes, long retryInterval) {
        boolean acquired = acquireLock(lockKey, lockValue, expireTime);
        if (acquired) {
            return true;
        }

        for (int i = 0; i < retryTimes; i++) {
            try {
                Thread.sleep(retryInterval);
                acquired = acquireLock(lockKey, lockValue, expireTime);
                if (acquired) {
                    return true;
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while trying to acquire lock: {}", e.getMessage(), e);
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }

    /**
     * 生成锁键
     * @param prefix 前缀
     * @param id ID
     * @return 锁键
     */
    public static String generateLockKey(String prefix, long id) {
        return prefix + ":" + id;
    }

    /**
     * 生成锁值
     * @return 锁值
     */
    public static String generateLockValue() {
        return Thread.currentThread().getName() + ":" + System.currentTimeMillis();
    }
}
