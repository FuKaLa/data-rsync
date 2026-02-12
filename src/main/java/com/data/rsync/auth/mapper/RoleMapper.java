package com.data.rsync.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.auth.model.Role;

import java.util.List;

/**
 * 角色Mapper接口
 */
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> selectByUserId(Long userId);

    /**
     * 根据角色名称查询角色
     * @param name 角色名称
     * @return 角色信息
     */
    Role selectByName(String name);

    /**
     * 根据角色代码查询角色
     * @param code 角色代码
     * @return 角色信息
     */
    Role selectByCode(String code);
}
