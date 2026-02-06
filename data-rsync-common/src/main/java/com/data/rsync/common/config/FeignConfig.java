package com.data.rsync.common.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端配置类
 * 优化微服务间的通信方式
 */
@Configuration
public class FeignConfig {

    /**
     * 配置Feign重试机制
     * @return Retryer实例
     */
    @Bean
    public Retryer feignRetryer() {
        // 初始间隔时间：100ms
        // 最大间隔时间：1000ms
        // 最大重试次数：3
        return new Retryer.Default(100, 1000, 3);
    }

    /**
     * 配置Feign超时时间
     * 注意：在Spring Cloud OpenFeign中，超时配置通常通过application.yml配置
     * 这里提供一个配置类，方便后续扩展
     */
    // 可以通过@Bean的方式配置Request.Options
    // @Bean
    // public Request.Options requestOptions() {
    //     return new Request.Options(
    //             5000, // 连接超时时间
    //             10000 // 读取超时时间
    //     );
    // }

}
