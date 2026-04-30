package com.slack.slackjarservice.common.enumtype.foundation;

import com.slack.slackjarservice.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 媒体业务类型枚举
 * @author zhn
 */
@Getter
@AllArgsConstructor
public enum MediaBizTypeEnum {

    /**
     * 图片
     */
    DOCUMENT_IMAGE("image", "图片", 15, Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp")),

    /**
     * 文件
     */
    DOCUMENT_FILE("document", "文件", 50, Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")),

    /**
     * 头像
     */
    AVATAR("avatar", "头像", 2, Arrays.asList("jpg", "jpeg", "png")),

    /**
     * 背景图
     */
    BACKGROUND("background", "背景图", 10, Arrays.asList("jpg", "jpeg", "png", "webp")),

    /**
     * 视频
     */
    VIDEO_FILE("video", "视频", 150, Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv")),

    /**
     * 音频
     */
    AUDIO_FILE("audio", "音频", 50, Arrays.asList("mp3", "wav", "aac", "flac", "ogg")),

    /**
     * 其他文件
     */
    OTHER("other", "其他文件", 150, Arrays.asList("zip", "rar", "7z", "tar", "gz")),
    ;

    private final String code;
    private final String desc;
    private final int sizeLimitMB;
    private final List<String> allowedExtensions;

    /**
     * 根据code获取枚举
     */
    public static MediaBizTypeEnum getByCode(String code) {
        for (MediaBizTypeEnum type : values()) {
            if (Objects.equals(type.getCode(), code)) {
                return type;
            }
        }
        throw new BusinessException(ResponseEnum.FILE_BIZ_TYPE);
    }

    /**
     * 校验文件格式是否允许，如果不允许则抛出异常
     * @param fileName 文件名
     */
    public void validateFileExtension(String fileName) {
        if (!isFileExtensionAllowed(fileName)) {
            throw new BusinessException(ResponseEnum.FILE_FORMAT_NOT_ALLOWED.getCode(),
                    ResponseEnum.FILE_FORMAT_NOT_ALLOWED.getMessage() + ", 允许的格式: " + String.join(", ", allowedExtensions));
        }
    }

    /**
     * 校验文件大小是否超出限制，如果超出则抛出异常
     * @param fileSize 文件大小（字节）
     */
    public void validateFileSize(long fileSize) {
        if (fileSize > getSizeLimitBytes()) {
            throw new BusinessException(ResponseEnum.FILE_EXCEED.getCode(),
                    ResponseEnum.FILE_EXCEED.getMessage() + ",最大上传:" + sizeLimitMB + "MB");
        }
    }

    /**
     * 校验文件格式是否允许
     * @param fileName 文件名
     * @return 是否允许
     */
    public boolean isFileExtensionAllowed(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        // 获取文件扩展名
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return allowedExtensions.contains(extension);
    }

    /**
     * 获取大小限制（字节）
     */
    public long getSizeLimitBytes() {
        return sizeLimitMB * 1024L * 1024L;
    }
}
