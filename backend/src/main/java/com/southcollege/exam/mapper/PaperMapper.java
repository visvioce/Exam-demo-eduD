package com.southcollege.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.southcollege.exam.entity.Paper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PaperMapper extends BaseMapper<Paper> {

    @Select("SELECT * FROM papers WHERE course_id = #{courseId} AND deleted = 0")
    List<Paper> selectByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT * FROM papers WHERE teacher_id = #{teacherId} AND deleted = 0")
    List<Paper> selectByTeacherId(@Param("teacherId") Long teacherId);
}
