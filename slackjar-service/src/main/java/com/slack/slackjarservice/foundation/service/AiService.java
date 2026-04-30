package com.slack.slackjarservice.foundation.service;

import com.slack.slackjarservice.foundation.model.request.AiChatRequest;
import com.slack.slackjarservice.foundation.model.response.AiChatResponse;

/**
 * AI服务接口
 *
 * @author slackjar
 * @since 2025-01-09
 */
public interface AiService {

    /**
     * 刷新当前生效的AI模型策略
     */
    void refreshActivePolicy();

    /**
     * 普通对话
     *
     * @param request 对话请求
     * @return 对话响应
     */
    AiChatResponse chat(AiChatRequest request);

    /**
     * 与AI进行流式对话
     *
     * @param request 对话请求
     * @return 流式响应
     */
    reactor.core.publisher.Flux<String> streamChat(AiChatRequest request);

}
