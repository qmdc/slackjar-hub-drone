package com.slack.slackjarservice.common.enumtype.foundation;

import com.slack.slackjarservice.common.exception.BusinessException;

/**
 * 证书来源枚举
 * 定义SSL证书的来源类型
 */
@SuppressWarnings("all")
public enum CertSourceEnum {

    TENCENT(0, "腾讯云"),
    ALIYUN(1, "阿里云"),
    HUAWEI(2, "华为云"),
    OTHER(3, "其他"),
    ;

    private final Integer code;
    private final String description;

    CertSourceEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 校验证书来源是否有效
     * @param code 证书来源编码
     */
    public static void validate(Integer code) {
        boolean valid = false;
        for (CertSourceEnum source : CertSourceEnum.values()) {
            if (source.getCode().equals(code)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new BusinessException(ResponseEnum.SSL_CERT_SOURCE_INVALID);
        }
    }

    @Override
    public String toString() {
        return code.toString();
    }
}
