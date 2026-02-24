package com.southcollege.exam.config;

import com.southcollege.exam.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 * 拦截所有需要认证的请求，验证JWT Token的有效性
 * 并将用户信息（userId、username、role）存入request属性中
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 在请求处理之前执行
     * 验证JWT Token，提取用户信息
     * @return true-继续处理请求，false-拦截请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取Authorization
        String token = request.getHeader("Authorization");
        
        // 检查token是否存在且以Bearer开头
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未提供token\",\"data\":null}");
            return false;
        }
        
        // 去掉Bearer前缀，获取纯token
        token = token.substring(7);
        
        // 验证token有效性
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\",\"data\":null}");
            return false;
        }
        
        // 从token中提取用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        
        // 将用户信息存入request属性，供后续使用
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        
        return true;
    }
}
