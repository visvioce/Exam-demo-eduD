package com.southcollege.exam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 配置
 * <p>
 * 配置说明：
 * <ul>
 * <li>禁用 CSRF（使用 JWT 认证）</li>
 * <li>配置 CORS 跨域</li>
 * <li>认证接口白名单：/api/auth/**、/swagger-ui/**、/v3/api-docs/**</li>
 * <li>其他接口通过自定义 JwtInterceptor + RoleInterceptor 实现认证和权限控制</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
	private String allowedOrigins;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// 禁用 CSRF（因为我们使用 JWT）
			.csrf(AbstractHttpConfigurer::disable)
			// 配置 CORS
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			// 配置请求授权
			// 白名单：认证接口、Swagger 文档
			// 其他接口通过自定义拦截器处理（JwtInterceptor + RoleInterceptor）
			.authorizeHttpRequests(auth -> auth
				// 认证接口白名单
				.requestMatchers("/api/auth/**").permitAll()
				// Swagger 文档白名单
				.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
				// 其他接口允许访问，由自定义拦截器处理认证
				.anyRequest().permitAll()
			);
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		String[] origins = allowedOrigins.split(",");
		for (String origin : origins) {
			configuration.addAllowedOriginPattern(origin.trim());
		}
		configuration.addAllowedMethod("*");
		configuration.addAllowedHeader("*");
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
