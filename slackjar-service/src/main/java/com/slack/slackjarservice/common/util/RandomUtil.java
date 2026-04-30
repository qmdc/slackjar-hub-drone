package com.slack.slackjarservice.common.util;

import com.slack.slackjarservice.common.config.LongIdGenerator;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 随机数工具类
 * 提供UUID生成、时间戳格式化等常用随机数相关功能
 */
@Slf4j
public class RandomUtil {

    /**
     * 获取不带分隔符的UUID
     * 例如：550e8400e29b41d4a716446655440000
     *
     * @return 32位无分隔符的UUID字符串
     */
    public static String getSimpleUUID() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    /**
     * 获取带分隔符的标准UUID
     * 例如：550e8400-e29b-41d4-a716-446655440000
     *
     * @return 36位标准UUID字符串
     */
    public static String getStandardUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取yyyyMMddHHmmssSSS格式的毫秒级时间戳字符串
     * 例如：20241219153045123
     *
     * @return 17位毫秒级时间戳字符串
     */
    public static String getCurrentMillisStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    /**
     * 获取yyyyMMddHHmmss格式的秒级时间戳字符串
     * 例如：20241219153045
     *
     * @return 14位秒级时间戳字符串
     */
    public static String getCurrentSecondStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * 生成指定长度的随机数字字符串
     *
     * @param length 随机数字字符串长度
     * @return 指定长度的随机数字字符串
     */
    public static String getRandomDigits(int length) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    /**
     * 生成指定长度的随机字母数字字符串
     *
     * @param length 随机字符串长度
     * @return 指定长度的随机字母数字字符串
     */
    public static String getRandomAlphanumeric(int length) {
        if (length <= 0) {
            return "";
        }
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成订单号（基于时间戳+随机数）
     * 格式：yyyyMMddHHmmssSSS + 6位随机数
     * 例如：2024121915304512345678
     *
     * @return 20位订单号字符串
     */
    public static String generateOrderNo() {
        return getCurrentMillisStr() + getRandomDigits(6);
    }

    /**
     * 生成邀请码（6位随机数字）
     *
     * @return 6位随机数字邀请码
     */
    public static String generateInviteCode() {
        return getRandomDigits(6);
    }

    /**
     * 生成验证码（4位随机数字）
     *
     * @return 4位随机数字验证码
     */
    public static String generateVerificationCode() {
        return getRandomDigits(4);
    }

    /**
     * 生成临时文件名（UUID + 时间戳）
     *
     * @param extension 文件扩展名
     * @return 临时文件名
     */
    public static String generateTempFileName(String extension) {
        String fileName = getCurrentMillisStr() + "_" + getSimpleUUID();
        if (extension != null && !extension.isEmpty()) {
            if (!extension.startsWith(".")) {
                fileName += ".";
            }
            fileName += extension;
        }
        return fileName;
    }

    /**
     * 获取画布ID
     */
    public static String getCanvasId(Long userId) {
        return getCurrentSecondStr() + "-" + userId;
    }

    /**
     * 生成Long类型的唯一ID
     * 使用雪花算法，确保ID不重复
     *
     * @return Long类型的唯一ID
     */
    public static Long generateLongId() {
        return LongIdGenerator.getInstance().nextId();
    }

    /**
     * 生成 TraceId
     * 格式：16位随机字符（简短且唯一）
     *
     * @return 16位 TraceId 字符串
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
