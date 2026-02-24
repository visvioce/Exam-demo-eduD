package com.southcollege.exam.controller;

import com.southcollege.exam.dto.request.ChangePasswordRequest;
import com.southcollege.exam.dto.request.LoginRequest;
import com.southcollege.exam.dto.request.RegisterRequest;
import com.southcollege.exam.dto.request.UpdateProfileRequest;
import com.southcollege.exam.dto.response.LoginResponse;
import com.southcollege.exam.dto.response.Result;
import com.southcollege.exam.dto.response.UserResponse;
import com.southcollege.exam.service.UserService;
import com.southcollege.exam.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户登录、注册、密码管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录", description = "通过用户名密码获取 JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request.getUsername(), request.getPassword()));
    }

    @Operation(summary = "用户注册", description = "注册新用户（默认为学生角色）")
    @PostMapping("/register")
    public Result<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        // 安全修复：注册强制设置为学生角色，防止权限提升攻击
        return Result.success(userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getNickname(),
                com.southcollege.exam.enums.RoleEnum.STUDENT.getCode()
        ));
    }

    @Operation(summary = "获取当前用户信息", description = "获取已登录用户的详细信息")
    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser(HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        return Result.success(userService.getCurrentUser(userId));
    }

    @Operation(summary = "修改密码", description = "修改当前用户的密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        Long userId = SecurityUtil.getCurrentUserId(httpRequest);
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }

    @Operation(summary = "更新个人资料", description = "更新当前用户的昵称和头像")
    @PutMapping("/profile")
    public Result<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request, HttpServletRequest httpRequest) {
        Long userId = SecurityUtil.getCurrentUserId(httpRequest);
        return Result.success(userService.updateProfile(userId, request.getNickname(), request.getAvatar()));
    }
}
