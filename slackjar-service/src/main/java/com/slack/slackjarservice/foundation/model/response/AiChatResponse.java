package com.slack.slackjarservice.foundation.model.response;

import lombok.Data;

/**
 * 对话响应模型
 *
 * @author slackjar
 * @since 2025-01-09
 */
@Data
public class AiChatResponse {

    /**
     * 响应消息内容
     */
    private String content;

    /**
     * 推理内容
     */
    private String reasoningContent;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 使用的token数量
     */
    private Integer tokensUsed;

    /**
     * 响应时间
     */
    private Long timestamp;

    /**
     * 花费时间
     */
    private Integer spendTime;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 错误信息（如果有）
     */
    private String error;

}
