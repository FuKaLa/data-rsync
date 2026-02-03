package com.data.rsync.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 熔断过滤器
 * 处理服务调用失败的情况
 */
@Component
public class CircuitBreakerFilter extends AbstractGatewayFilterFactory<CircuitBreakerFilter.Config> {

    public CircuitBreakerFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                return chain.filter(exchange);
            } catch (Exception e) {
                // 处理服务调用失败的情况
                return handleFallback(exchange, e, config);
            }
        };
    }

    /**
     * 处理服务调用失败的情况
     */
    private Mono<Void> handleFallback(ServerWebExchange exchange, Exception e, Config config) {
        // 这里可以实现具体的fallback逻辑
        // 例如返回错误响应、记录日志等
        return exchange.getResponse().setComplete();
    }

    /**
     * 过滤器配置类
     */
    public static class Config {
        private String circuitBreakerName;
        private String fallbackUri;

        public String getCircuitBreakerName() {
            return circuitBreakerName;
        }

        public void setCircuitBreakerName(String circuitBreakerName) {
            this.circuitBreakerName = circuitBreakerName;
        }

        public String getFallbackUri() {
            return fallbackUri;
        }

        public void setFallbackUri(String fallbackUri) {
            this.fallbackUri = fallbackUri;
        }
    }
}
