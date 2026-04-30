package com.slack.slackjarservice.foundation.aipolicy;

import com.slack.slackjarservice.common.enumtype.foundation.AiModelVendorEnum;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AI聊天策略管理器
 * 根据传入的模型类型选择对应的策略实现
 */
@Slf4j
@Component
public class AiChatStrategyManager {

    @Resource
    private List<AiChatStrategy> aiChatStrategies;

    private Map<AiModelVendorEnum, AiChatStrategy> strategyMap;

    /**
     * 初始化策略映射
     */
    @PostConstruct
    public void init() {
        log.info("初始化AI聊天策略管理器-start");
        strategyMap = aiChatStrategies.stream().collect(Collectors.toMap(
                AiChatStrategy::getVendorEnum, strategy -> strategy,
                (existing, replacement) -> {
                    log.warn("发现重复的AI策略: {}, 使用后者", existing.getVendorEnum());
                    return replacement;
                }
        ));
        log.info("初始化AI聊天策略管理器-end，共加载 {} 个策略", strategyMap.size());
    }

    /**
     * 根据模型类型获取对应的策略
     *
     * @param vendorEnum 模型提供商枚举
     * @return 对应的AI聊天策略
     */
    public Optional<AiChatStrategy> getStrategy(AiModelVendorEnum vendorEnum) {
        if (strategyMap == null) {
            init();
        }
        return Optional.ofNullable(strategyMap.get(vendorEnum));
    }

    /**
     * 获取所有已注册的策略
     *
     * @return 策略列表
     */
    public List<AiChatStrategy> getAllStrategies() {
        if (strategyMap == null) {
            init();
        }
        return List.copyOf(strategyMap.values());
    }
}
