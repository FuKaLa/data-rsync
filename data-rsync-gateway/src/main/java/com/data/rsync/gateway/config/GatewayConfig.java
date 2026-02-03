package com.data.rsync.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关配置类
 * 配置限流、过滤器等核心功能
 */
@Configuration
public class GatewayConfig {

    /**
     * 基于路径的限流键解析器
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getURI().getPath());
    }

    /**
     * 基于IP的限流键解析器
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For")
                != null ? exchange.getRequest().getHeaders().getFirst("X-Forwarded-For")
                : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }

    /**
     * 基于用户的限流键解析器
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

    /**
     * Redis限流配置
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // 每秒允许的请求数，令牌桶的容量，每个请求消耗的令牌数
        return new RedisRateLimiter(10, 20, 1);
    }

    /**
     * 自定义限流键解析器
     * 可以根据业务需求自定义限流策略
     */
    @Bean
    public KeyResolver customKeyResolver() {
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                // 可以根据业务需求自定义限流键
                return Mono.just(exchange.getRequest().getMethod() + ":" + exchange.getRequest().getURI().getPath());
            }
        };
    }
}
