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
 * 系统配置字典表(SysConfig)表实体类
 *
 * @author zhn
 * @since 2025-08-26 23:01:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_config")
public class SysConfig extends BaseModel {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 配置键名
     */
    private String configKey;
    /**
     * 配置值
     */
    private String configValue;
    /**
     * 配置分类
     */
    private String category;
    /**
     * 配置描述
     */
    private String description;
    /**
     * 启用状态（1：禁用、0启用）
     */
    private Integer status;
}

