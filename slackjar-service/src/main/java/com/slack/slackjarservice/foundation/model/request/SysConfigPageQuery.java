package com.slack.slackjarservice.foundation.model.request;

import com.slack.slackjarservice.common.base.BasePagination;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置分页查询请求
 *
 * @author zhn
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfigPageQuery extends BasePagination {

    /**
     * 配置分类（精确查询）
     */
    private String category;

    /**
     * 配置键名（精确查询）
     */
    private String configKey;

    /**
     * 配置状态（精确查询）
     */
    private Integer status;

    /**
     * 配置描述（模糊查询）
     */
    private String description;
}
