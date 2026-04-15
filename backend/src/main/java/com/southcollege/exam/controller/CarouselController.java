package com.southcollege.exam.controller;

import com.southcollege.exam.annotation.RequireRole;
import com.southcollege.exam.dto.response.Result;
import com.southcollege.exam.entity.Carousel;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.service.CarouselService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "轮播图管理", description = "首页轮播图增删改查")
@RestController
@RequestMapping("/api/carousels")
public class CarouselController {

    @Autowired
    private CarouselService carouselService;

    @GetMapping
    public Result<List<Carousel>> list() {
        return Result.success(carouselService.list());
    }

    @GetMapping("/{id}")
    public Result<Carousel> getById(@PathVariable Long id) {
        return Result.success(carouselService.getById(id));
    }

    @GetMapping("/active")
    public Result<List<Carousel>> getActive() {
        return Result.success(carouselService.getActive());
    }

    @PostMapping
    @RequireRole({RoleEnum.ADMIN})
    public Result<Boolean> save(@RequestBody Carousel carousel) {
        return Result.success(carouselService.save(carousel));
    }

    @PutMapping("/{id}")
    @RequireRole({RoleEnum.ADMIN})
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Carousel carousel) {
        carousel.setId(id);
        return Result.success(carouselService.updateById(carousel));
    }

    @DeleteMapping("/{id}")
    @RequireRole({RoleEnum.ADMIN})
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(carouselService.removeById(id));
    }
}
