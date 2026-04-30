package com.slack.slackjarservice.foundation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.slack.slackjarservice.foundation.entity.UserRole;

/**
 * 用户与角色的关联表，维护多对多关系(UserRole)表数据库访问层
 *
 * @author zhn
 * @since 2025-08-15 01:27:43
 */
@Mapper
public interface UserRoleDao extends BaseMapper<UserRole> {

}

