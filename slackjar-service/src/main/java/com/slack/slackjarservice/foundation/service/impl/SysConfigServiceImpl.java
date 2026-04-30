package com.slack.slackjarservice.foundation.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slack.slackjarservice.common.constant.ConfigKeys;
import com.slack.slackjarservice.common.enumtype.foundation.ConfigEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.common.response.PageResult;
import com.slack.slackjarservice.common.util.AssertUtil;
import com.slack.slackjarservice.foundation.dao.SysConfigDao;
import com.slack.slackjarservice.foundation.entity.SysConfig;
import com.slack.slackjarservice.foundation.event.AiRefreshEvent;
import com.slack.slackjarservice.foundation.event.OssRefreshEvent;
import com.slack.slackjarservice.foundation.model.dto.CategoryConfigDTO;
import com.slack.slackjarservice.foundation.model.dto.ServerConfigDTO;
import com.slack.slackjarservice.foundation.model.request.SysConfigPageQuery;
import com.slack.slackjarservice.foundation.model.request.SysConfigRequest;
import com.slack.slackjarservice.foundation.model.request.SysConfigSaveRequest;
import com.slack.slackjarservice.foundation.model.response.SysConfigItemResponse;
import com.slack.slackjarservice.foundation.model.response.SysConfigResponse;
import com.slack.slackjarservice.foundation.service.SysConfigService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 系统配置字典表(SysConfig)表服务实现类
 *
 * @author zhn
 * @since 2025-08-26 23:01:00
 */
@Service("sysConfigService")
public class SysConfigServiceImpl extends ServiceImpl<SysConfigDao, SysConfig> implements SysConfigService {

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void saveConfig(SysConfigRequest request) {
        // 先删除该分类下的所有配置
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getCategory, request.getCategory());
        this.remove(wrapper);

        // 如果有配置项，则新增配置
        if (request.getConfigItems() != null && !request.getConfigItems().isEmpty()) {
            List<SysConfig> configs = request.getConfigItems().stream().map(item -> {
                SysConfig config = new SysConfig();
                config.setCategory(request.getCategory());
                config.setConfigKey(item.getConfigKey());
                config.setConfigValue(item.getConfigValue());
                config.setDescription(item.getDescription());
                config.setStatus(item.getStatus() != null ? item.getStatus() : 0);
                return config;
            }).collect(Collectors.toList());

            this.saveBatch(configs);

            // SYSTEM_PARAMS、ALI_OSS_STORAGE有变更则触发OSS策略刷新事件
            configs.stream()
                    .filter(config -> ConfigEnum.SYSTEM_PARAMS.getKey().equals(config.getCategory())
                            || ConfigEnum.ALI_OSS_STORAGE.getKey().equals(config.getCategory()))
                    .findFirst()
                    .ifPresent(config -> eventPublisher.publishEvent(new OssRefreshEvent(this)));

            // SYSTEM_PARAMS、AI_DOUBAO_KEY有变更则触发AI策略刷新事件
            configs.stream()
                    .filter(config -> ConfigEnum.SYSTEM_PARAMS.getKey().equals(config.getCategory())
                            || ConfigEnum.DOUBAO_SEED_1_6.getKey().equals(config.getCategory()))
                    .findFirst()
                    .ifPresent(config -> eventPublisher.publishEvent(new AiRefreshEvent(this)));
        }
    }

    @Override
    public SysConfigResponse getConfigByCategory(String category) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getCategory, category);
        List<SysConfig> configs = this.list(wrapper);

        SysConfigResponse response = new SysConfigResponse();
        response.setCategory(category);

        if (configs != null && !configs.isEmpty()) {
            List<SysConfigResponse.ConfigItem> items = configs.stream().map(config -> {
                SysConfigResponse.ConfigItem item = new SysConfigResponse.ConfigItem();
                item.setConfigKey(config.getConfigKey());
                item.setConfigValue(config.getConfigValue());
                item.setDescription(config.getDescription());
                item.setStatus(config.getStatus());
                return item;
            }).collect(Collectors.toList());
            response.setConfigItems(items);
        }

        return response;
    }

    @Override
    public ServerConfigDTO getServerConfigDTO() {
        // 读取主服务器参数配置
        SysConfigResponse serverParams = getConfigByCategory(ConfigEnum.SERVER_PARAMS.getKey());

        // 将SysConfigResponse转换为ServerConfigDTO
        if (CollUtil.isEmpty(serverParams.getConfigItems())) {
            throw new BusinessException(ResponseEnum.SSL_CERT_SERVER_CONFIG_EMPTY);
        }

        Map<String, String> configMap = serverParams.getConfigItems().stream()
                .collect(Collectors.toMap(SysConfigResponse.ConfigItem::getConfigKey, SysConfigResponse.ConfigItem::getConfigValue));

        ServerConfigDTO serverConfig = new ServerConfigDTO();
        serverConfig.setServerIp(configMap.get(ConfigKeys.ServerParams.SERVER_IP));
        serverConfig.setServerPort(Integer.valueOf(configMap.get(ConfigKeys.ServerParams.SERVER_PORT)));
        serverConfig.setServerUsername(configMap.get(ConfigKeys.ServerParams.SERVER_USERNAME));
        serverConfig.setServerPassword(configMap.get(ConfigKeys.ServerParams.SERVER_PASSWORD));
        serverConfig.setSslCertPath(configMap.get(ConfigKeys.ServerParams.SSL_CERT_PATH));

        return serverConfig;
    }

    @Override
    public void saveConfigEntity(SysConfigSaveRequest request) {
        SysConfig config;
        if (Objects.nonNull(request.getId())) {
            config = this.getById(request.getId());
            AssertUtil.notNull(config, ResponseEnum.NOT_FOUND);
            // 修改时校验 category + configKey 组合是否已存在，排除自己
            long count = this.count(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getCategory, request.getCategory())
                    .eq(SysConfig::getConfigKey, request.getConfigKey())
                    .ne(SysConfig::getId, request.getId()));
            AssertUtil.isTrue(count == 0, ResponseEnum.DATA_EXISTS);
        } else {
            // 新增时校验 category + configKey 组合是否已存在
            long count = this.count(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getCategory, request.getCategory())
                    .eq(SysConfig::getConfigKey, request.getConfigKey()));
            AssertUtil.isTrue(count == 0, ResponseEnum.DATA_EXISTS);
            config = new SysConfig();
        }

        config.setCategory(request.getCategory());
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setDescription(request.getDescription());
        config.setStatus(Objects.nonNull(request.getStatus()) ? request.getStatus() : 0);

        this.saveOrUpdate(config);
    }

    @Override
    public void deleteConfigById(Long id) {
        SysConfig config = this.getById(id);
        AssertUtil.notNull(config, ResponseEnum.NOT_FOUND);
        this.removeById(id);
    }

    @Override
    public PageResult<SysConfigItemResponse> pageQueryConfigs(SysConfigPageQuery query) {
        LambdaQueryWrapper<SysConfig> queryWrapper = new LambdaQueryWrapper<>();

        // 分类精确查询
        if (Objects.nonNull(query.getCategory()) && !query.getCategory().isEmpty()) {
            queryWrapper.eq(SysConfig::getCategory, query.getCategory());
        }

        // 配置键名精确查询
        if (Objects.nonNull(query.getConfigKey()) && !query.getConfigKey().isEmpty()) {
            queryWrapper.eq(SysConfig::getConfigKey, query.getConfigKey());
        }

        // 状态精确查询
        if (Objects.nonNull(query.getStatus())) {
            queryWrapper.eq(SysConfig::getStatus, query.getStatus());
        }

        // 描述模糊查询
        if (Objects.nonNull(query.getDescription()) && !query.getDescription().isEmpty()) {
            queryWrapper.like(SysConfig::getDescription, query.getDescription());
        }

        // 默认按创建时间倒序
        queryWrapper.orderByDesc(SysConfig::getCreateTime);

        Page<SysConfig> configPage = this.page(new Page<>(query.getPageNo(), query.getPageSize()), queryWrapper);

        List<SysConfigItemResponse> items = configPage.getRecords().stream().map(config -> {
            SysConfigItemResponse item = new SysConfigItemResponse();
            item.setId(config.getId());
            item.setCategory(config.getCategory());
            item.setConfigKey(config.getConfigKey());
            item.setConfigValue(config.getConfigValue());
            item.setDescription(config.getDescription());
            item.setStatus(config.getStatus());
            item.setCreateTime(config.getCreateTime());
            item.setUpdateTime(config.getUpdateTime());
            return item;
        }).toList();

        return PageResult.of(items, configPage.getTotal(), query.getPageNo(), query.getPageSize());
    }
}

