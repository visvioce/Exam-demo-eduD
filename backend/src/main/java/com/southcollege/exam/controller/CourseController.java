package com.southcollege.exam.controller;

import com.southcollege.exam.annotation.RequireRole;
import com.southcollege.exam.dto.request.PageRequest;
import com.southcollege.exam.dto.response.PageResult;
import com.southcollege.exam.dto.response.Result;
import com.southcollege.exam.entity.Course;
import com.southcollege.exam.entity.User;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.service.CourseService;
import com.southcollege.exam.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "课程管理", description = "课程增删改查、加入/退出课程")
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 分页查询课程（推荐使用）
     * 数据隔离：教师只能查看自己创建的课程
     */
    @GetMapping("/page")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    @Operation(summary = "分页查询课程", description = "管理员和教师可访问，教师只能查看自己创建的课程")
    public Result<PageResult<Course>> page(
            @Valid PageRequest pageRequest,
            @Parameter(description = "教师ID") @RequestParam(required = false) Long teacherId,
            @Parameter(description = "课程状态") @RequestParam(required = false) String status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        return Result.success(courseService.page(pageRequest, teacherId, status, keyword, userId, userRole));
    }

    /**
     * 获取全部课程（不建议使用，数据量大时会有性能问题）
     * 数据隔离：教师只能查看自己的课程
     */
    @GetMapping
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    @Operation(summary = "获取全部课程", description = "不建议使用，数据量大时会有性能问题")
    public Result<List<Course>> list(HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);

        if (!RoleEnum.ADMIN.getCode().equals(userRole)) {
            // 非管理员只能查看自己的课程
            return Result.success(courseService.getByTeacherId(userId));
        }
        return Result.success(courseService.listWithTeacherNames());
    }

    /**
     * 获取活跃课程列表（所有用户可访问）
     */
    @GetMapping("/active")
    public Result<List<Course>> getActiveCourses() {
        return Result.success(courseService.getActiveCourses());
    }

    /**
     * 获取我的课程（学生查看已加入的课程）
     */
    @GetMapping("/my")
    public Result<List<Course>> getMyCourses(HttpServletRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(request);
        return Result.success(courseService.getMyCourses(studentId));
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id, HttpServletRequest request) {
        Course course = courseService.getByIdWithTeacherName(id);
        if (course == null) {
            throw new com.southcollege.exam.exception.BusinessException("课程不存在");
        }

        // 数据隔离检查：
        // ADMIN 可查看所有；TEACHER 仅可查看自己课程；STUDENT 仅可查看已加入课程
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        if (RoleEnum.ADMIN.getCode().equals(userRole)) {
            return Result.success(course);
        }

        if (RoleEnum.TEACHER.getCode().equals(userRole)) {
            if (!course.getTeacherId().equals(userId)) {
                throw new com.southcollege.exam.exception.BusinessException("无权查看该课程");
            }
            return Result.success(course);
        }

        if (RoleEnum.STUDENT.getCode().equals(userRole)) {
            if (!courseService.isCourseMember(id, userId)) {
                throw new com.southcollege.exam.exception.BusinessException("请先加入该课程");
            }
            return Result.success(course);
        }

        throw new com.southcollege.exam.exception.BusinessException("无权查看该课程");

    }

    @PostMapping
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<Boolean> save(@RequestBody Course course, HttpServletRequest request) {
        Long teacherId = SecurityUtil.getCurrentUserId(request);
        course.setTeacherId(teacherId);
        return Result.success(courseService.save(course));
    }

    @PutMapping("/{id}")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<Boolean> update(@PathVariable Long id, @RequestBody Course course, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);

        // 检查所有权并获取原课程
        courseService.checkOwnership(id, userId, userRole);

        // 获取原课程，保留不可修改的字段（teacherId）
        Course originalCourse = courseService.getById(id);
        if (originalCourse == null) {
            throw new com.southcollege.exam.exception.BusinessException("课程不存在");
        }

        // 强制保留原课程的所有者，防止篡改
        course.setId(id);
        course.setTeacherId(originalCourse.getTeacherId());

        return Result.success(courseService.updateById(course));
    }

    @DeleteMapping("/{id}")
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String userRole = SecurityUtil.getCurrentUserRole(request);
        courseService.checkOwnership(id, userId, userRole);

        // 检查是否可以删除
        courseService.checkCanDelete(id);

        return Result.success(courseService.removeById(id));
    }

    /**
     * 加入课程（学生）
     */
    @PostMapping("/{id}/join")
    @RequireRole(RoleEnum.STUDENT)
    public Result<Void> joinCourse(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(request);
        courseService.joinCourse(id, studentId);
        return Result.success();
    }

    /**
     * 退出课程（学生）
     */
    @PostMapping("/{id}/leave")
    @RequireRole(RoleEnum.STUDENT)
    public Result<Void> leaveCourse(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(request);
        courseService.leaveCourse(id, studentId);
        return Result.success();
    }

    /**
     * 检查当前用户是否已加入某课程（学生）
     * 优化接口：避免获取所有课程再判断
     */
    @GetMapping("/{id}/joined")
    @RequireRole(RoleEnum.STUDENT)
    public Result<Boolean> checkJoined(@PathVariable Long id, HttpServletRequest request) {
        Long studentId = SecurityUtil.getCurrentUserId(request);
        boolean joined = courseService.isCourseMember(id, studentId);
        return Result.success(joined);
    }

    @GetMapping("/{id}/members")
    public Result<List<User>> getCourseMembers(@PathVariable Long id, HttpServletRequest request) {
        Long userId = SecurityUtil.getCurrentUserId(request);
        String role = SecurityUtil.getCurrentUserRole(request);
        // 只有课程教师和成员可以查看成员列表
        Course course = courseService.getById(id);
        if (course == null) {
            return Result.error("课程不存在");
        }
        if (RoleEnum.ADMIN.getCode().equals(role) || course.getTeacherId().equals(userId)) {
            return Result.success(courseService.getCourseMembers(id));
        }
        // 检查是否是课程成员
        List<Course> myCourses = courseService.getMyCourses(userId);
        boolean isMember = myCourses.stream().anyMatch(c -> c.getId().equals(id));
        if (!isMember) {
            return Result.error("无权查看此课程成员");
        }
        return Result.success(courseService.getCourseMembers(id));
    }
}
