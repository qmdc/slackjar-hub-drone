package com.slack.slackjarservice.foundation.model.dto;

import lombok.Data;

/**
 * 阿里云OSS配置数据传输对象
 * 对应前端AppConfig.vue中的ali_oss_storage配置项
 */
@Data
public class AliyunOssConfigDTO {

    /**
     * OSS服务端节点地址
     */
    private String endpoint;

    /**
     * 阿里云访问密钥ID
     */
    private String accessKeyId;

    /**
     * 阿里云访问密钥
     */
    private String accessKeySecret;

    /**
     * OSS存储空间名称（Bucket名称）
     */
    private String bucket;

    /**
     * OSS 专用地域 ID
     */
    private String region;

    /**
     * 自定义域名
     */
    private String domain;

    /**
     * 上传回调URL
     * 文件上传完成后，OSS回调通知的地址
     * 用于处理上传完成后的业务逻辑
     */
    private String callbackUrl;

    /**
     * 文件上传根目录
     */
    private String filePath;

    /**
     * 文件命名规则
     * UUID：使用无分割线的UUID作为文件名
     * DATE：使用时间戳格式作为文件名
     * 默认使用DATE规则
     */
    private String fileNameRule;

}
