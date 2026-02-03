package com.data.rsync.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * 日志过滤器
 * 记录请求和响应的详细信息
 */
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 开始时间
            Instant start = Instant.now();

            // 记录请求信息
            if (config.isEnabled()) {
                logRequest(exchange, config);
            }

            // 处理请求
            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        // 结束时间
                        Instant end = Instant.now();
                        // 计算执行时间
                        long executionTime = Duration.between(start, end).toMillis();

                        // 记录响应信息
                        if (config.isEnabled()) {
                            logResponse(exchange, executionTime, config);
                        }
                    }));
        };
    }

    /**
     * 记录请求信息
     */
    private void logRequest(ServerWebExchange exchange, Config config) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String query = exchange.getRequest().getURI().getQuery();
        String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("[GATEWAY] Request: ")
                .append(method).append(" ").append(path);

        if (query != null) {
            logBuilder.append("?").append(query);
        }

        logBuilder.append(" from " + ip);

        // 记录请求头
        if (config.isLogHeaders()) {
            logBuilder.append(" Headers: " + exchange.getRequest().getHeaders());
        }

        // 记录请求体
        if (config.isLogBody()) {
            // 注意：记录请求体可能会影响性能，生产环境应谨慎使用
            logBuilder.append(" (Body logging enabled)");
        }

        logger.info(logBuilder.toString());
    }

    /**
     * 记录响应信息
     */
    private void logResponse(ServerWebExchange exchange, long executionTime, Config config) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        int statusCode = exchange.getResponse().getStatusCode().value();

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("[GATEWAY] Response: ")
                .append(method).append(" ").append(path)
                .append(" - ").append(statusCode)
                .append(" (").append(executionTime).append("ms)");

        // 记录响应头
        if (config.isLogHeaders()) {
            logBuilder.append(" Headers: " + exchange.getResponse().getHeaders());
        }

        logger.info(logBuilder.toString());
    }

    /**
     * 过滤器配置类
     */
    public static class Config {
        private boolean enabled = true;
        private boolean logHeaders = false;
        private boolean logBody = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isLogHeaders() {
            return logHeaders;
        }

        public void setLogHeaders(boolean logHeaders) {
            this.logHeaders = logHeaders;
        }

        public boolean isLogBody() {
            return logBody;
        }

        public void setLogBody(boolean logBody) {
            this.logBody = logBody;
        }
    }
}
