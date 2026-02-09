package com.data.rsync.auth.controller;

import com.data.rsync.common.model.Response;
import com.data.rsync.auth.service.AuthService;
import com.data.rsync.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理登录、注销等认证相关请求
 */
@RestController
@RequestMapping("")
@EnableConfigurationProperties(AuthController.JwtConfig.class)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Resource
    private AuthService authService;

    @Resource
    private PasswordEncoder passwordEncoder;

    // JWT 配置
    @Resource
    private JwtConfig jwtConfig;

    @ConfigurationProperties(prefix = "auth.jwt")
    public static class JwtConfig {
        private String secret;
        private long expireTime = 3600; // 默认 1小时
        private long refreshTime = 7200; // 默认 2小时

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

        public long getRefreshTime() {
            return refreshTime;
        }

        public void setRefreshTime(long refreshTime) {
            this.refreshTime = refreshTime;
        }
    }



    /**
     * 登录
     * @param params 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Response login(@RequestBody Map<String, String> params) {
        logger.info("开始处理登录请求，参数：{}", params);
        
        String username = params.get("username");
        String password = params.get("password");
        
        // 验证用户名和密码
        if (username == null || password == null) {
            logger.warn("登录请求参数不完整，用户名或密码为空");
            return Response.failure(400, "用户名和密码不能为空");
        }
        
        logger.debug("登录用户：{}", username);
        
        // 调用AuthService的方法，验证用户名和密码
        User user = authService.getUserByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("登录失败，用户名或密码错误：{}", username);
            return Response.failure(401, "用户名或密码错误");
        }
        
        // 检查用户状态
        if (!"ENABLE".equals(user.getStatus())) {
            logger.warn("登录失败，用户已被禁用：{}", username);
            return Response.failure(403, "用户已被禁用");
        }
        
        // 生成企业级的 JWT token
        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .claim("phone", user.getPhone())
                .claim("status", user.getStatus())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpireTime() * 1000))
                .signWith(SignatureAlgorithm.HS256, jwtConfig.getSecret().getBytes())
                .compact();
        
        logger.info("登录成功，用户：{}", username);
        
        // 返回登录成功结果，包含token
        return Response.success("登录成功", token);
    }

    /**
     * 注销
     * @return 注销结果
     */
    @PostMapping("/logout")
    public Response logout() {
        logger.info("开始处理注销请求");
        
        // 这里应该处理注销逻辑，比如清除token等
        
        logger.info("注销成功");
        return Response.success("注销成功");
    }

    /**
     * 获取当前用户信息
     * @return 当前用户信息
     */
    @GetMapping("/current-user")
    public Response getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("开始处理获取当前用户信息请求");
        
        try {
            // 从 Authorization 头中获取 token
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                logger.warn("获取当前用户信息失败，缺少或无效的 Authorization 头");
                return Response.failure(401, "未授权，请重新登录");
            }
            
            String token = authorization.substring(7);
            logger.debug("Extracted token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            
            // 解析 JWT token，获取用户信息
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtConfig.getSecret().getBytes())
                    .parseClaimsJws(token)
                    .getBody();
            
            // 从 claims 中提取用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", claims.getSubject());
            userInfo.put("userId", claims.get("userId"));
            userInfo.put("name", claims.get("name"));
            userInfo.put("email", claims.get("email"));
            userInfo.put("phone", claims.get("phone"));
            userInfo.put("status", claims.get("status"));
            
            logger.info("获取当前用户信息成功，用户：{}", claims.getSubject());
            return Response.success("获取成功", userInfo);
        } catch (Exception e) {
            logger.error("获取当前用户信息失败", e);
            return Response.failure(401, "无效的token，请重新登录");
        }
    }

}
