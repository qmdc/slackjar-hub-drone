package com.slack.slackjarservice.foundation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.slack.slackjarservice.foundation.entity.Permission;

/**
 * 权限信息表，存储系统中所有权限定义(Permission)表数据库访问层
 *
 * @author zhn
 * @since 2025-08-15 01:30:00
 */
@Mapper
public interface PermissionDao extends BaseMapper<Permission> {

}
