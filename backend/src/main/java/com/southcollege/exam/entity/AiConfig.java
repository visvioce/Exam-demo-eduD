package com.southcollege.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName(value = "user_ai_configs", autoResultMap = true)
public class AiConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String baseUrl;

    private String apiKey;

    /**
     * 模型列表（JSON数组）
     */
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> models = new ArrayList<>();

    /**
     * 当前激活的模型
     */
    private String activeModel;

    private LocalDateTime createdAt;

    /**
     * 判断是否有激活模型
     */
    public boolean hasActiveModel() {
        return activeModel != null && !activeModel.isEmpty() && models.contains(activeModel);
    }
}
