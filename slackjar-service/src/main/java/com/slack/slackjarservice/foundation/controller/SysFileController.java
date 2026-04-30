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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

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
    public ResponseEntity<ByteArrayResource> downloadLocalFile(@RequestParam("filePath") String filePath,
                                                                @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {
        File file;
        try {
            file = new File(localStoragePath, filePath);
        } catch (Exception e) {
            log.error("文件路径解析失败: {}", filePath, e);
            return ResponseEntity.badRequest().build();
        }

        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        long fileLength = file.length();
        String fileName;
        try {
            fileName = URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            fileName = "file";
        }
        String contentType = determineContentType(file);

        try {
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                List<HttpRange> ranges;
                try {
                    ranges = HttpRange.parseRanges(rangeHeader);
                } catch (Exception e) {
                    log.warn("无效的Range头: {}, 将返回完整文件", rangeHeader);
                    ranges = Collections.emptyList();
                }

                if (!ranges.isEmpty()) {
                    HttpRange httpRange = ranges.get(0);
                    long start = Math.max(0, httpRange.getRangeStart(fileLength));
                    long end = Math.min(fileLength - 1, httpRange.getRangeEnd(fileLength));
                    int contentLength = (int) (end - start + 1);

                    byte[] data = new byte[contentLength];
                    try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                        raf.seek(start);
                        raf.readFully(data);
                    }

                    return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                            .contentLength(contentLength)
                            .body(new ByteArrayResource(data));
                }
            }

            byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .contentLength(fileLength)
                    .body(new ByteArrayResource(data));
        } catch (Exception e) {
            log.error("读取文件失败: {}", filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    private String determineContentType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".mp4")) return "video/mp4";
        if (name.endsWith(".webm")) return "video/webm";
        if (name.endsWith(".ogg")) return "video/ogg";
        if (name.endsWith(".mov")) return "video/quicktime";
        if (name.endsWith(".avi")) return "video/x-msvideo";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".webp")) return "image/webp";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}

