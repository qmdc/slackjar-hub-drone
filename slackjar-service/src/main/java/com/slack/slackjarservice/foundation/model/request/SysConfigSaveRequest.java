package com.slack.slackjarservice.foundation.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统配置单个保存请求
 *
 * @author zhn
 */
@Data
public class SysConfigSaveRequest {

    /**
     * 配置 ID（更新时必填）
     */
    private Long id;

    /**
     * 配置分类
     */
    @NotBlank(message = "配置分类不能为空")
    @Size(min = 1, max = 50, message = "配置分类长度必须在1-50个字符之间")
    private String category;

    /**
     * 配置键名
     */
    @NotBlank(message = "配置键名不能为空")
    @Size(min = 1, max = 100, message = "配置键名长度必须在1-100个字符之间")
    private String configKey;

    /**
     * 配置值
     */
    @Size(max = 2000, message = "配置值长度不能超过2000个字符")
    private String configValue;

    /**
     * 配置描述
     */
    @Size(max = 500, message = "配置描述长度不能超过500个字符")
    private String description;

    /**
     * 启用状态（1：禁用、0启用）
     */
    private Integer status;
}
