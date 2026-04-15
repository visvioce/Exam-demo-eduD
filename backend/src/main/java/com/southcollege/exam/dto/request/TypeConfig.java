package com.southcollege.exam.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TypeConfig {
    private Integer count;
    private BigDecimal score;
    private String difficulty;
    private String subject;
}
