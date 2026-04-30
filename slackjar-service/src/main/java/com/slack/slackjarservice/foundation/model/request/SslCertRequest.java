package com.slack.slackjarservice.foundation.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * SSL证书上传请求模型
 *
 * @author zhn
 * @since 2025-08-26
 */
@Data
public class SslCertRequest {

    /**
     * SSL证书key内容
     */
    @NotBlank(message = "SSL证书key内容不能为空")
    private String sslCertKey;

    /**
     * SSL证书pem内容
     */
    @NotBlank(message = "SSL证书pem内容不能为空")
    private String sslCertPem;

    /**
     * 域名
     */
    @NotBlank(message = "域名不能为空")
    @Size(max = 100, message = "域名长度不能超过100个字符")
    private String domain;

    /**
     * ssl证书有效期（天）
     */
    @NotNull(message = "证书有效期不能为空")
    private Integer validityDays;

    /**
     * 证书来源 0-腾讯云 1-阿里云 2-华为云 3-其他
     */
    @NotNull(message = "证书来源不能为空")
    private Integer certSource;

    /**
     * 控制台文档
     */
    @Size(max = 500, message = "控制台文档长度不能超过500个字符")
    private String consoleWiki;

    /**
     * 申请时间
     */
    @NotNull(message = "申请时间不能为空")
    private Long applyTime;
    
}