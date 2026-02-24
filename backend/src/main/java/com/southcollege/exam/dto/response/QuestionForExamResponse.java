package com.southcollege.exam.dto.response;

import com.southcollege.exam.entity.Question;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 考试题目响应（不包含答案）
 * 用于学生参加考试时获取题目信息
 */
@Data
public class QuestionForExamResponse {

    private Long id;
    private String content;
    private String type;
    private String difficulty;
    private BigDecimal score;
    private String subject;
    private List<Question.Option> options;

    /**
     * 从 Question 实体转换（排除正确答案）
     */
    public static QuestionForExamResponse from(Question question) {
        QuestionForExamResponse response = new QuestionForExamResponse();
        response.setId(question.getId());
        response.setContent(question.getContent());
        response.setType(question.getType());
        response.setDifficulty(question.getDifficulty());
        response.setScore(question.getScore());
        response.setSubject(question.getSubject());
        response.setOptions(question.getOptions());
        // 不包含 correctAnswer 和 explanation
        return response;
    }
}
