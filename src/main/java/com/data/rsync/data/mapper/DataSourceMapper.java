package com.data.rsync.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.data.entity.DataSourceEntity;

import java.util.List;

/**
 * 数据源Mapper接口
 */
public interface DataSourceMapper extends BaseMapper<DataSourceEntity> {

    /**
     * 根据数据源名称查询数据源
     * @param name 数据源名称
     * @return 数据源信息
     */
    DataSourceEntity selectByName(String name);

    /**
     * 根据数据源类型查询数据源列表
     * @param type 数据源类型
     * @return 数据源列表
     */
    List<DataSourceEntity> selectByType(String type);

    /**
     * 根据数据源状态查询数据源列表
     * @param status 数据源状态
     * @return 数据源列表
     */
    List<DataSourceEntity> selectByStatus(String status);

    /**
     * 更新数据源状态
     * @param id 数据源ID
     * @param status 数据源状态
     * @return 更新结果
     */
    int updateStatus(Long id, String status);
}
