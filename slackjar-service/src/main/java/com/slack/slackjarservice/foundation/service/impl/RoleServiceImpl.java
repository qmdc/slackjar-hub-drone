package com.slack.slackjarservice.foundation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slack.slackjarservice.common.constant.BaseConstants;
import com.slack.slackjarservice.common.constant.CommonConstants;
import com.slack.slackjarservice.common.enumtype.foundation.EnableStatusEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.enumtype.foundation.RoleCodeEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.common.util.AssertUtil;
import com.slack.slackjarservice.foundation.dao.RoleDao;
import com.slack.slackjarservice.foundation.entity.Role;
import com.slack.slackjarservice.foundation.entity.RolePermission;
import com.slack.slackjarservice.foundation.entity.SysUser;
import com.slack.slackjarservice.foundation.entity.UserRole;
import com.slack.slackjarservice.foundation.model.request.RoleRequest;
import com.slack.slackjarservice.foundation.service.RolePermissionService;
import com.slack.slackjarservice.foundation.service.RoleService;
import com.slack.slackjarservice.foundation.service.SysUserService;
import com.slack.slackjarservice.foundation.service.UserRoleService;
import com.slack.slackjarservice.foundation.service.PermissionService;
import com.slack.slackjarservice.foundation.entity.Permission;
import com.slack.slackjarservice.foundation.model.response.PermissionResponse;
import com.slack.slackjarservice.foundation.model.request.RolePageQuery;
import com.slack.slackjarservice.foundation.model.request.RoleUserPageQuery;
import com.slack.slackjarservice.common.response.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slack.slackjarservice.foundation.model.response.RoleResponse;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 角色信息表，存储系统中所有角色定义(Role)表服务实现类
 *
 * @author zhn
 * @since 2025-08-15 00:18:01
 */
@Service("roleService")
public class RoleServiceImpl extends ServiceImpl<RoleDao, Role> implements RoleService {

    @Lazy
    @Resource
    private SysUserService sysUserService;

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private RolePermissionService rolePermissionService;

    @Lazy
    @Resource
    private PermissionService permissionService;

    @Override
    public List<Role> listByUserId(String loginId) {
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.eq(SysUser::getId, loginId);
        SysUser sysUser = sysUserService.getOne(userQueryWrapper);

        LambdaQueryWrapper<UserRole> userRoleQueryWrapper = new LambdaQueryWrapper<>();
        userRoleQueryWrapper.eq(UserRole::getUserId, sysUser.getId());
        List<UserRole> userRoles = userRoleService.list(userRoleQueryWrapper);

        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.in(Role::getId, roleIds);
        return list(roleQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(RoleRequest request) {
        Role role;
        if (request.getId() != null) {
            // 更新
            role = this.getById(request.getId());
            AssertUtil.notNull(role, ResponseEnum.NOT_FOUND);

            // 所有角色编码都不允许修改
            AssertUtil.equals(role.getRoleCode(), request.getRoleCode(), ResponseEnum.ROLE_CODE_NOT_ALLOW_MODIFY);

            // 系统内置角色不允许禁用
            if (RoleCodeEnum.getAllCodes().contains(role.getRoleCode())
                    && EnableStatusEnum.DISABLED.getCode() == request.getStatus()) {
                throw new BusinessException(ResponseEnum.ROLE_SYSTEM_NOT_ALLOW_DISABLE);
            }
        } else {
            // 新增校验角色编码是否重复
            long count = this.count(new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, request.getRoleCode()));
            AssertUtil.isTrue(count < BaseConstants.Digital.ONE, ResponseEnum.ROLE_CODE_REPEAT);
            role = new Role();
        }

        role.setRoleName(request.getRoleName());
        role.setRoleCode(request.getRoleCode());
        role.setDescription(request.getDescription());
        role.setRoleType(request.getRoleType() != null ? request.getRoleType() : CommonConstants.RoleType.CUSTOM);
        role.setStatus(request.getStatus() != null ? request.getStatus() : EnableStatusEnum.ENABLE.getCode());
        role.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        this.saveOrUpdate(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        Role role = this.getById(id);
        AssertUtil.notNull(role, ResponseEnum.NOT_FOUND);

        // 系统内置角色不允许删除
        if (RoleCodeEnum.getAllCodes().contains(role.getRoleCode())) {
            throw new BusinessException(ResponseEnum.ROLE_SYSTEM_NOT_ALLOW_DELETE);
        }

        // 删除角色
        this.removeById(id);

        // 删除角色权限关联
        rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));

        // 删除用户角色关联
        userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 1. 校验角色是否存在
        Role role = this.getById(roleId);
        AssertUtil.notNull(role, ResponseEnum.NOT_FOUND);

        // 2. 校验角色是否被禁用
        AssertUtil.isTrue(EnableStatusEnum.ENABLE.getCode() == role.getStatus(), ResponseEnum.USER_DISABLED);

        // 3. 删除该角色的所有权限
        rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));

        if (!CollectionUtils.isEmpty(permissionIds)) {
            // 4. 过滤掉不存在的权限
            List<Permission> existingPermissions = permissionService.listByIds(permissionIds);
            List<Long> validPermissionIds = existingPermissions.stream().map(Permission::getId).toList();

            // 5. 重新分配有效的权限
            if (!CollectionUtils.isEmpty(validPermissionIds)) {
                List<RolePermission> rolePermissions = validPermissionIds.stream().map(permissionId -> {
                    RolePermission rp = new RolePermission();
                    rp.setRoleId(roleId);
                    rp.setPermissionId(permissionId);
                    return rp;
                }).toList();
                rolePermissionService.saveBatch(rolePermissions);
            }
        }
    }

    @Override
    public java.util.Map<Long, List<PermissionResponse.PermissionItem>> getRolePermissionsBatch(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyMap();
        }

        // 查询所有角色权限关联
        LambdaQueryWrapper<RolePermission> rolePermissionQueryWrapper = new LambdaQueryWrapper<>();
        rolePermissionQueryWrapper.in(RolePermission::getRoleId, roleIds);
        List<RolePermission> rolePermissions = rolePermissionService.list(rolePermissionQueryWrapper);

        // 提取所有权限 ID
        List<Long> permissionIds = rolePermissions.stream().map(RolePermission::getPermissionId).distinct().toList();
        if (CollectionUtils.isEmpty(permissionIds)) {
            return Collections.emptyMap();
        }

        // 查询所有权限
        List<Permission> permissions = permissionService.listByIds(permissionIds);
        java.util.Map<Long, Permission> permissionMap = permissions.stream().collect(Collectors.toMap(Permission::getId, p -> p));

        // 查询所有角色信息
        List<Role> roles = this.listByIds(roleIds);
        java.util.Map<Long, Role> roleMap = roles.stream().collect(Collectors.toMap(Role::getId, r -> r));

        // 按角色 ID 分组，并填充 roleCode 和 roleIdCode
        java.util.Map<Long, List<PermissionResponse.PermissionItem>> result = new java.util.HashMap<>();
        for (Long roleId : roleIds) {
            Role role = roleMap.get(roleId);
            List<Long> permIdsForRole = rolePermissions.stream().filter(rp -> rp.getRoleId().equals(roleId)).map(RolePermission::getPermissionId).toList();

            List<PermissionResponse.PermissionItem> permissionItems = permIdsForRole.stream()
                    .map(permissionId -> {
                        Permission permission = permissionMap.get(permissionId);
                        if (permission == null) {
                            return null;
                        }
                        PermissionResponse.PermissionItem item = new PermissionResponse.PermissionItem();
                        BeanUtils.copyProperties(permission, item);
                        // 设置角色相关信息
                        if (role != null) {
                            item.setRoleCode(role.getRoleCode());
                            item.setRoleId(String.valueOf(role.getId()));
                            item.setRoleStatus(role.getStatus());
                        }
                        return item;
                    }).filter(java.util.Objects::nonNull).toList();
            result.put(roleId, permissionItems);
        }
        return result;
    }

    @Override
    public PageResult<RoleResponse.RoleItem> pageQueryRoles(RolePageQuery query) {
        // 构建分页对象
        Page<Role> page = new Page<>(query.getPageNo(), query.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();

        // 角色编码模糊查询
        if (query.getRoleCode() != null && !query.getRoleCode().isEmpty()) {
            queryWrapper.like(Role::getRoleCode, query.getRoleCode());
        }

        // 角色名称模糊查询
        if (query.getRoleName() != null && !query.getRoleName().isEmpty()) {
            queryWrapper.like(Role::getRoleName, query.getRoleName());
        }

        // 角色类型精确查询
        if (query.getRoleType() != null) {
            queryWrapper.eq(Role::getRoleType, query.getRoleType());
        }

        // 状态精确查询
        if (query.getStatus() != null) {
            queryWrapper.eq(Role::getStatus, query.getStatus());
        }

        // 排序处理（只允许预定义的字段）
        String sortBy = query.getSortBy() != null ? query.getSortBy() : BaseConstants.SORT_ORDER;
        String sortOrder = query.getSortOrder() != null ? query.getSortOrder() : BaseConstants.ASC;

        // 根据排序字段进行排序
        switch (sortBy) {
            case BaseConstants.SORT_ORDER -> {
                if (BaseConstants.ASC.equalsIgnoreCase(sortOrder)) {
                    queryWrapper.orderByAsc(Role::getSortOrder);
                } else {
                    queryWrapper.orderByDesc(Role::getSortOrder);
                }
            }
            case BaseConstants.CREATE_TIME -> {
                if (BaseConstants.ASC.equalsIgnoreCase(sortOrder)) {
                    queryWrapper.orderByAsc(Role::getCreateTime);
                } else {
                    queryWrapper.orderByDesc(Role::getCreateTime);
                }
            }
            case BaseConstants.ID -> {
                if (BaseConstants.ASC.equalsIgnoreCase(sortOrder)) {
                    queryWrapper.orderByAsc(Role::getId);
                } else {
                    queryWrapper.orderByDesc(Role::getId);
                }
            }
            default ->
                // 默认按排序号升序
                    queryWrapper.orderByAsc(Role::getSortOrder);
        }

        // 执行分页查询
        Page<Role> rolePage = this.page(page, queryWrapper);

        // 转换为响应对象
        List<RoleResponse.RoleItem> roleItems = rolePage.getRecords().stream().map(role -> {
            RoleResponse.RoleItem item = new RoleResponse.RoleItem();
            BeanUtils.copyProperties(role, item);
            return item;
        }).collect(Collectors.toList());

        // 构建分页结果
        return PageResult.of(roleItems, rolePage.getTotal(), query.getPageNo(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUsersToRole(Long roleId, List<Long> userIds) {
        // 1. 校验角色是否存在
        Role role = this.getById(roleId);
        AssertUtil.notNull(role, ResponseEnum.NOT_FOUND);

        // 2. 校验角色是否被禁用
        AssertUtil.isTrue(EnableStatusEnum.ENABLE.getCode() == role.getStatus(), ResponseEnum.USER_DISABLED);

        // 3. 删除该角色的所有用户
        userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, roleId));

        if (!CollectionUtils.isEmpty(userIds)) {
            // 4. 过滤掉不存在的用户
            List<SysUser> existingUsers = sysUserService.listByIds(userIds);
            List<Long> validUserIds = existingUsers.stream().map(SysUser::getId).toList();

            // 5. 重新分配给有效的用户
            if (!CollectionUtils.isEmpty(validUserIds)) {
                List<UserRole> userRoles = validUserIds.stream().map(userId -> {
                    UserRole ur = new UserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(roleId);
                    return ur;
                }).toList();
                userRoleService.saveBatch(userRoles);
            }
        }
    }

    @Override
    public List<SysUser> getUsersByRoleId(Long roleId) {
        if (Objects.isNull(roleId)) {
            return Collections.emptyList();
        }

        // 查询该角色的所有用户 ID
        LambdaQueryWrapper<UserRole> userRoleQueryWrapper = new LambdaQueryWrapper<>();
        userRoleQueryWrapper.eq(UserRole::getRoleId, roleId);
        List<UserRole> userRoles = userRoleService.list(userRoleQueryWrapper);

        if (CollectionUtils.isEmpty(userRoles)) {
            return Collections.emptyList();
        }

        // 提取所有用户 ID
        List<Long> userIds = userRoles.stream().map(UserRole::getUserId).toList();

        // 查询所有用户
        List<SysUser> users = sysUserService.listByIds(userIds);

        // 将密码置空后再返回
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    @Override
    public PageResult<SysUser> pageQueryRoleUsers(Long roleId, RoleUserPageQuery query) {
        if (Objects.isNull(roleId)) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }

        LambdaQueryWrapper<UserRole> userRoleQueryWrapper = new LambdaQueryWrapper<>();
        userRoleQueryWrapper.eq(UserRole::getRoleId, roleId);
        List<UserRole> userRoles = userRoleService.list(userRoleQueryWrapper);

        if (CollectionUtils.isEmpty(userRoles)) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }

        List<Long> userIds = userRoles.stream().map(UserRole::getUserId).toList();

        Page<SysUser> page = new Page<>(query.getPageNo(), query.getPageSize());
        LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.in(SysUser::getId, userIds);

        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            userQueryWrapper.like(SysUser::getUsername, query.getUsername());
        }
        if (query.getNickname() != null && !query.getNickname().isEmpty()) {
            userQueryWrapper.like(SysUser::getNickname, query.getNickname());
        }
        if (query.getStatus() != null) {
            userQueryWrapper.eq(SysUser::getStatus, query.getStatus());
        }

        userQueryWrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = sysUserService.page(page, userQueryWrapper);
        userPage.getRecords().forEach(user -> user.setPassword(null));

        return PageResult.of(userPage.getRecords(), userPage.getTotal(), query.getPageNo(), query.getPageSize());
    }
}

