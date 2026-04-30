package com.slack.slackjarservice.foundation.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限请求模型
 *
 * @author zhn
 */
@Data
public class PermissionRequest {

    /**
     * 权限ID（更新时必填）
     */
    private Long id;

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(min = 1, max = 50, message = "权限名称长度必须在1-50个字符之间")
    private String permissionName;

    /**
     * 权限编码
     */
    @NotBlank(message = "权限编码不能为空")
    @Size(min = 1, max = 50, message = "权限编码长度必须在1-50个字符之间")
    private String permissionCode;

    /**
     * 权限描述
     */
    @Size(max = 200, message = "权限描述长度不能超过200个字符")
    private String description;

    /**
     * 权限类型（1-菜单权限，2-按钮权限，3-接口权限）
     */
    @NotNull(message = "权限类型不能为空")
    private Integer permissionType;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 排序号
     */
    private Integer sortOrder;
}
