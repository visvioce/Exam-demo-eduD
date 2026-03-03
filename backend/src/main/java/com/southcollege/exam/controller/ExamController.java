package com.southcollege.exam.controller;

import com.southcollege.exam.annotation.RequireRole;
import com.southcollege.exam.dto.request.ExamCreateRequest;
import com.southcollege.exam.dto.request.ExamUpdateRequest;
import com.southcollege.exam.dto.request.PageRequest;
import com.southcollege.exam.dto.response.PageResult;
import com.southcollege.exam.dto.response.Result;
import com.southcollege.exam.entity.Exam;
import com.southcollege.exam.entity.ExamSession;
import com.southcollege.exam.enums.ExamStatusEnum;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.service.CourseService;
import com.southcollege.exam.service.ExamService;
import com.southcollege.exam.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试管理控制器
 * <p>
 * 提供考试全生命周期管理的 REST API 接口，包括：
 * <ul>
 *   <li>考试的增删改查（管理员和教师）</li>
 *   <li>考试发布和取消</li>
 *   <li>学生开始考试和提交答卷</li>
 *   <li>成绩查询和评阅</li>
 * </ul>
 * </p>
 * <p>
 * <b>权限控制：</b>
 * <ul>
 *   <li>管理员：所有操作</li>
 *   <li>教师：管理自己创建的考试</li>
 *   <li>学生：参加考试、查看成绩</li>
 * </ul>
 * </p>
 *
 * @author South College Exam Team
 * @version 1.0
 * @since 2024
 */
@Tag(name = "考试管理", description = "考试增删改查、发布、开始、提交")
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final CourseService courseService;

    /**
     * 分页查询考试（管理员和教师）
     * 数据隔离：教师只能查看自己创建的考试
     */
    @GetMapping("/page")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    @Operation(summary = "分页查询考试", description = "管理员和教师可访问，教师只能查看自己创建的考试")
    public Result<PageResult<Exam>> page(
            @Valid PageRequest pageRequest,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "教师ID") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "考试状态") @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        return Result.success(examService.page(pageRequest, courseId, teacherId, status, userId, userRole));
    }

    /**
     * 获取全部考试（管理员和教师）
     * 数据隔离：教师只能查看自己的考试
     */
    @GetMapping
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<List<Exam>> list(HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        return Result.success(examService.getByTeacherId(userId));
    }

    /**
     * 获取已发布的考试列表（学生可访问）
     */
    @GetMapping("/published")
    public Result<List<Exam>> getPublishedExams() {
        return Result.success(examService.getPublishedExams());
    }

    /**
     * 获取我的考试（学生）
     */
    @GetMapping("/my")
    @RequireRole(RoleEnum.STUDENT)
    public Result<List<Exam>> getMyExams(HttpServletRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(request);
        return Result.success(examService.getMyExams(studentId));
    }

    /**
     * 获取考试详情（所有登录用户可访问）
     * 数据隔离：教师只能查看自己创建的考试，学生只能查看已发布的考试
     */
    @GetMapping("/{id}")
    public Result<Exam> getById(@PathVariable Long id, HttpServletRequest request) {
        Exam exam = examService.getByIdWithDisplayFields(id);
        if (exam == null) {
            throw new com.southcollege.exam.exception.BusinessException("考试不存在");
        }

        // 数据隔离检查
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);

        if (RoleEnum.TEACHER.getCode().equals(userRole) || RoleEnum.ADMIN.getCode().equals(userRole)) {
            // 教师（含管理员角色）只能查看自己的考试
            if (!exam.getTeacherId().equals(userId)) {
                throw new com.southcollege.exam.exception.BusinessException("无权查看该考试");
            }
            return Result.success(exam);
        } else if (RoleEnum.STUDENT.getCode().equals(userRole)) {
            // 学生只能查看已发布的考试
            if (!"PUBLISHED".equals(exam.getStatus()) && !"STARTED".equals(exam.getStatus())) {
                throw new com.southcollege.exam.exception.BusinessException("考试未发布");
            }
            // 检查学生是否已加入考试对应的课程
            if (!courseService.isCourseMember(exam.getCourseId(), userId)) {
                throw new com.southcollege.exam.exception.BusinessException("请先加入课程");
            }
            return Result.success(exam);
        } else {
            throw new com.southcollege.exam.exception.BusinessException("无权查看该考试");
        }
    }

    @PostMapping
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    @Operation(summary = "创建考试", description = "管理员和教师可访问")
    public Result<Boolean> save(
            @Valid @RequestBody ExamCreateRequest examRequest,
            HttpServletRequest request) {
        Long teacherId = SecurityUtil.getCurrentUserId(request);
        Exam exam = new Exam();
        BeanUtils.copyProperties(examRequest, exam);
        exam.setTeacherId(teacherId);
        // 新建考试统一落库为草稿，避免依赖数据库默认值造成状态为空
        exam.setStatus(ExamStatusEnum.DRAFT.getCode());
        return Result.success(examService.save(exam));
    }

    @PutMapping("/{id}")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    @Operation(summary = "更新考试", description = "管理员和教师可访问，教师只能更新自己创建的考试")
    public Result<Boolean> update(
            @Parameter(description = "考试ID") @PathVariable Long id,
            @Valid @RequestBody ExamUpdateRequest examRequest,
            HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);

        // 检查所有权
        examService.checkOwnership(id, userId, userRole);

        // 获取原考试，保留不可修改的字段
        Exam originalExam = examService.getById(id);
        if (originalExam == null) {
            throw new com.southcollege.exam.exception.BusinessException("考试不存在");
        }

        // 复制可修改的字段
        Exam exam = new Exam();
        BeanUtils.copyProperties(examRequest, exam);
        exam.setId(id);
        exam.setTeacherId(originalExam.getTeacherId());

        return Result.success(examService.updateById(exam));
    }

    @DeleteMapping("/{id}")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    @Operation(summary = "删除考试", description = "管理员和教师可访问，教师只能删除自己创建的考试")
    public Result<Boolean> delete(
            @Parameter(description = "考试ID") @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        examService.checkOwnership(id, userId, userRole);

        // 检查是否有学生已参加考试
        examService.checkCanDelete(id);

        return Result.success(examService.removeById(id));
    }

    @PostMapping("/{id}/publish")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<Void> publishExam(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        examService.checkOwnership(id, userId, userRole);
        examService.publishExam(id);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<Void> cancelExam(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        examService.checkOwnership(id, userId, userRole);
        examService.cancelExam(id);
        return Result.success();
    }

    /**
     * 开始考试（学生）
     */
    @PostMapping("/{id}/start")
    @RequireRole(RoleEnum.STUDENT)
    public Result<ExamSession> startExam(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(request);
        return Result.success(examService.startExam(id, studentId));
    }

    /**
     * 提交考试（学生）
     */
    @PostMapping("/{id}/submit")
    @RequireRole(RoleEnum.STUDENT)
    public Result<Void> submitExam(@PathVariable Long id, @RequestBody List<ExamSession.Answer> answers, HttpServletRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(request);
        examService.submitExam(id, studentId, answers);
        return Result.success();
    }

    /**
     * 自动保存答案（学生）
     * 在考试过程中定时保存答案，防止浏览器崩溃导致数据丢失
     */
    @PutMapping("/{id}/auto-save")
    @RequireRole(RoleEnum.STUDENT)
    @Operation(summary = "自动保存答案", description = "学生在考试过程中定时保存答案")
    public Result<Void> autoSaveExam(@PathVariable Long id, @RequestBody List<ExamSession.Answer> answers, HttpServletRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(request);
        examService.autoSaveExam(id, studentId, answers);
        return Result.success();
    }

    /**
     * 获取考试的题目（学生参加考试时使用）
     * 学生可访问，用于获取考试中的题目列表（不包含正确答案）
     */
    @GetMapping("/{id}/questions")
    @RequireRole(RoleEnum.STUDENT)
    public Result<List<com.southcollege.exam.dto.response.QuestionForExamResponse>> getExamQuestions(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(request);
        return Result.success(examService.getExamQuestions(id, studentId));
    }

    /**
     * 获取考试的试卷信息（查看考试回顾时使用）
     * 学生可访问自己的考试，教师可访问自己创建的考试，管理员可访问所有考试
     */
    @GetMapping("/{id}/paper")
    public Result<com.southcollege.exam.entity.Paper> getExamPaper(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        return Result.success(examService.getExamPaper(id, userId, userRole));
    }

    /**
     * 获取考试回顾所需的完整题目信息（包含正确答案和解析）
     * 学生可访问自己的考试，教师可访问自己创建的考试，管理员可访问所有考试
     */
    @GetMapping("/{id}/review-questions")
    public Result<List<com.southcollege.exam.entity.Question>> getReviewQuestions(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        return Result.success(examService.getReviewQuestions(id, userId, userRole));
    }
}
