package com.southcollege.exam.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GeneratedQuestionResponse {
    private List<QuestionItem> questions;

    @Data
    public static class QuestionItem {
        private String content;
        private String type;
        private String difficulty;
        private BigDecimal score;
        private List<Option> options;
        private String correctAnswer;
        private String explanation;
        private List<ScoringCriterion> scoringCriteria;
    }

    @Data
    public static class Option {
        private String id;
        private String text;
    }

    @Data
    public static class ScoringCriterion {
        private String point;
        private BigDecimal score;
    }
}
