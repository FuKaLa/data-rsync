package com.data.rsync.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.auth.model.User;

import java.util.List;

/**
 * 用户Mapper接口
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User selectByUsername(String username);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    User selectByEmail(String email);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    User selectByPhone(String phone);

    /**
     * 根据条件查询用户
     * @param condition 条件
     * @return 用户列表
     */
    List<User> selectByCondition(String condition);

    /**
     * 分页查询用户
     * @param page 页码
     * @param size 每页大小
     * @return 用户列表
     */
    List<User> selectByPage(int page, int size);

    /**
     * 删除用户角色关联
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteUserRoles(Long userId);

    /**
     * 插入用户角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    int insertUserRole(Long userId, Long roleId);

    /**
     * 删除用户角色关联
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteUserRole(Long userId, Long roleId);

    /**
     * 删除用户权限关联
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteUserPermissions(Long userId);

    /**
     * 插入用户权限关联
     * @param userId 用户ID
     * @param permissionId 权限ID
     * @return 影响行数
     */
    int insertUserPermission(Long userId, Long permissionId);

    /**
     * 删除用户权限关联
     * @param userId 用户ID
     * @param permissionId 权限ID
     * @return 影响行数
     */
    int deleteUserPermission(Long userId, Long permissionId);
}
