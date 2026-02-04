package com.data.rsync.datasource;

import com.data.rsync.datasource.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.data.rsync.common.feign")
public class DataRsyncDataSourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncDataSourceApplication.class, args);
    }

    @Bean
    public CommandLineRunner shutdownHook(ConfigurableApplicationContext context, DataSourceService dataSourceService) {
        return args -> {
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    dataSourceService.shutdown();
                } catch (Exception e) {
                    System.err.println("Error during shutdown: " + e.getMessage());
                }
            }));
        };
    }

}
