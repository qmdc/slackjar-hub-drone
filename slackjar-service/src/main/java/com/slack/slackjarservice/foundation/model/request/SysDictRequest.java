package com.slack.slackjarservice.foundation.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 数据字典请求模型
 *
 * @author zhn
 */
@Data
public class SysDictRequest {

    /**
     * 字典ID（更新时必填）
     */
    private Long id;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 1, max = 50, message = "字典名称长度必须在1-50个字符之间")
    private String dictName;

    /**
     * 字典编码
     */
    @NotBlank(message = "字典编码不能为空")
    @Size(min = 1, max = 50, message = "字典编码长度必须在1-50个字符之间")
    private String dictCode;

    /**
     * 字典描述
     */
    @Size(max = 200, message = "字典描述长度不能超过200个字符")
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
    @Valid
    private List<DictItem> dictItems;

    /**
     * 字典项
     */
    @Data
    public static class DictItem {
        /**
         * 字典项ID（更新时必填）
         */
        private Long id;

        /**
         * 字典项标签
         */
        @NotBlank(message = "字典项标签不能为空")
        @Size(min = 1, max = 200, message = "字典项标签长度必须在1-50个字符之间")
        private String itemLabel;

        /**
         * 字典项值
         */
        @NotBlank(message = "字典项值不能为空")
        @Size(min = 1, max = 200, message = "字典项值长度必须在1-200个字符之间")
        private String itemValue;

        /**
         * 字典项描述
         */
        @Size(max = 200, message = "字典项描述长度不能超过200个字符")
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
}
