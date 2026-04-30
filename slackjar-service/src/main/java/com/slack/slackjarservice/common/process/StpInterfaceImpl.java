package com.slack.slackjarservice.common.process;

import cn.dev33.satoken.stp.StpInterface;
import com.slack.slackjarservice.foundation.entity.Permission;
import com.slack.slackjarservice.foundation.entity.Role;
import com.slack.slackjarservice.foundation.service.PermissionService;
import com.slack.slackjarservice.foundation.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义权限加载接口实现类
 *
 * @author zhn
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RoleService roleService;

    @Resource
    private PermissionService permissionService;

    /**
     * 返回一个账号所拥有的权限码集合
     * 标准RBAC模型：用户 -> 角色 -> 权限
     */
    @Override
    @Cacheable(value = "permission", key = "{#loginId, #loginType}")
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<Role> userRoles = roleService.listByUserId((String) loginId);
        List<Long> roleIds = userRoles.stream().map(Role::getId).toList();
        List<Permission> permissions = permissionService.listByRoleIds(roleIds);
        return permissions.stream().map(Permission::getPermissionCode).collect(Collectors.toList());
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    @Cacheable(value = "role", key = "{#loginId, #loginType}")
    public List<String> getRoleList(Object loginId, String loginType) {
        List<Role> userRoles = roleService.listByUserId((String) loginId);
        return userRoles.stream().map(Role::getRoleCode).collect(Collectors.toList());
    }
}
