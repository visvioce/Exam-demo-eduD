package com.southcollege.exam.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southcollege.exam.entity.Carousel;
import com.southcollege.exam.mapper.CarouselMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarouselService extends ServiceImpl<CarouselMapper, Carousel> {

    public List<Carousel> getActive() {
        return baseMapper.selectActive();
    }
}
