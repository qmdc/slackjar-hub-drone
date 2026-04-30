package com.slack.slackjarservice.common.enumtype.foundation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配置分类枚举
 */
@Getter
@SuppressWarnings("all")
@AllArgsConstructor
public enum ConfigEnum {

    SERVER_PARAMS("server_params", "主服务器参数"),
    SYSTEM_PARAMS("system_params", "系统参数"),
    ALI_OSS_STORAGE("ali_oss_storage", "阿里云OSS"),
    DOUBAO_SEED_1_6("ai_doubao_1_6_key", "豆包1.6"),
    CUSTOMIZE_KEY("customize_key", "自定义配置参数"),
    ;

    private final String key;
    private final String description;

    /**
     * 根据 key 获取枚举
     */
    public static ConfigEnum getByKey(String key) {
        for (ConfigEnum category : values()) {
            if (category.getKey().equals(key)) {
                return category;
            }
        }
        return null;
    }
}
