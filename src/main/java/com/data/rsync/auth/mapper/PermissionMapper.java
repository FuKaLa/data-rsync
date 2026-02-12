package com.data.rsync.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.auth.model.Permission;

import java.util.List;

/**
 * 权限Mapper接口
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色ID查询权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> selectByRoleId(Long roleId);

    /**
     * 根据权限名称查询权限
     * @param name 权限名称
     * @return 权限信息
     */
    Permission selectByName(String name);

    /**
     * 根据权限代码查询权限
     * @param code 权限代码
     * @return 权限信息
     */
    Permission selectByCode(String code);

    /**
     * 根据用户ID查询权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> selectByUserId(Long userId);
}
