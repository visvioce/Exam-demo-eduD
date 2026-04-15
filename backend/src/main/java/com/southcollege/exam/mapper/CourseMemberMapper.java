package com.southcollege.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.southcollege.exam.entity.CourseMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMemberMapper extends BaseMapper<CourseMember> {

    @Select("SELECT * FROM course_members WHERE course_id = #{courseId}")
    List<CourseMember> selectByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT * FROM course_members WHERE student_id = #{studentId}")
    List<CourseMember> selectByStudentId(@Param("studentId") Long studentId);
}
