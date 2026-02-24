package com.southcollege.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.southcollege.exam.annotation.RequireRole;
import com.southcollege.exam.dto.request.PageRequest;
import com.southcollege.exam.dto.response.PageResult;
import com.southcollege.exam.dto.response.Result;
import com.southcollege.exam.entity.Paper;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.service.PaperService;
import com.southcollege.exam.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "试卷管理", description = "试卷增删改查")
@RestController
@RequestMapping("/api/papers")
@RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
public class PaperController {

    @Autowired
    private PaperService paperService;

    @GetMapping
    public Result<List<Paper>> list(HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);

        if (!RoleEnum.ADMIN.getCode().equals(userRole)) {
            // 非管理员只能查看自己的试卷
            return Result.success(paperService.getByTeacherId(userId));
        }
        return Result.success(paperService.listWithCourseNames());
    }

    /**
     * 分页查询试卷
     * 数据隔离：教师只能查看自己的，管理员可以查看所有
     */
    @Operation(summary = "分页查询试卷", description = "支持关键字搜索和教师筛选")
    @GetMapping("/page")
    public Result<PageResult<Paper>> page(
            PageRequest pageRequest,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "教师ID") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "组卷方式") @RequestParam(required = false) String type,
            @Parameter(description = "试卷状态") @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);

        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();

        // 数据隔离：教师只能查看自己创建的试卷
        if (!RoleEnum.ADMIN.getCode().equals(userRole)) {
            if (teacherId != null && !teacherId.equals(userId)) {
                // 教师尝试查询其他教师的试卷，返回空结果
                return Result.success(PageResult.empty(pageRequest.getCurrent(), pageRequest.getSize()));
            }
            wrapper.eq(Paper::getTeacherId, userId);
        } else {
            // 管理员可以筛选教师
            if (teacherId != null) {
                wrapper.eq(Paper::getTeacherId, teacherId);
            }
        }

        // 关键字搜索
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(Paper::getName, keyword);
        }

        // 课程筛选
        if (courseId != null) {
            wrapper.eq(Paper::getCourseId, courseId);
        }

        // 组卷方式筛选
        if (StringUtils.isNotBlank(type)) {
            wrapper.eq(Paper::getType, type);
        }

        // 状态筛选
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(Paper::getStatus, status);
        }

        wrapper.orderByDesc(Paper::getCreatedAt);
        return Result.success(paperService.pageWithCourseNames(pageRequest, wrapper));
    }

    @GetMapping("/{id}")
    public Result<Paper> getById(@PathVariable Long id, HttpServletRequest request) {
        Paper paper = paperService.getByIdWithCourseName(id);
        if (paper == null) {
            throw new com.southcollege.exam.exception.BusinessException("试卷不存在");
        }

        // 数据隔离检查：非管理员只能查看自己的试卷
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        if (!RoleEnum.ADMIN.getCode().equals(userRole) && !paper.getTeacherId().equals(userId)) {
            throw new com.southcollege.exam.exception.BusinessException("无权查看该试卷");
        }

        return Result.success(paper);
    }

    @GetMapping("/course/{courseId}")
    public Result<List<Paper>> getByCourseId(@PathVariable Long courseId, HttpServletRequest request) {
        List<Paper> papers = paperService.getByCourseId(courseId);

        // 数据隔离：教师只能查看自己的试卷
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        if (!RoleEnum.ADMIN.getCode().equals(userRole)) {
            return Result.success(papers.stream()
                    .filter(p -> p.getTeacherId().equals(userId))
                    .toList());
        }
        return Result.success(papers);
    }

    /**
     * 获取考试用试卷信息（已废弃）
     * 学生不应直接访问试卷，应使用 /api/exams/{id}/questions 获取题目
     * @deprecated 使用 ExamController.getExamQuestions 替代
     */
    @GetMapping("/exam/{paperId}")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<Paper> getForExam(@PathVariable Long paperId, HttpServletRequest request) {
        Paper paper = paperService.getById(paperId);
        if (paper == null) {
            throw new com.southcollege.exam.exception.BusinessException("试卷不存在");
        }

        // 权限检查：只有教师和管理员可以访问
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        if (!RoleEnum.ADMIN.getCode().equals(userRole) && !paper.getTeacherId().equals(userId)) {
            throw new com.southcollege.exam.exception.BusinessException("无权查看该试卷");
        }

        return Result.success(paper);
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Paper paper, HttpServletRequest request) {
        Long teacherId = SecurityUtil.getCurrentUserId(request);
        paper.setTeacherId(teacherId);
        return Result.success(paperService.save(paper));
    }

    @PostMapping("/auto-generate")
    public Result<Paper> autoGenerate(@RequestBody com.southcollege.exam.dto.request.AutoGeneratePaperRequest request, HttpServletRequest httpRequest) {
        Long teacherId = SecurityUtil.getCurrentUserId(httpRequest);
        return Result.success(paperService.autoGenerate(request, teacherId));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Paper paper, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);

        // 检查所有权并获取原试卷
        paperService.checkOwnership(id, userId, userRole);

        // 获取原试卷，保留不可修改的字段（teacherId）
        Paper originalPaper = paperService.getById(id);
        if (originalPaper == null) {
            throw new com.southcollege.exam.exception.BusinessException("试卷不存在");
        }

        // 强制保留原试卷的所有者，防止篡改
        paper.setId(id);
        paper.setTeacherId(originalPaper.getTeacherId());

        return Result.success(paperService.updateById(paper));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        paperService.checkOwnership(id, userId, userRole);

        // 检查是否被考试引用
        paperService.checkCanDelete(id);

        return Result.success(paperService.removeById(id));
    }
}
