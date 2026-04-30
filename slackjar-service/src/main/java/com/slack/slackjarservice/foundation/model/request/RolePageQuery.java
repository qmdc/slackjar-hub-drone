package com.slack.slackjarservice.foundation.model.request;

import com.slack.slackjarservice.common.base.BasePagination;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色分页查询请求
 *
 * @author zhn
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageQuery extends BasePagination {

    /**
     * 角色编码（模糊查询）
     */
    private String roleCode;

    /**
     * 角色名称（模糊查询）
     */
    private String roleName;

    /**
     * 角色类型（1-系统角色，2-自定义角色）
     */
    private Integer roleType;

    /**
     * 状态（0-启用，1-禁用）
     */
    private Integer status;
}
