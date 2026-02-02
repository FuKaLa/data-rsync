package com.data.rsync.auth.service.impl;

import com.data.rsync.auth.model.User;
import com.data.rsync.auth.model.Role;
import com.data.rsync.auth.model.Permission;
import com.data.rsync.auth.model.AuditLog;
import com.data.rsync.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 权限管理服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== 用户管理 ====================

    @Override
    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        entityManager.persist(user);
        
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
        entityManager.persist(auditLog);
        
        return user;
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        User existingUser = entityManager.find(User.class, user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setStatus(user.getStatus());
        existingUser.setUpdateTime(LocalDateTime.now());
        entityManager.merge(existingUser);
        
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
        entityManager.persist(auditLog);
        
        return existingUser;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = entityManager.find(User.class, userId);
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
        entityManager.persist(auditLog);
        
        entityManager.remove(user);
    }

    @Override
    public User getUserById(Long userId) {
        return entityManager.find(User.class, userId);
    }

    @Override
    public User getUserByUsername(String username) {
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username");
        query.setParameter("username", username);
        List<User> users = query.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public List<User> getAllUsers() {
        Query query = entityManager.createQuery("SELECT u FROM User u");
        return query.getResultList();
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        entityManager.merge(user);
        
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
        entityManager.persist(auditLog);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId, boolean enabled) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStatus(enabled ? "ENABLE" : "DISABLE");
        user.setUpdateTime(LocalDateTime.now());
        entityManager.merge(user);
        
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
        entityManager.persist(auditLog);
    }

    // ==================== 角色管理 ====================

    @Override
    @Transactional
    public Role createRole(Role role) {
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        entityManager.persist(role);
        
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
        entityManager.persist(auditLog);
        
        return role;
    }

    @Override
    @Transactional
    public Role updateRole(Role role) {
        Role existingRole = entityManager.find(Role.class, role.getId());
        if (existingRole == null) {
            throw new RuntimeException("角色不存在");
        }
        
        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());
        existingRole.setStatus(role.getStatus());
        existingRole.setUpdateTime(LocalDateTime.now());
        entityManager.merge(existingRole);
        
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
        entityManager.persist(auditLog);
        
        return existingRole;
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = entityManager.find(Role.class, roleId);
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
        entityManager.persist(auditLog);
        
        entityManager.remove(role);
    }

    @Override
    public Role getRoleById(Long roleId) {
        return entityManager.find(Role.class, roleId);
    }

    @Override
    public Role getRoleByCode(String code) {
        Query query = entityManager.createQuery("SELECT r FROM Role r WHERE r.code = :code");
        query.setParameter("code", code);
        List<Role> roles = query.getResultList();
        return roles.isEmpty() ? null : roles.get(0);
    }

    @Override
    public List<Role> getAllRoles() {
        Query query = entityManager.createQuery("SELECT r FROM Role r");
        return query.getResultList();
    }

    @Override
    @Transactional
    public void toggleRoleStatus(Long roleId, boolean enabled) {
        Role role = entityManager.find(Role.class, roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        
        role.setStatus(enabled ? "ENABLE" : "DISABLE");
        role.setUpdateTime(LocalDateTime.now());
        entityManager.merge(role);
        
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
        entityManager.persist(auditLog);
    }

    // ==================== 权限管理 ====================

    @Override
    @Transactional
    public Permission createPermission(Permission permission) {
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        entityManager.persist(permission);
        
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
        entityManager.persist(auditLog);
        
        return permission;
    }

    @Override
    @Transactional
    public Permission updatePermission(Permission permission) {
        Permission existingPermission = entityManager.find(Permission.class, permission.getId());
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
        entityManager.merge(existingPermission);
        
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
        entityManager.persist(auditLog);
        
        return existingPermission;
    }

    @Override
    @Transactional
    public void deletePermission(Long permissionId) {
        Permission permission = entityManager.find(Permission.class, permissionId);
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
        entityManager.persist(auditLog);
        
        entityManager.remove(permission);
    }

    @Override
    public Permission getPermissionById(Long permissionId) {
        return entityManager.find(Permission.class, permissionId);
    }

    @Override
    public Permission getPermissionByCode(String code) {
        Query query = entityManager.createQuery("SELECT p FROM Permission p WHERE p.code = :code");
        query.setParameter("code", code);
        List<Permission> permissions = query.getResultList();
        return permissions.isEmpty() ? null : permissions.get(0);
    }

    @Override
    public List<Permission> getAllPermissions() {
        Query query = entityManager.createQuery("SELECT p FROM Permission p ORDER BY p.sort");
        return query.getResultList();
    }

    @Override
    @Transactional
    public void togglePermissionStatus(Long permissionId, boolean enabled) {
        Permission permission = entityManager.find(Permission.class, permissionId);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }
        
        permission.setStatus(enabled ? "ENABLE" : "DISABLE");
        permission.setUpdateTime(LocalDateTime.now());
        entityManager.merge(permission);
        
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
        entityManager.persist(auditLog);
    }

    // ==================== 权限分配 ====================

    @Override
    @Transactional
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // 先删除用户的所有角色
        Query deleteQuery = entityManager.createNativeQuery("DELETE FROM user_role WHERE user_id = :userId");
        deleteQuery.setParameter("userId", userId);
        deleteQuery.executeUpdate();
        
        // 再添加新角色
        for (Long roleId : roleIds) {
            Query insertQuery = entityManager.createNativeQuery("INSERT INTO user_role (user_id, role_id) VALUES (:userId, :roleId)");
            insertQuery.setParameter("userId", userId);
            insertQuery.setParameter("roleId", roleId);
            insertQuery.executeUpdate();
        }
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType("ASSIGN_ROLES");
        auditLog.setModule("AUTH");
        auditLog.setDescription("给用户分配角色: 用户ID=" + userId + ", 角色数=" + roleIds.size());
        auditLog.setObjectName("UserRole");
        auditLog.setObjectId(userId);
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        entityManager.persist(auditLog);
    }

    @Override
    @Transactional
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        // 先删除角色的所有权限
        Query deleteQuery = entityManager.createNativeQuery("DELETE FROM role_permission WHERE role_id = :roleId");
        deleteQuery.setParameter("roleId", roleId);
        deleteQuery.executeUpdate();
        
        // 再添加新权限
        for (Long permissionId : permissionIds) {
            Query insertQuery = entityManager.createNativeQuery("INSERT INTO role_permission (role_id, permission_id) VALUES (:roleId, :permissionId)");
            insertQuery.setParameter("roleId", roleId);
            insertQuery.setParameter("permissionId", permissionId);
            insertQuery.executeUpdate();
        }
        
        // 记录审计日志
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationType("ASSIGN_PERMISSIONS");
        auditLog.setModule("AUTH");
        auditLog.setDescription("给角色分配权限: 角色ID=" + roleId + ", 权限数=" + permissionIds.size());
        auditLog.setObjectName("RolePermission");
        auditLog.setObjectId(roleId);
        auditLog.setResult("SUCCESS");
        auditLog.setOperationTime(LocalDateTime.now());
        auditLog.setIp("127.0.0.1");
        entityManager.persist(auditLog);
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        Query query = entityManager.createNativeQuery(
            "SELECT r.* FROM role r JOIN user_role ur ON r.id = ur.role_id WHERE ur.user_id = :userId", Role.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    @Override
    public List<Permission> getRolePermissions(Long roleId) {
        Query query = entityManager.createNativeQuery(
            "SELECT p.* FROM permission p JOIN role_permission rp ON p.id = rp.permission_id WHERE rp.role_id = :roleId", Permission.class);
        query.setParameter("roleId", roleId);
        return query.getResultList();
    }

    @Override
    public List<Permission> getUserPermissions(Long userId) {
        Query query = entityManager.createNativeQuery(
            "SELECT DISTINCT p.* FROM permission p " +
            "JOIN role_permission rp ON p.id = rp.permission_id " +
            "JOIN user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = :userId", Permission.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    // ==================== 操作审计 ====================

    @Override
    @Transactional
    public void recordAuditLog(AuditLog auditLog) {
        auditLog.setOperationTime(LocalDateTime.now());
        entityManager.persist(auditLog);
    }

    @Override
    public AuditLog getAuditLogById(Long logId) {
        return entityManager.find(AuditLog.class, logId);
    }

    @Override
    public List<AuditLog> queryAuditLogs(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("SELECT a FROM AuditLog a WHERE 1=1");
        
        if (params.containsKey("userId")) {
            sql.append(" AND a.userId = :userId");
        }
        if (params.containsKey("username")) {
            sql.append(" AND a.username LIKE :username");
        }
        if (params.containsKey("operationType")) {
            sql.append(" AND a.operationType = :operationType");
        }
        if (params.containsKey("module")) {
            sql.append(" AND a.module = :module");
        }
        if (params.containsKey("result")) {
            sql.append(" AND a.result = :result");
        }
        if (params.containsKey("startTime")) {
            sql.append(" AND a.operationTime >= :startTime");
        }
        if (params.containsKey("endTime")) {
            sql.append(" AND a.operationTime <= :endTime");
        }
        
        sql.append(" ORDER BY a.operationTime DESC");
        
        Query query = entityManager.createQuery(sql.toString());
        
        if (params.containsKey("userId")) {
            query.setParameter("userId", params.get("userId"));
        }
        if (params.containsKey("username")) {
            query.setParameter("username", "%" + params.get("username") + "%");
        }
        if (params.containsKey("operationType")) {
            query.setParameter("operationType", params.get("operationType"));
        }
        if (params.containsKey("module")) {
            query.setParameter("module", params.get("module"));
        }
        if (params.containsKey("result")) {
            query.setParameter("result", params.get("result"));
        }
        if (params.containsKey("startTime")) {
            query.setParameter("startTime", params.get("startTime"));
        }
        if (params.containsKey("endTime")) {
            query.setParameter("endTime", params.get("endTime"));
        }
        
        if (params.containsKey("page") && params.containsKey("size")) {
            int page = (int) params.get("page");
            int size = (int) params.get("size");
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
        }
        
        return query.getResultList();
    }

    @Override
    @Transactional
    public void cleanExpiredAuditLogs(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        Query query = entityManager.createQuery("DELETE FROM AuditLog a WHERE a.operationTime < :cutoffDate");
        query.setParameter("cutoffDate", cutoffDate);
        query.executeUpdate();
    }

    // ==================== 权限验证 ====================

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        Query query = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM permission p " +
            "JOIN role_permission rp ON p.id = rp.permission_id " +
            "JOIN user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = :userId AND p.code = :permissionCode AND p.status = 1");
        query.setParameter("userId", userId);
        query.setParameter("permissionCode", permissionCode);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        Query query = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM role r " +
            "JOIN user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = :userId AND r.code = :roleCode AND r.status = 1");
        query.setParameter("userId", userId);
        query.setParameter("roleCode", roleCode);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }

    @Override
    public List<String> getUserDataPermissions(Long userId, String resourceType) {
        // 这里实现数据权限的获取逻辑
        // 例如，根据用户ID和资源类型获取用户可以访问的数据范围
        Query query = entityManager.createNativeQuery(
            "SELECT resource_id FROM user_data_permission " +
            "WHERE user_id = :userId AND resource_type = :resourceType");
        query.setParameter("userId", userId);
        query.setParameter("resourceType", resourceType);
        List<String> resourceIds = query.getResultList();
        return resourceIds != null ? resourceIds : new ArrayList<>();
    }
}
