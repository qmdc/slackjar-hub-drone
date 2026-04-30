package com.slack.slackjarservice.foundation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.slack.slackjarservice.foundation.entity.Role;

/**
 * 角色信息表，存储系统中所有角色定义(Role)表数据库访问层
 *
 * @author zhn
 * @since 2025-08-15 01:27:12
 */
@Mapper
public interface RoleDao extends BaseMapper<Role> {

}

