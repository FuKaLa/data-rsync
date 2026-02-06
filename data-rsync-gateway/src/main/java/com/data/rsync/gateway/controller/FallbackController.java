package com.data.rsync.gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 降级处理控制器
 * 处理服务熔断时的降级逻辑
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * 认证服务降级
     * @param exchange 服务交换对象
     * @return 降级响应
     */
    @RequestMapping("/auth")
    public Mono<String> authFallback(ServerWebExchange exchange) {
        return Mono.just("认证服务暂时不可用，请稍后重试");
    }

    /**
     * 数据源服务降级
     * @param exchange 服务交换对象
     * @return 降级响应
     */
    @RequestMapping("/data-source")
    public Mono<String> dataSourceFallback(ServerWebExchange exchange) {
        return Mono.just("数据源服务暂时不可用，请稍后重试");
    }

    /**
     * 监控服务降级
     * @param exchange 服务交换对象
     * @return 降级响应
     */
    @RequestMapping("/monitor")
    public Mono<String> monitorFallback(ServerWebExchange exchange) {
        return Mono.just("监控服务暂时不可用，请稍后重试");
    }

    /**
     * 任务管理服务降级
     * @param exchange 服务交换对象
     * @return 降级响应
     */
    @RequestMapping("/task-manager")
    public Mono<String> taskManagerFallback(ServerWebExchange exchange) {
        return Mono.just("任务管理服务暂时不可用，请稍后重试");
    }

    /**
     * 默认降级处理
     * @param exchange 服务交换对象
     * @return 降级响应
     */
    @RequestMapping("/**")
    public Mono<String> defaultFallback(ServerWebExchange exchange) {
        return Mono.just("服务暂时不可用，请稍后重试");
    }

}
