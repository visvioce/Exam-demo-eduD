package com.southcollege.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("announcements")
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String type;

    private String priority;

    private String status;

    private Long publisherId;

    @TableField(exist = false)
    private String publisherName;

    private LocalDateTime publishedAt;

    @TableLogic
    private Integer deleted;
}
