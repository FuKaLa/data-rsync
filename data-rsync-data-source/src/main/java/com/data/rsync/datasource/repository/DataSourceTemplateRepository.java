package com.data.rsync.datasource.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.data.rsync.datasource.entity.DataSourceTemplateEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据源模板仓库接口
 */
@Mapper
public interface DataSourceTemplateRepository extends BaseMapper<DataSourceTemplateEntity> {

    /**
     * 根据数据源类型查询模板
     * @param dataSourceType 数据源类型
     * @return 模板列表
     */
    List<DataSourceTemplateEntity> findByDataSourceType(String dataSourceType);

    /**
     * 查询系统预设模板
     * @param isSystem 是否为系统预设
     * @return 模板列表
     */
    List<DataSourceTemplateEntity> findByIsSystem(Boolean isSystem);

    /**
     * 根据名称查询模板
     * @param name 模板名称
     * @return 模板实体
     */
    DataSourceTemplateEntity findByName(String name);

}
