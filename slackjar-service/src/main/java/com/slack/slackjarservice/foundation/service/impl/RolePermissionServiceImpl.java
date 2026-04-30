package com.slack.slackjarservice.foundation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slack.slackjarservice.foundation.dao.RolePermissionDao;
import com.slack.slackjarservice.foundation.entity.RolePermission;
import com.slack.slackjarservice.foundation.service.RolePermissionService;
import org.springframework.stereotype.Service;

/**
 * 角色与权限的关联表，维护多对多关系(RolePermission)表服务实现类
 *
 * @author zhn
 * @since 2025-08-15 01:31:00
 */
@Service("rolePermissionService")
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionDao, RolePermission> implements RolePermissionService {

}
