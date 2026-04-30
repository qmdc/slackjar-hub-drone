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
 * 数据字典表，存储字典类型定义(SysDict)表实体类
 *
 * @author zhn
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict")
public class SysDict extends BaseModel {
    /**
     * 字典ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 字典名称（如：性别、状态）
     */
    private String dictName;
    /**
     * 字典编码（如：gender、status，唯一标识）
     */
    private String dictCode;
    /**
     * 字典描述
     */
    private String description;
    /**
     * 状态（0-启用，1-禁用）
     */
    private Integer status;
    /**
     * 排序号
     */
    private Integer sortOrder;
}
