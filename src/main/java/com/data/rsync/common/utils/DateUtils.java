package com.data.rsync.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期工具类
 */
public class DateUtils {

    /**
     * 默认日期时间格式
     */
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期格式
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认时间格式
     */
    private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /**
     * 格式化日期时间
     * @param dateTime 日期时间
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return formatDateTime(dateTime, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 格式化日期时间
     * @param dateTime 日期时间
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * 解析日期时间
     * @param dateTimeStr 日期时间字符串
     * @return 日期时间对象
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return parseDateTime(dateTimeStr, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 解析日期时间
     * @param dateTimeStr 日期时间字符串
     * @param pattern 格式模式
     * @return 日期时间对象
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    /**
     * 计算两个日期时间之间的差值（毫秒）
     * @param start 开始时间
     * @param end 结束时间
     * @return 差值（毫秒）
     */
    public static long betweenMillis(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MILLIS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的差值（秒）
     * @param start 开始时间
     * @param end 结束时间
     * @return 差值（秒）
     */
    public static long betweenSeconds(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的差值（分钟）
     * @param start 开始时间
     * @param end 结束时间
     * @return 差值（分钟）
     */
    public static long betweenMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 计算两个日期时间之间的差值（小时）
     * @param start 开始时间
     * @param end 结束时间
     * @return 差值（小时）
     */
    public static long betweenHours(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算两个日期时间之间的差值（天）
     * @param start 开始时间
     * @param end 结束时间
     * @return 差值（天）
     */
    public static long betweenDays(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 获取当前日期时间
     * @return 当前日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 向日期时间添加毫秒
     * @param dateTime 日期时间
     * @param millis 毫秒数
     * @return 新的日期时间
     */
    public static LocalDateTime plusMillis(LocalDateTime dateTime, long millis) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plus(millis, ChronoUnit.MILLIS);
    }

    /**
     * 向日期时间添加秒
     * @param dateTime 日期时间
     * @param seconds 秒数
     * @return 新的日期时间
     */
    public static LocalDateTime plusSeconds(LocalDateTime dateTime, long seconds) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plus(seconds, ChronoUnit.SECONDS);
    }

    /**
     * 向日期时间添加分钟
     * @param dateTime 日期时间
     * @param minutes 分钟数
     * @return 新的日期时间
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plus(minutes, ChronoUnit.MINUTES);
    }

    /**
     * 向日期时间添加小时
     * @param dateTime 日期时间
     * @param hours 小时数
     * @return 新的日期时间
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plus(hours, ChronoUnit.HOURS);
    }

    /**
     * 向日期时间添加天
     * @param dateTime 日期时间
     * @param days 天数
     * @return 新的日期时间
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plus(days, ChronoUnit.DAYS);
    }

}
