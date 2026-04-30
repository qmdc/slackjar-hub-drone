package com.slack.slackjarservice.foundation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.slack.slackjarservice.foundation.entity.RolePermission;

/**
 * 角色与权限的关联表，维护多对多关系(RolePermission)表数据库访问层
 *
 * @author zhn
 * @since 2025-08-15 01:31:00
 */
@Mapper
public interface RolePermissionDao extends BaseMapper<RolePermission> {

}
