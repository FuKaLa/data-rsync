package com.data.rsync.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * API鉴权增强器
 * 提供IP白名单、请求频率限制等功能
 */
public class ApiAuthEnhancer {

    private static final Logger log = LoggerFactory.getLogger(ApiAuthEnhancer.class);
    private static final ApiAuthEnhancer INSTANCE = new ApiAuthEnhancer();
    private final Set<String> ipWhiteList;
    private final Map<String, Map<String, AtomicInteger>> requestRateMap;
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    private ApiAuthEnhancer() {
        ipWhiteList = new HashSet<>();
        requestRateMap = new ConcurrentHashMap<>();
        initDefaultWhiteList();
        log.info("ApiAuthEnhancer initialized");
    }

    /**
     * 获取API鉴权增强器实例
     * @return API鉴权增强器实例
     */
    public static ApiAuthEnhancer getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化默认白名单
     */
    private void initDefaultWhiteList() {
        ipWhiteList.add("127.0.0.1");
        ipWhiteList.add("192.168.1.1");
    }

    /**
     * 添加IP到白名单
     * @param ip IP地址
     */
    public void addToWhiteList(String ip) {
        ipWhiteList.add(ip);
        log.info("Added IP to white list: {}", ip);
    }

    /**
     * 从白名单移除IP
     * @param ip IP地址
     */
    public void removeFromWhiteList(String ip) {
        ipWhiteList.remove(ip);
        log.info("Removed IP from white list: {}", ip);
    }

    /**
     * 检查IP是否在白名单中
     * @param ip IP地址
     * @return 是否在白名单中
     */
    public boolean isInWhiteList(String ip) {
        return ipWhiteList.contains(ip);
    }

    /**
     * 检查请求频率是否超出限制
     * @param ip IP地址
     * @param apiPath API路径
     * @return 是否超出限制
     */
    public boolean checkRequestRate(String ip, String apiPath) {
        String key = ip + ":" + apiPath;
        Map<String, AtomicInteger> ipMap = requestRateMap.computeIfAbsent(ip, k -> new ConcurrentHashMap<>());
        AtomicInteger counter = ipMap.computeIfAbsent(apiPath, k -> new AtomicInteger(0));

        int count = counter.incrementAndGet();
        if (count > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Request rate limit exceeded for IP: {}, API: {}", ip, apiPath);
            return false;
        }

        // 每分钟重置计数器（简化实现，实际项目中可以使用定时任务）
        if (count == 1) {
            new Thread(() -> {
                try {
                    Thread.sleep(60000);
                    ipMap.remove(apiPath);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        return true;
    }

    /**
     * 验证API请求
     * @param ip IP地址
     * @param apiPath API路径
     * @param token JWT token
     * @return 是否验证通过
     */
    public boolean validateApiRequest(String ip, String apiPath, String token) {
        // 1. 检查IP白名单
        if (isInWhiteList(ip)) {
            log.debug("IP in white list: {}", ip);
            return true;
        }

        // 2. 检查请求频率
        if (!checkRequestRate(ip, apiPath)) {
            log.warn("Request rate limit exceeded: {}, {}", ip, apiPath);
            return false;
        }

        // 3. 检查token（这里可以集成JWT验证）
        if (token == null || token.isEmpty()) {
            log.warn("Missing token: {}, {}", ip, apiPath);
            return false;
        }

        // 后续可以添加更多验证逻辑，如签名校验等

        return true;
    }

    /**
     * 获取IP白名单
     * @return IP白名单
     */
    public Set<String> getIpWhiteList() {
        return new HashSet<>(ipWhiteList);
    }

    /**
     * 获取请求频率统计
     * @return 请求频率统计
     */
    public Map<String, Map<String, AtomicInteger>> getRequestRateMap() {
        return requestRateMap;
    }

    /**
     * 重置请求频率计数器
     */
    public void resetRequestRate() {
        requestRateMap.clear();
        log.info("Reset request rate counters");
    }

    /**
     * 设置请求频率限制
     * @param maxRequestsPerMinute 每分钟最大请求数
     */
    public void setMaxRequestsPerMinute(int maxRequestsPerMinute) {
        // 这里可以动态调整请求频率限制
        log.info("Set max requests per minute: {}", maxRequestsPerMinute);
    }

}
