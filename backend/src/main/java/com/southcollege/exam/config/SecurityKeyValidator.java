package com.southcollege.exam.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 安全密钥验证器
 * 在应用启动时检查生产环境的密钥配置是否安全
 */
@Slf4j
@Component
public class SecurityKeyValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${aes.secret}")
    private String aesSecret;

    private final Environment environment;

    // 不安全的默认密钥列表
    private static final String[] INSECURE_JWT_SECRETS = {
            "your_jwt_secret_key_here_change_in_production_please_use_at_least_256_bits",
            "SouthCollegeExam2024GraduationJwtSecretKey"
    };

    private static final String[] INSECURE_AES_SECRETS = {
            "a1b2c3d4e5f6g7h8",
            "SouthCollege@2024"
    };

    public SecurityKeyValidator(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateSecurityKeys() {
        boolean isProduction = isProductionEnvironment();

        if (isProduction) {
            validateJwtSecret();
            validateAesSecret();
        } else {
            // 开发环境只做警告
            warnIfInsecureKey();
        }
    }

    private boolean isProductionEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.asList(activeProfiles).contains("prod");
    }

    private void validateJwtSecret() {
        // JWT 密钥至少需要 256 位 (32 字节) 才能用于 HS256
        if (jwtSecret.length() < 32) {
            throw new SecurityConfigurationException(
                    "JWT 密钥长度不足！生产环境必须使用至少 32 字符的密钥。当前长度: " + jwtSecret.length());
        }

        if (Arrays.asList(INSECURE_JWT_SECRETS).contains(jwtSecret)) {
            throw new SecurityConfigurationException(
                    "JWT 密钥使用了不安全的默认值！生产环境必须通过环境变量 JWT_SECRET 设置安全的密钥。");
        }

        log.info("JWT 密钥验证通过");
    }

    private void validateAesSecret() {
        // AES 密钥必须是 16、24 或 32 字节
        if (aesSecret.length() != 16 && aesSecret.length() != 24 && aesSecret.length() != 32) {
            throw new SecurityConfigurationException(
                    "AES 密钥长度无效！必须是 16、24 或 32 字符。当前长度: " + aesSecret.length());
        }

        if (Arrays.asList(INSECURE_AES_SECRETS).contains(aesSecret)) {
            throw new SecurityConfigurationException(
                    "AES 密钥使用了不安全的默认值！生产环境必须通过环境变量 AES_SECRET 设置安全的密钥。");
        }

        log.info("AES 密钥验证通过");
    }

    private void warnIfInsecureKey() {
        if (Arrays.asList(INSECURE_JWT_SECRETS).contains(jwtSecret)) {
            log.warn("⚠️ 警告: JWT 使用默认密钥，生产环境请设置环境变量 JWT_SECRET");
        }

        if (Arrays.asList(INSECURE_AES_SECRETS).contains(aesSecret)) {
            log.warn("⚠️ 警告: AES 使用默认密钥，生产环境请设置环境变量 AES_SECRET");
        }
    }

    /**
     * 安全配置异常
     */
    public static class SecurityConfigurationException extends RuntimeException {
        public SecurityConfigurationException(String message) {
            super(message);
        }
    }
}
