package com.slack.slackjarservice.foundation.model.dto;

import lombok.Data;

/**
 * Socket消息推送DTO
 */
@Data
public class SocketMessageDTO {

    /**
     * 推送毫秒级时间戳
     */
    private Long timestamp;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 推送内容
     */
    private Object content;

    private SocketMessageDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    private SocketMessageDTO(Object content) {
        this();
        this.content = content;
    }

    public SocketMessageDTO(Object content, String bizType) {
        this(content);
        this.bizType = bizType;
    }
}
