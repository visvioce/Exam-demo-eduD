package com.southcollege.exam.config;

import com.southcollege.exam.annotation.RequireRole;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
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
        if (userRole == null) {
            throw new BusinessException("未登录或 token 无效");
        }

        // 检查角色权限
        Set<String> requiredRoles = Arrays.stream(requireRole.value())
                .map(RoleEnum::getCode)
                .collect(Collectors.toSet());

        if (requireRole.requireAll()) {
            // 需要满足所有角色（单角色场景下与普通检查相同）
            if (!requiredRoles.contains(userRole)) {
                throw new BusinessException("权限不足，需要角色: " + requiredRoles);
            }
        } else {
            // 只需满足其中一个角色
            if (!requiredRoles.contains(userRole)) {
                throw new BusinessException("权限不足，需要角色: " + requiredRoles + " 之一");
            }
        }

        return true;
    }
}
