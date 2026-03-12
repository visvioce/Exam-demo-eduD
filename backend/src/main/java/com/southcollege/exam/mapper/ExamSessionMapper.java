package com.southcollege.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.southcollege.exam.entity.ExamSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamSessionMapper extends BaseMapper<ExamSession> {

    @Select("SELECT * FROM exam_sessions WHERE exam_id = #{examId}")
    List<ExamSession> selectByExamId(@Param("examId") Long examId);

    @Select("SELECT * FROM exam_sessions WHERE student_id = #{studentId}")
    List<ExamSession> selectByStudentId(@Param("studentId") Long studentId);

    @Select("SELECT * FROM exam_sessions WHERE exam_id = #{examId} AND student_id = #{studentId}")
    ExamSession selectByExamIdAndStudentId(@Param("examId") Long examId, @Param("studentId") Long studentId);

    @Select("<script>" +
            "SELECT * FROM exam_sessions " +
            "WHERE student_id = #{studentId} " +
            "AND exam_id IN " +
            "<foreach collection='examIds' item='examId' open='(' separator=',' close=')'>" +
            "#{examId}" +
            "</foreach>" +
            "</script>")
    List<ExamSession> selectByExamIdsAndStudentId(@Param("examIds") List<Long> examIds, @Param("studentId") Long studentId);

    @Select("SELECT es.* FROM exam_sessions es " +
            "JOIN exams e ON es.exam_id = e.id " +
            "WHERE e.teacher_id = #{teacherId}")
    List<ExamSession> selectByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT es.* FROM exam_sessions es " +
            "JOIN exams e ON es.exam_id = e.id " +
            "WHERE e.teacher_id = #{teacherId} " +
            "AND es.status = 'SUBMITTED' " +
            "AND es.grading_status = 'PENDING'")
    List<ExamSession> selectPendingGradingByTeacherId(@Param("teacherId") Long teacherId);
}
