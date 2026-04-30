package com.slack.slackjarservice.foundation.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色请求模型
 *
 * @author zhn
 */
@Data
public class RoleRequest {

    /**
     * 角色ID（更新时必填）
     */
    private Long id;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 1, max = 50, message = "角色名称长度必须在1-50个字符之间")
    private String roleName;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(min = 1, max = 50, message = "角色编码长度必须在1-50个字符之间")
    private String roleCode;

    /**
     * 角色描述
     */
    @Size(max = 200, message = "角色描述长度不能超过200个字符")
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
     * 权限ID列表（分配权限时使用）
     */
    private List<Long> permissionIds;
}
