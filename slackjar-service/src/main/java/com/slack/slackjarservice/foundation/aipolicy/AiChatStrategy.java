package com.slack.slackjarservice.foundation.aipolicy;

import com.slack.slackjarservice.common.enumtype.foundation.AiModelVendorEnum;
import com.slack.slackjarservice.foundation.model.request.AiChatRequest;
import com.slack.slackjarservice.foundation.model.response.AiChatResponse;
import reactor.core.publisher.Flux;

/**
 * AI聊天策略接口
 * 定义所有AI模型聊天行为的通用接口
 */
public interface AiChatStrategy {

    /**
     * 获取模型枚举
     *
     * @return 模型名称
     */
    AiModelVendorEnum getVendorEnum();

    /**
     * 刷新模型配置
     */
    void refreshConfig();

    /**
     * 执行AI对话
     *
     * @param request 对话请求
     * @return 对话响应
     */
    AiChatResponse chat(AiChatRequest request);

    /**
     * 执行AI流式对话
     *
     * @param request 对话请求
     * @return 流式响应
     */
    Flux<String> streamChat(AiChatRequest request);
}
