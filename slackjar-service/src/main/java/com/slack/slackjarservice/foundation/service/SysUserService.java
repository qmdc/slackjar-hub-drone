package com.slack.slackjarservice.foundation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.foundation.entity.SysUser;
import com.slack.slackjarservice.foundation.model.dto.UserInfoDTO;
import com.slack.slackjarservice.foundation.model.request.ChangePasswordRequest;
import com.slack.slackjarservice.foundation.model.request.RegisterRequest;
import com.slack.slackjarservice.foundation.model.request.UpdateUserInfoRequest;
import com.slack.slackjarservice.foundation.model.request.UserPageQuery;

import java.util.List;

/**
 * 系统用户表(SysUser)表服务接口
 *
 * @author zhn
 * @since 2025-08-15 01:28:01
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名和密码登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户信息
     */
    SysUser loginByUsernameAndPassword(String username, String password);

    /**
     * 根据用户 ID 查询用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息 DTO
     */
    UserInfoDTO getUserInfoById(Long userId);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册成功的用户信息
     */
    SysUser register(RegisterRequest request);

    /**
     * 分页条件查询用户列表
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageResult<UserInfoDTO> pageQueryUsers(UserPageQuery query);

    /**
     * 修改用户状态（启用、禁用）
     *
     * @param userId 用户 ID
     * @param status 状态（0-正常，1-禁用）
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 为用户分配角色
     *
     * @param userId  用户 ID
     * @param roleIds 角色 ID 列表
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 用户修改信息
     *
     * @param userId  用户 ID
     * @param request 修改信息请求
     * @return 修改成功的用户信息
     */
    SysUser updateUserInfo(Long userId, UpdateUserInfoRequest request);

    /**
     * 修改密码
     *
     * @param userId  用户 ID
     * @param request 修改密码请求
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 修改手机号
     *
     * @param userId 用户 ID
     * @param phone  新手机号
     */
    void changePhone(Long userId, String phone);

    /**
     * 修改邮箱
     *
     * @param userId 用户 ID
     * @param email  新邮箱
     */
    void changeEmail(Long userId, String email);

    /**
     * 异步推送用户 IP 对应的地级市信息
     *
     * @param userId 用户 ID
     */
    void pushIpCityInfo(Long userId);
}
