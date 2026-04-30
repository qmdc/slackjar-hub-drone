package com.slack.slackjarservice.foundation.model.response;

import lombok.Data;

import java.util.List;

/**
 * 批量删除文件响应
 * @author zhn
 * @since 2025-08-29 21:29:27
 */
@Data
public class BatchDeleteResponse {

    /**
     * 成功删除的文件列表
     */
    private List<String> successFiles = List.of();

    /**
     * 删除失败的文件列表
     */
    private List<String> failedFiles = List.of();

    /**
     * 成功删除的文件数量
     */
    private int successCount = 0;

    /**
     * 删除失败的文件数量
     */
    private int failedCount = 0;

    /**
     * 总文件数量
     */
    private int totalCount = 0;

}
