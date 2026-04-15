package com.southcollege.exam.constants;

public final class ExamConstants {
    
    private ExamConstants() {}
    
    /**
     * 考试时间宽限时间（秒）
     */
    public static final int GRACE_PERIOD_SECONDS = 30;
    
    /**
     * 自动保存间隔（毫秒）
     */
    public static final int AUTO_SAVE_INTERVAL_MS = 30000;
    
    /**
     * 默认考试时长（分钟）
     */
    public static final int DEFAULT_DURATION_MINUTES = 60;
    
    /**
     * 默认总分
     */
    public static final int DEFAULT_TOTAL_SCORE = 100;
    
    /**
     * 默认及格分
     */
    public static final int DEFAULT_PASS_SCORE = 60;
    
    /**
     * 题型代码
     */
    public static final String QUESTION_TYPE_SINGLE_CHOICE = "SINGLE_CHOICE";
    public static final String QUESTION_TYPE_MULTIPLE_CHOICE = "MULTIPLE_CHOICE";
    public static final String QUESTION_TYPE_TRUE_FALSE = "TRUE_FALSE";
    public static final String QUESTION_TYPE_FILL_BLANK = "FILL_BLANK";
    public static final String QUESTION_TYPE_ESSAY = "ESSAY";
    
    /**
     * 判断题答案
     */
    public static final String ANSWER_TRUE = "正确";
    public static final String ANSWER_FALSE = "错误";
}
