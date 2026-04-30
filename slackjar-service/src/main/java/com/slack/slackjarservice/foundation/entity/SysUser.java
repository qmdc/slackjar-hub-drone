package com.slack.slackjarservice.foundation.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.slack.slackjarservice.common.base.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 系统用户表(SysUser)表实体类
 *
 * @author zhn
 * @since 2025-08-15 01:28:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseModel {
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户名（唯一）
     */
    private String username;
    /**
     * 密码（加密存储）
     */
    private String password;
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
     * 状态（0-正常，1-禁用）
     */
    private Integer status;
    /**
     * 头像图片ID（关联sys_file表）
     */
    private Long avatarId;
    /**
     * 背景图片ID（关联sys_file表）
     */
    private Long backgroundId;
    /**
     * 最后登录时间（毫秒时间戳）
     */
    private Long lastLoginTime;
    /**
     * 最后登录IP
     */
    private String ip;
}

