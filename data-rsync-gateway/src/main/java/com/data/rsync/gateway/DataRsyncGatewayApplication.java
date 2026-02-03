package com.data.rsync.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * 数据同步网关应用主类
 * 企业级网关服务，负责服务路由、负载均衡、认证授权、限流熔断等功能
 */
@SpringBootApplication
@EnableDiscoveryClient
public class DataRsyncGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncGatewayApplication.class, args);
    }

    /**
     * 路由配置
     * 使用代码方式配置路由，与配置文件方式互补
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 数据源管理路由
                .route("data-source-route", r -> r.path("/data-source/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://data-rsync-data-source"))
                // 认证服务路由
                .route("auth-route", r -> r.path("/auth/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://data-rsync-auth"))
                // 任务管理路由
                .route("task-manager-route", r -> r.path("/task-manager/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://data-rsync-task-manager"))
                // 数据处理路由
                .route("data-process-route", r -> r.path("/data-process/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://data-rsync-data-process"))
                // 日志监听路由
                .route("log-listener-route", r -> r.path("/log-listener/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://data-rsync-log-listener"))
                // Milvus同步路由
                .route("milvus-sync-route", r -> r.path("/milvus-sync/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://data-rsync-milvus-sync"))
                // 监控服务路由
                .route("monitor-route", r -> r.path("/monitor/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://data-rsync-monitor"))
                // 管理服务路由
                .route("admin-route", r -> r.path("/admin/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://data-rsync-admin"))
                .build();
    }
}
