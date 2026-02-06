package com.data.rsync.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关配置类
 * 配置限流、过滤器等核心功能
 */
public class GatewayConfig {

    /**
     * 基于路径的限流键解析器
     */
    public KeyResolver pathKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getURI().getPath());
    }

    /**
     * 基于IP的限流键解析器
     */
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For")
                != null ? exchange.getRequest().getHeaders().getFirst("X-Forwarded-For")
                : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }

    /**
     * 基于用户的限流键解析器
     */
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }

    /**
     * Redis限流配置
     */
    public RedisRateLimiter redisRateLimiter() {
        // 每秒允许的请求数，令牌桶的容量，每个请求消耗的令牌数
        return new RedisRateLimiter(10, 20, 1);
    }

    /**
     * 自定义限流键解析器
     * 可以根据业务需求自定义限流策略
     */
    public KeyResolver customKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getMethod() + ":" + exchange.getRequest().getURI().getPath());
    }

    /**
     * 路由配置
     * 使用代码方式配置路由，与配置文件方式互补
     */
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 认证服务路由 - /gateway/api/auth/** → /auth/**
                .route("auth-service-route", r -> r.path("/gateway/api/auth/**")
                        .filters(f -> f.stripPrefix(3)
                                .prefixPath("/auth")
                                .retry(3))
                        .uri("lb://data-rsync-auth"))
                // 数据源管理路由 - /gateway/api/data-source/** → /source/api/data-source/**
                .route("data-source-route", r -> r.path("/gateway/api/data-source/**")
                        .filters(f -> f.stripPrefix(1)
                                .prefixPath("/source")
                                .retry(3))
                        .uri("lb://data-rsync-data-source"))
                // 监控服务路由 - /gateway/api/monitor/** → /monitor/api/monitor/**
                .route("monitor-route", r -> r.path("/gateway/api/monitor/**")
                        .filters(f -> f.stripPrefix(1)
                                .prefixPath("/monitor")
                                .retry(3))
                        .uri("lb://data-rsync-monitor"))
                // 任务管理路由 - /gateway/api/tasks/** → /manager/api/tasks/**
                .route("task-manager-route", r -> r.path("/gateway/api/tasks/**")
                        .filters(f -> f.stripPrefix(1)
                                .prefixPath("/manager")
                                .retry(3))
                        .uri("lb://data-rsync-task-manager"))
                // 健康检查路由
                .route("health-route", r -> r.path("/gateway/health")
                        .filters(f -> f.prefixPath("/auth"))
                        .uri("lb://data-rsync-auth"))
                .build();
    }
}
