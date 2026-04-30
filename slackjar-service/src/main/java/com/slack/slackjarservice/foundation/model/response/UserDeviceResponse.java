package com.slack.slackjarservice.foundation.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户设备信息响应对象
 *
 * @author zhn
 * @since 2025-04-22
 */
@Data
public class UserDeviceResponse {

    /**
     * 设备记录ID
     */
    private Long id;

    /**
     * 设备标识
     */
    private String device;

    /**
     * 登录IP
     */
    private String ipAddr;

    /**
     * 登录地点
     */
    private String location;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 是否为当前设备
     */
    private Boolean currentDevice;

    /**
     * 生效状态 0-生效、1-失效
     */
    private Integer status;
}
