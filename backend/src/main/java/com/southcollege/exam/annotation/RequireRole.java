package com.southcollege.exam.annotation;

import com.southcollege.exam.enums.RoleEnum;

import java.lang.annotation.*;

/**
 * 角色权限注解
 * 用于标记需要特定角色才能访问的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    /**
     * 需要的角色列表
     */
    RoleEnum[] value();

    /**
     * 是否需要满足所有角色（默认只需满足其中一个）
     */
    boolean requireAll() default false;
}
