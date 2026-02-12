package com.data.rsync.common.plugin.desensitize;

import java.util.Map;

/**
 * 脱敏配置
 */
public class DesensitizeConfig {

    private String type;
    private int prefixLength;
    private int suffixLength;
    private String maskChar;
    private int maskLength;
    private Map<String, Object> extraConfig;

    /**
     * 获取脱敏类型
     * @return 脱敏类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置脱敏类型
     * @param type 脱敏类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取前缀保留长度
     * @return 前缀保留长度
     */
    public int getPrefixLength() {
        return prefixLength;
    }

    /**
     * 设置前缀保留长度
     * @param prefixLength 前缀保留长度
     */
    public void setPrefixLength(int prefixLength) {
        this.prefixLength = prefixLength;
    }

    /**
     * 获取后缀保留长度
     * @return 后缀保留长度
     */
    public int getSuffixLength() {
        return suffixLength;
    }

    /**
     * 设置后缀保留长度
     * @param suffixLength 后缀保留长度
     */
    public void setSuffixLength(int suffixLength) {
        this.suffixLength = suffixLength;
    }

    /**
     * 获取掩码字符
     * @return 掩码字符
     */
    public String getMaskChar() {
        return maskChar;
    }

    /**
     * 设置掩码字符
     * @param maskChar 掩码字符
     */
    public void setMaskChar(String maskChar) {
        this.maskChar = maskChar;
    }

    /**
     * 获取掩码长度
     * @return 掩码长度
     */
    public int getMaskLength() {
        return maskLength;
    }

    /**
     * 设置掩码长度
     * @param maskLength 掩码长度
     */
    public void setMaskLength(int maskLength) {
        this.maskLength = maskLength;
    }

    /**
     * 获取额外配置
     * @return 额外配置
     */
    public Map<String, Object> getExtraConfig() {
        return extraConfig;
    }

    /**
     * 设置额外配置
     * @param extraConfig 额外配置
     */
    public void setExtraConfig(Map<String, Object> extraConfig) {
        this.extraConfig = extraConfig;
    }

    /**
     * 获取默认配置
     * @param type 脱敏类型
     * @return 默认配置
     */
    public static DesensitizeConfig getDefaultConfig(String type) {
        DesensitizeConfig config = new DesensitizeConfig();
        config.setType(type);
        config.setMaskChar("*");

        switch (type) {
            case "phone":
                config.setPrefixLength(3);
                config.setSuffixLength(4);
                config.setMaskLength(4);
                break;
            case "idcard":
                config.setPrefixLength(6);
                config.setSuffixLength(4);
                config.setMaskLength(8);
                break;
            case "email":
                config.setPrefixLength(2);
                config.setSuffixLength(0);
                config.setMaskLength(4);
                break;
            case "bankcard":
                config.setPrefixLength(4);
                config.setSuffixLength(4);
                config.setMaskLength(8);
                break;
            default:
                config.setPrefixLength(2);
                config.setSuffixLength(2);
                config.setMaskLength(4);
                break;
        }

        return config;
    }

}
