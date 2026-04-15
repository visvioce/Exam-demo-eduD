package com.southcollege.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southcollege.exam.dto.request.PageRequest;
import com.southcollege.exam.dto.response.PageResult;
import com.southcollege.exam.entity.Course;
import com.southcollege.exam.entity.CourseMember;
import com.southcollege.exam.entity.Exam;
import com.southcollege.exam.entity.User;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.exception.BusinessException;
import com.southcollege.exam.mapper.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程服务
 * 负责课程的增删改查、学生加入/退出课程、课程成员管理等
 */
@Service
public class CourseService extends ServiceImpl<CourseMapper, Course> {

    @Autowired
    private CourseMemberService courseMemberService;

    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    private ExamService examService;

    /**
     * 根据教师ID获取课程列表
     */
    public List<Course> getByTeacherId(Long teacherId) {
        List<Course> courses = lambdaQuery().eq(Course::getTeacherId, teacherId).list();
        fillTeacherNames(courses);
        return courses;
    }

    public Course getByIdWithTeacherName(Long id) {
        Course course = getById(id);
        if (course == null) {
            return null;
        }
        fillTeacherNames(List.of(course));
        return course;
    }

    public List<Course> listWithTeacherNames() {
        List<Course> courses = list();
        fillTeacherNames(courses);
        return courses;
    }

    /**
     * 获取可加入的课程
     * 状态为ACTIVE且（未设置截止时间 或 截止时间未到）
     */
    public List<Course> getActiveCourses() {
        List<Course> courses = lambdaQuery()
                .eq(Course::getStatus, "ACTIVE")
                .and(wrapper -> wrapper
                    .isNull(Course::getDeadline)  // 没有截止日期的可以选
                    .or()
                    .gt(Course::getDeadline, LocalDateTime.now())  // 或者截止日期未到的
                )
                .list();
        fillTeacherNames(courses);
        return courses;
    }

    /**
     * 获取学生加入的课程列表
     * 通过课程成员关联表查询
     */
    public List<Course> getMyCourses(Long studentId) {
        // 获取学生的所有课程成员记录
        List<CourseMember> members = courseMemberService.getByStudentId(studentId);
        // 提取课程ID列表
        List<Long> courseIds = members.stream()
                .map(CourseMember::getCourseId)
                .collect(Collectors.toList());
        if (courseIds.isEmpty()) {
            return List.of();
        }
        // 批量查询课程
        List<Course> courses = listByIds(courseIds);
        fillTeacherNames(courses);
        return courses;
    }

    /**
     * 学生加入课程
     * 检查课程状态、截止时间、是否已加入
     */
    @Transactional
    public void joinCourse(Long courseId, Long studentId) {
        // 检查课程是否存在
        Course course = getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        // 检查课程状态
        if (!"ACTIVE".equals(course.getStatus())) {
            throw new BusinessException("课程未开放");
        }
        // 检查选课截止时间
        if (course.getDeadline() != null && course.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException("选课已截止");
        }

        // 检查是否已加入
        List<CourseMember> existing = courseMemberService.lambdaQuery()
                .eq(CourseMember::getCourseId, courseId)
                .eq(CourseMember::getStudentId, studentId)
                .list();
        if (!existing.isEmpty()) {
            throw new BusinessException("已加入该课程");
        }

        // 创建课程成员记录
        CourseMember member = new CourseMember();
        member.setCourseId(courseId);
        member.setStudentId(studentId);
        member.setJoinedAt(LocalDateTime.now());
        courseMemberService.save(member);
    }

    /**
     * 学生退出课程
     */
    @Transactional
    public void leaveCourse(Long courseId, Long studentId) {
        // 查询课程成员记录
        List<CourseMember> members = courseMemberService.lambdaQuery()
                .eq(CourseMember::getCourseId, courseId)
                .eq(CourseMember::getStudentId, studentId)
                .list();
        if (members.isEmpty()) {
            throw new BusinessException("未加入该课程");
        }
        // 删除记录
        courseMemberService.removeById(members.get(0).getId());
    }

    /**
     * 获取课程的成员列表（学生列表）
     */
    public List<User> getCourseMembers(Long courseId) {
        // 获取课程的所有成员记录
        List<CourseMember> members = courseMemberService.getByCourseId(courseId);
        // 提取学生ID列表
        List<Long> studentIds = members.stream()
                .map(CourseMember::getStudentId)
                .collect(Collectors.toList());
        if (studentIds.isEmpty()) {
            return List.of();
        }
        // 批量查询学生信息
        return userService.listByIds(studentIds);
    }

    /**
     * 检查学生是否已加入课程
     */
    public boolean isCourseMember(Long courseId, Long studentId) {
        return courseMemberService.lambdaQuery()
                .eq(CourseMember::getCourseId, courseId)
                .eq(CourseMember::getStudentId, studentId)
                .count() > 0;
    }

    /**
     * 检查课程所有权
     * 管理员与教师均只能操作自己创建的课程
     */
    public void checkOwnership(Long courseId, Long userId, String userRole) {
        Course course = getById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        if (RoleEnum.ADMIN.getCode().equals(userRole)) {
            return;
        }
        if (!course.getTeacherId().equals(userId)) {
            throw new BusinessException("无权操作该课程");
        }
    }

    /**
     * 检查课程是否可以删除
     * 有学生加入或有考试的课程不能删除
     */
    public void checkCanDelete(Long courseId) {
        // 检查是否有学生加入
        List<CourseMember> members = courseMemberService.getByCourseId(courseId);
        if (!members.isEmpty()) {
            throw new BusinessException("该课程已有 " + members.size() + " 名学生加入，无法删除");
        }

        // 检查是否有关联的考试
        List<Exam> exams = examService.getByCourseId(courseId);
        if (!exams.isEmpty()) {
            throw new BusinessException("该课程已有 " + exams.size() + " 场考试，无法删除");
        }
    }

    /**
     * 分页查询课程
     */
    public PageResult<Course> page(PageRequest pageRequest, Long teacherId, String status, String keyword,
                                    Long currentUserId, String currentUserRole) {
        Page<Course> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        boolean isAdmin = RoleEnum.ADMIN.getCode().equals(currentUserRole);
        if (!isAdmin && teacherId != null && !teacherId.equals(currentUserId)) {
            return PageResult.empty(pageRequest.getCurrent(), pageRequest.getSize());
        }
        if (teacherId != null) {
            wrapper.eq(Course::getTeacherId, teacherId);
        } else if (!isAdmin) {
            wrapper.eq(Course::getTeacherId, currentUserId);
        }

        // 状态筛选
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(Course::getStatus, status);
        }

        // 关键字搜索
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Course::getName, keyword)
                    .or()
                    .like(Course::getCode, keyword));
        }

        // 排序
        applySorting(wrapper, pageRequest);

        Page<Course> result = page(page, wrapper);
        fillTeacherNames(result.getRecords());
        return PageResult.from(result);
    }

    private void fillTeacherNames(List<Course> courses) {
        if (courses == null || courses.isEmpty()) {
            return;
        }
        List<Long> teacherIds = courses.stream()
                .map(Course::getTeacherId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> teacherNameMap = userService.getDisplayNameMap(teacherIds);
        for (Course course : courses) {
            if (course.getTeacherId() == null) {
                continue;
            }
            String displayName = teacherNameMap.get(course.getTeacherId());
            if (StringUtils.isNotBlank(displayName)) {
                course.setTeacherName(displayName);
            }
        }
    }

    /**
     * 应用排序规则
     */
    private void applySorting(LambdaQueryWrapper<Course> wrapper, PageRequest pageRequest) {
        if (StringUtils.isBlank(pageRequest.getOrderBy())) {
            // 默认按 ID 降序
            wrapper.orderByDesc(Course::getId);
            return;
        }

        boolean isAsc = pageRequest.getAsc();
        String orderBy = pageRequest.getOrderBy().toLowerCase();

        switch (orderBy) {
            case "id" -> {
                if (isAsc) wrapper.orderByAsc(Course::getId);
                else wrapper.orderByDesc(Course::getId);
            }
            case "createtime", "created_at" -> {
                if (isAsc) wrapper.orderByAsc(Course::getCreatedAt);
                else wrapper.orderByDesc(Course::getCreatedAt);
            }
            case "deadline" -> {
                if (isAsc) wrapper.orderByAsc(Course::getDeadline);
                else wrapper.orderByDesc(Course::getDeadline);
            }
            case "credits" -> {
                if (isAsc) wrapper.orderByAsc(Course::getCredits);
                else wrapper.orderByDesc(Course::getCredits);
            }
            case "status" -> {
                if (isAsc) wrapper.orderByAsc(Course::getStatus);
                else wrapper.orderByDesc(Course::getStatus);
            }
            default -> wrapper.orderByDesc(Course::getId); // 无效字段使用默认排序
        }
    }
}
