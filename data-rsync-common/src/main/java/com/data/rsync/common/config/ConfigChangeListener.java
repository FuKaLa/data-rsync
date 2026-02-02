package com.data.rsync.common.config;

import com.alibaba.nacos.api.config.listener.Listener;

import java.util.concurrent.Executor;

/**
 * 配置变更监听器
 */
public interface ConfigChangeListener extends Listener {

    /**
     * 接收配置变更通知
     * @param configInfo 配置信息
     */
    void receiveConfigChange(String configInfo);

    /**
     * 获取执行器
     * @return 执行器
     */
    @Override
    default Executor getExecutor() {
        return null;
    }

    /**
     * 接收配置变更通知
     * @param configInfo 配置信息
     */
    @Override
    default void receiveConfigInfo(String configInfo) {
        receiveConfigChange(configInfo);
    }

}
