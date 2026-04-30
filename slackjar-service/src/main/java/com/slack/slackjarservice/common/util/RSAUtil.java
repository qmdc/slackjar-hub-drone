package com.slack.slackjarservice.common.util;

import com.slack.slackjarservice.common.enumtype.foundation.ResponseEnum;
import com.slack.slackjarservice.common.exception.BusinessException;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RSAUtil {

    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    /**
     * 使用公钥加密数据
     *
     * @param data      要加密的数据
     * @param publicKey 公钥
     * @return 加密后的数据
     */
    public static String encrypt(String data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.RSA_ENCRYPT_ERROR);
        }
    }

    /**
     * 使用私钥解密数据
     *
     * @param encryptedData 要解密的数据
     * @param privateKey    私钥
     * @return 解密后的数据
     */
    public static String decrypt(String encryptedData, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] data = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(data);
            return new String(decryptedData);
        } catch (Exception e) {
            throw new BusinessException(ResponseEnum.RSA_DECRYPT_ERROR);
        }
    }

}
