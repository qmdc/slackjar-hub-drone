package com.slack.slackjarservice.foundation.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除文件请求
 */
@Data
public class BatchDeleteRequest {

    /**
     * 要删除的文件路径列表(完整URL路径)
     */
    @NotEmpty(message = "文件路径列表不能为空")
    private List<String> filePaths;

}
