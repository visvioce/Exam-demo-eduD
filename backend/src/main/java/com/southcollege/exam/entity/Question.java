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

/**
 * 题目实体
 * <p>
 * 表示一道考试题目，支持5种题型：单选题、多选题、判断题、填空题、简答题。
 * </p>
 * <p>
 * <b>数据格式规范：</b>
 * <ul>
 *   <li>{@code correctAnswer} 根据题型不同，存储格式不同：
 *     <ul>
 *       <li>单选/判断：String（如 "A"、"正确"）</li>
 *       <li>多选/填空多空：List&lt;String&gt;（JSON数组）</li>
 *       <li>填空单空：String</li>
 *       <li>简答：String</li>
 *     </ul>
 *   </li>
 *   <li>详见 {@code docs/数据格式规范.md}</li>
 * </ul>
 * </p>
 *
 * @see com.southcollege.exam.service.QuestionService
 * @see com.southcollege.exam.enums.QuestionTypeEnum
 */
@Data
@TableName(value = "questions", autoResultMap = true)
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题目内容（支持HTML富文本） */
    private String content;

    /**
     * 题目类型
     * @see com.southcollege.exam.enums.QuestionTypeEnum
     */
    private String type;

    /**
     * 难度等级
     * @see com.southcollege.exam.enums.QuestionTypeEnum 中定义的难度枚举
     */
    private String difficulty;

    /** 题目分值 */
    private BigDecimal score;

    /** 创建该题目的教师ID */
    private Long teacherId;

    /** 所属学科/科目 */
    private String subject;

    /** 答案解析（支持HTML，学生端回顾时展示） */
    private String explanation;

    /** 逻辑删除标记 */
    @TableLogic
    private Integer deleted;

    // ========== JSON 字段（使用 JacksonTypeHandler 序列化）==========

    /**
     * 选项列表（选择题必填）
     * <p>每项包含 id（选项标识如 A/B/C/D）和 text（选项内容）</p>
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Option> options;

    /**
     * 正确答案
     * <p>
     * 类型为 Object 以兼容多种存储格式：
     * <ul>
     *   <li>String：单选/判断/填空单空/简答题答案</li>
     *   <li>List&lt;String&gt;：多选题选项数组 / 填空题多空答案数组</li>
     * </ul>
     * </p>
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object correctAnswer;

    /**
     * 评分标准（简答题必填）
     * <p>每项包含 point（评分要点描述）和 score（该要点分值）</p>
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ScoringCriterion> scoringCriteria;

    /**
     * 选项内部类
     */
    @Data
    public static class Option {
        /** 选项标识（如 A, B, C, D） */
        private String id;
        /** 选项文本内容 */
        private String text;
    }

    /**
     * 评分标准内部类（用于简答题）
     */
    @Data
    public static class ScoringCriterion {
        /** 评分要点描述 */
        private String point;
        /** 该要点的满分值 */
        private BigDecimal score;
    }
}
