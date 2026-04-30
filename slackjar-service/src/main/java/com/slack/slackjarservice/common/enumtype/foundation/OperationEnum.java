package com.slack.slackjarservice.common.enumtype.foundation;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举
 * @author zhn
 */
@Getter
@AllArgsConstructor
public enum OperationEnum {

    /**
     * 操作类型枚举
     */
    USER_LOGIN(1001, "登录"),
    USER_REGISTER(1002, "注册"),
    USER_LOGOUT(1003, "注销"),
    USER_UPSERT(1004, "更新/保存"),
    USER_DELETE(1006, "删除"),
    USER_QUERY(1007, "查询"),
    AI_CHAT(1011, "AI对话"),
    AI_CHAT_STREAM(1012, "AI流式对话"),
    FILE_UPLOAD(1013, "文件上传"),
    FILE_DOWNLOAD(1014, "文件下载"),
    FILE_DELETE(1015, "文件删除"),
    SCHEDULED_TASK(1016, "定时任务"),
    OTHER(9999, "其他操作"),
    ;

    private final int code;
    private final String desc;
}
