package com.southcollege.exam.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateQuestionRequest {

    @NotBlank(message = "学科不能为空")
    private String subject;

    private String type;

    private String difficulty;

    @Min(value = 1, message = "生成数量至少为1")
    @Max(value = 20, message = "单次最多生成20道题目")
    private Integer count = 5;

    private String requirements;
}
