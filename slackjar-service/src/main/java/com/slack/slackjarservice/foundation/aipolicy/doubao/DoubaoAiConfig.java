package com.slack.slackjarservice.foundation.aipolicy.doubao;

import com.slack.slackjarservice.common.constant.ConfigKeys;
import com.slack.slackjarservice.common.enumtype.foundation.ConfigEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.foundation.model.dto.AiDoubaoConfigDTO;
import com.slack.slackjarservice.foundation.model.response.SysConfigResponse;
import com.slack.slackjarservice.foundation.service.SysConfigService;
import com.volcengine.ark.runtime.service.ArkService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 豆包AI配置管理类
 * 负责豆包AI配置的加载和SDK客户端的初始化
 */
@Slf4j
@Component
public class DoubaoAiConfig {

    @Resource
    private SysConfigService sysConfigService;

    /**
     * 豆包AI模型配置
     */
    @Getter
    private AiDoubaoConfigDTO config;

    /**
     * 豆包SDK ArkService客户端
     */
    @Getter
    private ArkService arkService;

    /**
     * 初始化豆包AI配置
     */
    @PostConstruct
    public void init() {
        log.info("初始化豆包AI配置-start");
        refreshConfig();
        refreshArkService();
        log.info("初始化豆包AI配置-end，AiDoubaoConfigDTO:{}", config);
    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy() {
        config = null;
        if (arkService != null) {
            arkService.shutdownExecutor();
            arkService = null;
        }
    }

    /**
     * 刷新配置和客户端
     */
    public void refresh() {
        destroy();
        init();
    }

    /**
     * 刷新配置
     */
    private void refreshConfig() {
        try {
            SysConfigResponse configResponse = sysConfigService.getConfigByCategory(ConfigEnum.DOUBAO_SEED_1_6.getKey());
            if (configResponse != null && configResponse.getConfigItems() != null) {
                config = new AiDoubaoConfigDTO();
                Map<String, java.util.function.Consumer<String>> configMappers = java.util.Map.of(
                        ConfigKeys.AiDoubao.AI_DOUBAO_API_KEY, config::setApiKey,
                        ConfigKeys.AiDoubao.AI_DOUBAO_MODEL_URL, config::setModelUrl,
                        ConfigKeys.AiDoubao.AI_DOUBAO_MODEL_ID, config::setModelId,
                        ConfigKeys.AiDoubao.AI_DOUBAO_TEMPERATURE, config::setTemperature,
                        ConfigKeys.AiDoubao.AI_DOUBAO_MAX_TOKENS, config::setMaxTokens,
                        ConfigKeys.AiDoubao.AI_DOUBAO_WINDOW_SIZE, config::setWindowSize,
                        ConfigKeys.AiDoubao.AI_DOUBAO_COMPLETIONS_PATH, config::setCompletionsPath,
                        ConfigKeys.AiDoubao.AI_DOUBAO_DEEP_THINKING, config::setDeepThinking
                );
                configResponse.getConfigItems().forEach(item -> {
                    java.util.function.Consumer<String> setter = configMappers.get(item.getConfigKey());
                    if (setter != null) {
                        setter.accept(item.getConfigValue());
                    }
                });
                log.info("豆包AI参数刷新成功");
            } else {
                log.warn("豆包AI参数未配置");
                throw new BusinessException(ResponseEnum.AI_CONFIG);
            }
        } catch (Exception e) {
            log.error("刷新豆包AI配置失败", e);
            throw new BusinessException(ResponseEnum.AI_CONFIG_REFRESH);
        }
    }

    /**
     * 刷新ArkService客户端
     */
    private void refreshArkService() {
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        arkService = ArkService.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getModelUrl() + config.getCompletionsPath())
                .timeout(Duration.ofSeconds(60))
                .connectionPool(connectionPool)
                .dispatcher(dispatcher)
                .build();
        log.info("豆包SDK初始化成功: {}", arkService.toString());
    }
}
