package com.data.rsync.common.filter;

import com.data.rsync.common.utils.ConfigUtils;
import com.data.rsync.common.utils.SignatureUtils;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * API签名验证过滤器
 */
public class SignatureFilter implements Filter {

    private static final long DEFAULT_TIMEOUT = 300; // 默认超时时间（秒）

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 获取请求路径
            String requestUri = httpRequest.getRequestURI();
            
            // 跳过不需要验证的路径（如健康检查、静态资源等）
            if (shouldSkipValidation(requestUri)) {
                chain.doFilter(request, response);
                return;
            }

            // 获取签名和时间戳
            String signature = httpRequest.getParameter("signature");
            String timestampStr = httpRequest.getParameter("timestamp");

            // 检查签名和时间戳是否存在
            if (signature == null || timestampStr == null) {
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write("Missing signature or timestamp");
                return;
            }

            // 解析时间戳
            long timestamp;
            try {
                timestamp = Long.parseLong(timestampStr);
            } catch (NumberFormatException e) {
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write("Invalid timestamp");
                return;
            }

            // 验证时间戳是否过期
            if (!SignatureUtils.verifyTimestamp(timestamp, DEFAULT_TIMEOUT)) {
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.getWriter().write("Timestamp expired");
                return;
            }

            // 获取所有请求参数
            Map<String, Object> params = getAllParams(httpRequest);

            // 获取密钥
            String secretKey = getSecretKey();

            // 验证签名
            if (!SignatureUtils.verifySignature(params, signature, secretKey)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().write("Invalid signature");
                return;
            }

            // 验证通过，继续处理请求
            chain.doFilter(request, response);
        } catch (Exception e) {
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.getWriter().write("Internal server error");
        }
    }

    /**
     * 判断是否跳过签名验证
     * @param requestUri 请求路径
     * @return 是否跳过
     */
    private boolean shouldSkipValidation(String requestUri) {
        // 跳过健康检查路径
        if (requestUri.contains("/actuator/health")) {
            return true;
        }
        // 跳过静态资源路径
        if (requestUri.contains("/static/") || requestUri.contains("/public/")) {
            return true;
        }
        // 跳过登录路径
        if (requestUri.contains("/auth/login")) {
            return true;
        }
        return false;
    }

    /**
     * 获取所有请求参数
     * @param request HTTP请求
     * @return 请求参数
     */
    private Map<String, Object> getAllParams(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }

    /**
     * 获取密钥
     * @return 密钥
     */
    private String getSecretKey() {
        // 默认密钥（生产环境应该从配置文件中获取）
        return "default_secret_key"; // 生产环境应该从配置文件中获取
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
