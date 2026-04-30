package com.slack.slackjarservice.common.enumtype.foundation;

import com.slack.slackjarservice.common.exception.BusinessException;
import lombok.Getter;

/**
 * AI模型提供商枚举
 * 定义支持的AI模型提供商类型
 */
@Getter
@SuppressWarnings("all")
public enum AiModelVendorEnum {

    DOUBAO_SEED_1_6("Doubao-Seed-1.6", "豆包"),
    QWEN_3_5_PLUS("Qwen3.5-Plus", "通义千问"),
    KIMI_K2_5("Kimi-K2.5", "月之暗面"),
    ;

    private final String code;
    private final String description;

    AiModelVendorEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return code;
    }

    /**
     * 根据 code 获取枚举值
     *
     * @param code 模型代码（不区分大小写）
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果 code 不存在
     */
    public static AiModelVendorEnum getByCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new BusinessException("AI 模型代码不能为空");
        }
        for (AiModelVendorEnum vendor : values()) {
            if (vendor.getCode().equalsIgnoreCase(code)) {
                return vendor;
            }
        }
        throw new BusinessException("未知的 AI 模型代码：" + code);
    }
}
