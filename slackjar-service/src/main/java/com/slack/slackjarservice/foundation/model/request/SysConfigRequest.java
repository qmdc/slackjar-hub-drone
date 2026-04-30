package com.slack.slackjarservice.foundation.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 系统配置请求模型
 *
 * @author zhn
 * @since 2025-08-26
 */
@Data
public class SysConfigRequest {
    /**
     * 配置分类
     */
    @NotBlank(message = "配置分类不能为空")
    @Size(min = 1, max = 50, message = "配置分类长度必须在1-50个字符之间")
    private String category;

    /**
     * 配置列表
     */
    @Valid
    private List<ConfigItem> configItems;

    @Data
    public static class ConfigItem {
        /**
         * 配置键名
         */
        @Size(min = 1, max = 100, message = "配置键名长度必须在1-100个字符之间")
        private String configKey;

        /**
         * 配置值
         */
        @Size(max = 2000, message = "配置值长度不能超过2000个字符")
        private String configValue;

        /**
         * 配置描述（可选）
         */
        @Size(max = 500, message = "配置描述长度不能超过500个字符")
        private String description;

        /**
         * 启用状态（1：禁用、0启用）（可选）
         */
        private Integer status;
    }
}
