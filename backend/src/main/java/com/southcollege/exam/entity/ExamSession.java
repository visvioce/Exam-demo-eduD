package com.southcollege.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "exam_sessions", autoResultMap = true)
public class ExamSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 乐观锁版本号
     * 用于防止考试提交时的并发问题
     */
    @Version
    private Integer version;

    private Long examId;

    private Long studentId;

    @TableField(exist = false)
    private String studentName;

    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    private BigDecimal score;

    private BigDecimal totalScore;

    private String status;

    // 主观题评分状态: PENDING(待评分), GRADING(评分中), COMPLETED(评分完成)
    private String gradingStatus;

    // JSON 字段
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Answer> answers;

    @Data
    public static class Answer {
        private Long questionId;
        private String answer;
        private Boolean isCorrect;
        private BigDecimal score;
        // 题目类型，用于区分客观题和主观题
        private String questionType;
        // 主观题评分状态: PENDING(待评分), GRADED(已评分)
        private String gradingStatus;
        // 老师评语
        private String teacherComment;
    }
}
