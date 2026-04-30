package com.slack.slackjarservice.foundation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.slack.slackjarservice.common.base.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;

/**
 * 用户设备登录记录表(UserDevice)表实体类
 *
 * @author zhn
 * @since 2025-04-22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("user_device")
public class UserDevice extends BaseModel {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * Sa-Token 的 token 值
     */
    private String tokenValue;

    /**
     * 设备标识，如 PC、MAC、MOBILE
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
     * 生效状态 0-生效、1-失效
     */
    private Integer status;
}
