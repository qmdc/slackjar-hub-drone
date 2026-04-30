package com.slack.slackjarservice.foundation.model.response;

import lombok.Data;

import java.util.List;

/**
 * 系统配置响应模型
 *
 * @author zhn
 * @since 2025-08-26
 */
@Data
public class SysConfigResponse {
    /**
     * 配置分类
     */
    private String category;

    /**
     * 配置列表
     */
    private List<ConfigItem> configItems;

    @Data
    public static class ConfigItem {
        /**
         * 配置键名
         */
        private String configKey;

        /**
         * 配置值
         */
        private String configValue;

        /**
         * 配置描述
         */
        private String description;

        /**
         * 启用状态（1：禁用、0启用）
         */
        private Integer status;
    }
}
