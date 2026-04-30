package com.slack.slackjarservice.foundation.model.request;

import com.slack.slackjarservice.common.base.BasePagination;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典分页查询请求
 *
 * @author zhn
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictPageQuery extends BasePagination {

    /**
     * 字典名称（模糊查询）
     */
    private String dictName;

    /**
     * 字典编码（模糊查询）
     */
    private String dictCode;

    /**
     * 状态（0-启用，1-禁用）
     */
    private Integer status;
}
