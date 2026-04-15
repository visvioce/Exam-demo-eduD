package com.southcollege.exam.enums;

import lombok.Getter;

/**
 * 题目类型枚举
 */
@Getter
public enum QuestionTypeEnum {
    SINGLE_CHOICE("SINGLE_CHOICE", "单选题"),
    MULTIPLE_CHOICE("MULTIPLE_CHOICE", "多选题"),
    TRUE_FALSE("TRUE_FALSE", "判断题"),
    FILL_BLANK("FILL_BLANK", "填空题"),
    ESSAY("ESSAY", "问答题");

    private final String code;
    private final String description;

    QuestionTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static QuestionTypeEnum fromCode(String code) {
        for (QuestionTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的题目类型: " + code);
    }

    /**
     * 是否是客观题
     */
    public boolean isObjective() {
        return this == SINGLE_CHOICE || this == MULTIPLE_CHOICE || this == TRUE_FALSE || this == FILL_BLANK;
    }

    /**
     * 是否是主观题
     */
    public boolean isSubjective() {
        return this == ESSAY;
    }
}
