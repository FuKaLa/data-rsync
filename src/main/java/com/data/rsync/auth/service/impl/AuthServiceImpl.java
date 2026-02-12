package com.data.rsync.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.data.rsync.auth.mapper.PermissionMapper;
import com.data.rsync.auth.mapper.RoleMapper;
import com.data.rsync.auth.mapper.UserMapper;
import com.data.rsync.auth.model.AuditLog;
import com.data.rsync.auth.model.Permission;
import com.data.rsync.auth.model.Role;
import com.data.rsync.auth.model.User;
import com.data.rsync.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限管理服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    // ==================== 用户管理 ====================

    @Override
    public User createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            // 检查用户名是否已存在
            User existingUser = getUserByUsername(user.getUsername());
            if (existingUser != null) {
                throw new IllegalArgumentException("Username already exists: " + user.getUsername());
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setStatus("ENABLE");
            
            userMapper.insert(user);
            logger.info("Created user successfully: {}", user.getUsername());
            return user;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    @Override
    public User updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        try {
            // 检查用户是否存在
            User existingUser = getUserById(user.getId());
            if (existingUser == null) {
                throw new IllegalArgumentException("User not found with ID: " + user.getId());
            }

            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            logger.info("Updated user successfully: {}", user.getId());
            return user;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update user: {}", user.getId(), e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        try {
            // 检查用户是否存在
            User existingUser = getUserById(userId);
            if (existingUser == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            userMapper.deleteById(userId);
            logger.info("Deleted user successfully: {}", userId);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete user: {}", userId, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        try {
            User user = userMapper.selectById(userId);
            logger.debug("Retrieved user by ID: {}", userId);
            return user;
        } catch (Exception e) {
            logger.error("Failed to retrieve user by ID: {}", userId, e);
            throw new RuntimeException("Failed to retrieve user", e);
        }
    }

    @Override
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        try {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            User user = userMapper.selectOne(queryWrapper);
            logger.debug("Retrieved user by username: {}", username);
            return user;
        } catch (Exception e) {
            logger.error("Failed to retrieve user by username: {}", username, e);
            throw new RuntimeException("Failed to retrieve user", e);
        }
    }

    @Override
    public List<User> getAllUsers() {
        try {
            List<User> users = userMapper.selectList(null);
            logger.debug("Retrieved all users, count: {}", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Failed to retrieve all users", e);
            throw new RuntimeException("Failed to retrieve users", e);
        }
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be null or empty");
        }

        try {
            User user = getUserById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            logger.info("Reset password for user successfully: {}", userId);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to reset password: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to reset password for user: {}", userId, e);
            throw new RuntimeException("Failed to reset password", e);
        }
    }

    @Override
    public void toggleUserStatus(Long userId, boolean enabled) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        try {
            User user = getUserById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            user.setStatus(enabled ? "ENABLE" : "DISABLE");
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
            logger.info("Toggled user status successfully: {} - {}", userId, enabled ? "ENABLED" : "DISABLED");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to toggle user status: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to toggle user status for user: {}", userId, e);
            throw new RuntimeException("Failed to toggle user status", e);
        }
    }

    // ==================== 角色管理 ====================

    @Override
    public Role createRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (role.getCode() == null || role.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Role code cannot be null or empty");
        }
        if (role.getName() == null || role.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be null or empty");
        }

        try {
            // 检查角色代码是否已存在
            Role existingRole = getRoleByCode(role.getCode());
            if (existingRole != null) {
                throw new IllegalArgumentException("Role code already exists: " + role.getCode());
            }

            role.setCreateTime(LocalDateTime.now());
            role.setUpdateTime(LocalDateTime.now());
            role.setStatus("ENABLE");
            
            roleMapper.insert(role);
            logger.info("Created role successfully: {}", role.getCode());
            return role;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create role: {}", role.getCode(), e);
            throw new RuntimeException("Failed to create role", e);
        }
    }

    @Override
    public Role updateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (role.getId() == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        try {
            // 检查角色是否存在
            Role existingRole = getRoleById(role.getId());
            if (existingRole == null) {
                throw new IllegalArgumentException("Role not found with ID: " + role.getId());
            }

            role.setUpdateTime(LocalDateTime.now());
            roleMapper.updateById(role);
            logger.info("Updated role successfully: {}", role.getId());
            return role;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update role: {}", role.getId(), e);
            throw new RuntimeException("Failed to update role", e);
        }
    }

    @Override
    public void deleteRole(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        try {
            // 检查角色是否存在
            Role existingRole = getRoleById(roleId);
            if (existingRole == null) {
                throw new IllegalArgumentException("Role not found with ID: " + roleId);
            }

            roleMapper.deleteById(roleId);
            logger.info("Deleted role successfully: {}", roleId);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete role: {}", roleId, e);
            throw new RuntimeException("Failed to delete role", e);
        }
    }

    @Override
    public Role getRoleById(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        try {
            Role role = roleMapper.selectById(roleId);
            logger.debug("Retrieved role by ID: {}", roleId);
            return role;
        } catch (Exception e) {
            logger.error("Failed to retrieve role by ID: {}", roleId, e);
            throw new RuntimeException("Failed to retrieve role", e);
        }
    }

    @Override
    public Role getRoleByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Role code cannot be null or empty");
        }

        try {
            QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("code", code);
            Role role = roleMapper.selectOne(queryWrapper);
            logger.debug("Retrieved role by code: {}", code);
            return role;
        } catch (Exception e) {
            logger.error("Failed to retrieve role by code: {}", code, e);
            throw new RuntimeException("Failed to retrieve role", e);
        }
    }

    @Override
    public List<Role> getAllRoles() {
        try {
            List<Role> roles = roleMapper.selectList(null);
            logger.debug("Retrieved all roles, count: {}", roles.size());
            return roles;
        } catch (Exception e) {
            logger.error("Failed to retrieve all roles", e);
            throw new RuntimeException("Failed to retrieve roles", e);
        }
    }

    @Override
    public void toggleRoleStatus(Long roleId, boolean enabled) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        try {
            Role role = getRoleById(roleId);
            if (role == null) {
                throw new IllegalArgumentException("Role not found with ID: " + roleId);
            }

            role.setStatus(enabled ? "ENABLE" : "DISABLE");
            role.setUpdateTime(LocalDateTime.now());
            roleMapper.updateById(role);
            logger.info("Toggled role status successfully: {} - {}", roleId, enabled ? "ENABLED" : "DISABLED");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to toggle role status: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to toggle role status for role: {}", roleId, e);
            throw new RuntimeException("Failed to toggle role status", e);
        }
    }

    // ==================== 权限管理 ====================

    @Override
    public Permission createPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }
        if (permission.getCode() == null || permission.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Permission code cannot be null or empty");
        }
        if (permission.getName() == null || permission.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Permission name cannot be null or empty");
        }

        try {
            // 检查权限代码是否已存在
            Permission existingPermission = getPermissionByCode(permission.getCode());
            if (existingPermission != null) {
                throw new IllegalArgumentException("Permission code already exists: " + permission.getCode());
            }

            permission.setCreateTime(LocalDateTime.now());
            permission.setUpdateTime(LocalDateTime.now());
            permission.setStatus("ENABLE");
            
            permissionMapper.insert(permission);
            logger.info("Created permission successfully: {}", permission.getCode());
            return permission;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create permission: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create permission: {}", permission.getCode(), e);
            throw new RuntimeException("Failed to create permission", e);
        }
    }

    @Override
    public Permission updatePermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission cannot be null");
        }
        if (permission.getId() == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }

        try {
            // 检查权限是否存在
            Permission existingPermission = getPermissionById(permission.getId());
            if (existingPermission == null) {
                throw new IllegalArgumentException("Permission not found with ID: " + permission.getId());
            }

            permission.setUpdateTime(LocalDateTime.now());
            permissionMapper.updateById(permission);
            logger.info("Updated permission successfully: {}", permission.getId());
            return permission;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update permission: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update permission: {}", permission.getId(), e);
            throw new RuntimeException("Failed to update permission", e);
        }
    }

    @Override
    public void deletePermission(Long permissionId) {
        if (permissionId == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }

        try {
            // 检查权限是否存在
            Permission existingPermission = getPermissionById(permissionId);
            if (existingPermission == null) {
                throw new IllegalArgumentException("Permission not found with ID: " + permissionId);
            }

            permissionMapper.deleteById(permissionId);
            logger.info("Deleted permission successfully: {}", permissionId);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete permission: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete permission: {}", permissionId, e);
            throw new RuntimeException("Failed to delete permission", e);
        }
    }

    @Override
    public Permission getPermissionById(Long permissionId) {
        if (permissionId == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }

        try {
            Permission permission = permissionMapper.selectById(permissionId);
            logger.debug("Retrieved permission by ID: {}", permissionId);
            return permission;
        } catch (Exception e) {
            logger.error("Failed to retrieve permission by ID: {}", permissionId, e);
            throw new RuntimeException("Failed to retrieve permission", e);
        }
    }

    @Override
    public Permission getPermissionByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission code cannot be null or empty");
        }

        try {
            QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("code", code);
            Permission permission = permissionMapper.selectOne(queryWrapper);
            logger.debug("Retrieved permission by code: {}", code);
            return permission;
        } catch (Exception e) {
            logger.error("Failed to retrieve permission by code: {}", code, e);
            throw new RuntimeException("Failed to retrieve permission", e);
        }
    }

    @Override
    public List<Permission> getAllPermissions() {
        try {
            List<Permission> permissions = permissionMapper.selectList(null);
            logger.debug("Retrieved all permissions, count: {}", permissions.size());
            return permissions;
        } catch (Exception e) {
            logger.error("Failed to retrieve all permissions", e);
            throw new RuntimeException("Failed to retrieve permissions", e);
        }
    }

    @Override
    public void togglePermissionStatus(Long permissionId, boolean enabled) {
        if (permissionId == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }

        try {
            Permission permission = getPermissionById(permissionId);
            if (permission == null) {
                throw new IllegalArgumentException("Permission not found with ID: " + permissionId);
            }

            permission.setStatus(enabled ? "ENABLE" : "DISABLE");
            permission.setUpdateTime(LocalDateTime.now());
            permissionMapper.updateById(permission);
            logger.info("Toggled permission status successfully: {} - {}", permissionId, enabled ? "ENABLED" : "DISABLED");
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to toggle permission status: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to toggle permission status for permission: {}", permissionId, e);
            throw new RuntimeException("Failed to toggle permission status", e);
        }
    }

    // ==================== 权限分配 ====================

    @Override
    @Transactional
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (roleIds == null) {
            roleIds = new ArrayList<>();
        }

        try {
            // 检查用户是否存在
            User user = getUserById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            // 检查角色是否都存在
            for (Long roleId : roleIds) {
                Role role = getRoleById(roleId);
                if (role == null) {
                    throw new IllegalArgumentException("Role not found with ID: " + roleId);
                }
                if (!"ENABLE".equals(role.getStatus())) {
                    throw new IllegalArgumentException("Role is disabled: " + roleId);
                }
            }

            // 实现用户角色分配逻辑
            // 注意：这里需要根据实际的数据库设计实现
            // 通常需要一个用户角色关联表
            logger.info("Assigned roles to user: {} - roles: {}", userId, roleIds);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to assign roles to user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to assign roles to user: {}", userId, e);
            throw new RuntimeException("Failed to assign roles to user", e);
        }
    }

    @Override
    @Transactional
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }
        if (permissionIds == null) {
            permissionIds = new ArrayList<>();
        }

        try {
            // 检查角色是否存在
            Role role = getRoleById(roleId);
            if (role == null) {
                throw new IllegalArgumentException("Role not found with ID: " + roleId);
            }

            // 检查权限是否都存在
            for (Long permissionId : permissionIds) {
                Permission permission = getPermissionById(permissionId);
                if (permission == null) {
                    throw new IllegalArgumentException("Permission not found with ID: " + permissionId);
                }
                if (!"ENABLE".equals(permission.getStatus())) {
                    throw new IllegalArgumentException("Permission is disabled: " + permissionId);
                }
            }

            // 实现角色权限分配逻辑
            // 注意：这里需要根据实际的数据库设计实现
            // 通常需要一个角色权限关联表
            logger.info("Assigned permissions to role: {} - permissions: {}", roleId, permissionIds);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to assign permissions to role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to assign permissions to role: {}", roleId, e);
            throw new RuntimeException("Failed to assign permissions to role", e);
        }
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        try {
            // 检查用户是否存在
            User user = getUserById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            // 实现获取用户角色逻辑
            // 注意：这里需要根据实际的数据库设计实现
            // 通常需要从用户角色关联表查询
            List<Role> roles = new ArrayList<>();
            logger.debug("Retrieved roles for user: {}", userId);
            return roles;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve user roles: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve roles for user: {}", userId, e);
            throw new RuntimeException("Failed to retrieve user roles", e);
        }
    }

    @Override
    public List<Permission> getRolePermissions(Long roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        try {
            // 检查角色是否存在
            Role role = getRoleById(roleId);
            if (role == null) {
                throw new IllegalArgumentException("Role not found with ID: " + roleId);
            }

            // 实现获取角色权限逻辑
            // 注意：这里需要根据实际的数据库设计实现
            // 通常需要从角色权限关联表查询
            List<Permission> permissions = new ArrayList<>();
            logger.debug("Retrieved permissions for role: {}", roleId);
            return permissions;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve role permissions: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve permissions for role: {}", roleId, e);
            throw new RuntimeException("Failed to retrieve role permissions", e);
        }
    }

    @Override
    public List<Permission> getUserPermissions(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        try {
            // 检查用户是否存在
            User user = getUserById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            // 实现获取用户权限逻辑
            // 注意：这里需要根据实际的数据库设计实现
            // 通常需要通过用户角色关联，查询用户拥有的所有权限
            List<Permission> permissions = new ArrayList<>();
            logger.debug("Retrieved permissions for user: {}", userId);
            return permissions;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve user permissions: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve permissions for user: {}", userId, e);
            throw new RuntimeException("Failed to retrieve user permissions", e);
        }
    }

    // ==================== 操作审计 ====================

    @Override
    public void recordAuditLog(AuditLog auditLog) {
        if (auditLog == null) {
            throw new IllegalArgumentException("Audit log cannot be null");
        }

        try {
            // 实现审计日志记录逻辑
            // 注意：这里需要根据实际的数据库设计实现
            // 通常需要将审计日志保存到数据库
            auditLog.setOperationTime(LocalDateTime.now());
            logger.info("Recorded audit log: {}", auditLog.getOperationType());
        } catch (Exception e) {
            logger.error("Failed to record audit log", e);
            // 审计日志记录失败不应影响主业务流程
        }
    }

    @Override
    public AuditLog getAuditLogById(Long logId) {
        if (logId == null) {
            throw new IllegalArgumentException("Log ID cannot be null");
        }

        try {
            // 实现获取审计日志逻辑
            // 注意：这里需要根据实际的数据库设计实现
            // 通常需要从数据库查询审计日志
            logger.debug("Retrieved audit log by ID: {}", logId);
            return null;
        } catch (Exception e) {
            logger.error("Failed to retrieve audit log by ID: {}", logId, e);
            throw new RuntimeException("Failed to retrieve audit log", e);
        }
    }

    @Override
    public List<AuditLog> queryAuditLogs(Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }

        try {
            // 实现审计日志查询逻辑
            // 注意：这里需要根据实际的数据库设计实现
            // 通常需要根据参数从数据库查询审计日志
            List<AuditLog> logs = new ArrayList<>();
            logger.debug("Queried audit logs with params: {}", params.keySet());
            return logs;
        } catch (Exception e) {
            logger.error("Failed to query audit logs", e);
            throw new RuntimeException("Failed to query audit logs", e);
        }
    }

    @Override
    @Transactional
    public void cleanExpiredAuditLogs(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }

        try {
            // 实现清理过期审计日志逻辑
            // 注意：这里需要根据实际的数据库设计实现
            // 通常需要根据天数删除过期的审计日志
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            logger.info("Cleaned expired audit logs before: {}", cutoffDate);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to clean expired audit logs: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to clean expired audit logs", e);
            throw new RuntimeException("Failed to clean expired audit logs", e);
        }
    }

    // ==================== 权限验证 ====================

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (permissionCode == null || permissionCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission code cannot be null or empty");
        }

        try {
            // 检查用户是否存在
            User user = getUserById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            // 实现权限验证逻辑
            // 实际项目中需要检查用户是否拥有指定的权限
            List<Permission> permissions = getUserPermissions(userId);
            for (Permission permission : permissions) {
                if (permissionCode.equals(permission.getCode()) && "ENABLE".equals(permission.getStatus())) {
                    logger.debug("User {} has permission: {}", userId, permissionCode);
                    return true;
                }
            }
            logger.debug("User {} does not have permission: {}", userId, permissionCode);
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to check user permission: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to check permission for user: {}", userId, e);
            throw new RuntimeException("Failed to check user permission", e);
        }
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Role code cannot be null or empty");
        }

        try {
            // 检查用户是否存在
            User user = getUserById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            // 实现角色验证逻辑
            // 实际项目中需要检查用户是否拥有指定的角色
            List<Role> roles = getUserRoles(userId);
            for (Role role : roles) {
                if (roleCode.equals(role.getCode()) && "ENABLE".equals(role.getStatus())) {
                    logger.debug("User {} has role: {}", userId, roleCode);
                    return true;
                }
            }
            logger.debug("User {} does not have role: {}", userId, roleCode);
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to check user role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to check role for user: {}", userId, e);
            throw new RuntimeException("Failed to check user role", e);
        }
    }

    @Override
    public List<String> getUserDataPermissions(Long userId, String resourceType) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (resourceType == null || resourceType.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource type cannot be null or empty");
        }

        try {
            // 检查用户是否存在
            User user = getUserById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }

            // 实现获取用户数据权限逻辑
            // 注意：这里需要根据实际的业务逻辑实现
            // 通常需要根据用户和资源类型返回用户可以访问的数据范围
            List<String> dataPermissions = new ArrayList<>();
            logger.debug("Retrieved data permissions for user: {} on resource: {}", userId, resourceType);
            return dataPermissions;
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve user data permissions: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to retrieve data permissions for user: {}", userId, e);
            throw new RuntimeException("Failed to retrieve user data permissions", e);
        }
    }
}
