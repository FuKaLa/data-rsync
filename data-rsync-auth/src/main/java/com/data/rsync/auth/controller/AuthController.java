package com.data.rsync.auth.controller;

import com.data.rsync.common.model.Response;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 处理登录、注销等认证相关请求
 */
@RestController
@RequestMapping("")
public class AuthController {

    /**
     * 登录
     * @param params 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Response login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        
        // 验证用户名和密码
        if (username == null || password == null) {
            return Response.failure(400, "用户名和密码不能为空");
        }
        
        // 这里应该调用AuthService的登录方法，验证用户名和密码
        // 然后生成JWT token并返回
        
        // 暂时使用简单的验证逻辑，后续通过AuthService实现完整的登录逻辑
        if (!"admin".equals(username) || !"123456".equals(password)) {
            return Response.failure(401, "用户名或密码错误");
        }
        
        // 生成模拟的JWT token
        String token = "mock-jwt-token-" + System.currentTimeMillis();
        
        // 返回登录成功结果，包含token
        return Response.success("登录成功", token);
    }

    /**
     * 注销
     * @return 注销结果
     */
    @PostMapping("/logout")
    public Response logout() {
        // 这里应该处理注销逻辑，比如清除token等
        return Response.success("注销成功");
    }

    /**
     * 获取当前用户信息
     * @return 当前用户信息
     */
    @GetMapping("/current-user")
    public Response getCurrentUser() {
        // 这里应该从token中获取用户信息并返回
        return Response.success("获取成功", null);
    }

}
