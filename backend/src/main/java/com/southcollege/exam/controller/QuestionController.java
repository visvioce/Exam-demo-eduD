package com.southcollege.exam.controller;

import com.southcollege.exam.annotation.RequireRole;
import com.southcollege.exam.dto.request.PageRequest;
import com.southcollege.exam.dto.response.PageResult;
import com.southcollege.exam.dto.response.Result;
import com.southcollege.exam.entity.Question;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.service.QuestionService;
import com.southcollege.exam.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "题目管理", description = "题目增删改查")
@RestController
@RequestMapping("/api/questions")
@RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * 分页查询题目（推荐使用）
     */
    @GetMapping("/page")
    public Result<PageResult<Question>> page(PageRequest pageRequest,
                                             @RequestParam(required = false) Long teacherId,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(required = false) String subject,
                                             @RequestParam(required = false) String difficulty,
                                             HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        return Result.success(questionService.page(pageRequest, teacherId, type, subject, difficulty, userId, userRole));
    }

    /**
     * 获取全部题目（不建议使用，数据量大时会有性能问题）
     * 数据隔离：教师只能看自己的，管理员可以看所有
     */
    @GetMapping
    public Result<List<Question>> list(HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        if (RoleEnum.ADMIN.getCode().equals(userRole)) {
            return Result.success(questionService.list());
        }
        return Result.success(questionService.getByTeacherId(userId));
    }

    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id, HttpServletRequest request) {
        Question question = questionService.getById(id);
        if (question == null) {
            throw new com.southcollege.exam.exception.BusinessException("题目不存在");
        }

        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        if (RoleEnum.ADMIN.getCode().equals(userRole)) {
            return Result.success(question);
        }
        if (!question.getTeacherId().equals(userId)) {
            throw new com.southcollege.exam.exception.BusinessException("无权查看该题目");
        }

        return Result.success(question);
    }

    @GetMapping("/teacher/{teacherId}")
    public Result<List<Question>> getByTeacherId(@PathVariable Long teacherId, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        if (RoleEnum.ADMIN.getCode().equals(userRole)) {
            return Result.success(questionService.getByTeacherId(teacherId));
        }
        if (!teacherId.equals(userId)) {
            throw new com.southcollege.exam.exception.BusinessException("无权查询该教师的题目");
        }

        return Result.success(questionService.getByTeacherId(teacherId));
    }

    @GetMapping("/type/{type}")
    public Result<List<Question>> getByType(@PathVariable String type, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);

        List<Question> questions = questionService.getByType(type);
        if (RoleEnum.ADMIN.getCode().equals(userRole)) {
            return Result.success(questions);
        }
        return Result.success(questions.stream()
                .filter(q -> q.getTeacherId().equals(userId))
                .toList());
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody Question question, HttpServletRequest request) {
        Long teacherId = SecurityUtil.getCurrentUserId(request);
        question.setTeacherId(teacherId);
        return Result.success(questionService.save(question));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Question question, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);

        // 检查所有权并获取原题目
        questionService.checkOwnership(id, userId, userRole);

        // 获取原题目，保留不可修改的字段（teacherId）
        Question originalQuestion = questionService.getById(id);
        if (originalQuestion == null) {
            throw new com.southcollege.exam.exception.BusinessException("题目不存在");
        }

        // 强制保留原题目的所有者，防止篡改
        question.setId(id);
        question.setTeacherId(originalQuestion.getTeacherId());

        return Result.success(questionService.updateById(question));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        questionService.checkOwnership(id, userId, userRole);

        // 检查是否被试卷引用
        questionService.checkCanDelete(id);

        return Result.success(questionService.removeById(id));
    }

    /**
     * 获取所有学科列表
     */
    @GetMapping("/subjects")
    public Result<List<String>> getAllSubjects() {
        return Result.success(questionService.getAllSubjects());
    }
}
