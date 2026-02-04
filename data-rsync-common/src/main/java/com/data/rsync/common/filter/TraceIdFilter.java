package com.data.rsync.common.filter;

import com.data.rsync.common.utils.LogUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 链路ID过滤器
 */
public class TraceIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 从请求头中获取链路ID，如果没有则生成新的
            String traceId = httpRequest.getHeader(LogUtils.TRACE_ID);
            if (traceId == null || traceId.isEmpty()) {
                traceId = LogUtils.setTraceId();
            } else {
                LogUtils.setTraceId(traceId);
            }

            // 将链路ID设置到响应头中
            httpResponse.setHeader(LogUtils.TRACE_ID, traceId);

            // 继续处理请求
            chain.doFilter(request, response);
        } finally {
            // 清除MDC值，避免线程复用导致的问题
            LogUtils.clearMdc();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化方法
    }

    @Override
    public void destroy() {
        // 销毁方法
    }
}
