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

/**
 * 考试会话实体
 * <p>
 * 表示一个学生参加一场考试的完整记录，包含答题情况、得分、状态等。
 * 使用乐观锁（{@code version}）防止并发提交冲突。
 * </p>
 *
 * @see com.southcollege.exam.service.ExamService#startExam
 * @see com.southcollege.exam.service.ExamService#submitExam
 */
@Data
@TableName(value = "exam_sessions", autoResultMap = true)
public class ExamSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 乐观锁版本号
     * 用于防止考试提交时的并发问题（如同时自动保存和正式提交）
     */
    @Version
    private Integer version;

    /** 关联的考试ID {@link Exam#id} */
    private Long examId;

    /** 参加考试的学生ID {@link User#id} */
    private Long studentId;

    /** 学生显示名（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String studentName;

    /** 考试开始时间 */
    private LocalDateTime startedAt;

    /** 提交时间（null表示未提交） */
    private LocalDateTime submittedAt;

    /** 当前得分（客观题自动评分+主观题手动评分后的总分） */
    private BigDecimal score;

    /** 试卷满分 */
    private BigDecimal totalScore;

    /**
     * 会话状态
     * @see com.southcollege.exam.enums.ExamSessionStatusEnum
     */
    private String status;

    /**
     * 主观题评分状态
     * 可选值：PENDING(待评分) / GRADING(评分中) / COMPLETED(评分完成)
     * @see com.southcollege.exam.enums.GradingStatusEnum
     */
    private String gradingStatus;

    // ========== JSON 字段 ==========

    /**
     * 学生答案列表（JSON序列化存储）
     * 每项包含题目ID、学生答案、是否正确、得分、题型等
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Answer> answers;

    /**
     * 答案内部类
     * 记录学生对某道题的作答情况和评分结果
     */
    @Data
    public static class Answer {
        /** 题目ID {@link Question#id} */
        private Long questionId;
        /** 学生填写的答案（格式因题型而异，参见数据格式规范） */
        private String answer;
        /** 是否正确（仅客观题有值，主观题为null） */
        private Boolean isCorrect;
        /** 该题得分 */
        private BigDecimal score;
        /** 题目类型（用于区分客观题/主观题的判分逻辑） */
        private String questionType;
        /**
         * 主观题评分状态
         * PENDING: 待评分 / GRADED: 已评分
         */
        private String gradingStatus;
        /** 教师评语（仅主观题） */
        private String teacherComment;
    }
}
