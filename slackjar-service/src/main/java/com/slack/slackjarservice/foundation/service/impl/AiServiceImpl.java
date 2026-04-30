package com.slack.slackjarservice.foundation.service.impl;

import com.slack.slackjarservice.common.enumtype.foundation.AiModelVendorEnum;
import com.slack.slackjarservice.common.enumtype.foundation.PushWithBackendEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.foundation.aipolicy.AbstractAiChatStrategy;
import com.slack.slackjarservice.foundation.aipolicy.AiChatStrategy;
import com.slack.slackjarservice.foundation.aipolicy.AiChatStrategyManager;
import com.slack.slackjarservice.foundation.aipolicy.doubao.DoubaoAiConfig;
import com.slack.slackjarservice.foundation.model.dto.SocketMessageDTO;
import com.slack.slackjarservice.foundation.model.request.AiChatRequest;
import com.slack.slackjarservice.foundation.model.response.AiChatResponse;
import com.slack.slackjarservice.foundation.service.AiService;
import com.slack.slackjarservice.foundation.socketio.BackendMessagePush;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * AI服务实现类
 * 使用策略模式封装不同AI模型的调用
 *
 * @author slackjar
 * @since 2025-01-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    @Resource
    private AiChatStrategyManager aiChatStrategyManager;

    @Resource
    private AbstractAiChatStrategy abstractAiChatStrategy;

    @Resource
    private BackendMessagePush backendMessagePush;

    @Resource
    private DoubaoAiConfig doubaoAiConfig;

    @Resource(name = "databaseExecutor")
    private Executor databaseExecutor;

    @Override
    public void refreshActivePolicy() {
        // 刷新所有AI策略的配置
        aiChatStrategyManager.getAllStrategies().forEach(strategy -> {
            log.info("刷新AI策略: {}", strategy.getVendorEnum());
            strategy.refreshConfig();
        });
        backendMessagePush.broadcastMessage(new SocketMessageDTO("AI配置刷新成功", PushWithBackendEnum.SUCCESS_STRING_NOTICE.getCode()));
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        AiModelVendorEnum vendorEnum = AiModelVendorEnum.getByCode(request.getModelVendor());
        try {
            Optional<AiChatStrategy> chatStrategy = aiChatStrategyManager.getStrategy(vendorEnum);

            return chatStrategy.map(strategy -> {
                AbstractAiChatStrategy abstractStrategy = (AbstractAiChatStrategy) strategy;
                abstractStrategy.logRequest(request, vendorEnum);
                abstractStrategy.validateParams(request);
                return strategy.chat(request);
            }).orElseThrow(() -> new BusinessException(ResponseEnum.AI_CHAT_PROCESS));
        } catch (Exception e) {
            log.error("调用AI服务失败", e);
            return abstractAiChatStrategy.createErrorResponse(e.getMessage(), vendorEnum.getCode());
        }
    }

    @Override
    public Flux<String> streamChat(AiChatRequest request) {
        try {
            AiModelVendorEnum vendorEnum = AiModelVendorEnum.getByCode(request.getModelVendor());
            Optional<AiChatStrategy> chatStrategy = aiChatStrategyManager.getStrategy(vendorEnum);

            return chatStrategy.map(strategy -> {
                AbstractAiChatStrategy abstractStrategy = (AbstractAiChatStrategy) strategy;
                abstractStrategy.logRequest(request, vendorEnum);
                abstractStrategy.validateParams(request);
                return strategy.streamChat(request);
            }).orElseThrow(() -> new BusinessException(ResponseEnum.AI_CHAT_PROCESS));
        } catch (Exception e) {
            log.error("AI流式对话失败", e);
            return Flux.error(new BusinessException(ResponseEnum.AI_CHAT_PROCESS));
        }
    }

}
