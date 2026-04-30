package com.slack.slackjarservice.foundation.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.slack.slackjarservice.common.base.BaseController;
import com.slack.slackjarservice.common.enumtype.foundation.OperationEnum;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.foundation.model.request.BatchDeleteRequest;
import com.slack.slackjarservice.foundation.model.response.BatchDeleteResponse;
import com.slack.slackjarservice.foundation.model.response.FileUploadResponse;
import com.slack.slackjarservice.foundation.service.SysFileService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;

/**
 * 系统文件表(SysFile)表控制层
 * 提供文件上传、下载、删除等功能
 *
 * @author zhn
 * @since 2025-08-15 01:27:42
 */
@Slf4j
@RestController
@RequestMapping("/sys-file")
@Validated
public class SysFileController extends BaseController {

    @Resource
    private SysFileService sysFileService;

    /**
     * 常规上传文件
     *
     * @param file    上传的文件
     * @param bizType 业务类型
     * @param expired 过期时间（可选）
     * @return 上传结果
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SaCheckLogin
    public ApiResponse<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bizType") String bizType,
            @RequestParam(value = "expired", required = false, defaultValue = "-1") Long expired) {
        FileUploadResponse response = sysFileService.uploadFile(file, bizType, expired);
        recordOperateLog(OperationEnum.FILE_UPLOAD, "文件上传成功，业务类型: " + bizType + "，URL: " + response.getFileUrl());
        return success(response);
    }

    /**
     * 下载文件为byte字节
     *
     * @param filePath 文件路径
     * @return 文件内容
     */
    @GetMapping("/download")
    @SaCheckLogin
    public ApiResponse<byte[]> downloadFile(@RequestParam("filePath") String filePath) {
        byte[] fileContent = sysFileService.downloadFile(filePath);
        recordOperateLog(OperationEnum.FILE_DOWNLOAD, "文件下载成功，文件路径: " + filePath + "，响应字节SIZE：" + fileContent.length);
        return success(fileContent);
    }

    /**
     * 批量删除文件
     *
     * @param request 删除请求
     * @return 删除结果，包含成功和失败的文件列表
     */
    @PostMapping("/batch-delete")
    @SaCheckLogin
    public ApiResponse<BatchDeleteResponse> batchDeleteFiles(@Valid @RequestBody BatchDeleteRequest request) {
        BatchDeleteResponse response = sysFileService.batchDeleteFiles(request.getFilePaths());
        recordOperateLog(OperationEnum.FILE_DELETE, "批量删除文件成功，删除文件结果: " + response.toString());
        return success(response);
    }

    @Value("${system.local.storage.path:/Users/ppsn/Documents/trae/slack-hub-drone/file}")
    private String localStoragePath;

    @GetMapping("/local/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadLocalFile(@RequestParam("filePath") String filePath) {
        try {
            File file = new File(localStoragePath, filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            FileSystemResource resource = new FileSystemResource(file);
            String fileName = URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            log.error("下载本地文件失败: {}", filePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

