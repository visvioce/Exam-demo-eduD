package com.southcollege.exam.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum RoleEnum {
    ADMIN("ADMIN", "管理员"),
    TEACHER("TEACHER", "教师"),
    STUDENT("STUDENT", "学生");

    private final String code;
    private final String description;

    RoleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static RoleEnum fromCode(String code) {
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的角色: " + code);
    }
}
