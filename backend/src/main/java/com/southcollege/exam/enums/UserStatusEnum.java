package com.southcollege.exam.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatusEnum {
    INACTIVE("INACTIVE", "禁用"),
    ACTIVE("ACTIVE", "正常"),
    SUSPENDED("SUSPENDED", "锁定");

    @EnumValue  // MyBatis-Plus 会将此字段的值存入数据库
    private final String code;
    private final String description;

    UserStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserStatusEnum fromCode(String code) {
        if (code == null) {
            return ACTIVE; // 默认正常状态
        }
        for (UserStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的用户状态: " + code);
    }
}
