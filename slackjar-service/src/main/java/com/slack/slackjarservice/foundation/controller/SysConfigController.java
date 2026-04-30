package com.slack.slackjarservice.foundation.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.slack.slackjarservice.common.base.BaseController;
import com.slack.slackjarservice.common.enumtype.foundation.OperationEnum;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.foundation.model.dto.CategoryConfigDTO;
import com.slack.slackjarservice.foundation.model.request.SysConfigPageQuery;
import com.slack.slackjarservice.foundation.model.request.SysConfigRequest;
import com.slack.slackjarservice.foundation.model.request.SysConfigSaveRequest;
import com.slack.slackjarservice.foundation.model.response.SysConfigItemResponse;
import com.slack.slackjarservice.foundation.model.response.SysConfigResponse;
import com.slack.slackjarservice.foundation.service.SysConfigService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置字典表(SysConfig)表控制层
 *
 * @author zhn
 * @since 2025-08-26 23:01:00
 */
@RestController
@RequestMapping("/sys-config")
public class SysConfigController extends BaseController {

    @Resource
    private SysConfigService sysConfigService;

    /**
     * 按分类一对多保存配置（创建、修改、删除）
     *
     * @param request 配置请求参数
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @PostMapping("/save")
    public ApiResponse<Boolean> saveConfig(@Validated @RequestBody SysConfigRequest request) {
        sysConfigService.saveConfig(request);
        recordOperateLog(OperationEnum.USER_UPSERT, "保存配置成功，分类：" + request.getCategory());
        return success(true);
    }

    /**
     * 根据分类查询配置
     *
     * @param category 配置分类
     * @return 配置响应
     */
    @GetMapping("/query/category/{category}")
    public ApiResponse<SysConfigResponse> getConfigByCategory(@PathVariable String category) {
        SysConfigResponse response = sysConfigService.getConfigByCategory(category);
        recordOperateLog(OperationEnum.USER_QUERY, "查询配置成功，分类：" + category);
        return success(response);
    }

    /**
     * 保存或修改单个配置
     *
     * @param request 配置保存请求
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @PostMapping("/saveEntity")
    public ApiResponse<Boolean> saveConfigEntity(@Validated @RequestBody SysConfigSaveRequest request) {
        sysConfigService.saveConfigEntity(request);
        recordOperateLog(OperationEnum.USER_UPSERT, "保存配置成功，键：" + request.getConfigKey());
        return success(true);
    }

    /**
     * 根据 ID 删除配置
     *
     * @param id 配置 ID
     * @return 操作结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> deleteConfigById(@PathVariable Long id) {
        sysConfigService.deleteConfigById(id);
        recordOperateLog(OperationEnum.USER_DELETE, "删除配置成功，ID：" + id);
        return success(true);
    }

    /**
     * 分页条件查询配置列表
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    @SaCheckRole(value = {"ROLE_SUPER_ADMIN"}, mode = SaMode.OR)
    @PostMapping("/pageQuery")
    public ApiResponse<PageResult<SysConfigItemResponse>> pageQueryConfigs(@RequestBody SysConfigPageQuery query) {
        PageResult<SysConfigItemResponse> result = sysConfigService.pageQueryConfigs(query);
        recordOperateLog(OperationEnum.USER_QUERY, "分页查询配置列表成功");
        return success(result);
    }
}
