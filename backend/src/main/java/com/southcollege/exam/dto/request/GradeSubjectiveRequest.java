package com.southcollege.exam.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 主观题评分请求
 */
@Data
public class GradeSubjectiveRequest {

    /**
     * 考试记录ID
     */
    private Long examSessionId;

    /**
     * 主观题评分列表
     */
    private List<SubjectiveGrade> grades;

    @Data
    public static class SubjectiveGrade {
        /**
         * 题目ID
         */
        private Long questionId;

        /**
         * 得分
         */
        private BigDecimal score;

        /**
         * 评语
         */
        private String comment;
    }
}
