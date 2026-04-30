package com.slack.slackjarservice.common.enumtype.foundation;

/**
 * 存储厂商枚举
 * 定义支持的文件存储厂商类型
 */
@SuppressWarnings("all")
public enum StorageVendorEnum {

    ALIYUN("aliyun", "阿里云OSS"),
    TENCENT("tencent", "腾讯云COS"),
    MINIO("minio", "MinIO"),
    LOCAL("local", "本地存储"),
    ;

    private final String code;
    private final String description;

    StorageVendorEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
}
