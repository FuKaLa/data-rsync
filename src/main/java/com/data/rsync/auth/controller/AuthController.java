package com.data.rsync.auth.controller;

import com.data.rsync.common.model.Response;
import com.data.rsync.auth.service.AuthService;
import com.data.rsync.auth.model.User;
import com.data.rsync.auth.utils.JwtUtils;
import com.data.rsync.auth.vo.LoginRequest;
import com.data.rsync.auth.vo.LoginResponse;
import com.data.rsync.auth.vo.RefreshTokenRequest;
import com.data.rsync.auth.vo.RefreshTokenResponse;
import com.data.rsync.auth.vo.UserInfoResponse;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 * 处理登录、注销等认证相关请求
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 登录
     * @param loginRequest 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Response<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        logger.info("开始处理登录请求，参数：{}", loginRequest);
        
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
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
        
        // 准备JWT声明
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("name", user.getName());
        claims.put("email", user.getEmail());
        claims.put("phone", user.getPhone());
        claims.put("status", user.getStatus());
        claims.put("roles", user.getRoles());
        
        // 生成企业级的 JWT token
        String token = jwtUtils.generateToken(user.getUsername(), claims);
        // 生成刷新token
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());
        
        logger.info("登录成功，用户：{}", username);
        
        // 返回登录成功结果，包含token和refreshToken
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setUser(user);
        
        return Response.success("登录成功", loginResponse);
    }

    /**
     * 注册
     * @param user 用户信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Response register(@RequestBody @Valid User user) {
        logger.info("开始处理注册请求，用户：{}", user.getUsername());
        
        try {
            // 检查用户名是否已存在
            if (authService.getUserByUsername(user.getUsername()) != null) {
                logger.warn("注册失败，用户名已存在：{}", user.getUsername());
                return Response.failure(400, "用户名已存在");
            }
            
            // 设置默认状态
            user.setStatus("ENABLE");
            
            // 创建用户
            User createdUser = authService.createUser(user);
            
            logger.info("注册成功，用户：{}", user.getUsername());
            return Response.success("注册成功", createdUser);
        } catch (Exception e) {
            logger.error("注册失败，用户：{}", user.getUsername(), e);
            return Response.failure(500, "注册失败");
        }
    }

    /**
     * 注销
     * @param authorization Authorization头
     * @return 注销结果
     */
    @PostMapping("/logout")
    public Response logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("开始处理注销请求");
        
        try {
            // 从Authorization头中提取token并加入黑名单
            String token = jwtUtils.extractToken(authorization);
            if (token != null) {
                jwtUtils.blacklistToken(token);
                logger.info("Token已加入黑名单");
            }
        } catch (Exception e) {
            logger.error("注销时处理token失败", e);
        }
        
        logger.info("注销成功");
        return Response.success("注销成功");
    }

    /**
     * 刷新token
     * @param refreshTokenRequest 包含refreshToken的参数
     * @return 刷新结果
     */
    @PostMapping("/refresh")
    public Response<RefreshTokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        logger.info("开始处理token刷新请求");
        
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        try {
            // 刷新token
            String newToken = jwtUtils.refreshToken(refreshToken);
            if (newToken == null) {
                logger.warn("刷新token失败，无效的refreshToken");
                return Response.failure(401, "无效的refreshToken");
            }
            
            // 生成新的刷新token
            String newRefreshToken = jwtUtils.generateRefreshToken(jwtUtils.getSubject(refreshToken));
            
            logger.info("刷新token成功");
            
            RefreshTokenResponse refreshTokenResponse = new RefreshTokenResponse();
            refreshTokenResponse.setToken(newToken);
            refreshTokenResponse.setRefreshToken(newRefreshToken);
            
            return Response.success("刷新成功", refreshTokenResponse);
        } catch (Exception e) {
            logger.error("刷新token失败", e);
            return Response.failure(401, "刷新token失败");
        }
    }

    /**
     * 获取当前用户信息
     * @param authorization Authorization头
     * @return 当前用户信息
     */
    @GetMapping("/current-user")
    public Response<UserInfoResponse> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorization) {
        logger.info("开始处理获取当前用户信息请求");
        
        try {
            // 从 Authorization 头中获取 token
            String token = jwtUtils.extractToken(authorization);
            if (token == null) {
                logger.warn("获取当前用户信息失败，缺少或无效的 Authorization 头");
                return Response.failure(401, "未授权，请重新登录");
            }
            
            logger.debug("Extracted token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
            
            // 验证并解析 JWT token
            if (!jwtUtils.validateToken(token)) {
                logger.warn("获取当前用户信息失败，无效的 token");
                return Response.failure(401, "无效的token，请重新登录");
            }
            
            // 解析 token，获取用户信息
            Claims claims = jwtUtils.parseToken(token);
            
            // 从 claims 中提取用户信息
            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setUsername(claims.getSubject());
            userInfo.setUserId((Long) claims.get("userId"));
            userInfo.setName((String) claims.get("name"));
            userInfo.setEmail((String) claims.get("email"));
            userInfo.setPhone((String) claims.get("phone"));
            userInfo.setStatus((String) claims.get("status"));
            // 安全地转换roles类型
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List) {
                List<?> rolesList = (List<?>) rolesObj;
                List<String> roles = new ArrayList<>();
                for (Object role : rolesList) {
                    if (role instanceof String) {
                        roles.add((String) role);
                    }
                }
                userInfo.setRoles(roles);
            } else {
                userInfo.setRoles(new ArrayList<>());
            }
            
            logger.info("获取当前用户信息成功，用户：{}", claims.getSubject());
            return Response.success("获取成功", userInfo);
        } catch (Exception e) {
            logger.error("获取当前用户信息失败", e);
            return Response.failure(401, "无效的token，请重新登录");
        }
    }

} 

