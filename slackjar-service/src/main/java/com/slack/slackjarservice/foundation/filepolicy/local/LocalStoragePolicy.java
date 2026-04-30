package com.slack.slackjarservice.foundation.filepolicy.local;

import com.slack.slackjarservice.common.enumtype.foundation.MediaBizTypeEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.enumtype.foundation.StorageVendorEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.foundation.filepolicy.AbstractFileStoragePolicy;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class LocalStoragePolicy extends AbstractFileStoragePolicy {

    @Value("${system.local.storage.path:/Users/ppsn/Documents/trae/slack-hub-drone/file}")
    private String storagePath;

    private boolean initialized = false;

    @Override
    public StorageVendorEnum getVendorType() {
        return StorageVendorEnum.LOCAL;
    }

    @Override
    public void init() {
        try {
            Path path = Paths.get(storagePath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("创建本地存储目录: {}", storagePath);
            }
            initialized = true;
            log.info("本地存储策略初始化成功, 存储路径: {}", storagePath);
        } catch (IOException e) {
            log.error("本地存储策略初始化失败", e);
            throw new BusinessException(ResponseEnum.FILE_UPLOAD);
        }
    }

    @PreDestroy
    @Override
    public void destroy() {
        initialized = false;
        log.info("本地存储策略已销毁");
    }

    @Override
    public String uploadFile(MultipartFile file, MediaBizTypeEnum businessType) {
        String fileKey = "";
        try {
            validateFileSize(file, businessType);

            String extension = Objects.requireNonNull(file.getOriginalFilename())
                    .substring(file.getOriginalFilename().lastIndexOf("."));
            String timestamp = String.valueOf(System.currentTimeMillis());
            String businessPath = businessType.getCode();
            
            Path targetDir = Paths.get(storagePath, businessPath);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            fileKey = businessPath + "/" + timestamp + extension;
            Path targetPath = Paths.get(storagePath, fileKey);
            
            file.transferTo(targetPath.toFile());
            logOperation("文件上传", fileKey, true);
            return fileKey;
        } catch (Exception e) {
            logException("文件上传", fileKey, e);
            throw new BusinessException(ResponseEnum.FILE_UPLOAD);
        }
    }

    @Override
    public InputStream downloadFile(String fileKey) {
        try {
            Path filePath = Paths.get(storagePath, fileKey);
            if (!Files.exists(filePath)) {
                throw new BusinessException(ResponseEnum.FILE_NOT_EXIST);
            }
            logOperation("文件下载", fileKey, true);
            return Files.newInputStream(filePath);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logException("文件下载", fileKey, e);
            throw new BusinessException(ResponseEnum.FILE_DOWNLOAD);
        }
    }

    @Override
    public void deleteFile(String fileKey) {
        try {
            Path filePath = Paths.get(storagePath, fileKey);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logOperation("文件删除", fileKey, true);
            }
        } catch (Exception e) {
            logException("文件删除", fileKey, e);
            throw new BusinessException(ResponseEnum.FILE_DELETE);
        }
    }

    @Override
    public Map<String, String> getUploadSignature(String fileKey, MediaBizTypeEnum businessType, long expireTime) {
        Map<String, String> result = new HashMap<>();
        result.put("key", fileKey);
        result.put("businessType", businessType.getCode());
        logOperation("获取上传签名", fileKey, true);
        return result;
    }

    @Override
    public boolean handleUploadCallback(Map<String, String> callbackParams) {
        logOperation("处理上传回调", "callback", true);
        return true;
    }

    @Override
    public double getUploadProgress(String uploadId) {
        return 100.0;
    }

    @Override
    public String previewFile(String fileKey) {
        String fileUrl = getFileUrl(fileKey);
        logOperation("文件预览", fileKey, true);
        return fileUrl;
    }

    @Override
    public String getFileUrl(String fileKey) {
        return "/api/sys-file/local/download?filePath=" + fileKey;
    }

    public String getStoragePath() {
        return storagePath;
    }
}