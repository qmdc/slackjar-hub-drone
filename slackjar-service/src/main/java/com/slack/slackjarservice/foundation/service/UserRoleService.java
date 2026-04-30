package com.slack.slackjarservice.foundation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slack.slackjarservice.foundation.entity.UserRole;

/**
 * 用户与角色的关联表，维护多对多关系(UserRole)表服务接口
 *
 * @author zhn
 * @since 2025-08-15 01:27:43
 */
public interface UserRoleService extends IService<UserRole> {

}

