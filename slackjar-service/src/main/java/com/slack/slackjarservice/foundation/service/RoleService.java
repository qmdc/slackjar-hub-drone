package com.slack.slackjarservice.foundation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.foundation.entity.Role;
import com.slack.slackjarservice.foundation.entity.SysUser;
import com.slack.slackjarservice.foundation.model.request.RolePageQuery;
import com.slack.slackjarservice.foundation.model.request.RoleRequest;
import com.slack.slackjarservice.foundation.model.request.RoleUserPageQuery;
import com.slack.slackjarservice.foundation.model.response.RoleResponse;

import java.util.List;
import java.util.Map;

/**
 * 角色信息表，存储系统中所有角色定义(Role)表服务接口
 *
 * @author zhn
 * @since 2025-08-15 00:18:01
 */
public interface RoleService extends IService<Role> {

    /**
     * 通过用户id查询用户角色列表
     *
     * @param loginId 用户id
     * @return 用户角色列表
     */
    List<Role> listByUserId(String loginId);

    /**
     * 保存角色（新增或更新）
     *
     * @param request 角色请求参数
     */
    void saveRole(RoleRequest request);

    /**
     * 删除角色
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 为角色分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 批量获取角色的权限列表
     *
     * @param roleIds 角色 ID 列表
     * @return Map<roleId, 权限列表>
     */
    Map<Long, List<com.slack.slackjarservice.foundation.model.response.PermissionResponse.PermissionItem>> getRolePermissionsBatch(List<Long> roleIds);

    /**
     * 分页条件查询角色列表
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageResult<RoleResponse.RoleItem> pageQueryRoles(RolePageQuery query);

    /**
     * 为角色分配用户
     *
     * @param roleId  角色 ID
     * @param userIds 用户 ID 列表
     */
    void assignUsersToRole(Long roleId, List<Long> userIds);

    /**
     * 获取角色的用户列表
     *
     * @param roleId 角色 ID
     * @return 用户列表
     */
    List<SysUser> getUsersByRoleId(Long roleId);

    /**
     * 分页查询角色的用户列表
     *
     * @param roleId 角色 ID
     * @param query  分页查询条件
     * @return 分页结果
     */
    PageResult<SysUser> pageQueryRoleUsers(Long roleId, RoleUserPageQuery query);
}

