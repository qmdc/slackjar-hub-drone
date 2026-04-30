package com.slack.slackjarservice.foundation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.slack.slackjarservice.foundation.entity.SSLCertificate;
import com.slack.slackjarservice.foundation.model.request.SslCertRequest;

/**
 * SSL证书信息表(SslCertificate)表服务接口
 *
 * @author zhn
 * @since 2025-09-04 10:23:19
 */
public interface SSLCertService extends IService<SSLCertificate> {

    /**
     * 上传SSL证书
     * @param request SSL证书请求参数
     * @return SSL证书响应结果
     */
    SSLCertificate uploadCert(SslCertRequest request);

    /**
     * 根据域名查询SSL证书
     * @param domain 域名
     * @return SSL证书信息
     */
    SSLCertificate getByDomain(String domain);

}

