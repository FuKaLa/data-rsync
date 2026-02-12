package com.data.rsync.log.listener.config;

import com.data.rsync.common.config.FilterConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * 日志监听服务配置类
 * 排除FilterConfig类的扫描，避免signatureFilter过滤器重复注册
 */
@Configuration
@ComponentScan(basePackages = "com.data.rsync.common", excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = FilterConfig.class)
})
public class LogListenerConfig {

}
