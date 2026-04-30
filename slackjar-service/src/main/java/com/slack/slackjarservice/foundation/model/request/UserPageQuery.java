package com.slack.slackjarservice.foundation.model.request;

import com.slack.slackjarservice.common.base.BasePagination;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询请求
 *
 * @author zhn
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageQuery extends BasePagination {

    /**
     * 用户名（模糊查询）
     */
    private String username;

    /**
     * 昵称（模糊查询）
     */
    private String nickname;

    /**
     * 状态（0-正常，1-禁用）
     */
    private Integer status;
}
