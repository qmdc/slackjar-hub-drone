package com.slack.slackjarservice.foundation.model.dto;

import lombok.Data;

/**
 * AI模型配置基类
 * 包含所有AI模型共有的配置参数
 */
@Data
public class BaseAiConfigDto {

    /**
     * API访问密钥
     */
    private String apiKey;

    /**
     * 模型路径
     */
    private String modelUrl;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 模型温度 范围为[0, 2]（确定<->随机）
     */
    private String temperature;

    /**
     * 最大令牌数
     */
    private String maxTokens;

    /**
     * 补全路径
     */
    private String completionsPath;

}
