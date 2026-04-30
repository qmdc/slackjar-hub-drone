package com.slack.slackjarservice.foundation.model.response;

import lombok.Data;

import java.util.List;

/**
 * 数据字典响应模型
 *
 * @author zhn
 */
@Data
public class SysDictResponse {

    /**
     * 字典ID
     */
    private Long id;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典编码
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

    /**
     * 字典项列表
     */
    private List<DictItemResponse> dictItems;

    /**
     * 字典项响应
     */
    @Data
    public static class DictItemResponse {
        private Long id;
        private Long dictId;
        private String itemLabel;
        private String itemValue;
        private String description;
        private Integer status;
        private Integer sortOrder;
    }

    /**
     * 字典项（用于列表展示）
     */
    @Data
    public static class DictItem {
        private Long id;
        private String dictName;
        private String dictCode;
        private String description;
        private Integer status;
        private Integer sortOrder;
        private Long createTime;
        private Long updateTime;
    }
}
