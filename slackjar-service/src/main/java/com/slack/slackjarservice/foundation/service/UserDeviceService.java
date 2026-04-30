package com.slack.slackjarservice.foundation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.foundation.entity.UserDevice;
import com.slack.slackjarservice.foundation.model.request.UserDevicePageQuery;
import com.slack.slackjarservice.foundation.model.response.UserDeviceResponse;

import java.util.List;

/**
 * 用户设备登录记录表(UserDevice)表服务接口
 *
 * @author zhn
 * @since 2025-04-22
 */
public interface UserDeviceService extends IService<UserDevice> {

    /**
     * 记录用户设备登录信息
     *
     * @param userDevice 设备信息
     */
    void recordDeviceLogin(UserDevice userDevice);

    /**
     * 获取用户的所有设备列表
     *
     * @param userId 用户ID
     * @return 设备列表
     */
    List<UserDeviceResponse> getUserDevices(Long userId);

    /**
     * 强制下线指定设备
     *
     * @param userId 用户ID
     * @param deviceId 设备记录ID
     */
    void forceLogoutDevice(Long userId, Long deviceId);

    /**
     * 分页查询用户设备登录记录
     *
     * @param query 分页查询参数
     * @return 分页结果
     */
    PageResult<UserDeviceResponse> pageQueryUserDevices(UserDevicePageQuery query);
}
