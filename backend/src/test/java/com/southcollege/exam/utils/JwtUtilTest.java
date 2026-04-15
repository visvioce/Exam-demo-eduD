package com.southcollege.exam.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具类测试
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // 设置测试用的 secret 和 expiration
        ReflectionTestUtils.setField(jwtUtil, "secret", "this-is-a-test-secret-key-for-jwt-token-generation-minimum-256-bits");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1小时
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken(1L, "testuser", "STUDENT");

        assertNotNull(token);
        assertTrue(token.length() > 0);
        // JWT 格式：header.payload.signature
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void testParseToken() {
        String token = jwtUtil.generateToken(1L, "testuser", "STUDENT");

        Claims claims = jwtUtil.parseToken(token);

        assertNotNull(claims);
        assertEquals("1", claims.getSubject());
        assertEquals("testuser", claims.get("username", String.class));
        assertEquals("STUDENT", claims.get("role", String.class));
    }

    @Test
    void testGetUserIdFromToken() {
        String token = jwtUtil.generateToken(123L, "testuser", "STUDENT");

        Long userId = jwtUtil.getUserIdFromToken(token);

        assertEquals(123L, userId);
    }

    @Test
    void testGetUsernameFromToken() {
        String token = jwtUtil.generateToken(1L, "myusername", "STUDENT");

        String username = jwtUtil.getUsernameFromToken(token);

        assertEquals("myusername", username);
    }

    @Test
    void testGetRoleFromToken() {
        String token = jwtUtil.generateToken(1L, "testuser", "ADMIN");

        String role = jwtUtil.getRoleFromToken(token);

        assertEquals("ADMIN", role);
    }

    @Test
    void testValidateToken_Valid() {
        String token = jwtUtil.generateToken(1L, "testuser", "STUDENT");

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_Invalid() {
        String invalidToken = "invalid.token.here";

        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_Empty() {
        assertFalse(jwtUtil.validateToken(""));
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void testValidateToken_Tampered() {
        String token = jwtUtil.generateToken(1L, "testuser", "STUDENT");
        // 篡改 token
        String tamperedToken = token + "x";

        assertFalse(jwtUtil.validateToken(tamperedToken));
    }
}
