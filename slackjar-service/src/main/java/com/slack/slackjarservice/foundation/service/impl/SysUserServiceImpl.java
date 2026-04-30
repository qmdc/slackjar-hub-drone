package com.slack.slackjarservice.foundation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.slack.slackjarservice.common.enumtype.foundation.PushWithBackendEnum;
import com.slack.slackjarservice.common.enumtype.foundation.MediaBizTypeEnum;
import com.slack.slackjarservice.common.util.AddressUtil;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slack.slackjarservice.common.constant.LoginConstants;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.enumtype.foundation.EnableStatusEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.common.util.AssertUtil;
import com.slack.slackjarservice.common.util.RedisUtil;
import com.slack.slackjarservice.foundation.dao.SysUserDao;
import com.slack.slackjarservice.foundation.entity.*;
import com.slack.slackjarservice.foundation.model.dto.SocketMessageDTO;
import com.slack.slackjarservice.foundation.model.dto.UserInfoDTO;
import com.slack.slackjarservice.foundation.model.request.ChangePasswordRequest;
import com.slack.slackjarservice.foundation.model.request.RegisterRequest;
import com.slack.slackjarservice.foundation.model.request.UpdateUserInfoRequest;
import com.slack.slackjarservice.foundation.model.request.UserPageQuery;
import com.slack.slackjarservice.foundation.model.response.IpInfoResponse;
import com.slack.slackjarservice.foundation.service.*;
import com.slack.slackjarservice.foundation.socketio.BackendMessagePush;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * 系统用户表(SysUser)表服务实现类
 *
 * @author zhn
 * @since 2025-08-15 01:28:01
 */
@Service("sysUserService")
public class SysUserServiceImpl extends ServiceImpl<SysUserDao, SysUser> implements SysUserService {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private ThirdPartyService thirdPartyService;

    @Resource
    private SysFileService sysFileService;

    @Resource(name = "thirdPartyExecutor")
    private Executor thirdPartyExecutor;

    @Resource
    private BackendMessagePush backendMessagePush;

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private RoleService roleService;

    @Resource
    private UserDeviceService userDeviceService;

    @Override
    public SysUser loginByUsernameAndPassword(String username, String password) {
        // 检查登录错误次数
        checkLoginErrorCount(username);

        SysUser sysUser = this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        AssertUtil.notNull(sysUser, ResponseEnum.USER_NOT_EXIST);
        AssertUtil.equals(sysUser.getStatus(), EnableStatusEnum.ENABLE.getCode(), ResponseEnum.USER_DISABLED);

        // 验证密码
        if (!BCrypt.checkpw(password, sysUser.getPassword())) {
            // 记录登录错误次数
            recordLoginError(username);
            throw new BusinessException(ResponseEnum.PASSWORD_ERROR);
        }

        // 更新最后登录\IP
        sysUser.setLastLoginTime(System.currentTimeMillis());
        String clientIp = AddressUtil.getClientIp();
        sysUser.setIp(clientIp);
        this.baseMapper.updateById(sysUser);

        // 清除错误登录次数\累计登录次数
        clearLoginErrorCount(username);
        incrementLoginCount(sysUser.getId());

        // 登录
        StpUtil.login(sysUser.getId());

        UserDevice userDevice = new UserDevice();
        userDevice.setUserId(sysUser.getId());
        userDevice.setTokenValue(StpUtil.getTokenValue());
        userDevice.setIpAddr(clientIp);
        // 设置过期时间
        userDevice.setExpireTime(LocalDateTime.now().plusSeconds(StpUtil.getTokenTimeout()));
        // 设备类型 MOBILE MAC PC
        userDevice.setDevice(AddressUtil.getDeviceType());
        // 浏览器 Edge Chrome
        userDevice.setBrowser(AddressUtil.getBrowser());
        // 操作系统 MacOS Windows
        userDevice.setOs(AddressUtil.getOs());
        // 异步记录登录
        thirdPartyExecutor.execute(() -> userDeviceService.recordDeviceLogin(userDevice));

        sysUser.setPassword(null);
        return sysUser;
    }

    /**
     * 检查登录错误次数
     *
     * @param username 用户名
     */
    private void checkLoginErrorCount(String username) {
        String key = LoginConstants.getLoginErrorCountKey(username);
        Object countObj = redisUtil.get(key);

        if (countObj != null) {
            int count = Integer.parseInt(countObj.toString());
            if (count >= LoginConstants.MAX_LOGIN_ERROR_COUNT) {
                long retry = redisUtil.getExpire(key) / 60 + 1;
                throw new BusinessException("登录错误次数过多，请" + retry + "分钟后重试");
            }
        }
    }

    /**
     * 记录登录错误次数
     *
     * @param username 用户名
     */
    private void recordLoginError(String username) {
        String key = LoginConstants.getLoginErrorCountKey(username);
        Object countObj = redisUtil.get(key);

        int count = 1;
        if (countObj != null) {
            count = Integer.parseInt(countObj.toString()) + 1;
        }

        redisUtil.set(key, String.valueOf(count), LoginConstants.LOGIN_ERROR_TIMEOUT);
    }

    /**
     * 清除登录错误次数
     *
     * @param username 用户名
     */
    private void clearLoginErrorCount(String username) {
        String key = LoginConstants.getLoginErrorCountKey(username);
        redisUtil.delete(key);
    }

    /**
     * 累计登录次数
     *
     * @param userId 用户ID
     */
    private void incrementLoginCount(Long userId) {
        String key = LoginConstants.getLoginCountKey(userId);
        redisUtil.increment(key, 1);
    }

    /**
     * 获取用户登录次数
     *
     * @param userId 用户ID
     * @return 登录次数
     */
    public Long getUserLoginCount(Long userId) {
        String key = LoginConstants.getLoginCountKey(userId);
        Object countObj = redisUtil.get(key);
        return countObj != null ? Long.parseLong(countObj.toString()) : 0L;
    }

    @Override
    public UserInfoDTO getUserInfoById(Long userId) {
        SysUser sysUser = this.getById(userId);
        AssertUtil.notNull(sysUser, ResponseEnum.USER_NOT_EXIST);

        // 转换为DTO，不包含密码
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(sysUser, userInfoDTO);

        // 补充背景图、头像URL
        List<SysFile> userFiles = sysFileService.lambdaQuery()
                .eq(SysFile::getUserId, userId)
                .in(SysFile::getId, sysUser.getAvatarId(), sysUser.getBackgroundId())
                .in(SysFile::getBizType, MediaBizTypeEnum.AVATAR.getCode(), MediaBizTypeEnum.BACKGROUND.getCode())
                .list();
        for (SysFile file : userFiles) {
            String fileUrl = file.getFilePath();
            if (MediaBizTypeEnum.AVATAR.getCode().equals(file.getBizType())) {
                userInfoDTO.setAvatarUrl(fileUrl);
            } else if (MediaBizTypeEnum.BACKGROUND.getCode().equals(file.getBizType())) {
                userInfoDTO.setBackgroundUrl(fileUrl);
            }
        }

        return userInfoDTO;
    }

    @Override
    public SysUser register(RegisterRequest request) {
        // 检查两次密码是否一致
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResponseEnum.PASSWORD_NOT_MATCH);
        }

        // 检查用户名是否已存在
        SysUser existUser = this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername()));
        AssertUtil.isNull(existUser, ResponseEnum.USER_EXIST);

        // 创建新用户
        SysUser newUser = new SysUser();
        newUser.setUsername(request.getUsername());
        // 使用BCrypt加密密码
        newUser.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        newUser.setNickname(request.getUsername());
        newUser.setStatus(EnableStatusEnum.ENABLE.getCode());

        this.save(newUser);

        // 返回用户信息（不包含密码）
        newUser.setPassword(null);
        return newUser;
    }

    @Override
    public PageResult<UserInfoDTO> pageQueryUsers(UserPageQuery query) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        // 用户名模糊查询
        if (Objects.nonNull(query.getUsername()) && !query.getUsername().isEmpty()) {
            queryWrapper.like(SysUser::getUsername, query.getUsername());
        }

        // 昵称模糊查询
        if (Objects.nonNull(query.getNickname()) && !query.getNickname().isEmpty()) {
            queryWrapper.like(SysUser::getNickname, query.getNickname());
        }

        // 状态精确查询
        if (Objects.nonNull(query.getStatus())) {
            queryWrapper.eq(SysUser::getStatus, query.getStatus());
        }

        // 默认按创建时间倒序
        queryWrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = this.page(new Page<>(query.getPageNo(), query.getPageSize()), queryWrapper);

        List<UserInfoDTO> userItems = userPage.getRecords().stream().map(user -> {
            UserInfoDTO dto = new UserInfoDTO();
            BeanUtils.copyProperties(user, dto);
            return dto;
        }).toList();

        return PageResult.of(userItems, userPage.getTotal(), query.getPageNo(), query.getPageSize());
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = this.getById(userId);
        AssertUtil.notNull(user, ResponseEnum.USER_NOT_EXIST);

        user.setStatus(status);
        this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        // 1. 校验用户是否存在
        SysUser user = this.getById(userId);
        AssertUtil.notNull(user, ResponseEnum.USER_NOT_EXIST);

        // 2. 删除该用户的所有角色
        userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));

        if (!CollectionUtils.isEmpty(roleIds)) {
            // 3. 过滤掉不存在的角色
            List<Role> existingRoles = roleService.listByIds(roleIds);
            List<Long> validRoleIds = existingRoles.stream().map(Role::getId).toList();

            // 4. 重新分配给有效的角色
            if (!CollectionUtils.isEmpty(validRoleIds)) {
                List<UserRole> userRoles = validRoleIds.stream().map(roleId -> {
                    UserRole ur = new UserRole();
                    ur.setRoleId(roleId);
                    ur.setUserId(userId);
                    return ur;
                }).toList();
                userRoleService.saveBatch(userRoles);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser updateUserInfo(Long userId, UpdateUserInfoRequest request) {
        SysUser sysUser = this.getById(userId);
        AssertUtil.notNull(sysUser, ResponseEnum.USER_NOT_EXIST);

        if (Objects.nonNull(request.getNickname())) {
            sysUser.setNickname(request.getNickname());
        }
        if (Objects.nonNull(request.getAvatarId())) {
            sysUser.setAvatarId(request.getAvatarId());
        }

        if (Objects.nonNull(request.getBackgroundId())) {
            sysUser.setBackgroundId(request.getBackgroundId());
        }

        this.baseMapper.updateById(sysUser);
        SysUser newUser = this.baseMapper.selectById(userId);
        newUser.setPassword(null);
        return newUser;
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser sysUser = this.getById(userId);
        AssertUtil.notNull(sysUser, ResponseEnum.USER_NOT_EXIST);

        AssertUtil.isTrue(BCrypt.checkpw(request.getOldPassword(), sysUser.getPassword()), ResponseEnum.ORIGINAL_PASSWORD_ERROR);
        AssertUtil.isTrue(request.getNewPassword().equals(request.getConfirmPassword()), ResponseEnum.PASSWORD_NOT_MATCH);

        sysUser.setPassword(BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt()));
        this.baseMapper.updateById(sysUser);
    }

    @Override
    public void changePhone(Long userId, String phone) {
        SysUser sysUser = this.getById(userId);
        AssertUtil.notNull(sysUser, ResponseEnum.USER_NOT_EXIST);

        sysUser.setPhone(phone);
        this.baseMapper.updateById(sysUser);
    }

    @Override
    public void changeEmail(Long userId, String email) {
        SysUser sysUser = this.getById(userId);
        AssertUtil.notNull(sysUser, ResponseEnum.USER_NOT_EXIST);

        sysUser.setEmail(email);
        this.baseMapper.updateById(sysUser);
    }

    @Override
    public void pushIpCityInfo(Long userId) {
        SysUser sysUser = this.getById(userId);
        AssertUtil.notNull(sysUser, ResponseEnum.USER_NOT_EXIST);

        thirdPartyExecutor.execute(() -> {
            IpInfoResponse ipInfo = thirdPartyService.getIpInfo(sysUser.getIp());
            backendMessagePush.pushMessageToUser(String.valueOf(userId),
                    new SocketMessageDTO(ipInfo, PushWithBackendEnum.IP_CITY_INFO.getCode()));
        });
    }
}
