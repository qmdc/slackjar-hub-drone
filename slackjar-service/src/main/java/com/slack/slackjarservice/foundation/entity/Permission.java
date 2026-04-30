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
 * 权限信息表，存储系统中所有权限定义(Permission)表实体类
 *
 * @author zhn
 * @since 2025-08-15 01:30:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("permission")
public class Permission extends BaseModel {
    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 权限名称（如：用户管理、查看订单）
     */
    private String permissionName;
    /**
     * 权限编码（如：user:manage、order:view，唯一标识）
     */
    private String permissionCode;
    /**
     * 权限描述
     */
    private String description;
    /**
     * 权限类型（1-菜单权限，2-按钮权限，3-接口权限）
     */
    private Integer permissionType;
    /**
     * 父权限ID（用于构建权限树）
     */
    private Long parentId;
    /**
     * 排序号
     */
    private Integer sortOrder;
}
