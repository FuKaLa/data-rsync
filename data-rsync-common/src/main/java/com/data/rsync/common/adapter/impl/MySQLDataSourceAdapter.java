package com.data.rsync.common.adapter.impl;

import com.data.rsync.common.adapter.AbstractDataSourceAdapter;
import com.data.rsync.common.model.DataSource;

/**
 * MySQL 数据源适配器
 */
public class MySQLDataSourceAdapter extends AbstractDataSourceAdapter {

    @Override
    public String getDataSourceType() {
        return "MYSQL";
    }

    @Override
    protected String getDriverClassName(DataSource dataSource) {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    protected String buildConnectionUrl(DataSource dataSource) {
        StringBuilder url = new StringBuilder("jdbc:mysql://");
        url.append(dataSource.getHost()).append(":").append(dataSource.getPort());
        if (dataSource.getDatabase() != null && !dataSource.getDatabase().isEmpty()) {
            url.append("/").append(dataSource.getDatabase());
        }
        url.append("?useSSL=false&useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC");
        return url.toString();
    }

}
