package com.data.rsync.log.listener.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义过滤器配置类
 * 用于控制FilterConfig类的加载
 */
@Configuration
@ConditionalOnProperty(name = "filter.config.enabled", havingValue = "true", matchIfMissing = false)
public class CustomFilterConfig {

}
