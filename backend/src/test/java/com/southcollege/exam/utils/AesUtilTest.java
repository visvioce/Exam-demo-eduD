package com.southcollege.exam.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AES 加密工具类测试
 */
class AesUtilTest {

    @BeforeAll
    static void setUp() throws Exception {
        // 手动初始化 AesUtil（模拟 @PostConstruct）
        AesUtil aesUtil = new AesUtil();

        // 设置 secretKey 字段
        Field secretKeyField = AesUtil.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(aesUtil, "test_secret_key");

        // 调用 init 方法
        Method initMethod = AesUtil.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(aesUtil);
    }

    @Test
    void testEncryptAndDecrypt() {
        String plainText = "sk-test-api-key-123456";
        String encrypted = AesUtil.encrypt(plainText);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);

        String decrypted = AesUtil.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    void testEncryptNull() {
        String result = AesUtil.encrypt(null);
        assertNull(result);
    }

    @Test
    void testEncryptEmpty() {
        String result = AesUtil.encrypt("");
        assertEquals("", result);
    }

    @Test
    void testDecryptNull() {
        String result = AesUtil.decrypt(null);
        assertNull(result);
    }

    @Test
    void testDecryptEmpty() {
        String result = AesUtil.decrypt("");
        assertEquals("", result);
    }

    @Test
    void testEncryptChineseCharacters() {
        String plainText = "中文测试密钥";
        String encrypted = AesUtil.encrypt(plainText);
        String decrypted = AesUtil.decrypt(encrypted);

        assertEquals(plainText, decrypted);
    }
}
