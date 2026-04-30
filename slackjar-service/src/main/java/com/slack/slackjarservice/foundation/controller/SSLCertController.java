package com.slack.slackjarservice.foundation.controller;

import com.slack.slackjarservice.common.base.BaseController;
import com.slack.slackjarservice.common.enumtype.foundation.OperationEnum;
import com.slack.slackjarservice.common.response.ApiResponse;
import com.slack.slackjarservice.foundation.entity.SSLCertificate;
import com.slack.slackjarservice.foundation.model.request.SslCertRequest;
import com.slack.slackjarservice.foundation.service.SSLCertService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * SSL证书信息表(SslCertificate)表控制层
 *
 * @author zhn
 * @since 2025-09-04 10:23:18
 */
@RestController
@RequestMapping("/sslCertificate")
public class SSLCertController extends BaseController {

    @Resource
    private SSLCertService sslCertificateService;

    /**
     * 上传SSL证书
     * @param request SSL证书请求参数
     * @return 操作结果
     */
    @PostMapping("/upload-cert")
    public ApiResponse<SSLCertificate> uploadCert(@Validated @RequestBody SslCertRequest request) {
        SSLCertificate response = sslCertificateService.uploadCert(request);
        recordOperateLog(OperationEnum.USER_UPSERT, "SSL证书上传成功，域名：" + request.getDomain());
        return success(response);
    }

    /**
     * 根据域名查询SSL证书
     * @param domain 域名
     * @return SSL证书信息
     */
    @GetMapping("/get-by-domain")
    public ApiResponse<SSLCertificate> getByDomain(@RequestParam String domain) {
        SSLCertificate response = sslCertificateService.getByDomain(domain);
        recordOperateLog(OperationEnum.USER_QUERY, "查询SSL证书，domain：" +domain);
        return success(response);
    }

}

