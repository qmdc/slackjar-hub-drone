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
 * SSL证书信息表(SSLCertificate)表实体类
 *
 * @author zhn
 * @since 2025-09-04 10:23:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("ssl_certificate")
public class SSLCertificate extends BaseModel {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 域名
     */
    private String domain;
    /**
     * ssl证书key
     */
    private String sslCertKey;
    /**
     * ssl证书pem
     */
    private String sslCertPem;
    /**
     * SSL证书存放目录
     */
    private String sslCertPath;
    /**
     * ssl证书有效期（天）
     */
    private Integer validityDays;
    /**
     * 证书来源 0-腾讯云 1-阿里云 2-华为云 3-其他
     */
    private Integer certSource;
    /**
     * 控制台文档
     */
    private String consoleWiki;
    /**
     * 申请时间
     */
    private Long applyTime;
    
}

