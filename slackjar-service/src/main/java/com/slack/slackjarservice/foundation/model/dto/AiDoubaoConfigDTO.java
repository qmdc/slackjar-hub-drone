package com.slack.slackjarservice.foundation.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 豆包AI配置数据传输对象
 * 用于存储豆包AI的配置参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AiDoubaoConfigDTO extends BaseAiConfigDto {

    /**
     * 深度思考模式 (enabled、disabled、auto)
     */
    private String deepThinking;

    /**
     * 上下文窗口长度
     */
    private String windowSize;

}
