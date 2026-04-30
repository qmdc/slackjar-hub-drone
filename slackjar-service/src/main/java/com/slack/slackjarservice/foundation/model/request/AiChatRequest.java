package com.slack.slackjarservice.foundation.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 对话请求模型
 *
 * @author slackjar
 * @since 2025-01-09
 */
@Data
public class AiChatRequest {

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 提示语
     */
    @Size(max = 512, message = "提示语不能超过512个字符")
    private String prompt;

    /**
     * 业务类型:AiChatBizTypeEnum->code
     */
    @NotBlank(message = "业务类型不能为空")
    private String aiChatBizType;

    /**
     * 会话ID，用于保持对话上下文
     */
    private String sessionId;

    /**
     * 是否启用流式响应
     */
    private Boolean stream = false;

    /**
     * 对话历史，用于保持上下文记忆
     */
    private List<ChatMessage> chatHistory;

    /**
     * AI模型提供商，用于指定使用哪个模型
     */
    @NotNull(message = "AI模型提供商不能为空")
    private String modelVendor;

    /**
     * 聊天消息内部类
     */
    @Data
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
