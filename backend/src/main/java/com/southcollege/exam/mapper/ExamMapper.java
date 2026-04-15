package com.southcollege.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.southcollege.exam.entity.Exam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

    @Select("SELECT * FROM exams WHERE course_id = #{courseId} AND deleted = 0")
    List<Exam> selectByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT * FROM exams WHERE teacher_id = #{teacherId} AND deleted = 0")
    List<Exam> selectByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT * FROM exams WHERE status = #{status} AND deleted = 0")
    List<Exam> selectByStatus(@Param("status") String status);
}
