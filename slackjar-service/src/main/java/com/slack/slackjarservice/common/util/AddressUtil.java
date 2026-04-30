package com.slack.slackjarservice.common.util;

import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 地址工具类
 */
@Slf4j
public class AddressUtil {

    /**
     * 获取客户端IP地址
     *
     * @param request HttpServletRequest对象
     * @return IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理情况下，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 从当前请求上下文中获取客户端IP地址
     *
     * @return IP地址
     */
    public static String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            AssertUtil.notNull(attributes, ResponseEnum.NOT_FOUND);
            HttpServletRequest request = attributes.getRequest();
            return getClientIp(request);
        } catch (Exception e) {
            log.warn("获取客户端IP地址失败", e);
            return "0.0.0.0";
        }
    }

    /**
     * 从当前请求上下文中获取客户端URI地址
     *
     * @return IP地址
     */
    public static String getClientURI() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            AssertUtil.notNull(attributes, ResponseEnum.NOT_FOUND);
            HttpServletRequest request = attributes.getRequest();
            return request.getRequestURI();
        } catch (Exception e) {
            log.warn("获取客户端URI地址失败", e);
            return "";
        }
    }

    /**
     * 从 User-Agent 中解析设备类型
     *
     * @return 设备类型（PC/MAC/MOBILE/UNKNOWN）
     */
    public static String getDeviceType() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            AssertUtil.notNull(attributes, ResponseEnum.NOT_FOUND);
            HttpServletRequest request = attributes.getRequest();

            String userAgent = request.getHeader("User-Agent");
            if (userAgent == null) {
                return "UNKNOWN";
            }

            if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
                return "MOBILE";
            } else if (userAgent.contains("Macintosh") || userAgent.contains("Mac OS X")) {
                return "MAC";
            } else {
                return "PC";
            }
        } catch (Exception e) {
            log.warn("获取设备类型失败", e);
            return "UNKNOWN";
        }
    }

    /**
     * 从 User-Agent 中解析浏览器信息
     *
     * @return 浏览器名称（Edge/Chrome/Firefox/Safari/IE/Unknown）
     */
    public static String getBrowser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "Unknown";
            }

            HttpServletRequest request = attributes.getRequest();
            String userAgent = request.getHeader("User-Agent");
            if (userAgent == null || userAgent.isEmpty()) {
                return "Unknown";
            }

            if (userAgent.contains("Edg")) {
                return "Edge";
            } else if (userAgent.contains("Chrome")) {
                return "Chrome";
            } else if (userAgent.contains("Firefox")) {
                return "Firefox";
            } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
                return "Safari";
            } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
                return "IE";
            } else {
                return "Unknown";
            }
        } catch (Exception e) {
            log.warn("获取浏览器信息失败", e);
            return "Unknown";
        }
    }

    /**
     * 从 User-Agent 中解析操作系统信息
     *
     * @return 操作系统名称（Windows、Mac OS、Linux、Android、iOS、Unknown）
     */
    public static String getOs() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "Unknown";
            }

            HttpServletRequest request = attributes.getRequest();
            String userAgent = request.getHeader("User-Agent");
            if (userAgent == null || userAgent.isEmpty()) {
                return "Unknown";
            }

            if (userAgent.contains("Windows NT")) {
                return "Windows";
            } else if (userAgent.contains("Mac OS X")) {
                return "Mac OS";
            } else if (userAgent.contains("Linux")) {
                return "Linux";
            } else if (userAgent.contains("Android")) {
                return "Android";
            } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
                return "iOS";
            } else {
                return "Unknown";
            }
        } catch (Exception e) {
            log.warn("获取操作系统信息失败", e);
            return "Unknown";
        }
    }
}
