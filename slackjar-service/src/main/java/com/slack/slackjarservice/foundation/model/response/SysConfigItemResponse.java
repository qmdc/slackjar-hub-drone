package com.slack.slackjarservice.foundation.model.response;

import lombok.Data;

/**
 * 系统配置分页项响应
 *
 * @author zhn
 */
@Data
public class SysConfigItemResponse {

    private Long id;

    private String configKey;

    private String configValue;

    private String category;

    private String description;

    /**
     * 启用状态（1：禁用、0启用）
     */
    private Integer status;

    private Long createTime;

    private Long updateTime;
}
