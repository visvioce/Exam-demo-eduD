package com.southcollege.exam.utils;

import jakarta.servlet.http.HttpServletRequest;
import com.southcollege.exam.exception.BusinessException;

/**
 * 安全工具类
 * 用于获取当前用户信息和验证访问权限
 */
public class SecurityUtil {

    /**
     * 获取当前用户ID（带null检查）
     * @param request HTTP请求
     * @return 用户ID
     * @throws BusinessException 如果用户未登录或token无效
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new BusinessException("用户未登录或token已失效，请重新登录");
        }
        return userId;
    }

    /**
     * 获取当前用户ID（安全版本，返回null）
     * @param request HTTP请求
     * @return 用户ID，可能为null
     */
    public static Long getCurrentUserIdSafe(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    /**
     * 获取当前用户名（带null检查）
     * @param request HTTP请求
     * @return 用户名
     * @throws BusinessException 如果用户未登录或token无效
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            throw new BusinessException("无法获取用户信息，请重新登录");
        }
        return username;
    }

    /**
     * 获取当前用户角色（带null检查）
     * @param request HTTP请求
     * @return 用户角色
     * @throws BusinessException 如果用户未登录或role无效
     */
    public static String getCurrentUserRole(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (role == null) {
            throw new BusinessException("无法获取用户角色信息，请重新登录");
        }
        return role;
    }

    /**
     * 获取当前用户角色（安全版本，返回null）
     * @param request HTTP请求
     * @return 用户角色，可能为null
     */
    public static String getCurrentUserRoleSafe(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }

    /**
     * 检查当前用户是否拥有指定角色
     * @param request HTTP请求
     * @param requiredRoles 需要的角色列表
     * @return 是否拥有任一角色
     */
    public static boolean hasAnyRole(HttpServletRequest request, String... requiredRoles) {
        String role = getCurrentUserRoleSafe(request);
        if (role == null) return false;

        for (String required : requiredRoles) {
            if (role.equals(required)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否是管理员
     */
    public static boolean isAdmin(HttpServletRequest request) {
        return hasAnyRole(request, "ADMIN");
    }

    /**
     * 检查是否是教师
     */
    public static boolean isTeacher(HttpServletRequest request) {
        return hasAnyRole(request, "TEACHER");
    }

    /**
     * 检查是否是学生
     */
    public static boolean isStudent(HttpServletRequest request) {
        return hasAnyRole(request, "STUDENT");
    }
}
