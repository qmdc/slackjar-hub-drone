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
 * 数据字典项表，存储字典项定义(SysDictItem)表实体类
 *
 * @author zhn
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class SysDictItem extends BaseModel {
    /**
     * 字典项ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 字典ID（关联sys_dict表主键）
     */
    private Long dictId;
    /**
     * 字典项标签（如：男、女）
     */
    private String itemLabel;
    /**
     * 字典项值（如：1、2）
     */
    private String itemValue;
    /**
     * 字典项描述
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
