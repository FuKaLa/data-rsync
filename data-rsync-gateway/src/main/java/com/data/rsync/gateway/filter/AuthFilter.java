package com.data.rsync.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 认证过滤器
 * 验证请求的令牌和权限
 */
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 跳过认证的路径
            String path = exchange.getRequest().getURI().getPath();
            if (config.getSkipPaths() != null && config.getSkipPaths().contains(path)) {
                return chain.filter(exchange);
            }

            // 获取Authorization头
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);

            // 验证令牌
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return handleUnauthorized(exchange);
            }

            String token = authorization.substring(7);

            // 这里可以添加令牌验证逻辑，例如调用认证服务验证令牌
            // 示例：boolean isValid = authService.validateToken(token);
            boolean isValid = validateToken(token);

            if (!isValid) {
                return handleUnauthorized(exchange);
            }

            // 验证权限
            if (config.getRequiredRoles() != null && !config.getRequiredRoles().isEmpty()) {
                // 这里可以添加权限验证逻辑，例如从令牌中获取用户角色并验证
                // 示例：boolean hasPermission = authService.hasPermission(token, config.getRequiredRoles());
                boolean hasPermission = hasPermission(token, config.getRequiredRoles());

                if (!hasPermission) {
                    return handleForbidden(exchange);
                }
            }

            // 令牌验证通过，继续处理请求
            return chain.filter(exchange);
        };
    }

    /**
     * 处理未授权的请求
     */
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    /**
     * 处理禁止访问的请求
     */
    private Mono<Void> handleForbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    /**
     * 验证令牌
     * 实际项目中应该调用认证服务进行验证
     */
    private boolean validateToken(String token) {
        // 示例实现，实际项目中应该调用认证服务进行验证
        return token != null && token.length() > 0;
    }

    /**
     * 验证权限
     * 实际项目中应该调用认证服务进行验证
     */
    private boolean hasPermission(String token, List<String> requiredRoles) {
        // 示例实现，实际项目中应该从令牌中获取用户角色并验证
        return true;
    }

    /**
     * 过滤器配置类
     */
    public static class Config {
        private List<String> skipPaths;
        private List<String> requiredRoles;

        public List<String> getSkipPaths() {
            return skipPaths;
        }

        public void setSkipPaths(List<String> skipPaths) {
            this.skipPaths = skipPaths;
        }

        public List<String> getRequiredRoles() {
            return requiredRoles;
        }

        public void setRequiredRoles(List<String> requiredRoles) {
            this.requiredRoles = requiredRoles;
        }
    }
}
