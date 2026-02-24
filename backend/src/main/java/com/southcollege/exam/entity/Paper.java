package com.southcollege.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "papers", autoResultMap = true)
public class Paper {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Long courseId;

    @TableField(exist = false)
    private String courseName;

    private Long teacherId;

    private BigDecimal totalScore;

    private String type;

    private String status;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;

    // JSON 字段
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<PaperQuestion> questions;

    @Data
    public static class PaperQuestion {
        @com.fasterxml.jackson.annotation.JsonAlias("question_id")
        private Long questionId;
        private BigDecimal score;
    }
}
