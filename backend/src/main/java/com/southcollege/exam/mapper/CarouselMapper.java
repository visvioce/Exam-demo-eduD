package com.southcollege.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.southcollege.exam.entity.Carousel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CarouselMapper extends BaseMapper<Carousel> {

    @Select("SELECT * FROM carousels WHERE status = 'ACTIVE' AND deleted = 0 ORDER BY sort_order")
    List<Carousel> selectActive();
}
