package com.data.rsync.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis缓存管理器
 * 实现缓存失效策略和预热机制
 */
@Component
public class RedisCacheManager implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheManager.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存统计信息
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cachePuts = new AtomicLong(0);

    // 缓存过期时间配置（毫秒）
    private final Map<String, Long> expirationMap = new ConcurrentHashMap<>();

    // 缓存预热任务
    private final ExecutorService warmupExecutor = Executors.newSingleThreadExecutor();

    // 缓存清理任务
    private final ScheduledExecutorService cleanupExecutor = Executors.newScheduledThreadPool(1);

    /**
     * 初始化方法
     */
    @Override
    public void afterPropertiesSet() {
        initDefaultExpiration();
        initCacheCleanupTask();
        initCacheWarmup();
        logger.info("RedisCacheManager initialized successfully");
    }

    /**
     * 初始化默认过期时间配置
     */
    private void initDefaultExpiration() {
        // 短期缓存：10分钟
        expirationMap.put("short", 10 * 60 * 1000L);
        // 中期缓存：1小时
        expirationMap.put("medium", 60 * 60 * 1000L);
        // 长期缓存：24小时
        expirationMap.put("long", 24 * 60 * 60 * 1000L);
        // 永不过期
        expirationMap.put("forever", 0L);

        // 业务缓存过期时间
        expirationMap.put("user", 30 * 60 * 1000L); // 用户信息：30分钟
        expirationMap.put("task", 15 * 60 * 1000L); // 任务信息：15分钟
        expirationMap.put("datasource", 20 * 60 * 1000L); // 数据源信息：20分钟
        expirationMap.put("milvus", 10 * 60 * 1000L); // Milvus信息：10分钟
    }

    /**
     * 初始化缓存清理任务
     */
    private void initCacheCleanupTask() {
        // 每小时执行一次缓存清理
        cleanupExecutor.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredCache();
                logger.info("Cache cleanup task executed");
            } catch (Exception e) {
                logger.error("Cache cleanup task failed", e);
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    /**
     * 初始化缓存预热
     */
    private void initCacheWarmup() {
        warmupExecutor.submit(() -> {
            try {
                logger.info("Starting cache warmup...");
                warmupCommonCache();
                logger.info("Cache warmup completed");
            } catch (Exception e) {
                logger.error("Cache warmup failed", e);
            }
        });
    }

    /**
     * 预热常用缓存
     */
    private void warmupCommonCache() {
        // 这里可以添加常用数据的预热逻辑
        // 例如：
        // 1. 加载系统配置
        // 2. 加载常用字典数据
        // 3. 加载热点任务信息
        // 4. 加载热点数据源信息

        logger.info("Warming up common cache data");
        
        // 示例：预热系统配置
        Map<String, Object> systemConfig = new HashMap<>();
        systemConfig.put("version", "1.0.0");
        systemConfig.put("env", "development");
        systemConfig.put("maxSyncBatchSize", 1000);
        systemConfig.put("maxThreads", 10);
        set("system:config", systemConfig, "medium");

        // 示例：预热缓存统计信息
        set("cache:stats:init", System.currentTimeMillis(), "short");
    }

    /**
     * 清理过期缓存
     */
    private void cleanupExpiredCache() {
        // 这里可以添加缓存清理逻辑
        // 例如：
        // 1. 清理长期未访问的缓存
        // 2. 清理超过大小限制的缓存
        // 3. 清理特定前缀的缓存

        logger.debug("Cleaning up expired cache");
    }

    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     * @param expirationType 过期类型
     */
    public void set(String key, Object value, String expirationType) {
        try {
            Long expiration = expirationMap.getOrDefault(expirationType, expirationMap.get("medium"));
            if (expiration > 0) {
                redisTemplate.opsForValue().set(key, value, expiration, TimeUnit.MILLISECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            cachePuts.incrementAndGet();
            logger.debug("Set cache: {} with expiration type: {}", key, expirationType);
        } catch (Exception e) {
            logger.error("Failed to set cache: {}", key, e);
        }
    }

    /**
     * 设置缓存（自定义过期时间）
     * @param key 缓存键
     * @param value 缓存值
     * @param expiration 过期时间（毫秒）
     */
    public void set(String key, Object value, long expiration) {
        try {
            if (expiration > 0) {
                redisTemplate.opsForValue().set(key, value, expiration, TimeUnit.MILLISECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            cachePuts.incrementAndGet();
            logger.debug("Set cache: {} with expiration: {}ms", key, expiration);
        } catch (Exception e) {
            logger.error("Failed to set cache: {}", key, e);
        }
    }

    /**
     * 获取缓存
     * @param key 缓存键
     * @return 缓存值
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                cacheHits.incrementAndGet();
                logger.debug("Cache hit for key: {}", key);
            } else {
                cacheMisses.incrementAndGet();
                logger.debug("Cache miss for key: {}", key);
            }
            return value;
        } catch (Exception e) {
            logger.error("Failed to get cache: {}", key, e);
            cacheMisses.incrementAndGet();
            return null;
        }
    }

    /**
     * 删除缓存
     * @param key 缓存键
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            logger.debug("Deleted cache: {}", key);
        } catch (Exception e) {
            logger.error("Failed to delete cache: {}", key, e);
        }
    }

    /**
     * 批量删除缓存
     * @param keys 缓存键集合
     */
    public void delete(Set<String> keys) {
        try {
            redisTemplate.delete(keys);
            logger.debug("Deleted {} cache keys", keys.size());
        } catch (Exception e) {
            logger.error("Failed to delete cache keys", e);
        }
    }

    /**
     * 按前缀删除缓存
     * @param prefix 缓存键前缀
     */
    public void deleteByPrefix(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.debug("Deleted {} cache keys with prefix: {}", keys.size(), prefix);
            }
        } catch (Exception e) {
            logger.error("Failed to delete cache keys by prefix: {}", prefix, e);
        }
    }

    /**
     * 检查缓存是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            logger.error("Failed to check cache existence: {}", key, e);
            return false;
        }
    }

    /**
     * 设置缓存过期时间
     * @param key 缓存键
     * @param expiration 过期时间（毫秒）
     */
    public void expire(String key, long expiration) {
        try {
            redisTemplate.expire(key, expiration, TimeUnit.MILLISECONDS);
            logger.debug("Set expiration for cache: {} to {}ms", key, expiration);
        } catch (Exception e) {
            logger.error("Failed to set expiration for cache: {}", key, e);
        }
    }

    /**
     * 获取缓存剩余过期时间
     * @param key 缓存键
     * @return 剩余过期时间（毫秒），-1表示永不过期，-2表示不存在
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            logger.error("Failed to get expire time for cache: {}", key, e);
            return -2;
        }
    }

    /**
     * 获取缓存统计信息
     * @return 统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("hits", cacheHits.get());
        stats.put("misses", cacheMisses.get());
        stats.put("puts", cachePuts.get());
        stats.put("hitRate", calculateHitRate());
        stats.put("totalOperations", cacheHits.get() + cacheMisses.get());
        return stats;
    }

    /**
     * 计算缓存命中率
     * @return 命中率
     */
    private double calculateHitRate() {
        long total = cacheHits.get() + cacheMisses.get();
        return total > 0 ? (double) cacheHits.get() / total : 0.0;
    }

    /**
     * 重置缓存统计信息
     */
    public void resetCacheStats() {
        cacheHits.set(0);
        cacheMisses.set(0);
        cachePuts.set(0);
        logger.info("Cache stats reset");
    }

    /**
     * 关闭资源
     */
    public void shutdown() {
        warmupExecutor.shutdown();
        cleanupExecutor.shutdown();
        logger.info("RedisCacheManager shutdown");
    }
}
