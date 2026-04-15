package com.southcollege.exam.dto.response;

import com.southcollege.exam.entity.Question;
import com.southcollege.exam.utils.JsonUtil;
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
     * 填空题的空数量（不暴露正确答案内容，仅告知学生有多少个空需要填写）
     * 仅对 FILL_BLANK 类型有效，其他类型为 null
     */
    private Integer blankCount;

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

        if ("FILL_BLANK".equals(question.getType()) && question.getCorrectAnswer() != null) {
            List<String> answers = JsonUtil.parseStringList(question.getCorrectAnswer());
            response.setBlankCount(answers.size());
        }

        return response;
    }
}
