package com.southcollege.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.southcollege.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("SELECT * FROM questions WHERE teacher_id = #{teacherId} AND deleted = 0")
    List<Question> selectByTeacherId(@Param("teacherId") Long teacherId);

    @Select("SELECT * FROM questions WHERE type = #{type} AND deleted = 0")
    List<Question> selectByType(@Param("type") String type);

    @Select("SELECT DISTINCT subject FROM questions WHERE subject IS NOT NULL AND subject != '' AND deleted = 0 ORDER BY subject")
    List<String> selectAllSubjects();
}
