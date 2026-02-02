package com.data.rsync.common.config;

import com.data.rsync.common.filter.SignatureFilter;
import com.data.rsync.common.filter.TraceIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置类
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistrationBean() {
        FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TraceIdFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("traceIdFilter");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<SignatureFilter> signatureFilterRegistrationBean() {
        FilterRegistrationBean<SignatureFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SignatureFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("signatureFilter");
        registrationBean.setOrder(2);
        return registrationBean;
    }
}
