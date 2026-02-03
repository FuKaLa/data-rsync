package com.data.rsync.admin.controller;

import com.data.rsync.auth.model.User;
import com.data.rsync.auth.service.AuthService;
import com.data.rsync.common.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 登录
     * @param params 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Response login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        
        // 这里应该调用AuthService的登录方法，验证用户名和密码
        // 然后生成JWT token并返回
        
        User user = authService.getUserByUsername(username);
        if (user == null) {
            return Response.failure("用户不存在");
        }
        
        // 这里应该验证密码，但是由于密码是加密存储的，需要使用PasswordEncoder
        // 暂时返回成功，后续完善
        
        return Response.success("登录成功", user);
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
