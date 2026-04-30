package com.slack.slackjarservice.foundation.model.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户修改信息请求
 *
 * @author zhn
 */
@Data
public class UpdateUserInfoRequest {

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    private Long avatarId;

    private Long backgroundId;
}
