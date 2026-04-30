package com.slack.slackjarservice.foundation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slack.slackjarservice.foundation.entity.SysFile;
import com.slack.slackjarservice.foundation.model.response.BatchDeleteResponse;
import com.slack.slackjarservice.foundation.model.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 系统文件表(SysFile)表服务接口
 *
 * @author zhn
 * @since 2025-08-15 01:27:43
 */
public interface SysFileService extends IService<SysFile> {

    /**
     * 刷新文件存储策略
     */
    void refreshActivePolicy();

    /**
     * 常规上传文件
     */
    FileUploadResponse uploadFile(MultipartFile file, String bizType, Long expired);

    /**
     * 下载文件为byte字节
     */
    byte[] downloadFile(String filePath);

    /**
     * 批量删除文件
     * @param filePaths 文件路径列表(完整URL路径)
     * @return 批量删除结果响应
     */
    BatchDeleteResponse batchDeleteFiles(List<String> filePaths);

    /**
     * 获取文件访问URL
     * @param filePath 文件路径(不含域名)
     * @return 文件访问URL
     */
    String getFileUrl(String filePath);
}

