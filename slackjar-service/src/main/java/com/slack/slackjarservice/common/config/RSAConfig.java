package com.slack.slackjarservice.common.config;

import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class RSAConfig {

    private static final String ALGORITHM = "RSA";

    @Bean
    public PublicKey publicKey() {
        try {
            ClassPathResource resource = new ClassPathResource("keys/public_key.pem");
            String publicKeyStr = readKey(resource);
            // 移除PEM头尾和换行符
            publicKeyStr = publicKeyStr.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\n", "")
                    .replaceAll("\r", "");
            return loadPublicKey(publicKeyStr);
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.RSA_LOAD_PUBLIC_KEY_ERROR);
        }
    }

    @Bean
    public PrivateKey privateKey() {
        try {
            ClassPathResource resource = new ClassPathResource("keys/private_key.pem");
            String privateKeyStr = readKey(resource);
            // 移除PEM头尾和换行符
            privateKeyStr = privateKeyStr.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\n", "")
                    .replaceAll("\r", "");
            return loadPrivateKey(privateKeyStr);
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.RSA_LOAD_PRIVATE_KEY_ERROR);
        }
    }

    private String readKey(ClassPathResource resource) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("读取密钥文件失败" + e);
            throw new BusinessException(ResponseEnum.FILE_READER_STREAM);
        }
    }

    /**
     * 从字符串加载公钥
     *
     * @param publicKey 公钥字符串
     * @return PublicKey对象
     */
    public static PublicKey loadPublicKey(String publicKey) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.RSA_LOAD_PUBLIC_KEY_ERROR);
        }
    }

    /**
     * 从字符串加载私钥
     *
     * @param privateKey 私钥字符串
     * @return PrivateKey对象
     */
    public static PrivateKey loadPrivateKey(String privateKey) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(privateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.RSA_LOAD_PRIVATE_KEY_ERROR);
        }
    }
}
