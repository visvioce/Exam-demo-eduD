package com.southcollege.exam.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码工具类测试
 */
class PasswordUtilTest {

    @Test
    void testEncrypt() {
        String password = "test123456";
        String encrypted = PasswordUtil.encrypt(password);

        assertNotNull(encrypted);
        assertNotEquals(password, encrypted);
        assertTrue(encrypted.startsWith("$2a$") || encrypted.startsWith("$2b$"));
    }

    @Test
    void testMatches() {
        String password = "test123456";
        String encrypted = PasswordUtil.encrypt(password);

        assertTrue(PasswordUtil.matches(password, encrypted));
        assertFalse(PasswordUtil.matches("wrongpassword", encrypted));
    }

    @Test
    void testDifferentPasswordsHaveDifferentHashes() {
        String password = "test123456";
        String encrypted1 = PasswordUtil.encrypt(password);
        String encrypted2 = PasswordUtil.encrypt(password);

        // BCrypt 每次加密结果不同（因为使用了随机盐）
        assertNotEquals(encrypted1, encrypted2);
    }
}
