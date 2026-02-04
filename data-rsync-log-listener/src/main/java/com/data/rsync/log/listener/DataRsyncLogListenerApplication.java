package com.data.rsync.log.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.data.rsync.common.feign")
public class DataRsyncLogListenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataRsyncLogListenerApplication.class, args);
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

