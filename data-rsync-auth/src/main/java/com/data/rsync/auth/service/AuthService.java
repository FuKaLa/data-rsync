package com.data.rsync.auth.service;

import com.data.rsync.auth.model.User;
import com.data.rsync.auth.model.Role;
import com.data.rsync.auth.model.Permission;
import com.data.rsync.auth.model.AuditLog;

import java.util.List;
import java.util.Map;

/**
 * 权限管理服务接口
 */
public interface AuthService {

    // ==================== 用户管理 ====================

    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建的用户
     */
    User createUser(User user);

    /**
     * 更新用户
     * @param user 用户信息
     * @return 更新后的用户
     */
    User updateUser(User user);

    /**
     * 删除用户
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 获取用户
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 获取所有用户
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 启用/禁用用户
     * @param userId 用户ID
     * @param enabled 是否启用
     */
    void toggleUserStatus(Long userId, boolean enabled);

    // ==================== 角色管理 ====================

    /**
     * 创建角色
     * @param role 角色信息
     * @return 创建的角色
     */
    Role createRole(Role role);

    /**
     * 更新角色
     * @param role 角色信息
     * @return 更新后的角色
     */
    Role updateRole(Role role);

    /**
     * 删除角色
     * @param roleId 角色ID
     */
    void deleteRole(Long roleId);

    /**
     * 获取角色
     * @param roleId 角色ID
     * @return 角色信息
     */
    Role getRoleById(Long roleId);

    /**
     * 根据角色编码获取角色
     * @param code 角色编码
     * @return 角色信息
     */
    Role getRoleByCode(String code);

    /**
     * 获取所有角色
     * @return 角色列表
     */
    List<Role> getAllRoles();

    /**
     * 启用/禁用角色
     * @param roleId 角色ID
     * @param enabled 是否启用
     */
    void toggleRoleStatus(Long roleId, boolean enabled);

    // ==================== 权限管理 ====================

    /**
     * 创建权限
     * @param permission 权限信息
     * @return 创建的权限
     */
    Permission createPermission(Permission permission);

    /**
     * 更新权限
     * @param permission 权限信息
     * @return 更新后的权限
     */
    Permission updatePermission(Permission permission);

    /**
     * 删除权限
     * @param permissionId 权限ID
     */
    void deletePermission(Long permissionId);

    /**
     * 获取权限
     * @param permissionId 权限ID
     * @return 权限信息
     */
    Permission getPermissionById(Long permissionId);

    /**
     * 根据权限编码获取权限
     * @param code 权限编码
     * @return 权限信息
     */
    Permission getPermissionByCode(String code);

    /**
     * 获取所有权限
     * @return 权限列表
     */
    List<Permission> getAllPermissions();

    /**
     * 启用/禁用权限
     * @param permissionId 权限ID
     * @param enabled 是否启用
     */
    void togglePermissionStatus(Long permissionId, boolean enabled);

    // ==================== 权限分配 ====================

    /**
     * 给用户分配角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 给角色分配权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 获取用户的角色
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getUserRoles(Long userId);

    /**
     * 获取角色的权限
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getRolePermissions(Long roleId);

    /**
     * 获取用户的权限
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getUserPermissions(Long userId);

    // ==================== 操作审计 ====================

    /**
     * 记录审计日志
     * @param auditLog 审计日志
     */
    void recordAuditLog(AuditLog auditLog);

    /**
     * 获取审计日志
     * @param logId 日志ID
     * @return 审计日志
     */
    AuditLog getAuditLogById(Long logId);

    /**
     * 查询审计日志
     * @param params 查询参数
     * @return 审计日志列表
     */
    List<AuditLog> queryAuditLogs(Map<String, Object> params);

    /**
     * 清理过期审计日志
     * @param days 保留天数
     */
    void cleanExpiredAuditLogs(int days);

    // ==================== 权限验证 ====================

    /**
     * 检查用户是否有权限
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 检查用户是否有角色
     * @param userId 用户ID
     * @param roleCode 角色编码
     * @return 是否有角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 获取用户的数据权限
     * @param userId 用户ID
     * @param resourceType 资源类型
     * @return 数据权限
     */
    List<String> getUserDataPermissions(Long userId, String resourceType);

}
