package com.slack.slackjarservice.foundation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.slack.slackjarservice.foundation.entity.SysConfig;

/**
 * 系统配置字典表(SysConfig)表数据库访问层
 *
 * @author zhn
 * @since 2025-08-26 23:01:00
 */
@Mapper
public interface SysConfigDao extends BaseMapper<SysConfig> {

}

