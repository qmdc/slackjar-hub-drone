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
 * 角色与权限的关联表，维护多对多关系(RolePermission)表实体类
 *
 * @author zhn
 * @since 2025-08-15 01:31:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("role_permission")
public class RolePermission extends BaseModel {
    /**
     * 关联记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 角色ID（关联角色表主键）
     */
    private Long roleId;
    /**
     * 权限ID（关联权限表主键）
     */
    private Long permissionId;
}
