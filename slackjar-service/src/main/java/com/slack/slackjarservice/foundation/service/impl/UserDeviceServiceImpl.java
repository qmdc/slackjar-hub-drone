package com.slack.slackjarservice.foundation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slack.slackjarservice.common.enumtype.foundation.EnableStatusEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.common.util.AddressUtil;
import com.slack.slackjarservice.common.util.AssertUtil;
import com.slack.slackjarservice.foundation.dao.UserDeviceDao;
import com.slack.slackjarservice.foundation.entity.UserDevice;
import com.slack.slackjarservice.foundation.model.request.UserDevicePageQuery;
import com.slack.slackjarservice.foundation.model.response.IpInfoResponse;
import com.slack.slackjarservice.foundation.model.response.UserDeviceResponse;
import com.slack.slackjarservice.foundation.service.ThirdPartyService;
import com.slack.slackjarservice.foundation.service.UserDeviceService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 用户设备登录记录表(UserDevice)表服务实现类
 *
 * @author zhn
 * @since 2025-04-22
 */
@Service("userDeviceService")
public class UserDeviceServiceImpl extends ServiceImpl<UserDeviceDao, UserDevice> implements UserDeviceService {

    @Resource
    private UserDeviceDao userDeviceDao;

    @Resource
    private ThirdPartyService thirdPartyService;

    @Override
    public void recordDeviceLogin(UserDevice userDevice) {
        // 地区 陕西西安
        userDevice.setLocation(thirdPartyService.getIpInfo(userDevice.getIpAddr()).getCity());
        // 登录时间
        userDevice.setLoginTime(LocalDateTime.now());
        userDevice.setStatus(EnableStatusEnum.ENABLE.getCode());
        userDeviceDao.insert(userDevice);
    }

    @Override
    public List<UserDeviceResponse> getUserDevices(Long userId) {
        List<UserDevice> devices = userDeviceDao.selectList(new LambdaQueryWrapper<UserDevice>()
                .eq(UserDevice::getUserId, userId)
                .eq(UserDevice::getStatus, EnableStatusEnum.ENABLE.getCode())
                .gt(UserDevice::getExpireTime, LocalDateTime.now())
                .orderByDesc(UserDevice::getLoginTime));

        return devices.stream().map(device -> {
            UserDeviceResponse response = new UserDeviceResponse();
            BeanUtils.copyProperties(device, response);
            // 标记当前设备
            response.setCurrentDevice(Objects.equals(device.getTokenValue(), StpUtil.getTokenValue()));
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceLogoutDevice(Long userId, Long deviceId) {
        UserDevice device = userDeviceDao.selectById(deviceId);
        AssertUtil.notNull(device, ResponseEnum.NOT_FOUND);

        // 校验是否是该用户的设备
        AssertUtil.equals(device.getUserId(), userId, ResponseEnum.NO_PERMISSION_LOGIN_INFO);

        // 删除设备记录
        String tokenValue = device.getTokenValue();
        this.update(new LambdaUpdateWrapper<UserDevice>().eq(UserDevice::getUserId, userId)
                .eq(UserDevice::getTokenValue, tokenValue).set(UserDevice::getStatus, EnableStatusEnum.DISABLED.getCode()));

        // 调用 Sa-Token 踢下线
        StpUtil.logoutByTokenValue(tokenValue);
    }

    @Override
    public PageResult<UserDeviceResponse> pageQueryUserDevices(UserDevicePageQuery query) {
        Long userId = Long.valueOf(StpUtil.getLoginId().toString());

        LambdaQueryWrapper<UserDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDevice::getUserId, userId);

        // 设备类型筛选
        if (StringUtils.hasText(query.getDevice())) {
            queryWrapper.eq(UserDevice::getDevice, query.getDevice());
        }

        // 浏览器筛选
        if (StringUtils.hasText(query.getBrowser())) {
            queryWrapper.eq(UserDevice::getBrowser, query.getBrowser());
        }

        // 操作系统筛选
        if (StringUtils.hasText(query.getOs())) {
            queryWrapper.eq(UserDevice::getOs, query.getOs());
        }

        // 生效状态筛选
        if (Objects.nonNull(query.getStatus())) {
            queryWrapper.eq(UserDevice::getStatus, query.getStatus());
        }

        // 排序
        if ("asc".equalsIgnoreCase(query.getSortOrder())) {
            queryWrapper.orderByAsc(UserDevice::getLoginTime);
        } else {
            queryWrapper.orderByDesc(UserDevice::getLoginTime);
        }

        Page<UserDevice> page = userDeviceDao.selectPage(new Page<>(query.getPageNo(), query.getPageSize()), queryWrapper);

        List<UserDeviceResponse> responseList = page.getRecords().stream().map(device -> {
            UserDeviceResponse response = new UserDeviceResponse();
            BeanUtils.copyProperties(device, response);
            response.setCurrentDevice(Objects.equals(device.getTokenValue(), StpUtil.getTokenValue()));
            return response;
        }).toList();

        return PageResult.of(responseList, page.getTotal(), query.getPageNo(), query.getPageSize());
    }
}
