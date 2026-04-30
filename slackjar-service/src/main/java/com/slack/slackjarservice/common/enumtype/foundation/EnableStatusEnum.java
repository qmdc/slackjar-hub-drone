package com.slack.slackjarservice.common.enumtype.foundation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 启用/生效状态枚举
 *
 * @author zhn
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("all")
public enum EnableStatusEnum {

    ENABLE(0, "启用/生效"),
    DISABLED(1, "禁用/失效");

    private final int code;
    private final String desc;
}
