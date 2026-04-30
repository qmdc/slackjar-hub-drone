package com.slack.slackjarservice.foundation.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.slack.slackjarservice.common.base.BaseController;
import com.slack.slackjarservice.common.enumtype.foundation.EnableStatusEnum;
import com.slack.slackjarservice.common.process.PasswordDecrypt;
import com.slack.slackjarservice.common.enumtype.foundation.OperationEnum;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.foundation.entity.Role;
import com.slack.slackjarservice.foundation.entity.SysUser;
import com.slack.slackjarservice.foundation.entity.UserRole;
import com.slack.slackjarservice.foundation.model.dto.UserInfoDTO;
import com.slack.slackjarservice.foundation.model.request.ChangePasswordRequest;
import com.slack.slackjarservice.foundation.model.request.LoginByUserNameRequest;
import com.slack.slackjarservice.foundation.model.request.EncryptedLoginRequest;
import com.slack.slackjarservice.foundation.model.request.RegisterRequest;
import com.slack.slackjarservice.foundation.model.request.UpdateUserInfoRequest;
import com.slack.slackjarservice.foundation.model.request.UserPageQuery;
import com.slack.slackjarservice.foundation.model.request.UserDevicePageQuery;
import com.slack.slackjarservice.foundation.model.response.UserDeviceResponse;
import com.slack.slackjarservice.foundation.service.RoleService;
import com.slack.slackjarservice.foundation.service.SysUserService;
import com.slack.slackjarservice.foundation.service.UserRoleService;
import com.slack.slackjarservice.foundation.service.UserDeviceService;
import com.slack.slackjarservice.foundation.entity.UserDevice;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

/**
 * 系统用户表(SysUser)表控制层
 *
 * @author zhn
 * @since 2025-08-15 00:18:02
 */
@RestController
@RequestMapping("/sys-user")
public class SysUserController extends BaseController {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private PasswordDecrypt passwordDecryptor;

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private RoleService roleService;

    @Resource
    private UserDeviceService userDeviceService;

    /**
     * 使用普通密码登录
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/login/user-name")
    public ApiResponse<SaTokenInfo> loginByUserName(@Validated @RequestBody LoginByUserNameRequest request) {
        SysUser sysUser = sysUserService.loginByUsernameAndPassword(request.getUsername(), request.getPassword());
        recordOperateLog(OperationEnum.USER_LOGIN, "登录成功:" + sysUser);
        return success(StpUtil.getTokenInfo());
    }

    /**
     * 使用加密密码登录
     *
     * @param request 加密登录请求
     * @return 登录结果
     */
    @PostMapping("/login/encrypted")
    public ApiResponse<SaTokenInfo> loginWithEncryptedPassword(@Validated @RequestBody EncryptedLoginRequest request) {
        String decryptedPassword = passwordDecryptor.decrypt(request.getEncryptedPassword());
        SysUser sysUser = sysUserService.loginByUsernameAndPassword(request.getUsername(), decryptedPassword);
        recordOperateLog(OperationEnum.USER_LOGIN, "登录成功:" + sysUser);
        return success(StpUtil.getTokenInfo());
    }

    /**
     * 退出登录
     *
     * @return 退出登录结果
     */
    @GetMapping("/logout")
    public ApiResponse<Void> logout() {
        Long userId = getLoginUserId();
        // 修改当前设备的登录为失效
        String currentTokenValue = StpUtil.getTokenValue();
        userDeviceService.update(new LambdaUpdateWrapper<UserDevice>().eq(UserDevice::getUserId, userId)
                .eq(UserDevice::getTokenValue, currentTokenValue).set(UserDevice::getStatus, EnableStatusEnum.DISABLED.getCode()));
        recordOperateLog(OperationEnum.USER_LOGOUT, "退出登录:" + userId);
        // 只退出当前设备的 token
        StpUtil.logoutByTokenValue(currentTokenValue);
        return success();
    }

    /**
     * 根据用户 ID 查询用户信息
     *
     * @param userId 用户 ID
     * @return 用户信息 DTO
     */
    @GetMapping("/info/{userId}")
    public ApiResponse<UserInfoDTO> getUserInfo(@PathVariable Long userId) {
        UserInfoDTO userInfoDTO = sysUserService.getUserInfoById(userId);
        recordOperateLog(OperationEnum.USER_QUERY, "查询用户信息成功:" + userId);
        return success(userInfoDTO);
    }

    /**
     * 推送用户 IP 对应的地级市信息（异步）
     *
     * @return 操作结果
     */
    @PostMapping("/pushIpCityInfo")
    public ApiResponse<Void> pushIpCityInfo() {
        sysUserService.pushIpCityInfo(getLoginUserId());
        return success();
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public ApiResponse<SysUser> register(@Validated @RequestBody RegisterRequest request) {
        SysUser sysUser = sysUserService.register(request);
        recordOperateLog(OperationEnum.USER_REGISTER, "注册成功:" + sysUser.getUsername());
        return success(sysUser);
    }

    /**
     * 用户修改信息
     *
     * @param request 修改信息请求
     * @return 操作结果
     */
    @PutMapping("/updateInfo")
    public ApiResponse<SysUser> updateUserInfo(@Validated @RequestBody UpdateUserInfoRequest request) {
        SysUser sysUser = sysUserService.updateUserInfo(getLoginUserId(), request);
        recordOperateLog(OperationEnum.USER_UPSERT, "修改用户信息成功:" + getLoginUserId());
        return success(sysUser);
    }

    /**
     * 分页条件查询用户列表
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @PostMapping("/pageQuery")
    public ApiResponse<PageResult<UserInfoDTO>> pageQueryUsers(@RequestBody UserPageQuery query) {
        PageResult<UserInfoDTO> result = sysUserService.pageQueryUsers(query);
        recordOperateLog(OperationEnum.USER_QUERY, "分页查询用户列表成功");
        return success(result);
    }

    /**
     * 修改用户状态（启用、禁用）
     *
     * @param userId 用户 ID
     * @param status 状态（0-正常，1-禁用）
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @PutMapping("/updateStatus/{userId}/{status}")
    public ApiResponse<Boolean> updateUserStatus(@PathVariable Long userId, @PathVariable Integer status) {
        sysUserService.updateUserStatus(userId, status);
        recordOperateLog(OperationEnum.USER_UPSERT, "修改用户状态成功，用户 ID：" + userId + "，状态：" + status);
        return success(true);
    }

    /**
     * 获取用户的角色列表
     *
     * @param userId 用户 ID
     * @return 角色列表
     */
    @GetMapping("/{userId}/roles")
    public ApiResponse<List<Role>> getUserRoles(@PathVariable Long userId) {
        List<UserRole> userRoles = userRoleService.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return success(List.of());
        }
        List<Role> roles = roleService.listByIds(roleIds);
        roles.sort(Comparator.comparingInt(Role::getSortOrder));
        return success(roles);
    }

    /**
     * 为用户分配角色
     *
     * @param userId  用户 ID
     * @param roleIds 角色 ID 列表
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @PostMapping("/{userId}/roles")
    public ApiResponse<Boolean> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        sysUserService.assignRolesToUser(userId, roleIds);
        recordOperateLog(OperationEnum.USER_UPSERT, "为用户分配角色，用户 ID:" + userId);
        return success(true);
    }

    /**
     * 获取当前用户登陆状态有效的设备列表
     *
     * @return 设备列表
     */
    @GetMapping("/devices")
    public ApiResponse<List<UserDeviceResponse>> getUserDevices() {
        Long userId = getLoginUserId();
        List<UserDeviceResponse> devices = userDeviceService.getUserDevices(userId);
        recordOperateLog(OperationEnum.USER_QUERY, "查询用户设备列表成功，用户 ID:" + userId);
        return success(devices);
    }

    /**
     * 强制下线指定设备（只能下线自己的其他设备）
     *
     * @param deviceId 设备记录ID
     * @return 操作结果
     */
    @PostMapping("/devices/{deviceId}/logout")
    public ApiResponse<Boolean> forceLogoutDevice(@PathVariable Long deviceId) {
        Long userId = getLoginUserId();
        userDeviceService.forceLogoutDevice(userId, deviceId);
        recordOperateLog(OperationEnum.USER_LOGOUT, "强制下线设备，用户 ID:" + userId + "，设备 ID:" + deviceId);
        return success(true);
    }

    /**
     * 分页查询用户设备登录记录
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    @PostMapping("/devices/pageQuery")
    public ApiResponse<PageResult<UserDeviceResponse>> pageQueryUserDevices(@RequestBody UserDevicePageQuery query) {
        PageResult<UserDeviceResponse> result = userDeviceService.pageQueryUserDevices(query);
        recordOperateLog(OperationEnum.USER_QUERY, "分页查询用户设备登录记录成功");
        return success(result);
    }

    /**
     * 修改密码
     *
     * @param request 修改密码请求
     * @return 操作结果
     */
    @PutMapping("/changePassword")
    public ApiResponse<Void> changePassword(@Validated @RequestBody ChangePasswordRequest request) {
        sysUserService.changePassword(getLoginUserId(), request);
        recordOperateLog(OperationEnum.USER_UPSERT, "修改密码成功，用户 ID:" + getLoginUserId());
        return success();
    }

    /**
     * 修改手机号
     *
     * @param phone 新手机号
     * @return 操作结果
     */
    @PutMapping("/changePhone")
    public ApiResponse<Void> changePhone(@RequestParam String phone) {
        sysUserService.changePhone(getLoginUserId(), phone);
        recordOperateLog(OperationEnum.USER_UPSERT, "修改手机号成功，用户 ID:" + getLoginUserId());
        return success();
    }

    /**
     * 修改邮箱
     *
     * @param email 新邮箱
     * @return 操作结果
     */
    @PutMapping("/changeEmail")
    public ApiResponse<Void> changeEmail(@RequestParam String email) {
        sysUserService.changeEmail(getLoginUserId(), email);
        recordOperateLog(OperationEnum.USER_UPSERT, "修改邮箱成功，用户 ID:" + getLoginUserId());
        return success();
    }
}
