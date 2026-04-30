package com.slack.slackjarservice.foundation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.foundation.entity.Permission;
import com.slack.slackjarservice.foundation.model.request.PermissionPageQuery;
import com.slack.slackjarservice.foundation.model.request.PermissionRequest;
import com.slack.slackjarservice.foundation.model.response.PermissionResponse;

import java.util.List;

/**
 * 权限信息表，存储系统中所有权限定义(Permission)表服务接口
 *
 * @author zhn
 * @since 2025-08-15 01:30:00
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 通过角色 ID 列表查询权限列表
     *
     * @param roleIds 角色 ID 列表
     * @return 权限列表
     */
    List<Permission> listByRoleIds(List<Long> roleIds);

    /**
     * 保存权限（新增或更新）
     *
     * @param request 权限请求参数
     */
    void savePermission(PermissionRequest request);

    /**
     * 删除权限
     *
     * @param id 权限 ID
     */
    void deletePermission(Long id);

    /**
     * 获取权限详情（包含分配的角色列表）
     *
     * @param id 权限 ID
     * @return 权限详情
     */
    PermissionResponse.PermissionDetail getPermissionDetailById(Long id);

    /**
     * 分页条件查询权限列表
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageResult<PermissionResponse.PermissionItem> pageQueryPermissions(PermissionPageQuery query);

    /**
     * 将权限分配给角色
     *
     * @param permissionId 权限 ID
     * @param roleIds      角色 ID 列表
     */
    void assignRolesToPermission(Long permissionId, List<Long> roleIds);

    /**
     * 获取权限树
     *
     * @return 权限树
     */
    List<PermissionResponse> getPermissionTree();
}
