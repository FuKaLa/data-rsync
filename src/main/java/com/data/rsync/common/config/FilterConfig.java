package com.data.rsync.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置类
 */
@Configuration
@ConditionalOnProperty(name = "filter.config.enabled", havingValue = "true", matchIfMissing = false)
public class FilterConfig {

    // 暂时移除所有过滤器的注册，避免重复注册问题
}

