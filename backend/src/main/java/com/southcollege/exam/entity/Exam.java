package com.southcollege.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试实体
 * <p>
 * 表示一场完整的考试，包含考试基本信息、关联的试卷、时间设置、状态管理等。
 * </p>
 *
 * @see com.southcollege.exam.service.ExamService
 * @see com.southcollege.exam.entity.Paper
 */
@Data
@TableName("exams")
public class Exam {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 考试标题 */
    private String title;

    /** 考试描述/说明 */
    private String description;

    /** 关联的课程ID（{@link Course#id}） */
    private Long courseId;

    /** 关联的试卷ID（{@link Paper#id}） */
    private Long paperId;

    /** 创建该考试的教师ID（{@link User#id}） */
    private Long teacherId;

    /** 教师显示名（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String teacherName;

    /** 课程名称（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String courseName;

    /** 考试开始时间 */
    private LocalDateTime startedAt;

    /** 考试结束时间 */
    private LocalDateTime endedAt;

    /** 考试时长（分钟），null表示不限制时长，以结束时间为准 */
    private Integer duration;

    /** 试卷总分（由试卷题目分值汇总） */
    private BigDecimal totalScore;

    /** 及格分数 */
    private BigDecimal passScore;

    /**
     * 考试状态
     * @see com.southcollege.exam.enums.ExamStatusEnum
     */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 逻辑删除标记（0=未删除, 1=已删除） */
    @TableLogic
    private Integer deleted;

    /**
     * 当前学生对该考试的状态（非数据库字段，仅用于前端展示）
     * 可选值：NOT_STARTED / IN_PROGRESS / SUBMITTED / GRADED
     * @see com.southcollege.exam.enums.ExamSessionStatusEnum
     */
    @TableField(exist = false)
    private String studentExamStatus;
}
