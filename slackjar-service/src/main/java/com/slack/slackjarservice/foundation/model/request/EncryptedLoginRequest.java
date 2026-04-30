package com.slack.slackjarservice.foundation.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 加密登录请求数据模型
 */
@Data
public class EncryptedLoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 32, message = "用户名长度必须在4-32个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 2048, message = "密码长度必须在6-2048个字符之间")
    private String encryptedPassword;
}