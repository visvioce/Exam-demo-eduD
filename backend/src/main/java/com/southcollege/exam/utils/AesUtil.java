package com.southcollege.exam.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * AES 加密工具类
 * 用于加密敏感数据（如 API Key）
 */
@Component
public class AesUtil {

    @Value("${aes.secret:a1b2c3d4e5f6g7h8}")
    private String secretKey;

    private static AES aes;

    @PostConstruct
    public void init() {
        // 确保 key 长度为 16 字节（128位）
        byte[] key = padKey(secretKey.getBytes(StandardCharsets.UTF_8));
        aes = SecureUtil.aes(key);
    }

    /**
     * 加密
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        byte[] encrypted = aes.encrypt(plainText);
        return HexUtil.encodeHexStr(encrypted);
    }

    /**
     * 解密
     */
    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        byte[] decrypted = aes.decrypt(HexUtil.decodeHex(encryptedText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 填充 key 到 16 字节
     */
    private byte[] padKey(byte[] key) {
        byte[] result = new byte[16];
        int len = Math.min(key.length, 16);
        System.arraycopy(key, 0, result, 0, len);
        // 不足 16 字节用 0 填充
        for (int i = len; i < 16; i++) {
            result[i] = 0;
        }
        return result;
    }
}
