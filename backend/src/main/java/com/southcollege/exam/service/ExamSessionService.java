package com.southcollege.exam.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southcollege.exam.entity.ExamSession;
import com.southcollege.exam.mapper.ExamSessionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamSessionService extends ServiceImpl<ExamSessionMapper, ExamSession> {

    public List<ExamSession> getByExamId(Long examId) {
        return baseMapper.selectByExamId(examId);
    }

    public List<ExamSession> getByStudentId(Long studentId) {
        return baseMapper.selectByStudentId(studentId);
    }

    public ExamSession getByExamIdAndStudentId(Long examId, Long studentId) {
        return baseMapper.selectByExamIdAndStudentId(examId, studentId);
    }

    /**
     * 获取某教师所有考试的记录
     */
    public List<ExamSession> getByTeacherId(Long teacherId) {
        return baseMapper.selectByTeacherId(teacherId);
    }

    /**
     * 获取需要评分的考试记录（包含主观题且待评分）
     */
    public List<ExamSession> getPendingGradingSessions(Long teacherId) {
        return baseMapper.selectPendingGradingByTeacherId(teacherId);
    }

    /**
     * 获取某场考试中需要评分的记录
     */
    public List<ExamSession> getPendingGradingByExamId(Long examId) {
        return lambdaQuery()
                .eq(ExamSession::getExamId, examId)
                .eq(ExamSession::getStatus, "SUBMITTED")
                .eq(ExamSession::getGradingStatus, "PENDING")
                .list();
    }
}
