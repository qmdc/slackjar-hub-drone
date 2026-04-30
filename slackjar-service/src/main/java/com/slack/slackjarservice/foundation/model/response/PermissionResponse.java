package com.slack.slackjarservice.foundation.model.response;

import com.slack.slackjarservice.foundation.entity.Role;
import lombok.Data;

import java.util.List;

/**
 * 权限响应模型
 *
 * @author zhn
 */
@Data
public class PermissionResponse {

    private Long id;

    private String permissionName;

    private String permissionCode;

    private String description;

    private Integer permissionType;

    private Long parentId;

    private Integer sortOrder;

    /**
     * 子权限列表（用于树形结构）
     */
    private List<PermissionResponse> children;

    /**
     * 权限项（用于列表展示）
     */
    @Data
    public static class PermissionItem {
        private Long id;
        private String permissionName;
        private String permissionCode;
        private String description;
        private Integer permissionType;
        private Long parentId;
        private Integer sortOrder;
        private String roleCode;
        private String roleId;
        private Integer roleStatus;
    }

    /**
     * 权限详情（包含分配的角色列表）
     */
    @Data
    public static class PermissionDetail {
        private Long id;
        private String permissionName;
        private String permissionCode;
        private String description;
        private Integer permissionType;
        private Long parentId;
        private Integer sortOrder;
        /**
         * 该权限分配给了哪些角色
         */
        private List<Role> roles;
    }
}
