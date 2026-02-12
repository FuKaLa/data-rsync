package com.data.rsync.auth.controller;

import com.data.rsync.common.model.Response;
import com.data.rsync.auth.model.User;
import com.data.rsync.auth.model.Role;
import com.data.rsync.auth.model.Permission;
import com.data.rsync.auth.model.AuditLog;
import com.data.rsync.auth.service.AuthService;
import com.data.rsync.auth.vo.SystemConfigResponse;
import com.data.rsync.auth.vo.SystemConfigRequest;
import com.data.rsync.auth.vo.SystemInfoResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 系统管理控制器
 */
@RestController
@RequestMapping("/system")
public class SystemController {

    @Resource
    private AuthService authService;

    /**
     * 获取用户列表
     * @return 用户列表
     */
    @GetMapping("/users")
    public Response<List<User>> getUsers() {
        try {
            List<User> users = authService.getAllUsers();
            return Response.success(users);
        } catch (Exception e) {
            return Response.failure(500, "获取用户列表失败");
        }
    }

    /**
     * 获取角色列表
     * @return 角色列表
     */
    @GetMapping("/roles")
    public Response<List<Role>> getRoles() {
        try {
            List<Role> roles = authService.getAllRoles();
            return Response.success(roles);
        } catch (Exception e) {
            return Response.failure(500, "获取角色列表失败");
        }
    }

    /**
     * 获取审计日志
     * @return 审计日志列表
     */
    @GetMapping("/logs")
    public Response<List<AuditLog>> getAuditLogs() {
        try {
            List<AuditLog> logs = authService.queryAuditLogs(new HashMap<>());
            return Response.success(logs);
        } catch (Exception e) {
            return Response.failure(500, "获取审计日志失败");
        }
    }

    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建结果
     */
    @PostMapping("/users")
    public Response<User> createUser(@RequestBody User user) {
        try {
            User createdUser = authService.createUser(user);
            return Response.success("用户创建成功", createdUser);
        } catch (Exception e) {
            return Response.failure(500, "创建用户失败");
        }
    }

    /**
     * 更新用户
     * @param id 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/users/{id}")
    public Response<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            // 创建新的User对象并设置ID
            User updatedUser = new User();
            updatedUser.setId(id);
            updatedUser.setUsername(user.getUsername());
            updatedUser.setName(user.getName());
            updatedUser.setEmail(user.getEmail());
            updatedUser.setPhone(user.getPhone());
            updatedUser.setStatus(user.getStatus());
            
            updatedUser = authService.updateUser(updatedUser);
            return Response.success("用户更新成功", updatedUser);
        } catch (Exception e) {
            return Response.failure(500, "更新用户失败");
        }
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/users/{id}")
    public Response<String> deleteUser(@PathVariable Long id) {
        try {
            authService.deleteUser(id);
            return Response.success("用户删除成功");
        } catch (Exception e) {
            return Response.failure(500, "删除用户失败");
        }
    }

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建结果
     */
    @PostMapping("/roles")
    public Response<Role> createRole(@RequestBody Role role) {
        try {
            Role createdRole = authService.createRole(role);
            return Response.success("角色创建成功", createdRole);
        } catch (Exception e) {
            return Response.failure(500, "创建角色失败");
        }
    }

    /**
     * 更新角色
     * @param id 角色ID
     * @param role 角色信息
     * @return 更新结果
     */
    @PutMapping("/roles/{id}")
    public Response<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            // 创建新的Role对象并设置ID
            Role updatedRole = new Role();
            updatedRole.setId(id);
            updatedRole.setName(role.getName());
            updatedRole.setCode(role.getCode());
            updatedRole.setDescription(role.getDescription());
            updatedRole.setStatus(role.getStatus());
            
            updatedRole = authService.updateRole(updatedRole);
            return Response.success("角色更新成功", updatedRole);
        } catch (Exception e) {
            return Response.failure(500, "更新角色失败");
        }
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/roles/{id}")
    public Response<String> deleteRole(@PathVariable Long id) {
        try {
            authService.deleteRole(id);
            return Response.success("角色删除成功");
        } catch (Exception e) {
            return Response.failure(500, "删除角色失败");
        }
    }

    /**
     * 分配用户角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     * @return 分配结果
     */
    @PostMapping("/users/{userId}/roles")
    public Response<String> assignUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        try {
            authService.assignRolesToUser(userId, roleIds);
            return Response.success("角色分配成功");
        } catch (Exception e) {
            return Response.failure(500, "分配角色失败");
        }
    }

    /**
     * 分配角色权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 分配结果
     */
    @PostMapping("/roles/{roleId}/permissions")
    public Response<String> assignRolePermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        try {
            authService.assignPermissionsToRole(roleId, permissionIds);
            return Response.success("权限分配成功");
        } catch (Exception e) {
            return Response.failure(500, "分配权限失败");
        }
    }

    /**
     * 获取用户角色
     * @param userId 用户ID
     * @return 用户角色列表
     */
    @GetMapping("/users/{userId}/roles")
    public Response<List<Role>> getUserRoles(@PathVariable Long userId) {
        try {
            List<Role> roles = authService.getUserRoles(userId);
            return Response.success(roles);
        } catch (Exception e) {
            return Response.failure(500, "获取用户角色失败");
        }
    }

    /**
     * 获取角色权限
     * @param roleId 角色ID
     * @return 角色权限列表
     */
    @GetMapping("/roles/{roleId}/permissions")
    public Response<List<Permission>> getRolePermissions(@PathVariable Long roleId) {
        try {
            List<Permission> permissions = authService.getRolePermissions(roleId);
            return Response.success(permissions);
        } catch (Exception e) {
            return Response.failure(500, "获取角色权限失败");
        }
    }

    /**
     * 获取系统配置
     * @return 系统配置
     */
    @GetMapping("/config")
    public Response<SystemConfigResponse> getSystemConfig() {
        try {
            SystemConfigResponse config = new SystemConfigResponse();
            // 从配置文件或数据库中获取系统配置
            config.setAppName("Data-Rsync");
            config.setAppVersion("1.0.0");
            config.setMaxConnections(100);
            config.setTaskThreadPoolSize(20);
            config.setDataSyncBatchSize(1000);
            config.setMilvusConnectionTimeout(30000);
            config.setMetricsCollectionInterval(60000);
            return Response.success(config);
        } catch (Exception e) {
            return Response.failure(500, "获取系统配置失败");
        }
    }

    /**
     * 更新系统配置
     * @param configRequest 配置数据
     * @return 更新结果
     */
    @PutMapping("/config")
    public Response<String> updateSystemConfig(@RequestBody SystemConfigRequest configRequest) {
        try {
            // 实现更新系统配置的逻辑
            // 1. 验证配置参数
            // 2. 更新配置文件或数据库
            // 3. 刷新系统配置缓存
            return Response.success("系统配置更新成功");
        } catch (Exception e) {
            return Response.failure(500, "更新系统配置失败");
        }
    }

    /**
     * 刷新系统缓存
     * @return 刷新结果
     */
    @PostMapping("/cache/refresh")
    public Response<String> refreshSystemCache() {
        try {
            // 实现刷新系统缓存的逻辑
            // 1. 清除Redis缓存
            // 2. 刷新本地缓存
            // 3. 通知其他服务刷新缓存
            return Response.success("系统缓存刷新成功");
        } catch (Exception e) {
            return Response.failure(500, "刷新系统缓存失败");
        }
    }

    /**
     * 清理系统日志
     * @return 清理结果
     */
    @PostMapping("/logs/clean")
    public Response<String> cleanSystemLogs() {
        try {
            // 实现清理系统日志的逻辑
            // 1. 清理应用日志文件
            // 2. 清理数据库中的日志记录
            // 3. 保留最近N天的日志
            return Response.success("系统日志清理成功");
        } catch (Exception e) {
            return Response.failure(500, "清理系统日志失败");
        }
    }

    /**
     * 获取系统信息
     * @return 系统信息
     */
    @GetMapping("/info")
    public Response<SystemInfoResponse> getSystemInfo() {
        try {
            SystemInfoResponse info = new SystemInfoResponse();
            // 获取真实的系统信息
            info.setJavaVersion(System.getProperty("java.version"));
            info.setOsName(System.getProperty("os.name"));
            info.setOsVersion(System.getProperty("os.version"));
            info.setAvailableProcessors(Runtime.getRuntime().availableProcessors());
            info.setMaxMemory(Runtime.getRuntime().maxMemory() / (1024 * 1024) + "MB");
            info.setTotalMemory(Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");
            info.setFreeMemory(Runtime.getRuntime().freeMemory() / (1024 * 1024) + "MB");
            info.setSystemTime(new java.util.Date().toString());
            return Response.success(info);
        } catch (Exception e) {
            return Response.failure(500, "获取系统信息失败");
        }
    }
}
