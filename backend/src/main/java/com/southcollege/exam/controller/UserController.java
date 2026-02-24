package com.southcollege.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southcollege.exam.annotation.RequireRole;
import com.southcollege.exam.dto.request.PageRequest;
import com.southcollege.exam.dto.response.PageResult;
import com.southcollege.exam.dto.response.Result;
import com.southcollege.exam.entity.User;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口
 * 仅管理员可访问
 */
@Tag(name = "用户管理", description = "用户增删改查（仅管理员）")
@RestController
@RequestMapping("/api/users")
@RequireRole(RoleEnum.ADMIN)
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取所有用户", description = "获取系统中所有用户列表")
    @GetMapping
    public Result<List<User>> list() {
        return Result.success(userService.list());
    }

    /**
     * 分页查询用户
     */
    @Operation(summary = "分页查询用户", description = "支持关键字搜索和角色筛选")
    @GetMapping("/page")
    public Result<PageResult<User>> page(
            PageRequest pageRequest,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色筛选") @RequestParam(required = false) String role) {
        Page<User> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索（用户名或昵称）
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getNickname, keyword);
        }

        // 角色筛选
        if (StringUtils.isNotBlank(role)) {
            wrapper.eq(User::getRole, role);
        }

        // 排序
        if (StringUtils.isNotBlank(pageRequest.getOrderBy())) {
            if (pageRequest.getAsc()) {
                wrapper.orderByAsc(User::getId);
            } else {
                wrapper.orderByDesc(User::getId);
            }
        } else {
            wrapper.orderByDesc(User::getId);
        }

        Page<User> result = userService.page(page, wrapper);
        return Result.success(PageResult.from(result));
    }

    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    @GetMapping("/{id}")
    public Result<User> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Operation(summary = "创建用户", description = "新增系统用户，密码将自动加密存储")
    @PostMapping
    public Result<Boolean> save(@Valid @RequestBody User user) {
        return Result.success(userService.createUser(user));
    }

    @Operation(summary = "更新用户", description = "更新用户信息，不允许修改密码")
    @PutMapping("/{id}")
    public Result<Boolean> update(@Parameter(description = "用户ID") @PathVariable Long id, @Valid @RequestBody User user) {
        user.setId(id);
        return Result.success(userService.updateUser(user));
    }

    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    /**
     * 批量删除用户
     */
    @Operation(summary = "批量删除用户", description = "根据ID列表批量删除用户")
    @DeleteMapping("/batch")
    public Result<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        return Result.success(userService.removeByIds(ids));
    }
}
