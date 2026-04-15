package com.southcollege.exam.config;

import com.southcollege.exam.annotation.RequireRole;
import com.southcollege.exam.entity.User;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.exception.BusinessException;
import com.southcollege.exam.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色权限拦截器
 * 复用 JwtInterceptor 已存入 request 的用户信息，避免重复解析 token 和查询数据库
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 预检请求不校验角色
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        // 如果没有注解，检查类上是否有注解
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        // 如果没有权限注解，直接放行
        if (requireRole == null) {
            return true;
        }

        // 从 request 属性获取用户信息（由 JwtInterceptor 存入）
        String userRole = (String) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        
        // JJWT 0.12.x 兼容性修复：如果role为空但userId存在，从数据库查询真实角色
        if (userId == null) {
            throw new BusinessException("未登录或 token 无效");
        }
        
        // 兼容JJWT解析问题：如果JWT中role字段为null，从数据库查询真实角色
        if (userRole == null) {
            User user = userService.getById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            userRole = user.getRole();
            // 将真实角色存回request，避免后续重复查询
            request.setAttribute("role", userRole);
        }

        // 检查角色权限
        Set<String> requiredRoles = Arrays.stream(requireRole.value())
                .map(Enum::name)
                .collect(Collectors.toSet());

        // 检查用户角色是否在所需角色列表中
        if (!requiredRoles.contains(userRole)) {
            throw new BusinessException("权限不足");
        }

        return true;
    }
}
