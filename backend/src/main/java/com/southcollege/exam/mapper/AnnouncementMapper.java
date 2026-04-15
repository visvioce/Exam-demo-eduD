package com.southcollege.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.southcollege.exam.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    @Select("SELECT * FROM announcements WHERE status = #{status} AND deleted = 0 ORDER BY published_at DESC")
    List<Announcement> selectByStatus(@Param("status") String status);

    @Select("SELECT * FROM announcements WHERE type = #{type} AND status = 'PUBLISHED' AND deleted = 0 ORDER BY published_at DESC")
    List<Announcement> selectPublishedByType(@Param("type") String type);
}
