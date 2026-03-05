package com.xx.xianqijava.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES加密工具类
 * 用于敏感数据（如身份证号）的加密存储
 */
@Slf4j
@Component
public class AESUtil {

    /**
     * 加密密钥（建议从配置文件读取，这里使用默认值）
     */
    private static String SECRET_KEY = "xianqi-secret-key-16bytes";

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加密模式和填充方式
     */
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 设置加密密钥
     */
    @Value("${auth.aes.secret-key:xianqi-secret-key-16bytes}")
    public void setSecretKey(String secretKey) {
        AESUtil.SECRET_KEY = secretKey;
    }

    /**
     * AES加密
     *
     * @param plainText 明文
     * @return Base64编码的密文
     */
    public static String encrypt(String plainText) {
        try {
            // 确保密钥长度为16字节（128位）
            byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 16) {
                byte[] paddedKey = new byte[16];
                System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
                keyBytes = paddedKey;
            } else if (keyBytes.length > 16) {
                byte[] truncatedKey = new byte[16];
                System.arraycopy(keyBytes, 0, truncatedKey, 0, 16);
                keyBytes = truncatedKey;
            }

            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * AES解密
     *
     * @param cipherText Base64编码的密文
     * @return 明文
     */
    public static String decrypt(String cipherText) {
        try {
            // 确保密钥长度为16字节（128位）
            byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 16) {
                byte[] paddedKey = new byte[16];
                System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
                keyBytes = paddedKey;
            } else if (keyBytes.length > 16) {
                byte[] truncatedKey = new byte[16];
                System.arraycopy(keyBytes, 0, truncatedKey, 0, 16);
                keyBytes = truncatedKey;
            }

            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }
}
