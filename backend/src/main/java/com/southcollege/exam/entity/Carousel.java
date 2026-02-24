package com.southcollege.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("carousels")
public class Carousel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String imageUrl;

    private String linkUrl;

    private String description;

    private Integer sortOrder;

    private String status;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @TableLogic
    private Integer deleted;
}
