package com.slack.slackjarservice.foundation.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息DTO，不包含敏感字段
 */
@Data
public class UserInfoDTO implements Serializable {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户状态（0-正常，1-禁用）
     */
    private Integer status;

    /**
     * 头像图片（关联sys_file表）
     */
    private String avatarUrl;

    /**
     * 头像图片id（关联sys_file表）
     */
    private Long avatarId;

    /**
     * 背景图片（关联sys_file表）
     */
    private String backgroundUrl;

    /**
     * 背景图片Id（关联sys_file表）
     */
    private Long backgroundId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后登录时间
     */
    private Long lastLoginTime;

    /**
     * IP地址
     */
    private String ip;

    /**
     * IP对应的地级市
     */
    private String city;

}
