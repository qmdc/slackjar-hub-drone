package com.slack.slackjarservice.foundation.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.slack.slackjarservice.common.base.BaseModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 角色信息表，存储系统中所有角色定义(Role)表实体类
 *
 * @author zhn
 * @since 2025-08-15 01:27:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class Role extends BaseModel {
    /**
     * 角色ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 角色名称（如：管理员、普通用户）
     */
    private String roleName;
    /**
     * 角色编码（如：ROLE_ADMIN，唯一标识）
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
}

