package com.data.rsync.common.plugin.desensitize;

/**
 * 数据脱敏插件接口
 */
public interface DesensitizePlugin {

    /**
     * 获取脱敏插件名称
     * @return 脱敏插件名称
     */
    String getName();

    /**
     * 获取脱敏类型
     * @return 脱敏类型
     */
    String getType();

    /**
     * 脱敏处理
     * @param data 原始数据
     * @param config 脱敏配置
     * @return 脱敏后的数据
     */
    String desensitize(String data, DesensitizeConfig config);

    /**
     * 检查是否支持该类型的脱敏
     * @param type 脱敏类型
     * @return 是否支持
     */
    boolean supports(String type);

}
