package com.data.rsync.milvus.sync;

import com.data.rsync.milvus.sync.service.impl.MilvusSyncServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import com.data.rsync.common.config.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.data.rsync.common.feign")
@ComponentScan(basePackages = {"com.data.rsync.milvus.sync", "com.data.rsync.common"}, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = FilterConfig.class)
})
public class DataRsyncMilvusSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncMilvusSyncApplication.class, args);
    }

    @Bean
    public CommandLineRunner shutdownHook(ConfigurableApplicationContext context, MilvusSyncServiceImpl milvusSyncService) {
        return args -> {
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    milvusSyncService.shutdown();
                } catch (Exception e) {
                    System.err.println("Error during shutdown: " + e.getMessage());
                }
            }));
        };
    }

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                // 移除已注册的signatureFilter过滤器
                if (servletContext.getFilterRegistration("signatureFilter") != null) {
                    // 由于removeMappingForUrlPattern方法不存在，我们直接跳过
                    // servletContext.getFilterRegistration("signatureFilter").removeMappingForUrlPattern("/*");
                }
            }
        };
    }

}
