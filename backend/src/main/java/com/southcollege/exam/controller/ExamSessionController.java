package com.southcollege.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.southcollege.exam.annotation.RequireRole;
import com.southcollege.exam.dto.request.GradeSubjectiveRequest;
import com.southcollege.exam.dto.request.PageRequest;
import com.southcollege.exam.dto.response.ExamResultResponse;
import com.southcollege.exam.dto.response.PageResult;
import com.southcollege.exam.dto.response.Result;
import com.southcollege.exam.entity.Exam;
import com.southcollege.exam.entity.ExamSession;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.service.ExamService;
import com.southcollege.exam.service.ExamSessionService;
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

@Tag(name = "考试记录管理", description = "考试记录查询、主观题评分")
@RestController
@RequestMapping("/api/exam-sessions")
public class ExamSessionController {

    @Autowired
    private ExamSessionService examSessionService;

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    /**
     * 获取全部考试记录（管理员和教师）
     * 数据隔离：教师只能查看自己考试的记录
     */
    @GetMapping
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<List<ExamSession>> list(HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        List<ExamSession> sessions = examSessionService.getByTeacherId(userId);
        fillStudentNames(sessions);
        return Result.success(sessions);
    }

    /**
     * 分页查询考试记录
     * 数据隔离：教师只能查看自己考试的记录
     */
    @Operation(summary = "分页查询考试记录", description = "支持考试ID、学生ID和状态筛选")
    @GetMapping("/page")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<PageResult<ExamSession>> page(
            PageRequest pageRequest,
            @Parameter(description = "考试ID") @RequestParam(required = false) Long examId,
            @Parameter(description = "学生ID") @RequestParam(required = false) Long studentId,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "评分状态") @RequestParam(required = false) String gradingStatus,
            HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);

        Page<ExamSession> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        LambdaQueryWrapper<ExamSession> wrapper = new LambdaQueryWrapper<>();

        // 数据隔离：管理员与教师都只能查看自己考试的记录
        List<Long> teacherExamIds = examService.getByTeacherId(userId).stream()
                .map(Exam::getId)
                .toList();
        if (teacherExamIds.isEmpty()) {
            return Result.success(PageResult.empty(pageRequest.getCurrent(), pageRequest.getSize()));
        }
        wrapper.in(ExamSession::getExamId, teacherExamIds);

        // 考试筛选
        if (examId != null) {
            // 验证是否有权限查看该考试
            Exam exam = examService.getById(examId);
            if (exam == null || !exam.getTeacherId().equals(userId)) {
                return Result.error("无权查看该考试的记录");
            }
            wrapper.eq(ExamSession::getExamId, examId);
        }

        // 学生筛选
        if (studentId != null) {
            wrapper.eq(ExamSession::getStudentId, studentId);
        }

        // 状态筛选
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(ExamSession::getStatus, status);
        }

        // 评分状态筛选
        if (StringUtils.isNotBlank(gradingStatus)) {
            wrapper.eq(ExamSession::getGradingStatus, gradingStatus);
        }

        wrapper.orderByDesc(ExamSession::getStartedAt);
        Page<ExamSession> result = examSessionService.page(page, wrapper);
        fillStudentNames(result.getRecords());
        return Result.success(PageResult.from(result));
    }

    @GetMapping("/{id}")
    public Result<ExamSession> getById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);

        ExamSession session = examSessionService.getById(id);
        if (session == null) {
            return Result.error("记录不存在");
        }

        // 权限校验：只有学生本人或考试教师可以查看
        Exam exam = examService.getById(session.getExamId());
        if (exam == null) {
            return Result.error("考试不存在");
        }

        boolean isTeacher = exam.getTeacherId().equals(userId);
        boolean isOwner = session.getStudentId().equals(userId);

        if (!isTeacher && !isOwner) {
            return Result.error("无权查看该考试记录");
        }

        return Result.success(session);
    }

    /**
     * 获取某考试的所有记录
     * 数据隔离：教师只能查看自己考试的记录
     */
    @GetMapping("/exam/{examId}")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<List<ExamSession>> getByExamId(@PathVariable Long examId, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);

        // 验证权限
        Exam exam = examService.getById(examId);
        if (exam == null) {
            return Result.error("考试不存在");
        }

        if (!exam.getTeacherId().equals(userId)) {
            return Result.error("无权查看该考试的记录");
        }

        List<ExamSession> sessions = examSessionService.getByExamId(examId);
        fillStudentNames(sessions);
        return Result.success(sessions);
    }

    @GetMapping("/my")
    public Result<List<ExamSession>> getMySessions(HttpServletRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(request);
        return Result.success(examSessionService.getByStudentId(studentId));
    }

    /**
     * 获取当前老师需要评分的考试记录
     */
    @GetMapping("/pending-grading")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<List<ExamSession>> getPendingGradingSessions(HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        List<ExamSession> sessions = examSessionService.getPendingGradingSessions(userId);

        fillStudentNames(sessions);
        return Result.success(sessions);
    }

    /**
     * 获取某场考试中需要评分的记录
     * 数据隔离：教师只能查看自己考试的记录
     */
    @GetMapping("/pending-grading/exam/{examId}")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<List<ExamSession>> getPendingGradingByExamId(@PathVariable Long examId, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);

        // 验证权限
        Exam exam = examService.getById(examId);
        if (exam == null) {
            return Result.error("考试不存在");
        }

        if (!exam.getTeacherId().equals(userId)) {
            return Result.error("无权查看该考试的记录");
        }

        List<ExamSession> sessions = examSessionService.getPendingGradingByExamId(examId);
        fillStudentNames(sessions);
        return Result.success(sessions);
    }

    private void fillStudentNames(List<ExamSession> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        List<Long> studentIds = sessions.stream()
                .map(ExamSession::getStudentId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> nameMap = userService.getDisplayNameMap(studentIds);
        for (ExamSession session : sessions) {
            if (session.getStudentId() == null) {
                continue;
            }
            session.setStudentName(nameMap.get(session.getStudentId()));
        }
    }

    /**
     * 老师给主观题评分
     */
    @PostMapping("/grade")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<Void> gradeSubjectiveAnswers(@RequestBody GradeSubjectiveRequest request, HttpServletRequest httpRequest) {
        Long userId = SecurityUtil.getCurrentUserId(httpRequest);
        String userRole = SecurityUtil.getCurrentUserRole(httpRequest);
        examService.gradeSubjectiveAnswers(userId, userRole, request);
        return Result.success();
    }

    /**
     * 老师触发当前考试批量自动阅卷（客观题重评）
     */
    @PostMapping("/exam/{examId}/auto-grade")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<Integer> autoGradeByExam(@PathVariable Long examId, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        int processedCount = examService.autoGradeByExam(examId, userId, userRole);
        return Result.success(processedCount);
    }

    /**
     * 获取考试结果详情
     */
    @GetMapping("/{id}/result")
    public Result<ExamResultResponse> getExamResult(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        return Result.success(examService.getExamResult(id, userId, userRole));
    }
}
