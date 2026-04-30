package com.slack.slackjarservice.foundation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.slack.slackjarservice.foundation.entity.UserDevice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户设备登录记录表(UserDevice)表数据库访问层
 *
 * @author zhn
 * @since 2025-04-22
 */
@Mapper
public interface UserDeviceDao extends BaseMapper<UserDevice> {
}
