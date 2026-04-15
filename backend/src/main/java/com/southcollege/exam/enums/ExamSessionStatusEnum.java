package com.southcollege.exam.enums;

import lombok.Getter;

/**
 * 考试会话状态枚举
 */
@Getter
public enum ExamSessionStatusEnum {
    NOT_STARTED("NOT_STARTED", "未开始"),
    IN_PROGRESS("IN_PROGRESS", "进行中"),
    SUBMITTED("SUBMITTED", "已提交"),
    GRADED("GRADED", "已评分");

    private final String code;
    private final String description;

    ExamSessionStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ExamSessionStatusEnum fromCode(String code) {
        for (ExamSessionStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的考试会话状态: " + code);
    }
}
