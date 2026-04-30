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
 * 用户与角色的关联表，维护多对多关系(UserRole)表实体类
 *
 * @author zhn
 * @since 2025-08-15 01:27:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("user_role")
public class UserRole extends BaseModel {
    /**
     * 关联记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户ID（关联用户表主键）
     */
    private Long userId;
    /**
     * 角色ID（关联角色表主键）
     */
    private Long roleId;
}

