package com.southcollege.exam.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试结果响应
 */
@Data
public class ExamResultResponse {

    /**
     * 考试记录ID
     */
    private Long examSessionId;

    /**
     * 考试ID
     */
    private Long examId;

    /**
     * 考试标题
     */
    private String examTitle;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;

    /**
     * 客观题得分
     */
    private BigDecimal objectiveScore;

    /**
     * 主观题得分
     */
    private BigDecimal subjectiveScore;

    /**
     * 总分
     */
    private BigDecimal totalScore;

    /**
     * 试卷总分
     */
    private BigDecimal maxScore;

    /**
     * 评分状态
     */
    private String gradingStatus;

    /**
     * 答题详情
     */
    private List<AnswerDetail> answers;

    @Data
    public static class AnswerDetail {
        private Long questionId;
        private String questionContent;
        private String questionType;
        private String answer;
        private Boolean isCorrect;
        private BigDecimal score;
        private BigDecimal maxScore;
        private String gradingStatus;
        private String teacherComment;
    }
}
