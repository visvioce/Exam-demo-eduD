package com.southcollege.exam.utils;

import com.southcollege.exam.entity.Exam;
import com.southcollege.exam.enums.ExamStatusEnum;
import com.southcollege.exam.constants.ExamConstants;
import java.time.LocalDateTime;

public final class ExamStatusHelper {
    
    private ExamStatusHelper() {}
    
    /**
     * 检查考试是否可以开始（已发布或已开始状态）
     */
    public static boolean canStart(Exam exam) {
        return ExamStatusEnum.PUBLISHED.getCode().equals(exam.getStatus())
                || ExamStatusEnum.STARTED.getCode().equals(exam.getStatus());
    }
    
    /**
     * 检查考试是否在时间范围内（含30秒宽限时间）
     */
    public static boolean isWithinTimeRange(Exam exam, LocalDateTime now) {
        if (exam.getStartedAt() == null || exam.getEndedAt() == null) {
            return false;
        }
        return !now.isBefore(exam.getStartedAt()) 
                && !now.isAfter(exam.getEndedAt().plusSeconds(ExamConstants.GRACE_PERIOD_SECONDS));
    }
    
    /**
     * 检查考试是否已结束（含30秒宽限时间）
     */
    public static boolean isEnded(Exam exam, LocalDateTime now) {
        if (exam.getEndedAt() == null) {
            return false;
        }
        return now.isAfter(exam.getEndedAt().plusSeconds(ExamConstants.GRACE_PERIOD_SECONDS));
    }
    
    /**
     * 检查考试是否未开始
     */
    public static boolean isNotStarted(Exam exam, LocalDateTime now) {
        if (exam.getStartedAt() == null) {
            return true;
        }
        return now.isBefore(exam.getStartedAt());
    }
    
    /**
     * 检查考试时间是否已设置
     */
    public static boolean hasValidTimeSettings(Exam exam) {
        return exam.getStartedAt() != null && exam.getEndedAt() != null;
    }
    
    /**
     * 检查考试是否可以被取消
     */
    public static boolean canCancel(Exam exam) {
        return ExamStatusEnum.DRAFT.getCode().equals(exam.getStatus())
                || ExamStatusEnum.PUBLISHED.getCode().equals(exam.getStatus());
    }
}
