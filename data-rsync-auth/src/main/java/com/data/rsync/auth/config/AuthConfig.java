package com.data.rsync.auth.config;

import com.data.rsync.common.config.CommonMyBatisPlusConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;

/**
 * 权限管理配置类
 */
@Configuration
@MapperScan(basePackages = "com.data.rsync.auth.mapper")
@Import(CommonMyBatisPlusConfig.class)
public class AuthConfig {

    /**
     * 密码编码器
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

