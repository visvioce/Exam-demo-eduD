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
@TableName("courses")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String description;

    private String coverUrl;

    private Long teacherId;

    @TableField(exist = false)
    private String teacherName;

    private BigDecimal credits;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime deadline;

    @TableLogic
    private Integer deleted;
}
