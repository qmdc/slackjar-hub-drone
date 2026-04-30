package com.slack.slackjarservice.foundation.service.impl;

import cn.hutool.extra.ssh.JschUtil;
import cn.hutool.extra.ssh.Sftp;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.slack.slackjarservice.common.enumtype.foundation.CertSourceEnum;
import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.foundation.dao.SSLCertificateDao;
import com.slack.slackjarservice.foundation.entity.SSLCertificate;
import com.slack.slackjarservice.foundation.model.dto.ServerConfigDTO;
import com.slack.slackjarservice.foundation.model.request.SslCertRequest;
import com.slack.slackjarservice.foundation.service.SSLCertService;
import com.slack.slackjarservice.foundation.service.SysConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SSL证书信息表(SslCertificate)表服务实现类
 *
 * @author zhn
 * @since 2025-09-04 10:23:19
 */
@Slf4j
@Service("sslCertificateService")
public class SSLCertServiceImpl extends ServiceImpl<SSLCertificateDao, SSLCertificate> implements SSLCertService {

    @Resource
    private SysConfigService sysConfigService;

    @Override
    public SSLCertificate uploadCert(SslCertRequest request) {
        CertSourceEnum.validate(request.getCertSource());
        try {
            // 1. 读取主服务器参数配置
            ServerConfigDTO serverConfig = sysConfigService.getServerConfigDTO();

            // 2. 创建SFTP连接
            try (Sftp sftp = createSftpConnection(serverConfig)) {

                // 3. 获取SSL证书存储路径
                String sslCertPath = serverConfig.getSslCertPath();

                // 4. 创建证书目录（如果不存在）
                try {
                    sftp.cd(sslCertPath);
                } catch (Exception e) {
                    // 目录不存在，创建目录
                    sftp.mkdir(sslCertPath);
                    sftp.cd(sslCertPath);
                }

                // 5. 创建证书文件
                String keyFileName = request.getDomain() + ".key";
                String pemFileName = request.getDomain() + ".pem";

                // 6. 覆盖上传证书文件
                // 使用OVERWRITE模式确保覆盖已存在的文件
                try (java.io.InputStream keyInputStream = new java.io.ByteArrayInputStream(request.getSslCertKey().getBytes());
                     java.io.InputStream pemInputStream = new java.io.ByteArrayInputStream(request.getSslCertPem().getBytes())) {

                    sftp.upload(sslCertPath, keyFileName, keyInputStream);
                    sftp.upload(sslCertPath, pemFileName, pemInputStream);

                    // 验证文件是否上传成功
                    if (!sftp.exist(keyFileName) || !sftp.exist(pemFileName)) {
                        throw new BusinessException(ResponseEnum.SSL_CERT_UPLOAD_VERIFY_FAIL);
                    }
                }

                // 7. 封装数据库参数，进行存库
                SSLCertificate sslCertificate = assemblyAndPersistence(request, sslCertPath);
                log.info("SSL证书上传成功，域名: {}, 服务器: {}, 保存路径: {}",
                        sslCertificate.getDomain(), serverConfig.getServerIp(), sslCertPath);
                return sslCertificate;
            }
        } catch (Exception e) {
            log.error("SSL证书上传失败，域名: {}", request.getDomain(), e);
            throw new BusinessException(ResponseEnum.SSL_CERT_SFTP_UPLOAD_FAIL);
        }
    }

    /**
     * 创建SFTP连接
     *
     * @param serverConfig 服务器配置信息
     * @return SFTP连接对象
     * @throws BusinessException 连接创建失败时抛出异常
     */
    private Sftp createSftpConnection(ServerConfigDTO serverConfig) throws BusinessException {
        try {
            return JschUtil.createSftp(serverConfig.getServerIp(), serverConfig.getServerPort(),
                    serverConfig.getServerUsername(), serverConfig.getServerPassword());
        } catch (Exception e) {
            log.error("创建SFTP连接失败，服务器IP: {}，端口: {}", serverConfig.getServerIp(), serverConfig.getServerPort(), e);
            throw new BusinessException(ResponseEnum.SSL_CERT_SFTP_CONNECT_FAIL.getMessage() + ": " + e.getMessage());
        }
    }

    private SSLCertificate assemblyAndPersistence(SslCertRequest request, String sslCertPath) {
        SSLCertificate sslCertificate = new SSLCertificate();
        sslCertificate.setDomain(request.getDomain());
        sslCertificate.setSslCertKey(request.getSslCertKey());
        sslCertificate.setSslCertPem(request.getSslCertPem());
        sslCertificate.setSslCertPath(sslCertPath);
        sslCertificate.setValidityDays(request.getValidityDays());
        sslCertificate.setCertSource(request.getCertSource());
        sslCertificate.setConsoleWiki(request.getConsoleWiki());
        sslCertificate.setApplyTime(request.getApplyTime());

        SSLCertificate existingCert = this.lambdaQuery().eq(SSLCertificate::getDomain, request.getDomain()).one();

        if (existingCert != null) {
            sslCertificate.setId(existingCert.getId());
            this.updateById(sslCertificate);
            log.info("SSL证书更新成功，域名: {}", request.getDomain());
        } else {
            this.save(sslCertificate);
            log.info("SSL证书新增成功，域名: {}", request.getDomain());
        }

        return this.lambdaQuery().eq(SSLCertificate::getDomain, request.getDomain()).one();
    }

    @Override
    public SSLCertificate getByDomain(String domain) {
        return this.lambdaQuery().eq(SSLCertificate::getDomain, domain).one();
    }

}

