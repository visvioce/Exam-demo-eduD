package com.southcollege.exam.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试更新请求
 */
@Data
@Schema(description = "考试更新请求")
public class ExamUpdateRequest {

    @Size(max = 100, message = "考试标题长度不能超过100个字符")
    @Schema(description = "考试标题", example = "期中考试")
    private String title;

    @Size(max = 500, message = "考试描述长度不能超过500个字符")
    @Schema(description = "考试描述")
    private String description;

    @Positive(message = "课程ID必须为正数")
    @Schema(description = "课程ID", example = "1")
    private Long courseId;

    @Positive(message = "试卷ID必须为正数")
    @Schema(description = "试卷ID", example = "1")
    private Long paperId;

    @Schema(description = "开始时间", example = "2024-01-01T09:00:00")
    private LocalDateTime startedAt;

    @Schema(description = "结束时间", example = "2024-01-01T11:00:00")
    private LocalDateTime endedAt;

    @Positive(message = "考试时长必须为正数")
    @Max(value = 480, message = "考试时长不能超过480分钟")
    @Schema(description = "考试时长（分钟）", example = "120")
    private Integer duration;

    @Positive(message = "总分必须为正数")
    @Max(value = 1000, message = "总分不能超过1000分")
    @Schema(description = "总分", example = "100")
    private BigDecimal totalScore;

    @Positive(message = "及格分必须为正数")
    @Schema(description = "及格分", example = "60")
    private BigDecimal passScore;

    @Pattern(regexp = "^(DRAFT|PUBLISHED|STARTED|ENDED|CANCELLED)?$", message = "考试状态值无效")
    @Schema(description = "考试状态", example = "DRAFT")
    private String status;
}
