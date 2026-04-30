package com.slack.slackjarservice.foundation.filepolicy;

import com.slack.slackjarservice.common.enumtype.foundation.MediaBizTypeEnum;
import com.slack.slackjarservice.common.enumtype.foundation.StorageVendorEnum;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.Map;

/**
 * 文件存储策略接口
 * 定义所有文件存储厂商需要实现的方法
 */
public interface FileStoragePolicy {

    /**
     * 获取厂商类型
     */
    StorageVendorEnum getVendorType();

    /**
     * 初始化存储客户端
     */
    void init();

    /**
     * 销毁存储客户端
     */
    void destroy();

    /**
     * 文件直接上传
     * 返回示例：media/video/20241219153045123.mp4
     */
    String uploadFile(MultipartFile file, MediaBizTypeEnum businessType);

     /**
     * 文件下载
     */
    InputStream downloadFile(String fileKey);

    /**
     * 删除文件
     */
    void deleteFile(String fileKey);

    /**
     * 获取上传签名（用于前端直传）
     */
    Map<String, String> getUploadSignature(String fileKey, MediaBizTypeEnum businessType, long expireTime);

    /**
     * 处理上传回调
     */
    boolean handleUploadCallback(Map<String, String> callbackParams);

    /**
     * 获取上传进度
     */
    double getUploadProgress(String uploadId);

    /**
     * 预览文件
     */
    String previewFile(String fileKey);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String fileKey);
}
