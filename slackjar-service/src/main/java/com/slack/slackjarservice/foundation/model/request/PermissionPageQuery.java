package com.slack.slackjarservice.foundation.model.request;

import com.slack.slackjarservice.common.base.BasePagination;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限分页查询请求
 *
 * @author zhn
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PermissionPageQuery extends BasePagination {

    /**
     * 权限编码（模糊查询）
     */
    private String permissionCode;

    /**
     * 权限名称（模糊查询）
     */
    private String permissionName;

    /**
     * 权限类型（1-菜单权限，2-按钮权限，3-接口权限）
     */
    private Integer permissionType;
}
