package com.data.rsync.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.reactive.CorsWebFilter;

/**
 * 网关配置注册类
 * 注册所有网关相关的bean
 */
@Configuration
public class GatewayConfigRegister {

    private final GatewayConfig gatewayConfig = new GatewayConfig();

    /**
     * 基于路径的限流键解析器
     */
    @Bean
    @Primary
    public KeyResolver pathKeyResolver() {
        return gatewayConfig.pathKeyResolver();
    }

    /**
     * 基于IP的限流键解析器
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return gatewayConfig.ipKeyResolver();
    }

    /**
     * 基于用户的限流键解析器
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return gatewayConfig.userKeyResolver();
    }

    /**
     * Redis限流配置
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return gatewayConfig.redisRateLimiter();
    }

    /**
     * 自定义限流键解析器
     */
    @Bean
    public KeyResolver customKeyResolver() {
        return gatewayConfig.customKeyResolver();
    }

    /**
     * 路由配置
     * 使用代码方式配置路由，与配置文件方式互补
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return gatewayConfig.customRouteLocator(builder);
    }
}
