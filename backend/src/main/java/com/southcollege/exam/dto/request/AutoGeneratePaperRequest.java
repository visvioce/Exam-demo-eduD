package com.southcollege.exam.dto.request;

import lombok.Data;

@Data
public class AutoGeneratePaperRequest {

    private String name;

    private String description;

    private Long courseId;

    private String subject;

    private String difficulty;

    private TypeConfig singleChoice;

    private TypeConfig multipleChoice;

    private TypeConfig trueFalse;

    private TypeConfig fillBlank;

    private TypeConfig essay;

    private Integer singleChoiceCount;

    private java.math.BigDecimal singleChoiceScore;

    private Integer multipleChoiceCount;

    private java.math.BigDecimal multipleChoiceScore;

    private Integer trueFalseCount;

    private java.math.BigDecimal trueFalseScore;

    private Integer fillBlankCount;

    private java.math.BigDecimal fillBlankScore;

    private Integer essayCount;

    private java.math.BigDecimal essayScore;
}
