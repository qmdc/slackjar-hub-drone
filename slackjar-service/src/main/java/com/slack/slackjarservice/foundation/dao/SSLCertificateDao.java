package com.slack.slackjarservice.foundation.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.slack.slackjarservice.foundation.entity.SSLCertificate;

/**
 * SSL证书信息表(SslCertificate)表数据库访问层
 *
 * @author zhn
 * @since 2025-09-04 10:23:18
 */
@Mapper
public interface SSLCertificateDao extends BaseMapper<SSLCertificate> {

}

