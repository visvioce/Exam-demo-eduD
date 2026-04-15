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

/**
 * 试卷实体
 * <p>
 * 表示一份试卷，包含题目列表及每道题的分值设置。
 * 试卷与考试是多对一关系（一场考试使用一份试卷）。
 * </p>
 *
 * @see com.southcollege.exam.service.PaperService
 */
@Data
@TableName(value = "papers", autoResultMap = true)
public class Paper {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 试卷名称 */
    private String name;

    /** 试卷描述/说明 */
    private String description;

    /** 关联的课程ID {@link Course#id} */
    private Long courseId;

    /** 课程名称（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String courseName;

    /** 创建该试卷的教师ID {@link User#id} */
    private Long teacherId;

    /** 试卷总分（由各题分值汇总） */
    private BigDecimal totalScore;

    /**
     * 试卷类型
     * 如 MANUAL（手动组卷）/ AUTO（自动组卷）
     */
    private String type;

    /** 试卷状态（DRAFT / PUBLISHED 等） */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;

    // ========== JSON 字段 ==========

    /**
     * 试卷题目列表（JSON序列化存储）
     * 每项包含题目ID和该题在本次考试中的分值
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<PaperQuestion> questions;

    /**
     * 试卷-题目关联内部类
     * 记录某道题目在当前试卷中的分值配置
     */
    @Data
    public static class PaperQuestion {
        /** 题目ID {@link Question#id} */
        @com.fasterxml.jackson.annotation.JsonAlias("question_id")
        private Long questionId;
        /** 该题在当前试卷中的分值 */
        private BigDecimal score;
    }
}
