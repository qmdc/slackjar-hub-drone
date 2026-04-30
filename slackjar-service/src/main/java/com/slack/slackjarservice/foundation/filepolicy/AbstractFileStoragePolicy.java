package com.slack.slackjarservice.foundation.filepolicy;

import com.slack.slackjarservice.common.enumtype.foundation.MediaBizTypeEnum;
import com.slack.slackjarservice.common.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * 文件存储策略抽象基类
 * 提供通用的日志记录和配置参数管理
 */
@Slf4j
public abstract class AbstractFileStoragePolicy implements FileStoragePolicy {

    /**
     * 记录操作日志
     */
    protected void logOperation(String operation, String fileKey, boolean success) {
        if (success) {
            log.info("[{}] {} 操作成功: {}", getVendorType(), operation, fileKey);
        } else {
            log.error("[{}] {} 操作失败: {}", getVendorType(), operation, fileKey);
        }
    }

    /**
     * 记录异常日志
     */
    protected void logException(String operation, String fileKey, Exception e) {
        log.error("[{}] {} 操作异常: {}, 错误信息: {}",
                 getVendorType(), operation, fileKey, e.getMessage(), e);
    }

    /**
     * 验证文件大小限制和文件格式
     */
    protected void validateFileSize(MultipartFile file, MediaBizTypeEnum businessType) {
        // 校验文件大小
        businessType.validateFileSize(file.getSize());

        // 校验文件格式
        businessType.validateFileExtension(file.getOriginalFilename());
    }

    /**
     * 生成文件全路径(不含域名)
     * 如：media/video/20241219153045123.mp4
     */
    protected String generateFileKey(String filePath, String fileNameRule,
                                     String originalFilename, MediaBizTypeEnum businessType) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String path = filePath.endsWith("/") ? filePath + businessType.getCode() : filePath + "/"  + businessType.getCode();
        String fullPath = path + "/" + RandomUtil.getCurrentMillisStr() + extension;
        if (Objects.equals(fileNameRule, "UUID")) {
            fullPath = path + "/" + RandomUtil.getSimpleUUID() + extension;
        } else if (Objects.equals(fileNameRule, "DATE")) {
            fullPath = path + "/" + RandomUtil.getCurrentMillisStr() + extension;
        }

        if (fullPath.startsWith("/")) {
            fullPath = fullPath.substring(1);
        }
        return fullPath;
    }

}
