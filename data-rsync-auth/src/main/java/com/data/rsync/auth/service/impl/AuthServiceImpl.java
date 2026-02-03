package com.data.rsync.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.data.rsync.auth.mapper.UserMapper;
import com.data.rsync.auth.mapper.RoleMapper;
import com.data.rsync.auth.mapper.PermissionMapper;
import com.data.rsync.auth.mapper.AuditLogMapper;
import com.data.rsync.auth.model.User;
import com.data.rsync.auth.model.Role;
import com.data.rsync.auth.model.Permission;
import com.data.rsync.auth.model.AuditLog;
import com.data.rsync.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 权限管理服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private AuditLogMapper auditLogMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== 用户管理 ====================

    @Override
    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(user.getId());
        auditLog.setUsername(user.getUsername());
        auditLog.setOperationType("CREATE_USER");
        auditLog.setModule("AUTH");
        auditLog.setDescription("创建用户: " + user.getUsername());
        auditLog.setObjectName("User");
        auditLog.setObjectId(user.getId());
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
        
        return user;
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setStatus(user.getStatus());
        existingUser.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(existingUser);
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(existingUser.getId());
        auditLog.setUsername(existingUser.getUsername());
        auditLog.setOperationType("UPDATE_USER");
        auditLog.setModule("AUTH");
        auditLog.setDescription("更新用户: " + existingUser.getUsername());
        auditLog.setObjectName("User");
        auditLog.setObjectId(existingUser.getId());
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
        
        return existingUser;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(user.getUsername());
        auditLog.setOperationType("DELETE_USER");
        auditLog.setModule("AUTH");
        auditLog.setDescription("删除用户: " + user.getUsername());
        auditLog.setObjectName("User");
        auditLog.setObjectId(userId);
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
        
        userMapper.deleteById(userId);
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(user.getUsername());
        auditLog.setOperationType("RESET_PASSWORD");
        auditLog.setModule("AUTH");
        auditLog.setDescription("重置用户密码: " + user.getUsername());
        auditLog.setObjectName("User");
        auditLog.setObjectId(userId);
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId, boolean enabled) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStatus(enabled ? "ENABLE" : "DISABLE");
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(user.getUsername());
        auditLog.setOperationType(enabled ? "ENABLE_USER" : "DISABLE_USER");
        auditLog.setModule("AUTH");
        auditLog.setDescription((enabled ? "启用" : "禁用") + "用户: " + user.getUsername());
        auditLog.setObjectName("User");
        auditLog.setObjectId(userId);
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
    }

    // ==================== 角色管理 ====================

    @Override
    @Transactional
    public Role createRole(Role role) {
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(role);
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType("CREATE_ROLE");
        auditLog.setModule("AUTH");
        auditLog.setDescription("创建角色: " + role.getName());
        auditLog.setObjectName("Role");
        auditLog.setObjectId(role.getId());
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
        
        return role;
    }

    @Override
    @Transactional
    public Role updateRole(Role role) {
        Role existingRole = roleMapper.selectById(role.getId());
        if (existingRole == null) {
            throw new RuntimeException("角色不存在");
        }
        
        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());
        existingRole.setStatus(role.getStatus());
        existingRole.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(existingRole);
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType("UPDATE_ROLE");
        auditLog.setModule("AUTH");
        auditLog.setDescription("更新角色: " + existingRole.getName());
        auditLog.setObjectName("Role");
        auditLog.setObjectId(existingRole.getId());
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
        
        return existingRole;
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType("DELETE_ROLE");
        auditLog.setModule("AUTH");
        auditLog.setDescription("删除角色: " + role.getName());
        auditLog.setObjectName("Role");
        auditLog.setObjectId(roleId);
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
        
        roleMapper.deleteById(roleId);
    }

    @Override
    public Role getRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    public Role getRoleByCode(String code) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        return roleMapper.selectOne(queryWrapper);
    }

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.selectList(null);
    }

    @Override
    @Transactional
    public void toggleRoleStatus(Long roleId, boolean enabled) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        role.setStatus(enabled ? "ENABLE" : "DISABLE");
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType(enabled ? "ENABLE_ROLE" : "DISABLE_ROLE");
        auditLog.setModule("AUTH");
        auditLog.setDescription((enabled ? "启用" : "禁用") + "角色: " + role.getName());
        auditLog.setObjectName("Role");
        auditLog.setObjectId(roleId);
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
    }

    // ==================== 权限管理 ====================

    @Override
    @Transactional
    public Permission createPermission(Permission permission) {
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        permissionMapper.insert(permission);
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType("CREATE_PERMISSION");
        auditLog.setModule("AUTH");
        auditLog.setDescription("创建权限: " + permission.getName());
        auditLog.setObjectName("Permission");
        auditLog.setObjectId(permission.getId());
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
        
        return permission;
    }

    @Override
    @Transactional
    public Permission updatePermission(Permission permission) {
        Permission existingPermission = permissionMapper.selectById(permission.getId());
        if (existingPermission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        existingPermission.setName(permission.getName());
        existingPermission.setDescription(permission.getDescription());
        existingPermission.setResourcePath(permission.getResourcePath());
        existingPermission.setParentId(permission.getParentId());
        existingPermission.setSort(permission.getSort());
        existingPermission.setStatus(permission.getStatus());
        existingPermission.setUpdateTime(LocalDateTime.now());
        permissionMapper.updateById(existingPermission);
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType("UPDATE_PERMISSION");
        auditLog.setModule("AUTH");
        auditLog.setDescription("更新权限: " + existingPermission.getName());
        auditLog.setObjectName("Permission");
        auditLog.setObjectId(existingPermission.getId());
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
        
        return existingPermission;
    }

    @Override
    @Transactional
    public void deletePermission(Long permissionId) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType("DELETE_PERMISSION");
        auditLog.setModule("AUTH");
        auditLog.setDescription("删除权限: " + permission.getName());
        auditLog.setObjectName("Permission");
        auditLog.setObjectId(permissionId);
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
        
        permissionMapper.deleteById(permissionId);
    }

    @Override
    public Permission getPermissionById(Long permissionId) {
        return permissionMapper.selectById(permissionId);
    }

    @Override
    public Permission getPermissionByCode(String code) {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        return permissionMapper.selectOne(queryWrapper);
    }

    @Override
    public List<Permission> getAllPermissions() {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        return permissionMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void togglePermissionStatus(Long permissionId, boolean enabled) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        permission.setStatus(enabled ? "ENABLE" : "DISABLE");
        permission.setUpdateTime(LocalDateTime.now());
        permissionMapper.updateById(permission);
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType(enabled ? "ENABLE_PERMISSION" : "DISABLE_PERMISSION");
        auditLog.setModule("AUTH");
        auditLog.setDescription((enabled ? "启用" : "禁用") + "权限: " + permission.getName());
        auditLog.setObjectName("Permission");
        auditLog.setObjectId(permissionId);
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        auditLogMapper.insert(auditLog);
    }

    // ==================== 权限分配 ====================

    @Override
    @Transactional
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // 这里需要在 UserMapper 中添加自定义方法来处理用户角色关联
        // 暂时留空，后续需要实现
    }

    @Override
    @Transactional
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 这里需要在 RoleMapper 中添加自定义方法来处理角色权限关联
        // 暂时留空，后续需要实现
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        // 这里需要在 UserMapper 中添加自定义方法来获取用户角色
        // 暂时返回空列表，后续需要实现
        return new ArrayList<>();
    }

    @Override
    public List<Permission> getRolePermissions(Long roleId) {
        // 这里需要在 RoleMapper 中添加自定义方法来获取角色权限
        // 暂时返回空列表，后续需要实现
        return new ArrayList<>();
    }

    @Override
    public List<Permission> getUserPermissions(Long userId) {
        // 这里需要在 UserMapper 中添加自定义方法来获取用户权限
        // 暂时返回空列表，后续需要实现
        return new ArrayList<>();
    }

    // ==================== 操作审计 ====================

    @Override
    @Transactional
    public void recordAuditLog(AuditLog auditLog) {
        auditLog.setOperationTime(LocalDateTime.now());
        auditLogMapper.insert(auditLog);
    }

    @Override
    public AuditLog getAuditLogById(Long logId) {
        return auditLogMapper.selectById(logId);
    }

    @Override
    public List<AuditLog> queryAuditLogs(Map<String, Object> params) {
        QueryWrapper<AuditLog> queryWrapper = new QueryWrapper<>();
        
        if (params.containsKey("userId")) {
            queryWrapper.eq("user_id", params.get("userId"));
        }
        if (params.containsKey("username")) {
            queryWrapper.like("username", params.get("username"));
        }
        if (params.containsKey("operationType")) {
            queryWrapper.eq("operation_type", params.get("operationType"));
        }
        if (params.containsKey("module")) {
            queryWrapper.eq("module", params.get("module"));
        }
        if (params.containsKey("result")) {
            queryWrapper.eq("result", params.get("result"));
        }
        if (params.containsKey("startTime")) {
            queryWrapper.ge("operation_time", params.get("startTime"));
        }
        if (params.containsKey("endTime")) {
            queryWrapper.le("operation_time", params.get("endTime"));
        }
        
        queryWrapper.orderByDesc("operation_time");
        
        if (params.containsKey("page") && params.containsKey("size")) {
            int page = (int) params.get("page");
            int size = (int) params.get("size");
            // 这里需要使用分页插件，暂时留空
        }
        
        return auditLogMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void cleanExpiredAuditLogs(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        QueryWrapper<AuditLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("operation_time", cutoffDate);
        auditLogMapper.delete(queryWrapper);
    }

    // ==================== 权限验证 ====================

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        // 这里需要在 UserMapper 中添加自定义方法来验证权限
        // 暂时返回 false，后续需要实现
        return false;
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        // 这里需要在 UserMapper 中添加自定义方法来验证角色
        // 暂时返回 false，后续需要实现
        return false;
    }

    @Override
    public List<String> getUserDataPermissions(Long userId, String resourceType) {
        // 这里实现数据权限的获取逻辑
        // 例如，根据用户ID和资源类型获取用户可以访问的数据范围
        // 暂时返回空列表，后续需要实现
        return new ArrayList<>();
    }
}
