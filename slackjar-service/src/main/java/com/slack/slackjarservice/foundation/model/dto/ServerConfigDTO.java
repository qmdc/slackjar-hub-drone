package com.slack.slackjarservice.foundation.model.dto;

import lombok.Data;

/**
 * 服务器配置DTO
 *
 * @author zhn
 * @since 2025-08-26
 */
@Data
public class ServerConfigDTO {

    /**
     * 服务器IP地址
     */
    private String serverIp;

    /**
     * SSH连接端口号（默认为22）
     */
    private Integer serverPort;

    /**
     * 登录服务器的用户名
     */
    private String serverUsername;

    /**
     * 登录服务器的密码
     */
    private String serverPassword;

    /**
     * SSL证书存放目录
     */
    private String sslCertPath;
    
}