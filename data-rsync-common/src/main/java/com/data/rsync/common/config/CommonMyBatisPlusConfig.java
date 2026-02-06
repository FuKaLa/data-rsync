package com.data.rsync.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * 公共 MyBatis-Plus 配置类
 * 提供统一的 MyBatis-Plus 配置，供所有服务使用
 */
@Configuration
@RefreshScope
public class CommonMyBatisPlusConfig {

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    @Value("${spring.datasource.url:${DB_URL:jdbc:mysql://localhost:3306/data_rsync?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai}}")
    private String url;

    @Value("${spring.datasource.username:${DB_USERNAME:root}}")
    private String username;

    @Value("${spring.datasource.password:${DB_PASSWORD:123456}}")
    private String password;

    /**
     * 配置 MyBatis-Plus 插件
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 数据源配置
     * @return DataSource
     */
    @Bean
    @RefreshScope
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    /**
     * SqlSessionFactory 配置
     * @param dataSource DataSource
     * @return SqlSessionFactory
     * @throws Exception 异常
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean sessionFactory = new MybatisSqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPlugins(mybatisPlusInterceptor());
        return sessionFactory.getObject();
    }

}
