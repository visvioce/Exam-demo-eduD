package com.southcollege.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southcollege.exam.annotation.RequireRole;
import com.southcollege.exam.dto.request.PageRequest;
import com.southcollege.exam.dto.response.PageResult;
import com.southcollege.exam.dto.response.Result;
import com.southcollege.exam.entity.Announcement;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.service.AnnouncementService;
import com.southcollege.exam.service.UserService;
import com.southcollege.exam.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "公告管理", description = "系统公告增删改查")
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserService userService;

    /**
     * 获取公告列表（所有登录用户可访问）
     */
    @GetMapping
    public Result<List<Announcement>> list() {
        List<Announcement> announcements = announcementService.list();
        fillPublisherNames(announcements);
        return Result.success(announcements);
    }

    /**
     * 分页查询公告（所有登录用户可访问）
     */
    @Operation(summary = "分页查询公告", description = "支持关键字搜索和状态筛选")
    @GetMapping("/page")
    public Result<PageResult<Announcement>> page(
            PageRequest pageRequest,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "类型筛选") @RequestParam(required = false) String type) {
        Page<Announcement> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Announcement::getTitle, keyword);
        }

        // 状态筛选
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(Announcement::getStatus, status);
        }

        // 类型筛选
        if (StringUtils.isNotBlank(type)) {
            wrapper.eq(Announcement::getType, type);
        }

        wrapper.orderByDesc(Announcement::getPublishedAt);
        Page<Announcement> result = announcementService.page(page, wrapper);
        fillPublisherNames(result.getRecords());
        return Result.success(PageResult.from(result));
    }

    /**
     * 获取公告详情（所有登录用户可访问）
     */
    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement != null) {
            fillPublisherNames(List.of(announcement));
        }
        return Result.success(announcement);
    }

    private void fillPublisherNames(List<Announcement> announcements) {
        if (announcements == null || announcements.isEmpty()) {
            return;
        }
        List<Long> publisherIds = announcements.stream()
                .map(Announcement::getPublisherId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> nameMap = userService.getDisplayNameMap(publisherIds);
        for (Announcement announcement : announcements) {
            if (announcement.getPublisherId() == null) {
                continue;
            }
            String displayName = nameMap.get(announcement.getPublisherId());
            if (StringUtils.isNotBlank(displayName)) {
                announcement.setPublisherName(displayName);
            }
        }
    }

    /**
     * 创建公告（管理员和教师）
     */
    @PostMapping
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<Boolean> save(@RequestBody Announcement announcement, HttpServletRequest request) {
        Long publisherId = SecurityUtil.getCurrentUserId(request);
        announcement.setPublisherId(publisherId);
        return Result.success(announcementService.save(announcement));
    }

    /**
     * 更新公告（管理员和教师）
     */
    @PutMapping("/{id}")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Announcement announcement, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        announcementService.checkOwnership(id, userId, userRole);
        announcement.setId(id);
        return Result.success(announcementService.updateById(announcement));
    }

    /**
     * 删除公告（管理员和教师）
     */
    @DeleteMapping("/{id}")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        announcementService.checkOwnership(id, userId, userRole);
        return Result.success(announcementService.removeById(id));
    }
}
