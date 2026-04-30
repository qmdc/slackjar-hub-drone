package com.slack.slackjarservice.foundation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.slack.slackjarservice.foundation.entity.SysUser;

/**
 * 系统用户表(SysUser)表数据库访问层
 *
 * @author zhn
 * @since 2025-08-15 01:28:01
 */
@Mapper
public interface SysUserDao extends BaseMapper<SysUser> {

}

