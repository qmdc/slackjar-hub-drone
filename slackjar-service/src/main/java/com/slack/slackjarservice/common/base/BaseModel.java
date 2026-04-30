package com.slack.slackjarservice.common.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 基础实体类，包含通用字段
 * 所有实体类可继承此类，避免重复定义通用字段
 * @author zhn
 */
@Data
public class BaseModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建时间（毫秒时间戳）
     * 插入时自动填充
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Long createTime;

    /**
     * 更新时间（毫秒时间戳）
     * 插入和更新时自动填充
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;

    /**
     * 逻辑删除（0-未删，1-已删）
     * MyBatis-Plus逻辑删除注解，自动过滤已删除数据
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 版本号（用于乐观锁）
     * MyBatis-Plus乐观锁注解，更新时自动校验版本
     */
    @Version
    @TableField("version")
    private Long version;
}
