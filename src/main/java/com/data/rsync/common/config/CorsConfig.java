package com.data.rsync.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS配置类
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${data-rsync.cors.enabled:true}")
    private boolean enabled;

    @Value("${data-rsync.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${data-rsync.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${data-rsync.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${data-rsync.cors.max-age:3600}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (enabled) {
            registry.addMapping("/**")
                    .allowedOriginPatterns(allowedOrigins.split(","))
                    .allowedMethods(allowedMethods.split(","))
                    .allowedHeaders(allowedHeaders.split(","))
                    .allowCredentials(true)
                    .maxAge(maxAge);
        }
    }
}
