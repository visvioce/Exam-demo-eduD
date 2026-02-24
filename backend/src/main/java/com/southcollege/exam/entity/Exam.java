package com.southcollege.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exams")
public class Exam {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Long courseId;

    private Long paperId;

    private Long teacherId;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String courseName;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Integer duration;

    private BigDecimal totalScore;

    private BigDecimal passScore;

    private String status;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;

    // 学生考试状态（临时字段，不存储到数据库）
    @TableField(exist = false)
    private String studentExamStatus;
}
