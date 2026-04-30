package com.slack.slackjarservice.common.enumtype.foundation;

import com.slack.slackjarservice.common.exception.BusinessException;
import lombok.Getter;

/**
 * Socket消息业务类型枚举 前端推送
 */
@Getter
public enum PushWithFrontEnum {

    // 系统通知
    SYSTEM_NOTICE("SYSTEM_NOTICE", "系统通知"),
    // 用户消息
    USER_MESSAGE("USER_MESSAGE", "用户消息"),
    // 业务提醒
    BUSINESS_REMINDER("BUSINESS_REMINDER", "业务提醒"),
    ;

    private final String code;
    private final String description;

    PushWithFrontEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static void verifyBizType(String code) {
        for (PushWithFrontEnum type : values()) {
            if (type.getCode().equals(code)) {
                return;
            }
        }
        throw new BusinessException(ResponseEnum.SOCKET_BIZ_NOT_FOUND);
    }
}
