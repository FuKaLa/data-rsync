package com.data.rsync.log.listener;

import com.data.rsync.log.listener.service.impl.LogListenerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class DataRsyncLogListenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncLogListenerApplication.class, args);
    }

    @Bean
    public CommandLineRunner shutdownHook(ConfigurableApplicationContext context, LogListenerServiceImpl logListenerService) {
        return args -> {
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    logListenerService.shutdown();
                } catch (Exception e) {
                    System.err.println("Error during shutdown: " + e.getMessage());
                }
            }));
        };
    }

}
