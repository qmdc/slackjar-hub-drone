package com.slack.slackjarservice.foundation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.common.util.AssertUtil;
import com.slack.slackjarservice.foundation.dao.PermissionDao;
import com.slack.slackjarservice.foundation.entity.Permission;
import com.slack.slackjarservice.foundation.entity.Role;
import com.slack.slackjarservice.foundation.entity.RolePermission;
import com.slack.slackjarservice.foundation.model.request.PermissionPageQuery;
import com.slack.slackjarservice.foundation.model.request.PermissionRequest;
import com.slack.slackjarservice.foundation.model.response.PermissionResponse;
import com.slack.slackjarservice.foundation.service.PermissionService;
import com.slack.slackjarservice.foundation.service.RolePermissionService;
import com.slack.slackjarservice.foundation.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 权限信息表，存储系统中所有权限定义(Permission)表服务实现类
 * 标准RBAC模型：用户 -> 角色 -> 权限
 *
 * @author zhn
 * @since 2025-08-15 01:30:00
 */
@Service("permissionService")
public class PermissionServiceImpl extends ServiceImpl<PermissionDao, Permission> implements PermissionService {

    @Resource
    private RolePermissionService rolePermissionService;

    @Resource
    private RoleService roleService;

    @Override
    public List<Permission> listByRoleIds(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<RolePermission> rolePermissionQueryWrapper = new LambdaQueryWrapper<>();
        rolePermissionQueryWrapper.in(RolePermission::getRoleId, roleIds);
        List<RolePermission> rolePermissions = rolePermissionService.list(rolePermissionQueryWrapper);

        List<Long> permissionIds = rolePermissions.stream().map(RolePermission::getPermissionId).distinct().toList();

        if (CollectionUtils.isEmpty(permissionIds)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Permission> permissionQueryWrapper = new LambdaQueryWrapper<>();
        permissionQueryWrapper.in(Permission::getId, permissionIds);
        return list(permissionQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePermission(PermissionRequest request) {
        Permission permission;
        if (Objects.nonNull(request.getId())) {
            // 更新
            permission = this.getById(request.getId());
            AssertUtil.notNull(permission, ResponseEnum.NOT_FOUND);

            // 权限编码不允许修改
            AssertUtil.equals(permission.getPermissionCode(), request.getPermissionCode(), ResponseEnum.PERMISSION_CODE_NOT_ALLOW_MODIFY);
        } else {
            // 新增校验权限编码是否重复
            long count = this.count(new LambdaQueryWrapper<Permission>().eq(Permission::getPermissionCode, request.getPermissionCode()));
            AssertUtil.isTrue(count == 0, ResponseEnum.PERMISSION_CODE_REPEAT);

            permission = new Permission();
        }

        permission.setPermissionName(request.getPermissionName());
        permission.setPermissionCode(request.getPermissionCode());
        permission.setDescription(request.getDescription());
        permission.setPermissionType(request.getPermissionType());
        permission.setParentId(request.getParentId());
        permission.setSortOrder(Objects.nonNull(request.getSortOrder()) ? request.getSortOrder() : 1);

        this.saveOrUpdate(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        // 1. 校验权限是否存在
        Permission permission = this.getById(id);
        AssertUtil.notNull(permission, ResponseEnum.NOT_FOUND);

        // 2. 校验是否存在子权限
        long childCount = this.count(new LambdaQueryWrapper<Permission>().eq(Permission::getParentId, id));
        AssertUtil.isTrue(childCount == 0, ResponseEnum.PERMISSION_CODE_EXITS_SUB_LEVEL);

        // 3. 删除权限与角色的关联关系
        rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getPermissionId, id));

        // 4. 删除权限
        this.removeById(id);
    }

    @Override
    public PermissionResponse.PermissionDetail getPermissionDetailById(Long id) {
        Permission permission = this.getById(id);
        AssertUtil.notNull(permission, ResponseEnum.NOT_FOUND);

        PermissionResponse.PermissionDetail detail = new PermissionResponse.PermissionDetail();
        BeanUtils.copyProperties(permission, detail);

        // 查询该权限分配给了哪些角色
        LambdaQueryWrapper<RolePermission> rpQueryWrapper = new LambdaQueryWrapper<>();
        rpQueryWrapper.eq(RolePermission::getPermissionId, id);
        List<RolePermission> rolePermissions = rolePermissionService.list(rpQueryWrapper);

        if (!CollectionUtils.isEmpty(rolePermissions)) {
            List<Long> roleIds = rolePermissions.stream().map(RolePermission::getRoleId).toList();
            List<Role> roles = roleService.listByIds(roleIds);
            detail.setRoles(roles);
        } else {
            detail.setRoles(Collections.emptyList());
        }

        return detail;
    }

    @Override
    public PageResult<PermissionResponse.PermissionItem> pageQueryPermissions(PermissionPageQuery query) {
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();

        // 权限编码模糊查询
        if (Objects.nonNull(query.getPermissionCode()) && !query.getPermissionCode().isEmpty()) {
            queryWrapper.like(Permission::getPermissionCode, query.getPermissionCode());
        }

        // 权限名称模糊查询
        if (Objects.nonNull(query.getPermissionName()) && !query.getPermissionName().isEmpty()) {
            queryWrapper.like(Permission::getPermissionName, query.getPermissionName());
        }

        // 权限类型精确查询
        if (Objects.nonNull(query.getPermissionType())) {
            queryWrapper.eq(Permission::getPermissionType, query.getPermissionType());
        }

        // 默认按排序号正序
        queryWrapper.orderByAsc(Permission::getSortOrder);

        Page<Permission> permissionPage = this.page(new Page<>(query.getPageNo(), query.getPageSize()), queryWrapper);

        List<PermissionResponse.PermissionItem> permissionItems = permissionPage.getRecords().stream().map(permission -> {
            PermissionResponse.PermissionItem item = new PermissionResponse.PermissionItem();
            BeanUtils.copyProperties(permission, item);
            return item;
        }).toList();

        return PageResult.of(permissionItems, permissionPage.getTotal(), query.getPageNo(), query.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToPermission(Long permissionId, List<Long> roleIds) {
        // 校验权限是否存在
        Permission permission = this.getById(permissionId);
        AssertUtil.notNull(permission, ResponseEnum.NOT_FOUND);

        // 删除该权限的所有角色关联
        rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getPermissionId, permissionId));

        if (!CollectionUtils.isEmpty(roleIds)) {
            // 过滤掉不存在的角色
            List<Role> existingRoles = roleService.listByIds(roleIds);
            List<Long> validRoleIds = existingRoles.stream().map(Role::getId).toList();

            if (!CollectionUtils.isEmpty(validRoleIds)) {
                List<RolePermission> rolePermissions = validRoleIds.stream().map(roleId -> {
                    RolePermission rp = new RolePermission();
                    rp.setRoleId(roleId);
                    rp.setPermissionId(permission.getId());
                    return rp;
                }).toList();
                rolePermissionService.saveBatch(rolePermissions);
            }
        }
    }

    @Override
    public List<PermissionResponse> getPermissionTree() {
        List<Permission> permissions = this.list(new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getSortOrder));

        Map<Long, List<Permission>> parentMap = permissions.stream().collect(Collectors.groupingBy(
                p -> Objects.nonNull(p.getParentId()) ? p.getParentId() : 0L));

        List<Permission> rootPermissions = parentMap.getOrDefault(0L, Collections.emptyList());

        return rootPermissions.stream().map(permission -> {
            PermissionResponse response = new PermissionResponse();
            BeanUtils.copyProperties(permission, response);
            response.setChildren(buildChildren(permission.getId(), parentMap));
            return response;
        }).toList();
    }

    private List<PermissionResponse> buildChildren(Long parentId, Map<Long, List<Permission>> parentMap) {
        List<Permission> children = parentMap.getOrDefault(parentId, Collections.emptyList());
        if (CollectionUtils.isEmpty(children)) {
            return null;
        }
        return children.stream().map(permission -> {
            PermissionResponse response = new PermissionResponse();
            BeanUtils.copyProperties(permission, response);
            response.setChildren(buildChildren(permission.getId(), parentMap));
            return response;
        }).toList();
    }
}
