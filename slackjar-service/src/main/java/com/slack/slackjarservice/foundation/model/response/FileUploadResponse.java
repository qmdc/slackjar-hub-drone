package com.slack.slackjarservice.foundation.model.response;

import lombok.Data;

/**
 * 文件上传响应
 *
 * @author zhn
 */
@Data
public class FileUploadResponse {

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 业务文件类型
     */
    private String fileType;

    /**
     * 缩略图URL（如果有）
     */
    private String thumbnailUrl;

    /**
     * 过期时间(毫秒时间戳),-1代表不过期
     */
    private Long expired;
}
