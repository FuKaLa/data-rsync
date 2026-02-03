package com.data.rsync.admin.controller;

import com.data.rsync.auth.model.User;
import com.data.rsync.auth.service.AuthService;
import com.data.rsync.common.model.Response;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统控制器
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    @Resource
    private AuthService authService;

    /**
     * 获取所有用户
     * @return 用户列表
     */
    @GetMapping("/users")
    public Response getUsers() {
        List<User> users = authService.getAllUsers();
        return Response.success("获取成功", users);
    }

    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建结果
     */
    @PostMapping("/users")
    public Response createUser(@RequestBody User user) {
        User createdUser = authService.createUser(user);
        return Response.success("创建成功", createdUser);
    }

    /**
     * 更新用户
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/users")
    public Response updateUser(@RequestBody User user) {
        User updatedUser = authService.updateUser(user);
        return Response.success("更新成功", updatedUser);
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/users/{id}")
    public Response deleteUser(@PathVariable Long id) {
        authService.deleteUser(id);
        return Response.success("删除成功");
    }

    /**
     * 重置用户密码
     * @param id 用户ID
     * @param params 重置参数
     * @return 重置结果
     */
    @PostMapping("/users/{id}/reset-password")
    public Response resetPassword(@PathVariable Long id, @RequestBody List<String> params) {
        String newPassword = params.get(0);
        authService.resetPassword(id, newPassword);
        return Response.success("重置成功");
    }

    /**
     * 获取系统配置
     * @return 系统配置
     */
    @GetMapping("/config")
    public Response getConfig() {
        // 这里应该从配置中心获取系统配置
        return Response.success("获取成功", null);
    }

    /**
     * 更新系统配置
     * @param config 配置信息
     * @return 更新结果
     */
    @PutMapping("/config")
    public Response updateConfig(@RequestBody Object config) {
        // 这里应该更新配置中心的系统配置
        return Response.success("更新成功");
    }

    /**
     * 获取审计日志
     * @return 审计日志
     */
    @GetMapping("/audit-logs")
    public Response getAuditLogs() {
        // 这里应该调用AuthService的queryAuditLogs方法
        return Response.success("获取成功", null);
    }

}
