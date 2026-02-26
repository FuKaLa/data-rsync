package com.data.rsync.auth.filter;

import com.data.rsync.auth.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * JWT认证过滤器，用于验证请求中的token
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ROLES_CLAIM = "roles";
    private static final String PERMISSIONS_CLAIM = "permissions";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 跳过不需要认证的路径
        if (shouldSkipAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
            String token = jwtUtils.extractToken(authorizationHeader);
            
            if (token != null && jwtUtils.validateToken(token)) {
                processValidToken(request, token);
            } else if (token != null) {
                // Token存在但无效
                logger.warn("Invalid token provided for request: {}", request.getRequestURI());
                SecurityContextHolder.clearContext();
            }
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired for request: {}", request.getRequestURI(), e);
            handleAuthenticationError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
            return;
        } catch (SignatureException e) {
            logger.warn("Invalid token signature for request: {}", request.getRequestURI(), e);
            handleAuthenticationError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token signature");
            return;
        } catch (MalformedJwtException e) {
            logger.warn("Malformed token for request: {}", request.getRequestURI(), e);
            handleAuthenticationError(response, HttpServletResponse.SC_BAD_REQUEST, "Malformed token");
            return;
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported token for request: {}", request.getRequestURI(), e);
            handleAuthenticationError(response, HttpServletResponse.SC_BAD_REQUEST, "Unsupported token");
            return;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid token argument for request: {}", request.getRequestURI(), e);
            handleAuthenticationError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid token");
            return;
        } catch (Exception e) {
            logger.error("Unexpected error during JWT authentication for request: {}", request.getRequestURI(), e);
            handleAuthenticationError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 处理有效的token
     * @param request HTTP请求
     * @param token JWT token
     * @throws Exception 处理过程中的异常
     */
    private void processValidToken(HttpServletRequest request, String token) throws Exception {
        Claims claims = jwtUtils.parseToken(token);
        String username = claims.getSubject();

        if (username == null || username.trim().isEmpty()) {
            logger.warn("Token subject is null or empty");
            SecurityContextHolder.clearContext();
            return;
        }

        // 从claims中获取角色和权限信息
        Collection<GrantedAuthority> authorities = extractAuthorities(claims);

        // 创建UserDetails对象
        UserDetails userDetails = new User(
                username,
                "",
                authorities
        );

        // 创建Authentication对象
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
        );

        // 设置到SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.debug("JWT authentication successful for user: {}", username);
    }

    /**
     * 从claims中提取权限信息
     * @param claims JWT claims
     * @return 权限集合
     */
    private Collection<GrantedAuthority> extractAuthorities(Claims claims) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // 提取角色信息
        Object rolesObj = claims.get(ROLES_CLAIM);
        if (rolesObj != null) {
            if (rolesObj instanceof List) {
                for (Object role : (List<?>) rolesObj) {
                    if (role instanceof String) {
                        String roleStr = (String) role;
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr.toUpperCase()));
                    }
                }
            } else if (rolesObj instanceof String) {
                String roleStr = (String) rolesObj;
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr.toUpperCase()));
            }
        }

        // 提取权限信息
        Object permissionsObj = claims.get(PERMISSIONS_CLAIM);
        if (permissionsObj != null) {
            if (permissionsObj instanceof List) {
                for (Object permission : (List<?>) permissionsObj) {
                    if (permission instanceof String) {
                        authorities.add(new SimpleGrantedAuthority((String) permission));
                    }
                }
            } else if (permissionsObj instanceof String) {
                authorities.add(new SimpleGrantedAuthority((String) permissionsObj));
            }
        }

        return authorities;
    }

    /**
     * 处理认证错误
     * @param response HTTP响应
     * @param statusCode HTTP状态码
     * @param message 错误消息
     * @throws IOException IO异常
     */
    private void handleAuthenticationError(HttpServletResponse response, int statusCode, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(statusCode);
        response.setContentType("application/json; charset=UTF-8");
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"code\":\"" + statusCode + "\",\"message\":\"" + message + "\"}");
            writer.flush();
        }
    }

    /**
     * 判断是否跳过认证
     * @param request HTTP请求
     * @return 是否跳过认证
     */
    private boolean shouldSkipAuthentication(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        
        // 跳过不需要认证的路径
        return requestURI.contains("/auth/") ||
               requestURI.contains("/public/") ||
               requestURI.startsWith("/actuator/") ||
               requestURI.contains("/health") ||
               requestURI.equals("/favicon.ico");
    }
}
