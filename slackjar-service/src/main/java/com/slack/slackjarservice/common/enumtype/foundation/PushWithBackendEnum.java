package com.slack.slackjarservice.common.enumtype.foundation;

import com.slack.slackjarservice.common.exception.BusinessException;
import lombok.Getter;

/**
 * Socket消息业务类型枚举 后端推送
 * @author zhn
 */
@Getter
public enum PushWithBackendEnum {

    // 成功-普通字符串通知消息
    SUCCESS_STRING_NOTICE("SUCCESS_STRING_NOTICE", "成功-普通字符串通知消息"),
    // 失败-普通字符串通知消息
    FAIL_STRING_NOTICE("FAIL_STRING_NOTICE", "失败-普通字符串通知消息"),
    // IP地级市信息
    IP_CITY_INFO("IP_CITY_INFO", "IP地级市信息"),
    VIDEO_DETECTION_FRAME("VIDEO_DETECTION_FRAME", "视频检测帧推送"),
    ;

    private final String code;
    private final String description;

    PushWithBackendEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举值
     *
     * @param code 业务类型code
     * @return 对应的枚举值
     */
    public static PushWithBackendEnum fromCode(String code) {
        for (PushWithBackendEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new BusinessException(ResponseEnum.SOCKET_BIZ_NOT_FOUND);
    }
}
