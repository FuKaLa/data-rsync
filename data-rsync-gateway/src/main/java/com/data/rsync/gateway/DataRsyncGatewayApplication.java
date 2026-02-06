package com.data.rsync.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 数据同步网关应用主类
 * 企业级网关服务，负责服务路由、负载均衡、认证授权、限流熔断等功能
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
public class DataRsyncGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncGatewayApplication.class, args);
    }

}

