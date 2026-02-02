package com.data.rsync.datasource.repository;

import com.data.rsync.datasource.entity.DataSourceDiagnoseReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据源诊断报告仓库接口
 */
@Repository
public interface DataSourceDiagnoseReportRepository extends JpaRepository<DataSourceDiagnoseReportEntity, Long> {

    /**
     * 根据数据源ID查询诊断报告
     * @param dataSourceId 数据源ID
     * @return 诊断报告列表
     */
    List<DataSourceDiagnoseReportEntity> findByDataSourceId(Long dataSourceId);

    /**
     * 根据数据源ID查询最新的诊断报告
     * @param dataSourceId 数据源ID
     * @return 最新的诊断报告
     */
    DataSourceDiagnoseReportEntity findTopByDataSourceIdOrderByDiagnoseTimeDesc(Long dataSourceId);

}
