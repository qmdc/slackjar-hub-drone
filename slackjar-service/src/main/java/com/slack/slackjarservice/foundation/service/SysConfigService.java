package com.slack.slackjarservice.foundation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.foundation.entity.SysConfig;
import com.slack.slackjarservice.foundation.model.dto.CategoryConfigDTO;
import com.slack.slackjarservice.foundation.model.dto.ServerConfigDTO;
import com.slack.slackjarservice.foundation.model.request.SysConfigPageQuery;
import com.slack.slackjarservice.foundation.model.request.SysConfigRequest;
import com.slack.slackjarservice.foundation.model.request.SysConfigSaveRequest;
import com.slack.slackjarservice.foundation.model.response.SysConfigItemResponse;
import com.slack.slackjarservice.foundation.model.response.SysConfigResponse;

import java.util.List;

/**
 * 系统配置字典表(SysConfig)表服务接口
 *
 * @author zhn
 * @since 2025-08-26 23:01:00
 */
public interface SysConfigService extends IService<SysConfig> {

    /**
     * 保存配置（先删除后新增，一对多）
     *
     * @param request 配置请求参数
     */
    void saveConfig(SysConfigRequest request);

    /**
     * 保存或修改单个配置
     *
     * @param request 配置保存请求
     */
    void saveConfigEntity(SysConfigSaveRequest request);

    /**
     * 根据 ID 删除配置
     *
     * @param id 配置 ID
     */
    void deleteConfigById(Long id);

    /**
     * 分页条件查询配置列表
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageResult<SysConfigItemResponse> pageQueryConfigs(SysConfigPageQuery query);

    /**
     * 根据分类查询配置
     *
     * @param category 配置分类
     * @return 配置响应
     */
    SysConfigResponse getConfigByCategory(String category);

    /**
     * 获取主服务器配置参数 DTO
     */
    ServerConfigDTO getServerConfigDTO();
}
