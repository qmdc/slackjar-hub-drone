package com.slack.slackjarservice.foundation.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.slack.slackjarservice.common.base.BaseController;
import com.slack.slackjarservice.common.enumtype.foundation.OperationEnum;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.foundation.model.request.PermissionPageQuery;
import com.slack.slackjarservice.foundation.model.request.PermissionRequest;
import com.slack.slackjarservice.foundation.model.response.PermissionResponse;
import com.slack.slackjarservice.foundation.service.PermissionService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限信息表，存储系统中所有权限定义(Permission)表控制层
 *
 * @author zhn
 */
@RestController
@RequestMapping("/permission")
public class PermissionController extends BaseController {

    @Resource
    private PermissionService permissionService;

    /**
     * 分页条件查询权限列表
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    @PostMapping("/pageQuery")
    public ApiResponse<PageResult<PermissionResponse.PermissionItem>> pageQueryPermissions(@RequestBody PermissionPageQuery query) {
        PageResult<PermissionResponse.PermissionItem> result = permissionService.pageQueryPermissions(query);
        recordOperateLog(OperationEnum.USER_QUERY, "分页查询权限列表成功");
        return success(result);
    }

    /**
     * 保存权限（新增、修改）
     *
     * @param request 权限请求参数
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @PostMapping("/save")
    public ApiResponse<Boolean> savePermission(@Validated @RequestBody PermissionRequest request) {
        permissionService.savePermission(request);
        recordOperateLog(OperationEnum.USER_UPSERT, "保存权限成功：" + request.getPermissionName());
        return success(true);
    }

    /**
     * 删除权限
     *
     * @param id 权限 ID
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        recordOperateLog(OperationEnum.USER_DELETE, "删除权限成功，ID：" + id);
        return success(true);
    }

    /**
     * 获取权限详情（包含分配的角色列表）
     *
     * @param id 权限 ID
     * @return 权限详情
     */
    @GetMapping("/detail/{id}")
    public ApiResponse<PermissionResponse.PermissionDetail> getPermissionDetail(@PathVariable Long id) {
        PermissionResponse.PermissionDetail detail = permissionService.getPermissionDetailById(id);
        recordOperateLog(OperationEnum.USER_QUERY, "查询权限详情成功，ID：" + id);
        return success(detail);
    }

    /**
     * 将权限分配给角色
     *
     * @param permissionId 权限 ID
     * @param roleIds      角色 ID 列表
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @PostMapping("/assignRoles/{permissionId}")
    public ApiResponse<Boolean> assignRolesToPermission(@PathVariable Long permissionId, @RequestBody List<Long> roleIds) {
        permissionService.assignRolesToPermission(permissionId, roleIds);
        recordOperateLog(OperationEnum.USER_UPSERT, "为权限分配角色成功，权限 ID：" + permissionId);
        return success(true);
    }

    /**
     * 获取权限树
     *
     * @return 权限树
     */
    @GetMapping("/getTree")
    public ApiResponse<List<PermissionResponse>> getPermissionTree() {
        List<PermissionResponse> tree = permissionService.getPermissionTree();
        recordOperateLog(OperationEnum.USER_QUERY, "查询权限树成功");
        return success(tree);
    }
}
