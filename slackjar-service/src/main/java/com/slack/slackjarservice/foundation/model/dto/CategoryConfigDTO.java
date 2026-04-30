package com.slack.slackjarservice.foundation.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 按分类分组的配置DTO
 *
 * @author zhn
 * @since 2025-08-26
 */
@Data
public class CategoryConfigDTO implements Serializable {
    /**
     * 配置分类名称
     */
    private String categoryName;

    /**
     * 该分类下的配置项列表
     */
    private List<ConfigItem> configItems;

    @Data
    public static class ConfigItem implements Serializable {
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
