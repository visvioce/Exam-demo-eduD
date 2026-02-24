package com.southcollege.exam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * 安全配置验证器
 * 防止在生产环境使用默认密钥等不安全配置
 */
@Component
public class SecurityConfigValidator {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${aes.secret:}")
    private String aesSecret;

    @PostConstruct
    public void validateConfiguration() {
        // 检查是否使用默认值
        if ("SouthCollegeExam2024GraduationJwtSecretKey".equals(jwtSecret)) {
            throw new IllegalStateException(
                "❌ JWT_SECRET 使用了默认值不安全！\n" +
                "请在环境变量中设置强随机密钥，例如：\n" +
                "export JWT_SECRET=$(openssl rand -base64 32)\n" +
                "或在 application.yml 中配置具体的密钥值。"
            );
        }
        if ("SouthCollege@2024".equals(aesSecret)) {
            throw new IllegalStateException(
                "❌ AES_SECRET 使用了默认值不安全！\n" +
                "请在环境变量中设置强随机密钥，例如：\n" +
                "export AES_SECRET=$(openssl rand -base64 16)\n" +
                "或在 application.yml 中配置具体的密钥值。"
            );
        }

        // 检查密钥长度
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException(
                "❌ JWT_SECRET 长度至少需要 32 位字符，当前长度: " + jwtSecret.length() + "\n" +
                "生成方法：openssl rand -base64 32"
            );
        }
        if (aesSecret.length() < 16) {
            throw new IllegalStateException(
                "❌ AES_SECRET 长度至少需要 16 位字符，当前长度: " + aesSecret.length() + "\n" +
                "生成方法：openssl rand -base64 16"
            );
        }

        System.out.println("✅ 安全配置验证通过 - 密钥长度合规");
    }
}
