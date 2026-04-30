package com.slack.slackjarservice.foundation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slack.slackjarservice.foundation.entity.RolePermission;

/**
 * 角色与权限的关联表，维护多对多关系(RolePermission)表服务接口
 *
 * @author zhn
 * @since 2025-08-15 01:31:00
 */
public interface RolePermissionService extends IService<RolePermission> {

}
