package com.slack.slackjarservice.foundation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slack.slackjarservice.foundation.dao.UserRoleDao;
import com.slack.slackjarservice.foundation.entity.UserRole;
import com.slack.slackjarservice.foundation.service.UserRoleService;
import org.springframework.stereotype.Service;

/**
 * 用户与角色的关联表，维护多对多关系(UserRole)表服务实现类
 *
 * @author zhn
 * @since 2025-08-15 01:27:43
 */
@Service("userRoleService")
public class UserRoleServiceImpl extends ServiceImpl<UserRoleDao, UserRole> implements UserRoleService {

}

