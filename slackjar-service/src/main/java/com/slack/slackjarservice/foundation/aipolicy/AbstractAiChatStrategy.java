package com.slack.slackjarservice.foundation.aipolicy;

import com.slack.slackjarservice.common.enumtype.foundation.AiChatBizTypeEnum;
import com.slack.slackjarservice.common.enumtype.foundation.AiModelVendorEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.foundation.model.request.AiChatRequest;
import com.slack.slackjarservice.foundation.model.response.AiChatResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * AI聊天策略抽象基类
 * 提供AI聊天策略的通用实现
 */
@Slf4j
public abstract class AbstractAiChatStrategy implements AiChatStrategy {

    /**
     * AI请求日志
     */
    public void logRequest(AiChatRequest request, AiModelVendorEnum currentVendor) {
        log.info("发起AI对话请求:{}, 使用模型:{}", request, currentVendor);
    }

    /**
     * AI请求参数校验
     */
    public void validateParams(AiChatRequest request) {
        boolean validateLength = AiChatBizTypeEnum.validateLength(request.getAiChatBizType(), request.getMessage());
        if (!validateLength) {
            throw new BusinessException(
                    ResponseEnum.AI_CHAT_MESSAGE_LENGTH.getCode(),
                    ResponseEnum.AI_CHAT_MESSAGE_LENGTH.getMessage() + " Max Length:" +
                            AiChatBizTypeEnum.fromCode(request.getAiChatBizType()).getMaxLength());
        }
    }

    /**
     * 创建错误响应
     *
     * @param errorMessage 错误信息
     * @param modelName    模型名称
     * @return 错误响应
     */
    public AiChatResponse createErrorResponse(String errorMessage, String modelName) {
        AiChatResponse response = new AiChatResponse();
        response.setContent("抱歉，我遇到了一些问题，请稍后重试。");
        response.setError(errorMessage);
        response.setTimestamp(System.currentTimeMillis());
        response.setModel(modelName);
        return response;
    }
}
