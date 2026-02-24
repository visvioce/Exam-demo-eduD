package com.southcollege.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "questions", autoResultMap = true)
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private String type;

    private String difficulty;

    private BigDecimal score;

    private Long teacherId;

    private String subject;

    private String explanation;

    @TableLogic
    private Integer deleted;

    // JSON 字段
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Option> options;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object correctAnswer;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ScoringCriterion> scoringCriteria;

    @Data
    public static class Option {
        private String id;
        private String text;
    }

    @Data
    public static class ScoringCriterion {
        private String point;
        private BigDecimal score;
    }
}
