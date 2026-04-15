package com.southcollege.exam.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Token 验证结果枚举
     */
    @Getter
    public static class TokenValidationResult {
        private final boolean valid;
        private final String errorCode;
        private final String message;

        private TokenValidationResult(boolean valid, String errorCode, String message) {
            this.valid = valid;
            this.errorCode = errorCode;
            this.message = message;
        }

        public static TokenValidationResult valid() {
            return new TokenValidationResult(true, null, "Token 有效");
        }

        public static TokenValidationResult expired() {
            return new TokenValidationResult(false, "TOKEN_EXPIRED", "Token 已过期，请重新登录");
        }

        public static TokenValidationResult invalidSignature() {
            return new TokenValidationResult(false, "INVALID_SIGNATURE", "Token 签名无效");
        }

        public static TokenValidationResult malformed() {
            return new TokenValidationResult(false, "MALFORMED_TOKEN", "Token 格式错误");
        }

        public static TokenValidationResult invalid() {
            return new TokenValidationResult(false, "INVALID_TOKEN", "Token 无效");
        }

        public static TokenValidationResult empty() {
            return new TokenValidationResult(false, "EMPTY_TOKEN", "Token 为空");
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 验证 Token（简单版本，返回布尔值）
     */
    public boolean validateToken(String token) {
        return validateTokenWithResult(token).isValid();
    }

    /**
     * 验证 Token（详细版本，返回验证结果）
     */
    public TokenValidationResult validateTokenWithResult(String token) {
        if (token == null || token.trim().isEmpty()) {
            return TokenValidationResult.empty();
        }

        try {
            parseToken(token);
            return TokenValidationResult.valid();
        } catch (ExpiredJwtException e) {
            return TokenValidationResult.expired();
        } catch (SignatureException e) {
            return TokenValidationResult.invalidSignature();
        } catch (MalformedJwtException e) {
            return TokenValidationResult.malformed();
        } catch (UnsupportedJwtException e) {
            return new TokenValidationResult(false, "UNSUPPORTED_TOKEN", "不支持的 Token 类型");
        } catch (IllegalArgumentException e) {
            return TokenValidationResult.invalid();
        } catch (Exception e) {
            return TokenValidationResult.invalid();
        }
    }
}
