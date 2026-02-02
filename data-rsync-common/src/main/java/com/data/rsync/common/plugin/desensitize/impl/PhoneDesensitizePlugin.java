package com.data.rsync.common.plugin.desensitize.impl;

import com.data.rsync.common.plugin.desensitize.DesensitizeConfig;
import com.data.rsync.common.plugin.desensitize.DesensitizePlugin;

/**
 * 手机号脱敏插件
 */
public class PhoneDesensitizePlugin implements DesensitizePlugin {

    private static final String NAME = "phone-desensitize-plugin";
    private static final String TYPE = "phone";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String desensitize(String data, DesensitizeConfig config) {
        if (data == null || data.isEmpty() || data.length() < 11) {
            return data;
        }

        int prefixLength = config.getPrefixLength();
        int suffixLength = config.getSuffixLength();
        String maskChar = config.getMaskChar();
        int maskLength = config.getMaskLength();

        if (prefixLength + suffixLength >= data.length()) {
            return data;
        }

        StringBuilder result = new StringBuilder();
        result.append(data.substring(0, prefixLength));
        result.append(maskChar.repeat(maskLength));
        result.append(data.substring(data.length() - suffixLength));

        return result.toString();
    }

    @Override
    public boolean supports(String type) {
        return TYPE.equalsIgnoreCase(type);
    }

}
