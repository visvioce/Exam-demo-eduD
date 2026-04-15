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

    /**
     * 检查题目是否被任何试卷引用
     * 使用 MySQL JSON_SEARCH 在数据库层面判断，避免加载全表
     * @return 包含该题目的试卷名称列表
     */
    @Select("SELECT name FROM papers WHERE deleted = 0 AND JSON_CONTAINS(questions, CAST(#{questionId} AS JSON), '$[*].questionId')")
    List<String> selectPaperNamesByQuestionId(@Param("questionId") Long questionId);
}
