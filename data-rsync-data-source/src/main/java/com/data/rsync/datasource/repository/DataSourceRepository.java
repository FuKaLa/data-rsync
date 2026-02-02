package com.data.rsync.datasource.repository;

import com.data.rsync.datasource.entity.DataSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据源仓库接口
 */
@Repository
public interface DataSourceRepository extends JpaRepository<DataSourceEntity, Long> {

    /**
     * 根据名称查找数据源
     * @param name 数据源名称
     * @return 数据源实体
     */
    DataSourceEntity findByName(String name);

    /**
     * 根据类型查找数据源
     * @param type 数据源类型
     * @return 数据源列表
     */
    List<DataSourceEntity> findByType(String type);

    /**
     * 根据状态查找数据源
     * @param enabled 启用状态
     * @return 数据源列表
     */
    List<DataSourceEntity> findByEnabled(Boolean enabled);

    /**
     * 检查名称是否存在
     * @param name 数据源名称
     * @return 是否存在
     */
    boolean existsByName(String name);

}
