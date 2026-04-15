package com.southcollege.exam.enums;

import lombok.Getter;

/**
 * 评分状态枚举
 */
@Getter
public enum GradingStatusEnum {
    PENDING("PENDING", "待评分"),
    GRADING("GRADING", "评分中"),
    GRADED("GRADED", "已评分"),
    COMPLETED("COMPLETED", "评分完成");

    private final String code;
    private final String description;

    GradingStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static GradingStatusEnum fromCode(String code) {
        for (GradingStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的评分状态: " + code);
    }
}
