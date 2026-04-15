package com.southcollege.exam.enums;

import lombok.Getter;

/**
 * 考试状态枚举
 */
@Getter
public enum ExamStatusEnum {
    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    STARTED("STARTED", "进行中"),
    ENDED("ENDED", "已结束"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;

    ExamStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ExamStatusEnum fromCode(String code) {
        for (ExamStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的考试状态: " + code);
    }
}
