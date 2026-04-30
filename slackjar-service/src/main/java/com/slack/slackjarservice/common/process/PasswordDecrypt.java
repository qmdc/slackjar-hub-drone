package com.slack.slackjarservice.common.process;

import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import com.slack.slackjarservice.common.util.RSAUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;

@Component
public class PasswordDecrypt {

    @Resource
    private PrivateKey privateKey;

    /**
     * 解密密码
     *
     * @param encryptedPassword 加密后的密码
     * @return 解密后的密码
     */
    public String decrypt(String encryptedPassword) {
        try {
            return RSAUtil.decrypt(encryptedPassword, privateKey);
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.RSA_DECRYPT_ERROR);
        }
    }

}
