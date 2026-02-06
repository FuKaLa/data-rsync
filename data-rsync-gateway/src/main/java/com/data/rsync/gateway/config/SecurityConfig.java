package com.data.rsync.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 安全配置类
 * 配置 Spring Security 规则，允许特定路径的访问
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchanges -> exchanges
                // 允许登录路径的访问
                .pathMatchers("/api/auth/login").permitAll()
                // 允许健康检查路径的访问
                .pathMatchers("/health").permitAll()
                // 允许其他所有路径的访问
                .anyExchange().permitAll()
            )
            // 禁用 CSRF 保护，因为这是一个 API 网关
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
