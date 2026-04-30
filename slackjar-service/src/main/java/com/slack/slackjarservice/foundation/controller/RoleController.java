package com.slack.slackjarservice.foundation.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.slack.slackjarservice.common.base.BaseController;
import com.slack.slackjarservice.common.enumtype.foundation.OperationEnum;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.foundation.entity.SysUser;
import com.slack.slackjarservice.foundation.model.request.RolePageQuery;
import com.slack.slackjarservice.foundation.model.request.RoleRequest;
import com.slack.slackjarservice.foundation.model.request.RoleUserPageQuery;
import com.slack.slackjarservice.foundation.model.response.PermissionResponse;
import com.slack.slackjarservice.foundation.model.response.RoleResponse;
import com.slack.slackjarservice.foundation.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色信息表，存储系统中所有角色定义(Role)表控制层
 *
 * @author zhn
 * @since 2025-08-15 01:27:12
 */
@RestController
@RequestMapping("/role")
public class RoleController extends BaseController {

    @Resource
    private RoleService roleService;

    /**
     * 分页条件查询角色列表
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    @PostMapping("/pageQuery")
    public ApiResponse<PageResult<RoleResponse.RoleItem>> pageQueryRoles(@RequestBody RolePageQuery query) {
        PageResult<RoleResponse.RoleItem> result = roleService.pageQueryRoles(query);
        recordOperateLog(OperationEnum.USER_QUERY, "分页查询角色列表成功");
        return success(result);
    }

    /**
     * 保存角色（新增、修改）
     *
     * @param request 角色请求参数
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @PostMapping("/save")
    public ApiResponse<Boolean> saveRole(@Validated @RequestBody RoleRequest request) {
        roleService.saveRole(request);
        recordOperateLog(OperationEnum.USER_UPSERT, "保存角色成功：" + request.getRoleName());
        return success(true);
    }

    /**
     * 删除角色
     *
     * @param id 角色ID
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        recordOperateLog(OperationEnum.USER_DELETE, "删除角色成功，ID：" + id);
        return success(true);
    }

    /**
     * 为角色分配权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @PostMapping("/assignPermissions/{roleId}")
    public ApiResponse<Boolean> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(roleId, permissionIds);
        recordOperateLog(OperationEnum.USER_UPSERT, "为角色分配权限成功，角色ID：" + roleId);
        return success(true);
    }

    /**
     * 批量获取角色的权限列表
     *
     * @param roleIds 角色 ID 列表
     * @return 所有角色的权限列表（包含 roleCode 和 roleIdCode）
     */
    @PostMapping("/getPermissions/batch")
    public ApiResponse<List<PermissionResponse.PermissionItem>> getRolePermissionsBatch(@RequestBody List<Long> roleIds) {
        Map<Long, List<PermissionResponse.PermissionItem>> result = roleService.getRolePermissionsBatch(roleIds);
        // 将 Map 转换为扁平化的 List
        List<PermissionResponse.PermissionItem> flatList = result.values().stream().flatMap(List::stream).toList();
        recordOperateLog(OperationEnum.USER_QUERY, "批量查询角色权限成功，角色数量：" + (roleIds != null ? roleIds.size() : 0));
        return success(flatList);
    }

    /**
     * 为角色分配用户
     *
     * @param userIds 分配用户列表
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @PostMapping("/assignUsers/{roleId}")
    public ApiResponse<Boolean> assignUsersToRole(@PathVariable Long roleId, @RequestBody List<Long> userIds) {
        roleService.assignUsersToRole(roleId, userIds);
        recordOperateLog(OperationEnum.USER_UPSERT, "为角色分配用户成功，角色 ID：" + roleId);
        return success(true);
    }

    /**
     * 获取角色的用户列表
     *
     * @param roleId 角色 ID
     * @return 用户列表
     */
    @GetMapping("/getUsers/{roleId}")
    public ApiResponse<List<SysUser>> getUsersByRoleId(@PathVariable Long roleId) {
        List<SysUser> users = roleService.getUsersByRoleId(roleId);
        recordOperateLog(OperationEnum.USER_QUERY, "查询角色用户成功，角色 ID：" + roleId);
        return success(users);
    }

    /**
     * 分页查询角色的用户列表
     *
     * @param roleId 角色 ID
     * @param query  分页查询条件
     * @return 分页结果
     */
    @PostMapping("/getUsers/{roleId}/pageQuery")
    public ApiResponse<PageResult<SysUser>> pageQueryRoleUsers(@PathVariable Long roleId, @RequestBody RoleUserPageQuery query) {
        PageResult<SysUser> result = roleService.pageQueryRoleUsers(roleId, query);
        recordOperateLog(OperationEnum.USER_QUERY, "分页查询角色用户成功，角色 ID：" + roleId);
        return success(result);
    }
}
