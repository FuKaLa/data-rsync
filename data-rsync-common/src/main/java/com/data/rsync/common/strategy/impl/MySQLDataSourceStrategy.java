package com.data.rsync.common.strategy.impl;

import com.data.rsync.common.constants.DataRsyncConstants;
import com.data.rsync.common.strategy.AbstractDataSourceStrategy;

/**
 * MySQL数据源策略实现类
 */
public class MySQLDataSourceStrategy extends AbstractDataSourceStrategy {

    @Override
    public String getDataSourceType() {
        return DataRsyncConstants.DataSourceType.MYSQL;
    }
}
