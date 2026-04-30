package com.slack.slackjarservice.foundation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slack.slackjarservice.common.enumtype.foundation.PushWithBackendEnum;
import com.slack.slackjarservice.common.enumtype.foundation.MediaBizTypeEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.common.util.AssertUtil;
import com.slack.slackjarservice.common.util.StringUtil;
import com.slack.slackjarservice.foundation.dao.SysFileDao;
import com.slack.slackjarservice.foundation.entity.SysFile;
import com.slack.slackjarservice.foundation.filepolicy.FileStoragePolicy;
import com.slack.slackjarservice.foundation.filepolicy.FileStoragePolicyManager;
import com.slack.slackjarservice.foundation.model.dto.SocketMessageDTO;
import com.slack.slackjarservice.foundation.model.response.BatchDeleteResponse;
import com.slack.slackjarservice.foundation.model.response.FileUploadResponse;
import com.slack.slackjarservice.foundation.service.SysFileService;
import com.slack.slackjarservice.foundation.socketio.BackendMessagePush;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统文件表(SysFile)表服务实现类
 *
 * @author zhn
 * @since 2025-08-15 01:27:43
 */
@Slf4j
@Service("sysFileService")
public class SysFileServiceImpl extends ServiceImpl<SysFileDao, SysFile> implements SysFileService {

    @Resource
    private FileStoragePolicyManager fileStoragePolicyManager;

    @Resource
    private BackendMessagePush backendMessagePush;

    @Override
    public void refreshActivePolicy() {
        Boolean refreshed = fileStoragePolicyManager.refreshManager();
        if (refreshed) {
            backendMessagePush.broadcastMessage(new SocketMessageDTO(
                    "文件存储策略刷新成功", PushWithBackendEnum.SUCCESS_STRING_NOTICE.getCode()));
        } else {
            backendMessagePush.broadcastMessage(new SocketMessageDTO(
                    "文件存储策略刷新失败", PushWithBackendEnum.FAIL_STRING_NOTICE.getCode()));
        }
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String bizType, Long expired) {
        try {
            if (file == null || file.isEmpty()) {
                throw new BusinessException(ResponseEnum.FILE_NOT_EMPTY);
            }

            // 获取业务类型枚举
            MediaBizTypeEnum businessType = MediaBizTypeEnum.getByCode(bizType);

            // 在上传前先保存需要的文件信息（上传后临时文件会被删除）
            long fileSize = file.getSize();
            String originalFilename = file.getOriginalFilename();
            String contentType = file.getContentType();

            // 检查文件大小限制
            businessType.validateFileSize(fileSize);

            // 检查文件格式
            businessType.validateFileExtension(originalFilename);

            // 获取存储策略
            var storagePolicy = fileStoragePolicyManager.getPolicy();

            // 先上传文件（MultipartFile的输入流只能读取一次，transferTo会移动临时文件）
            String fileKey = storagePolicy.uploadFile(file, businessType);

            // 上传成功后，从本地存储的文件计算MD5
            String fileMd5 = null;
            if (storagePolicy instanceof com.slack.slackjarservice.foundation.filepolicy.local.LocalStoragePolicy) {
                var localPolicy = (com.slack.slackjarservice.foundation.filepolicy.local.LocalStoragePolicy) storagePolicy;
                String localPath = localPolicy.getStoragePath() + "/" + fileKey;
                fileMd5 = DigestUtil.md5Hex(new java.io.FileInputStream(localPath));
            } else {
                // 对于其他存储策略，需要先下载再计算MD5
                try (var inputStream = storagePolicy.downloadFile(fileKey)) {
                    fileMd5 = DigestUtil.md5Hex(inputStream);
                }
            }

            // 保存文件信息到数据库（使用之前保存的文件信息）
            SysFile sysFile = getSysFile(fileSize, originalFilename, expired, fileKey, businessType, fileMd5);
            this.save(sysFile);

            log.info("文件上传成功：{}，用户ID：{}", getFileUrl(fileKey), StpUtil.getLoginIdAsLong());
            return getFileUploadResponse(fileSize, contentType, expired, fileKey, sysFile.getId());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(e.getMessage());
        }
    }

    @NotNull
    private FileUploadResponse getFileUploadResponse(long fileSize, String contentType, Long expired, String fileKey, Long fileId) {
        FileUploadResponse response = new FileUploadResponse();
        response.setFileId(fileId);
        response.setFileName(fileKey.substring(fileKey.lastIndexOf("/") + 1));
        response.setFileUrl(getFileUrl(fileKey));
        response.setFilePath(fileKey);
        response.setFileSize(fileSize);
        response.setFileType(contentType);
        response.setExpired(expired);
        response.setThumbnailUrl(null);
        return response;
    }

    @NotNull
    private SysFile getSysFile(long fileSize, String originalFilename, Long expired, String fileKey,
                               MediaBizTypeEnum businessType, String fileMd5) {
        SysFile sysFile = new SysFile();
        sysFile.setFileName(fileKey.substring(fileKey.lastIndexOf("/") + 1));
        sysFile.setFilePath(getFileUrl(fileKey));
        sysFile.setUserId(StpUtil.getLoginIdAsLong());
        sysFile.setFileSize(fileSize);
        sysFile.setFileType(businessType.getCode());
        sysFile.setFileMd5(fileMd5);
        sysFile.setStorageType(fileStoragePolicyManager.getCurrentVendor().getCode());
        sysFile.setBizType(businessType.getCode());
        sysFile.setExpired(expired);
        // 默认公开
        sysFile.setAccessLevel(1);
        // 默认审核通过
        sysFile.setAuditStatus(1);
        // 下载次数
        sysFile.setDownloadCount(0);
        // 上传完成
        sysFile.setUploadStatus(1);
        return sysFile;
    }

    @Override
    public byte[] downloadFile(String filePath) {
        String fileKey = StringUtil.extractRelativePath(filePath);
        // 查询数据库确认文件是否存在且属于当前用户
        List<SysFile> sysFiles = lambdaQuery().eq(SysFile::getFilePath, fileKey).list();
        AssertUtil.notEmpty(sysFiles, ResponseEnum.FILE_NOT_EXIST);
        SysFile sysFile = sysFiles.get(0);
        if ((sysFile.getAccessLevel() == 0 || sysFile.getAccessLevel() == 2) && StpUtil.getLoginIdAsLong() != sysFile.getUserId()) {
            throw new BusinessException(ResponseEnum.FILE_ACCESS_NOT);
        }

        // 获取存储策略
        var storagePolicy = fileStoragePolicyManager.getPolicy();

        // 下载文件并使用try-with-resources确保资源正确关闭
        try (var inputStream = storagePolicy.downloadFile(fileKey)) {
            byte[] fileContent = inputStream.readAllBytes();

            // 更新下载次数
            lambdaUpdate().set(SysFile::getDownloadCount, sysFile.getDownloadCount() + 1).eq(SysFile::getId, sysFile.getId()).update();

            return fileContent;
        } catch (IOException e) {
            log.error("文件下载失败，fileKey: {}", fileKey, e);
            throw new BusinessException(ResponseEnum.FILE_DOWNLOAD);
        }
    }

    @Override
    public BatchDeleteResponse batchDeleteFiles(List<String> filePaths) {
        BatchDeleteResponse response = new BatchDeleteResponse();
        if (CollectionUtils.isEmpty(filePaths)) {
            return response;
        }

        List<String> successFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        // 转换URL
        List<String> fileKeys = new ArrayList<>();
        for (String filePath : filePaths) {
            try {
                fileKeys.add(StringUtil.extractRelativePath(filePath));
            } catch (BusinessException e) {
                failedFiles.add(filePath);
            }
        }

        if (CollectionUtils.isEmpty(fileKeys)) {
            response.setFailedFiles(failedFiles);
            response.setFailedCount(failedFiles.size());
            response.setTotalCount(failedFiles.size());
            return response;
        }

        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 查询数据库中存在的文件
        List<SysFile> existingFiles = lambdaQuery().in(SysFile::getFilePath, fileKeys).eq(SysFile::getUserId, currentUserId).list();

        // 将存在的文件路径映射为Set
        Set<String> existingFilePaths = existingFiles.stream().map(SysFile::getFilePath).collect(Collectors.toSet());

        // 获取存储策略
        var storagePolicy = fileStoragePolicyManager.getPolicy();

        // 批量删除文件
        for (String fileKey : fileKeys) {
            // 检查文件是否属于当前用户
            if (!existingFilePaths.contains(fileKey)) {
                log.warn("文件不存在或不属于当前用户：{}，用户ID：{}", fileKey, currentUserId);
                failedFiles.add(fileKey);
                continue;
            }

            try {
                // 删除物理文件
                storagePolicy.deleteFile(fileKey);
                successFiles.add(fileKey);
                log.info("文件:{} 删除成功，用户ID：{}", fileKey, currentUserId);
            } catch (BusinessException e) {
                log.error("文件:{} 删除失败，用户ID：{}，错误：", fileKey, currentUserId, e);
                failedFiles.add(fileKey);
            }
        }

        // 批量更新数据库状态（只更新成功删除的文件）
        if (!successFiles.isEmpty()) {
            lambdaUpdate().set(SysFile::getDeleted, 1).in(SysFile::getFilePath, successFiles).update();
            log.info("批量更新数据库状态完成，共更新：{}个文件", successFiles.size());
        }

        // 设置响应结果
        response.setSuccessFiles(successFiles.stream().map(this::getFileUrl).collect(Collectors.toList()));
        response.setFailedFiles(failedFiles.stream().map(this::getFileUrl).collect(Collectors.toList()));
        response.setSuccessCount(successFiles.size());
        response.setFailedCount(failedFiles.size());
        response.setTotalCount(successFiles.size() + failedFiles.size());

        // 打印删除结果日志
        log.info("批量删除完成，总文件数：{}，成功：{}，失败：{}，用户ID：{}",
                response.getTotalCount(), response.getSuccessCount(), response.getFailedCount(), currentUserId);

        return response;
    }

    @Override
    public String getFileUrl(String fileKey) {
        FileStoragePolicy storagePolicy = fileStoragePolicyManager.getPolicy();
        return storagePolicy.getFileUrl(fileKey);
    }
}

