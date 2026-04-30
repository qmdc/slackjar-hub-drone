package com.slack.slackjarservice.foundation.model.response;

import lombok.Data;

import java.util.List;

/**
 * 角色响应模型
 *
 * @author zhn
 */
@Data
public class RoleResponse {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 角色类型（1-系统角色，2-自定义角色）
     */
    private Integer roleType;

    /**
     * 状态（0-启用，1-禁用）
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;

    /**
     * 角色项（用于列表展示）
     */
    @Data
    public static class RoleItem {
        private Long id;
        private String roleName;
        private String roleCode;
        private String description;
        private Integer roleType;
        private Integer status;
        private Integer sortOrder;
    }
}
