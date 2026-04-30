package com.slack.slackjarservice.common.constant;

/**
 * 提示语常量
 */
public class PromptConstants {

    public static String getBasicChatKey(Integer limitSize) {
        return String.format("[严格遵守：回答字数控制在%d字以内，无论前文的prompt模板提何种字数要求，均以最后的字数限制作为唯一标准创作]", limitSize);
    }
}
