package com.slack.slackjarservice.common.enumtype.foundation;

import com.slack.slackjarservice.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI对话业务类型枚举
 */
@Getter
@AllArgsConstructor
public enum AiChatBizTypeEnum {

    /**
     * 基础对话
     */
    BASIC_CHAT("BASIC_CHAT", "基础对话", 200),

    /**
     * 流式对话
     */
    STREAM_CHAT("STREAM_CHAT", "流式对话", 500),

    /**
     * 内容总结
     */
    TEXT_SUMMARY("TEXT_SUMMARY", "内容总结", 2000),

    /**
     * 异常分析
     */
    EXCEPTION_ANALYSIS("EXCEPTION_ANALYSIS", "异常分析", 2000),

    /**
     * 智能命名
     */
    INTELLIGENT("INTELLIGENT", "智能命名", 100),

    /**
     * 文本翻译
     */
    TRANSLATION("TRANSLATION", "文本翻译", 500),
    ;

    /**
     * 业务类型编码（英文命名）
     */
    private final String code;

    /**
     * 业务类型描述
     */
    private final String desc;

    /**
     * 最大长度限制（字符数）
     */
    private final Integer maxLength;

    /**
     * 根据编码获取枚举值
     *
     * @param code 编码
     * @return 对应的枚举值，未找到返回null
     */
    public static AiChatBizTypeEnum fromCode(String code) {
        if (code == null) {
            throw new BusinessException(ResponseEnum.AI_CHAT_MESSAGE_TYPE);
        }
        for (AiChatBizTypeEnum type : AiChatBizTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new BusinessException(ResponseEnum.AI_CHAT_MESSAGE_TYPE);
    }

    /**
     * 校验给定文本长度是否超过当前枚举值的最大长度限制
     *
     * @param code 业务类型编码
     * @param text 待校验的文本
     * @return 如果文本长度未超过限制返回true，否则返回false
     */
    public static boolean validateLength(String code, String text) {
        AiChatBizTypeEnum type = fromCode(code);
        return text.length() <= type.getMaxLength();
    }
}
