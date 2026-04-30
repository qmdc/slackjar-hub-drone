package com.slack.slackjarservice.foundation.filepolicy.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.slack.slackjarservice.common.constant.ConfigKeys;
import com.slack.slackjarservice.common.enumtype.foundation.MediaBizTypeEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ConfigEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.enumtype.foundation.StorageVendorEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.foundation.filepolicy.AbstractFileStoragePolicy;
import com.slack.slackjarservice.foundation.model.dto.AliyunOssConfigDTO;
import com.slack.slackjarservice.foundation.model.response.SysConfigResponse;
import com.slack.slackjarservice.foundation.service.SysConfigService;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 阿里云OSS文件存储策略实现
 */
@Slf4j
@Component
public class AliyunOssStoragePolicy extends AbstractFileStoragePolicy {

    private OSS ossClient;

    private AliyunOssConfigDTO ossConfig;

    @Resource
    private SysConfigService sysConfigService;

    @Override
    public StorageVendorEnum getVendorType() {
        return StorageVendorEnum.ALIYUN;
    }

    @Override
    public void init() {
        SysConfigResponse configResponse = sysConfigService.getConfigByCategory(ConfigEnum.ALI_OSS_STORAGE.getKey());

        this.ossConfig = new AliyunOssConfigDTO();

        // 创建配置映射表 - 优雅地替代 switch 语句
        Map<String, Consumer<String>> configMappers = Map.of(
                ConfigKeys.AliOssStorage.ALI_OSS_ENDPOINT, ossConfig::setEndpoint,
                ConfigKeys.AliOssStorage.ALI_OSS_ACCESS_KEY, ossConfig::setAccessKeyId,
                ConfigKeys.AliOssStorage.ALI_OSS_SECRET_KEY, ossConfig::setAccessKeySecret,
                ConfigKeys.AliOssStorage.ALI_OSS_BUCKET, ossConfig::setBucket,
                ConfigKeys.AliOssStorage.ALI_OSS_REGION, ossConfig::setRegion,
                ConfigKeys.AliOssStorage.ALI_OSS_DOMAIN, ossConfig::setDomain,
                ConfigKeys.AliOssStorage.ALI_OSS_CALLBACK_URL, ossConfig::setCallbackUrl,
                ConfigKeys.AliOssStorage.ALI_OSS_FILE_PATH, ossConfig::setFilePath,
                ConfigKeys.AliOssStorage.ALI_OSS_FILE_NAME_RULE, ossConfig::setFileNameRule
        );

        // 使用Map优雅地处理配置映射
        configResponse.getConfigItems().forEach(item -> {
            Consumer<String> setter = configMappers.get(item.getConfigKey());
            if (setter != null) {
                setter.accept(item.getConfigValue());
            }
        });

        ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
        log.info("阿里云OSS客户端初始化成功, 存储参数: {}", ossConfig.toString());
    }

    @PreDestroy
    @Override
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("阿里云OSS客户端已关闭");
        }
    }

    @Override
    public String uploadFile(MultipartFile file, MediaBizTypeEnum businessType) {
        String fileKey = "";
        try {
            validateFileSize(file, businessType);

            fileKey = generateFileKey(ossConfig.getFilePath(), ossConfig.getFileNameRule(),
                    Objects.requireNonNull(file.getOriginalFilename()), businessType);
            ossClient.putObject(ossConfig.getBucket(), fileKey, new ByteArrayInputStream(file.getBytes()));

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
            OSSObject ossObject = ossClient.getObject(ossConfig.getBucket(), fileKey);
            logOperation("文件下载", fileKey, true);
            return ossObject.getObjectContent();
        } catch (Exception e) {
            logException("文件下载", fileKey, e);
            throw new BusinessException(ResponseEnum.FILE_DOWNLOAD);
        }
    }

    @Override
    public Map<String, String> getUploadSignature(String fileKey, MediaBizTypeEnum businessType, long expireTime) {
        try {
            // 生成上传策略，添加业务类型限制
            Date expiration = new Date(System.currentTimeMillis() + expireTime);
            PolicyConditions policyConditions = new PolicyConditions();
            policyConditions.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, businessType.getSizeLimitBytes());

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConditions);
            String signature = ossClient.calculatePostSignature(postPolicy);

            Map<String, String> result = new HashMap<>();
            result.put("policy", postPolicy);
            result.put("signature", signature);
            result.put("OSSAccessKeyId", ossConfig.getAccessKeyId());
            result.put("key", fileKey);
            result.put("success_action_status", "200");
            result.put("bucket", ossConfig.getBucket());
            result.put("endpoint", ossConfig.getEndpoint());
            result.put("businessType", String.valueOf(businessType.getCode()));

            logOperation("获取上传签名", fileKey, true);
            return result;
        } catch (Exception e) {
            logException("获取上传签名", fileKey, e);
            throw new BusinessException(ResponseEnum.FILE_SIGNATURE);
        }
    }

    @Override
    public void deleteFile(String fileKey) {
        try {
            ossClient.deleteObject(ossConfig.getBucket(), fileKey);
            logOperation("文件删除", fileKey, true);
        } catch (Exception e) {
            logException("文件删除", fileKey, e);
            throw new BusinessException(ResponseEnum.FILE_DELETE);
        }
    }

    @Override
    public boolean handleUploadCallback(Map<String, String> callbackParams) {
        // 处理上传回调验证
        try {
            String authorization = callbackParams.get("Authorization");
            // 验证回调签名等逻辑
            logOperation("处理上传回调", "callback", true);
            return true;
        } catch (Exception e) {
            logException("处理上传回调", "callback", e);
            throw new BusinessException(ResponseEnum.FILE_CALLBACK);
        }
    }

    @Override
    public double getUploadProgress(String uploadId) {
        try {
            ListPartsRequest listPartsRequest = new ListPartsRequest(ossConfig.getBucket(), "", uploadId);
            PartListing partListing = ossClient.listParts(listPartsRequest);
            int uploadedParts = partListing.getParts().size();
            // 这里需要根据具体业务逻辑计算进度
            return uploadedParts * 100.0 / 10;
        } catch (Exception e) {
            logException("获取上传进度", uploadId, e);
            return 0;
        }
    }

    @Override
    public String previewFile(String fileKey) {
        String fileUrl = getFileUrl(fileKey);
        logOperation("文件预览", fileKey, true);
        return fileUrl;
    }

    @Override
    public String getFileUrl(String fileKey) {
        if (ossConfig.getDomain() != null && !ossConfig.getDomain().trim().isEmpty()) {
            return ossConfig.getDomain().endsWith("/") ? ossConfig.getDomain() + fileKey : ossConfig.getDomain() + "/" + fileKey;
        }
        throw new BusinessException(ResponseEnum.FILE_DOMAIN);
    }
}
