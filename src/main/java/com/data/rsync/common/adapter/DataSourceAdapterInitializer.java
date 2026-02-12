package com.data.rsync.common.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * 数据源适配器初始化器
 * 在应用启动时初始化数据源适配器
 */
@Component
public class DataSourceAdapterInitializer implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(DataSourceAdapterInitializer.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initializing data source adapters...");
        
        // 初始化默认适配器
        DataSourceAdapterFactory.initDefaultAdapters();
        
        log.info("Data source adapter initialization completed");
        log.info("Supported data source types: {}", DataSourceAdapterFactory.getSupportedTypes());
    }
}
