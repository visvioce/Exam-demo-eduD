package com.southcollege.exam.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southcollege.exam.dto.request.GradeSubjectiveRequest;
import com.southcollege.exam.dto.request.PageRequest;
import com.southcollege.exam.dto.response.ExamResultResponse;
import com.southcollege.exam.dto.response.PageResult;
import com.southcollege.exam.dto.response.QuestionForExamResponse;
import com.southcollege.exam.entity.*;
import com.southcollege.exam.enums.ExamSessionStatusEnum;
import com.southcollege.exam.enums.ExamStatusEnum;
import com.southcollege.exam.enums.GradingStatusEnum;
import com.southcollege.exam.enums.QuestionTypeEnum;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.exception.BusinessException;
import com.southcollege.exam.mapper.ExamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考试服务类
 * <p>
 * 负责考试的全生命周期管理，包括：
 * <ul>
 *   <li>考试的创建、发布、取消</li>
 *   <li>考试会话的管理（开始、提交）</li>
 *   <li>自动评分（客观题）和手动评分（主观题）</li>
 *   <li>成绩统计和查询</li>
 *   <li>考试状态自动刷新（草稿→已发布→进行中→已结束）</li>
 * </ul>
 * </p>
 * <p>
 * <b>核心业务流程：</b>
 * <ol>
 *   <li>教师创建考试（草稿状态）</li>
 *   <li>教师发布考试（已发布状态）</li>
 *   <li>学生在考试时间范围内开始考试（创建考试会话）</li>
 *   <li>学生答题并提交（自动评分客观题）</li>
 *   <li>教师评阅主观题（如有）</li>
 *   <li>公布成绩</li>
 * </ol>
 * </p>
 *
 * @author South College Exam Team
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor
public class ExamService extends ServiceImpl<ExamMapper, Exam> {

    // 客观题类型
    private static final List<String> OBJECTIVE_TYPES = List.of(
            QuestionTypeEnum.SINGLE_CHOICE.getCode(),
            QuestionTypeEnum.MULTIPLE_CHOICE.getCode(),
            QuestionTypeEnum.TRUE_FALSE.getCode(),
            QuestionTypeEnum.FILL_BLANK.getCode()
    );
    // 主观题类型
    private static final List<String> SUBJECTIVE_TYPES = List.of(
            QuestionTypeEnum.ESSAY.getCode()
    );

    private final PaperService paperService;
    private final QuestionService questionService;
    private final ExamSessionService examSessionService;
    private final CourseService courseService;
    private final UserService userService;

    /**
     * 根据课程ID获取考试列表
     */
    public List<Exam> getByCourseId(Long courseId) {
        return baseMapper.selectByCourseId(courseId);
    }

    /**
     * 根据教师ID获取考试列表
     */
    public List<Exam> getByTeacherId(Long teacherId) {
        refreshExamStatuses();
        List<Exam> exams = baseMapper.selectByTeacherId(teacherId);
        fillExamDisplayFields(exams);
        return exams;
    }

    public List<Exam> listWithDisplayFields() {
        refreshExamStatuses();
        List<Exam> exams = list();
        fillExamDisplayFields(exams);
        return exams;
    }

    public Exam getByIdWithDisplayFields(Long id) {
        refreshExamStatuses();
        Exam exam = getById(id);
        if (exam == null) {
            return null;
        }
        fillExamDisplayFields(List.of(exam));
        return exam;
    }

    /**
     * 根据试卷ID获取考试列表
     */
    public List<Exam> getByPaperId(Long paperId) {
        return lambdaQuery()
                .eq(Exam::getPaperId, paperId)
                .list();
    }

    /**
     * 根据状态获取考试列表
     */
    public List<Exam> getByStatus(String status) {
        return baseMapper.selectByStatus(status);
    }

    /**
     * 获取已发布的、未结束的考试
     */
    public List<Exam> getPublishedExams() {
        refreshExamStatuses();
        List<Exam> exams = lambdaQuery()
                .in(Exam::getStatus, ExamStatusEnum.PUBLISHED.getCode(), ExamStatusEnum.STARTED.getCode())
                .ge(Exam::getEndedAt, LocalDateTime.now())
                .list();
        fillExamDisplayFields(exams);
        return exams;
    }

    /**
     * 获取考试的题目列表（学生参加考试时使用）
     * 检查学生是否已加入课程，返回考试中包含的题目信息（不含正确答案）
     */
    public List<QuestionForExamResponse> getExamQuestions(Long examId, Long studentId) {
        refreshExamStatuses();
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 检查学生是否已加入课程
        if (!courseService.isCourseMember(exam.getCourseId(), studentId)) {
            throw new BusinessException("请先加入对应课程");
        }

        // 检查考试状态
        if (!ExamStatusEnum.PUBLISHED.getCode().equals(exam.getStatus()) &&
            !ExamStatusEnum.STARTED.getCode().equals(exam.getStatus())) {
            throw new BusinessException("考试未发布");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartedAt())) {
            throw new BusinessException("考试未开始");
        }
        if (now.isAfter(exam.getEndedAt().plusSeconds(30))) {
            throw new BusinessException("考试已结束");
        }

        // 题目仅对已有进行中会话的学生开放，避免提前获取试题内容
        ExamSession session = examSessionService.getByExamIdAndStudentId(examId, studentId);
        if (session == null) {
            throw new BusinessException("未开始该考试");
        }
        if (!ExamSessionStatusEnum.IN_PROGRESS.getCode().equals(session.getStatus())) {
            throw new BusinessException("当前考试状态不允许继续作答");
        }
        if (exam.getDuration() != null) {
            LocalDateTime durationDeadline = sessionDeadline(session, exam.getDuration());
            if (now.isAfter(durationDeadline)) {
                throw new BusinessException("考试已超时");
            }
        }

        // 获取试卷
        Paper paper = paperService.getById(exam.getPaperId());
        if (paper == null || paper.getQuestions() == null) {
            return List.of();
        }

        // 获取题目ID列表
        List<Long> questionIds = paper.getQuestions().stream()
                .map(Paper.PaperQuestion::getQuestionId)
                .toList();

        // 批量查询题目，并转换为不包含答案的响应对象
        List<Question> questions = questionService.listByIds(questionIds);
        return questions.stream()
                .map(QuestionForExamResponse::from)
                .toList();
    }

    /**
     * 获取考试的试卷信息（查看考试回顾时使用）
     * 包含题目和正确答案
     * 学生：只有在考试结束后或已提交考试后才能查看
     * 教师/管理员：只能查看自己创建的考试
     */
    public Paper getExamPaper(Long examId, Long userId, String userRole) {
        refreshExamStatuses();
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 教师（含管理员角色）只能查看自己创建的考试
        if (RoleEnum.TEACHER.getCode().equals(userRole) || RoleEnum.ADMIN.getCode().equals(userRole)) {
            if (!exam.getTeacherId().equals(userId)) {
                throw new BusinessException("无权查看该考试");
            }
            Paper paper = paperService.getById(exam.getPaperId());
            if (paper == null) {
                throw new BusinessException("试卷不存在");
            }
            return paper;
        }

        // 学生需要检查是否已参加且已提交/已结束
        if (RoleEnum.STUDENT.getCode().equals(userRole)) {
            // 检查学生是否已加入课程
            if (!courseService.isCourseMember(exam.getCourseId(), userId)) {
                throw new BusinessException("请先加入对应课程");
            }

            // 检查学生是否已经参加过考试
            ExamSession session = examSessionService.getByExamIdAndStudentId(examId, userId);
            if (session == null) {
                throw new BusinessException("您尚未参加该考试");
            }

            // 检查是否可以查看答案：考试已结束 或 学生已提交
            boolean canViewAnswers = ExamStatusEnum.ENDED.getCode().equals(exam.getStatus())
                    || ExamSessionStatusEnum.SUBMITTED.getCode().equals(session.getStatus())
                    || ExamSessionStatusEnum.GRADED.getCode().equals(session.getStatus());

            if (!canViewAnswers) {
                throw new BusinessException("考试尚未结束，暂时无法查看答案");
            }

            Paper paper = paperService.getById(exam.getPaperId());
            if (paper == null) {
                throw new BusinessException("试卷不存在");
            }
            return paper;
        }

        throw new BusinessException("无权查看该考试");
    }

    /**
     * 获取考试回顾所需的完整题目信息（包含正确答案和解析）
     * 学生：只能查看自己已经参加且已提交/已结束的考试的题目
     * 教师/管理员：只能查看自己创建的考试的题目
     */
    public List<Question> getReviewQuestions(Long examId, Long userId, String userRole) {
        refreshExamStatuses();
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 教师（含管理员角色）只能查看自己创建的考试的题目
        if (RoleEnum.TEACHER.getCode().equals(userRole) || RoleEnum.ADMIN.getCode().equals(userRole)) {
            if (!exam.getTeacherId().equals(userId)) {
                throw new BusinessException("无权查看该考试");
            }
            Paper paper = paperService.getById(exam.getPaperId());
            if (paper == null || paper.getQuestions() == null) {
                return List.of();
            }
            List<Long> questionIds = paper.getQuestions().stream()
                    .map(Paper.PaperQuestion::getQuestionId)
                    .toList();
            return questionService.listByIds(questionIds);
        }

        // 学生只能查看自己已经参加且已提交/已结束的考试的题目
        if (RoleEnum.STUDENT.getCode().equals(userRole)) {
            // 检查学生是否已加入课程
            if (!courseService.isCourseMember(exam.getCourseId(), userId)) {
                throw new BusinessException("请先加入对应课程");
            }

            // 检查学生是否已经参加过考试
            ExamSession session = examSessionService.getByExamIdAndStudentId(examId, userId);
            if (session == null) {
                throw new BusinessException("您尚未参加该考试");
            }

            // 检查是否可以查看答案：考试已结束 或 学生已提交
            boolean canViewAnswers = ExamStatusEnum.ENDED.getCode().equals(exam.getStatus())
                    || ExamSessionStatusEnum.SUBMITTED.getCode().equals(session.getStatus())
                    || ExamSessionStatusEnum.GRADED.getCode().equals(session.getStatus());

            if (!canViewAnswers) {
                throw new BusinessException("考试尚未结束，暂时无法查看答案");
            }

            Paper paper = paperService.getById(exam.getPaperId());
            if (paper == null || paper.getQuestions() == null) {
                return List.of();
            }
            List<Long> questionIds = paper.getQuestions().stream()
                    .map(Paper.PaperQuestion::getQuestionId)
                    .toList();
            return questionService.listByIds(questionIds);
        }

        throw new BusinessException("无权查看该考试");
    }

    /**
     * 获取学生的考试列表
     * 查询学生已加入课程的所有已发布考试，并附带学生的考试记录状态
     */
    public List<Exam> getMyExams(Long studentId) {
        refreshExamStatuses();
        // 获取学生加入的课程
        List<Course> myCourses = courseService.getMyCourses(studentId);
        List<Long> courseIds = myCourses.stream()
                .map(Course::getId)
                .toList();
        if (courseIds.isEmpty()) {
            return List.of();
        }
        // 获取这些课程的已发布考试
        List<Exam> exams = lambdaQuery()
                .in(Exam::getCourseId, courseIds)
                .ne(Exam::getStatus, ExamStatusEnum.DRAFT.getCode())
                .list();

        // 查询每个考试的学生考试记录状态
        for (Exam exam : exams) {
            ExamSession session = examSessionService.getByExamIdAndStudentId(exam.getId(), studentId);
            if (session != null) {
                exam.setStudentExamStatus(session.getStatus());
            } else {
                exam.setStudentExamStatus(ExamSessionStatusEnum.NOT_STARTED.getCode());
            }
        }

        fillExamDisplayFields(exams);
        return exams;
    }

    /**
     * 开始考试
     * 检查考试状态、时间、课程成员身份，创建或返回考试记录
     */
    @Transactional
    public ExamSession startExam(Long examId, Long studentId) {
        refreshExamStatuses();
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        if (!ExamStatusEnum.PUBLISHED.getCode().equals(exam.getStatus())
                && !ExamStatusEnum.STARTED.getCode().equals(exam.getStatus())) {
            throw new BusinessException("考试未发布");
        }

        // 检查学生是否已加入课程
        if (!courseService.isCourseMember(exam.getCourseId(), studentId)) {
            throw new BusinessException("请先加入对应课程");
        }

        // 检查考试时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartedAt())) {
            throw new BusinessException("考试未开始");
        }
        if (now.isAfter(exam.getEndedAt())) {
            throw new BusinessException("考试已结束");
        }

        // 检查是否已参加过（已提交的不能重复参加）
        ExamSession existing = examSessionService.getByExamIdAndStudentId(examId, studentId);
        if (existing != null && ExamSessionStatusEnum.SUBMITTED.getCode().equals(existing.getStatus())) {
            throw new BusinessException("已参加过该考试");
        }
        if (existing != null) {
            // 继续考试，返回已有的session
            return existing;
        }

        // 创建新的考试记录
        ExamSession session = new ExamSession();
        session.setExamId(examId);
        session.setStudentId(studentId);
        session.setStartedAt(now);
        session.setStatus(ExamSessionStatusEnum.IN_PROGRESS.getCode());
        session.setTotalScore(exam.getTotalScore());
        examSessionService.save(session);
        return session;
    }

    /**
     * 自动保存答案
     * 在考试过程中定时保存答案，防止浏览器崩溃导致数据丢失
     */
    @Transactional
    public void autoSaveExam(Long examId, Long studentId, List<ExamSession.Answer> answers) {
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        if (!ExamStatusEnum.PUBLISHED.getCode().equals(exam.getStatus())
                && !ExamStatusEnum.STARTED.getCode().equals(exam.getStatus())) {
            throw new BusinessException("考试未发布");
        }
        if (LocalDateTime.now().isAfter(exam.getEndedAt().plusSeconds(30))) {
            throw new BusinessException("考试已结束，无法继续保存");
        }
        // 获取考试记录
        ExamSession session = examSessionService.getByExamIdAndStudentId(examId, studentId);
        if (session == null) {
            throw new BusinessException("未开始该考试");
        }
        if (!ExamSessionStatusEnum.IN_PROGRESS.getCode().equals(session.getStatus())) {
            throw new BusinessException("当前考试状态不允许保存");
        }
        if (ExamSessionStatusEnum.SUBMITTED.getCode().equals(session.getStatus())) {
            throw new BusinessException("已提交过该考试，无法保存");
        }
        if (exam.getDuration() != null) {
            LocalDateTime durationDeadline = sessionDeadline(session, exam.getDuration());
            if (LocalDateTime.now().isAfter(durationDeadline)) {
                throw new BusinessException("考试已超时，无法继续保存");
            }
        }

        // 仅保存答案，不改变状态，不评分
        session.setAnswers(answers);
        examSessionService.updateById(session);
    }

    /**
     * 提交考试
     * 保存答案，客观题自动评分，主观题标记为待评分
     */
    @Transactional
    public void submitExam(Long examId, Long studentId, List<ExamSession.Answer> answers) {
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        if (!ExamStatusEnum.PUBLISHED.getCode().equals(exam.getStatus())
                && !ExamStatusEnum.STARTED.getCode().equals(exam.getStatus())) {
            throw new BusinessException("考试未发布");
        }

        // 获取考试记录
        ExamSession session = examSessionService.getByExamIdAndStudentId(examId, studentId);
        if (session == null) {
            throw new BusinessException("未开始该考试");
        }
        if (!ExamSessionStatusEnum.IN_PROGRESS.getCode().equals(session.getStatus())) {
            throw new BusinessException("当前考试状态不允许提交");
        }
        if (ExamSessionStatusEnum.SUBMITTED.getCode().equals(session.getStatus())) {
            throw new BusinessException("已提交过该考试");
        }

        // 检查是否过了考试绝对结束时间（结束后不允许再提交）
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(exam.getEndedAt().plusSeconds(30))) {
            throw new BusinessException("考试已结束");
        }
        if (exam.getDuration() != null) {
            LocalDateTime durationDeadline = sessionDeadline(session, exam.getDuration());
            if (now.isAfter(durationDeadline)) {
                throw new BusinessException("考试已超时");
            }
        }

        // 验证答案格式
        validateAnswers(answers);

        // 自动评分（仅客观题）
        BigDecimal objectiveScore = BigDecimal.ZERO;
        boolean hasSubjective = false;
        Paper paper = paperService.getById(exam.getPaperId());
        Set<Long> paperQuestionIds = getPaperQuestionIdSet(paper);
        validateAnswerQuestionIds(answers, paperQuestionIds);

        // 批量查询题目信息，避免 N+1 问题
        List<Long> questionIds = answers.stream()
                .map(ExamSession.Answer::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Question> questionMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        if (paper != null && paper.getQuestions() != null) {
            for (ExamSession.Answer answer : answers) {
                // 从 Map 中获取题目，避免 N+1 查询
                Question question = questionMap.get(answer.getQuestionId());
                if (question != null) {
                    // 记录题目类型
                    answer.setQuestionType(question.getType());

                    if (OBJECTIVE_TYPES.contains(question.getType())) {
                        // 客观题：自动评分
                        boolean isCorrect = checkAnswer(question, answer.getAnswer());
                        answer.setIsCorrect(isCorrect);
                        answer.setGradingStatus(GradingStatusEnum.GRADED.getCode());

                        if (isCorrect) {
                            BigDecimal score = getQuestionScore(paper, answer.getQuestionId());
                            answer.setScore(score);
                            objectiveScore = objectiveScore.add(score);
                        } else {
                            answer.setScore(BigDecimal.ZERO);
                        }
                    } else if (SUBJECTIVE_TYPES.contains(question.getType())) {
                        // 主观题：标记为待评分
                        hasSubjective = true;
                        answer.setIsCorrect(null);
                        answer.setScore(null);
                        answer.setGradingStatus(GradingStatusEnum.PENDING.getCode());
                    }
                }
            }
        }

        // 保存考试结果
        session.setAnswers(answers);
        session.setScore(objectiveScore); // 暂时只保存客观题得分
        session.setSubmittedAt(now);
        session.setStatus(hasSubjective ? ExamSessionStatusEnum.SUBMITTED.getCode() : ExamSessionStatusEnum.GRADED.getCode());
        session.setGradingStatus(hasSubjective ? GradingStatusEnum.PENDING.getCode() : GradingStatusEnum.COMPLETED.getCode());
        examSessionService.updateById(session);
    }

    /**
     * 检查答案是否正确
     * 支持单选题、多选题、判断题的答案比较
     */
    private boolean checkAnswer(Question question, String answer) {
        if (question.getCorrectAnswer() == null || answer == null) {
            return false;
        }

        // 多选题答案处理（数组格式）
        if (QuestionTypeEnum.MULTIPLE_CHOICE.getCode().equals(question.getType())) {
            try {
                // 解析学生答案（JSON数组字符串 -> List）
                List<String> studentAnswers = JSONUtil.toList(answer, String.class);
                // 解析正确答案（JSON数组字符串 -> List）
                List<String> correctAnswers = JSONUtil.toList(question.getCorrectAnswer().toString(), String.class);

                // 排序后比较（忽略顺序）
                studentAnswers.sort(String::compareTo);
                correctAnswers.sort(String::compareTo);

                return studentAnswers.equals(correctAnswers);
            } catch (Exception e) {
                // JSON解析失败，降级为字符串比较
                return question.getCorrectAnswer().toString().equalsIgnoreCase(answer.trim());
            }
        }

        // 判断题兼容 A/B 与 正确/错误 两种存储格式
        if (QuestionTypeEnum.TRUE_FALSE.getCode().equals(question.getType())) {
            String normalizedStudent = normalizeTrueFalseAnswer(answer);
            String normalizedCorrect = normalizeTrueFalseAnswer(question.getCorrectAnswer().toString());
            if (normalizedStudent != null && normalizedCorrect != null) {
                return normalizedStudent.equals(normalizedCorrect);
            }
        }

        // 填空题：支持多答案、JSON数组、逗号/分号/换行分隔，并忽略大小写与多余空白
        if (QuestionTypeEnum.FILL_BLANK.getCode().equals(question.getType())) {
            return isFillBlankAnswerCorrect(answer, question.getCorrectAnswer());
        }

        // 单选题、判断题：简单字符串比较（忽略大小写和空格）
        return question.getCorrectAnswer().toString().trim().equalsIgnoreCase(answer.trim());
    }

    private boolean isFillBlankAnswerCorrect(String studentAnswer, Object correctAnswer) {
        String normalizedStudent = normalizeText(studentAnswer);
        if (normalizedStudent.isEmpty() || correctAnswer == null) {
            return false;
        }

        List<String> candidates = parseFillBlankCandidates(correctAnswer);
        if (candidates.isEmpty()) {
            return false;
        }

        for (String candidate : candidates) {
            String normalizedCandidate = normalizeText(candidate);
            if (normalizedCandidate.isEmpty()) {
                continue;
            }
            if (normalizedStudent.equals(normalizedCandidate)) {
                return true;
            }
            if (isNumericEquivalent(normalizedStudent, normalizedCandidate)) {
                return true;
            }
        }
        return false;
    }

    private List<String> parseFillBlankCandidates(Object correctAnswer) {
        if (correctAnswer == null) {
            return List.of();
        }
        String raw = correctAnswer.toString();
        if (raw == null) {
            return List.of();
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return List.of();
        }

        try {
            if (JSONUtil.isTypeJSONArray(value)) {
                List<String> arr = JSONUtil.toList(value, String.class);
                return arr == null ? List.of() : arr.stream().filter(StringUtils::isNotBlank).toList();
            }
        } catch (Exception ignored) {
            // JSON 解析失败走分隔符降级
        }

        if (value.contains("\n") || value.contains(",") || value.contains("，")
                || value.contains(";") || value.contains("；") || value.contains("|")) {
            return java.util.Arrays.stream(value.split("[\\n,，;；|]+"))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .toList();
        }

        return List.of(value);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace('\u00A0', ' ')
                .replace('\u3000', ' ')
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    private boolean isNumericEquivalent(String a, String b) {
        try {
            return new BigDecimal(a).compareTo(new BigDecimal(b)) == 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String normalizeTrueFalseAnswer(String answer) {
        if (answer == null) {
            return null;
        }
        String value = answer.trim();
        if ("A".equalsIgnoreCase(value) || "正确".equals(value) || "true".equalsIgnoreCase(value)) {
            return "TRUE";
        }
        if ("B".equalsIgnoreCase(value) || "错误".equals(value) || "false".equalsIgnoreCase(value)) {
            return "FALSE";
        }
        return null;
    }

    /**
     * 从试卷中获取题目分值
     */
    private BigDecimal getQuestionScore(Paper paper, Long questionId) {
        if (paper.getQuestions() == null) {
            return BigDecimal.ZERO;
        }
        return paper.getQuestions().stream()
                .filter(q -> q.getQuestionId().equals(questionId))
                .findFirst()
                .map(Paper.PaperQuestion::getScore)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 发布考试
     */
    @Transactional
    public void publishExam(Long examId) {
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 兼容历史数据：空状态按草稿处理
        if (exam.getStatus() == null) {
            exam.setStatus(ExamStatusEnum.DRAFT.getCode());
        }

        // 仅允许草稿状态发布，避免非法状态跳转
        if (!ExamStatusEnum.DRAFT.getCode().equals(exam.getStatus())) {
            throw new BusinessException("仅草稿状态的考试可以发布");
        }

        // 基础时间校验
        if (exam.getStartedAt() == null || exam.getEndedAt() == null) {
            throw new BusinessException("请先设置考试开始和结束时间");
        }
        if (!exam.getStartedAt().isBefore(exam.getEndedAt())) {
            throw new BusinessException("考试开始时间必须早于结束时间");
        }

        exam.setStatus(ExamStatusEnum.PUBLISHED.getCode());
        updateById(exam);
    }

    /**
     * 取消考试
     */
    @Transactional
    public void cancelExam(Long examId) {
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 仅允许草稿/已发布状态取消，禁止进行中/已结束的考试被取消
        boolean cancellable = ExamStatusEnum.DRAFT.getCode().equals(exam.getStatus())
                || ExamStatusEnum.PUBLISHED.getCode().equals(exam.getStatus());
        if (!cancellable) {
            throw new BusinessException("当前考试状态不允许取消");
        }

        exam.setStatus(ExamStatusEnum.CANCELLED.getCode());
        updateById(exam);
    }

    /**
     * 检查考试所有权
     */
    public void checkOwnership(Long examId, Long userId, String userRole) {
        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        if (!exam.getTeacherId().equals(userId)) {
            throw new BusinessException("无权操作该考试");
        }
    }

    /**
     * 检查考试是否可以删除
     * 已有学生参加的考试不能删除
     */
    public void checkCanDelete(Long examId) {
        List<ExamSession> sessions = examSessionService.getByExamId(examId);
        if (!sessions.isEmpty()) {
            throw new BusinessException("已有 " + sessions.size() + " 名学生参加该考试，无法删除");
        }
    }

    /**
     * 老师给主观题评分
     */
    @Transactional
    public void gradeSubjectiveAnswers(Long operatorId, String operatorRole, GradeSubjectiveRequest request) {
        if (request == null || request.getExamSessionId() == null) {
            throw new BusinessException("评分请求参数不完整");
        }
        if (request.getGrades() == null || request.getGrades().isEmpty()) {
            throw new BusinessException("请至少提交一题评分结果");
        }

        // 获取考试记录
        ExamSession session = examSessionService.getById(request.getExamSessionId());
        if (session == null) {
            throw new BusinessException("考试记录不存在");
        }

        // 验证考试是否属于当前操作人
        Exam exam = getById(session.getExamId());
        if (exam == null || !exam.getTeacherId().equals(operatorId)) {
            throw new BusinessException("无权评分该考试");
        }

        // 验证是否已提交且待评分
        if (!ExamSessionStatusEnum.SUBMITTED.getCode().equals(session.getStatus())) {
            throw new BusinessException("考试尚未提交");
        }
        if (!GradingStatusEnum.PENDING.getCode().equals(session.getGradingStatus())) {
            throw new BusinessException("该考试无需评分");
        }

        // 获取试卷信息用于验证分数
        Paper paper = paperService.getById(exam.getPaperId());

        // 更新主观题得分
        List<ExamSession.Answer> answers = session.getAnswers();
        if (answers == null || answers.isEmpty()) {
            throw new BusinessException("该考试没有可评分的答案");
        }
        BigDecimal subjectiveScore = BigDecimal.ZERO;

        for (GradeSubjectiveRequest.SubjectiveGrade grade : request.getGrades()) {
            if (grade == null || grade.getQuestionId() == null || grade.getScore() == null) {
                throw new BusinessException("评分数据不完整");
            }

            // 查找对应的答案
            ExamSession.Answer answer = answers.stream()
                    .filter(a -> a.getQuestionId().equals(grade.getQuestionId()))
                    .findFirst()
                    .orElse(null);

            if (answer == null) {
                continue;
            }

            // 验证是否是主观题
            if (!SUBJECTIVE_TYPES.contains(answer.getQuestionType())) {
                throw new BusinessException("题目 " + grade.getQuestionId() + " 不是主观题，不能手动评分");
            }

            // 验证分数是否超过题目满分
            BigDecimal maxScore = getQuestionScore(paper, grade.getQuestionId());
            if (grade.getScore().compareTo(maxScore) > 0) {
                throw new BusinessException("题目 " + grade.getQuestionId() + " 的得分不能超过满分 " + maxScore);
            }

            // 验证分数不能为负数
            if (grade.getScore().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("题目 " + grade.getQuestionId() + " 的得分不能为负数");
            }

            // 更新答案评分
            answer.setScore(grade.getScore());
            answer.setTeacherComment(grade.getComment());
            answer.setGradingStatus(GradingStatusEnum.GRADED.getCode());
            answer.setIsCorrect(grade.getScore().compareTo(BigDecimal.ZERO) > 0);
        }

        // 检查是否所有主观题都已评分
        boolean allGraded = answers.stream()
                .filter(a -> SUBJECTIVE_TYPES.contains(a.getQuestionType()))
                .allMatch(a -> GradingStatusEnum.GRADED.getCode().equals(a.getGradingStatus()));

        if (!allGraded) {
            throw new BusinessException("还有主观题未评分，请完成所有主观题的评分");
        }

        // 计算主观题总分
        for (ExamSession.Answer answer : answers) {
            if (SUBJECTIVE_TYPES.contains(answer.getQuestionType()) && answer.getScore() != null) {
                subjectiveScore = subjectiveScore.add(answer.getScore());
            }
        }

        // 计算最终总分（客观题 + 主观题）
        BigDecimal objectiveScore = session.getScore() != null ? session.getScore() : BigDecimal.ZERO;
        BigDecimal finalScore = objectiveScore.add(subjectiveScore);

        // 保存结果
        session.setScore(finalScore);
        session.setAnswers(answers);
        session.setStatus(ExamSessionStatusEnum.GRADED.getCode());
        session.setGradingStatus(GradingStatusEnum.COMPLETED.getCode());
        examSessionService.updateById(session);
    }

    /**
     * 批量自动阅卷（按考试）
     * 用于重跑客观题自动评分逻辑（含填空题），便于回归测试与数据修复
     */
    @Transactional
    public int autoGradeByExam(Long examId, Long operatorId, String operatorRole) {
        checkOwnership(examId, operatorId, operatorRole);

        Exam exam = getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        List<ExamSession> sessions = examSessionService.getByExamId(examId);
        if (sessions == null || sessions.isEmpty()) {
            return 0;
        }

        Paper paper = paperService.getById(exam.getPaperId());
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }

        List<Long> questionIds = sessions.stream()
                .filter(s -> s.getAnswers() != null && !s.getAnswers().isEmpty())
                .flatMap(s -> s.getAnswers().stream())
                .map(ExamSession.Answer::getQuestionId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Question> questionMap = questionIds.isEmpty()
                ? Map.of()
                : questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        int processed = 0;
        for (ExamSession session : sessions) {
            if (session.getAnswers() == null || session.getAnswers().isEmpty()) {
                continue;
            }
            boolean submittedLike = session.getSubmittedAt() != null
                    || ExamSessionStatusEnum.SUBMITTED.getCode().equals(session.getStatus())
                    || ExamSessionStatusEnum.GRADED.getCode().equals(session.getStatus());
            if (!submittedLike) {
                continue;
            }
            // 兼容历史状态：只要已提交且有答案，都允许重跑自动阅卷

            BigDecimal objectiveScore = BigDecimal.ZERO;
            BigDecimal gradedEssayScore = BigDecimal.ZERO;
            boolean hasEssay = false;
            boolean allEssayGraded = true;

            for (ExamSession.Answer answer : session.getAnswers()) {
                if (answer == null || answer.getQuestionId() == null) {
                    continue;
                }
                Question question = questionMap.get(answer.getQuestionId());
                if (question == null) {
                    continue;
                }

                answer.setQuestionType(question.getType());
                if (OBJECTIVE_TYPES.contains(question.getType())) {
                    boolean isCorrect = checkAnswer(question, answer.getAnswer());
                    answer.setIsCorrect(isCorrect);
                    answer.setGradingStatus(GradingStatusEnum.GRADED.getCode());
                    BigDecimal score = isCorrect ? getQuestionScore(paper, answer.getQuestionId()) : BigDecimal.ZERO;
                    answer.setScore(score);
                    objectiveScore = objectiveScore.add(score);
                } else if (SUBJECTIVE_TYPES.contains(question.getType())) {
                    hasEssay = true;
                    if ((GradingStatusEnum.GRADED.getCode().equals(answer.getGradingStatus())
                            || GradingStatusEnum.COMPLETED.getCode().equals(answer.getGradingStatus()))
                            && answer.getScore() != null) {
                        gradedEssayScore = gradedEssayScore.add(answer.getScore());
                        answer.setIsCorrect(answer.getScore().compareTo(BigDecimal.ZERO) > 0);
                    } else {
                        allEssayGraded = false;
                        answer.setIsCorrect(null);
                        answer.setScore(null);
                        answer.setGradingStatus(GradingStatusEnum.PENDING.getCode());
                    }
                }
            }

            if (!hasEssay) {
                session.setScore(objectiveScore);
                session.setStatus(ExamSessionStatusEnum.GRADED.getCode());
                session.setGradingStatus(GradingStatusEnum.COMPLETED.getCode());
            } else if (allEssayGraded) {
                session.setScore(objectiveScore.add(gradedEssayScore));
                session.setStatus(ExamSessionStatusEnum.GRADED.getCode());
                session.setGradingStatus(GradingStatusEnum.COMPLETED.getCode());
            } else {
                session.setScore(objectiveScore);
                session.setStatus(ExamSessionStatusEnum.SUBMITTED.getCode());
                session.setGradingStatus(GradingStatusEnum.PENDING.getCode());
            }

            examSessionService.updateById(session);
            processed++;
        }

        return processed;
    }

    /**
     * 获取考试结果详情
     * 区分客观题和主观题得分
     */
    public ExamResultResponse getExamResult(Long examSessionId, Long userId, String userRole) {
        ExamSession session = examSessionService.getById(examSessionId);
        if (session == null) {
            throw new BusinessException("考试记录不存在");
        }

        Exam exam = getById(session.getExamId());
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }

        // 验证权限（学生只能看自己的；老师/管理员只能看自己课程的）
        if (!session.getStudentId().equals(userId) && !exam.getTeacherId().equals(userId)) {
            throw new BusinessException("无权查看该考试结果");
        }

        Paper paper = paperService.getById(exam.getPaperId());

        // 计算客观题和主观题得分
        BigDecimal objectiveScore = BigDecimal.ZERO;
        BigDecimal subjectiveScore = BigDecimal.ZERO;

        List<ExamResultResponse.AnswerDetail> answerDetails = new ArrayList<>();

        // 批量查询题目信息，避免 N+1 问题
        List<ExamSession.Answer> sessionAnswers = session.getAnswers();
        if (sessionAnswers == null || sessionAnswers.isEmpty()) {
            throw new BusinessException("暂无答题记录");
        }
        List<Long> questionIds = sessionAnswers.stream()
                .map(ExamSession.Answer::getQuestionId)
                .collect(Collectors.toList());
        Map<Long, Question> questionMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        for (ExamSession.Answer answer : sessionAnswers) {
            Question question = questionMap.get(answer.getQuestionId());
            BigDecimal maxScore = getQuestionScore(paper, answer.getQuestionId());

            if (OBJECTIVE_TYPES.contains(answer.getQuestionType())) {
                objectiveScore = objectiveScore.add(answer.getScore() != null ? answer.getScore() : BigDecimal.ZERO);
            } else if (SUBJECTIVE_TYPES.contains(answer.getQuestionType())) {
                subjectiveScore = subjectiveScore.add(answer.getScore() != null ? answer.getScore() : BigDecimal.ZERO);
            }

            ExamResultResponse.AnswerDetail detail = new ExamResultResponse.AnswerDetail();
            detail.setQuestionId(answer.getQuestionId());
            detail.setQuestionContent(question != null ? question.getContent() : "");
            detail.setQuestionType(answer.getQuestionType());
            detail.setAnswer(answer.getAnswer());
            detail.setIsCorrect(answer.getIsCorrect());
            detail.setScore(answer.getScore());
            detail.setMaxScore(maxScore);
            detail.setGradingStatus(answer.getGradingStatus());
            detail.setTeacherComment(answer.getTeacherComment());
            answerDetails.add(detail);
        }

        ExamResultResponse result = new ExamResultResponse();
        result.setExamSessionId(session.getId());
        result.setExamId(exam.getId());
        result.setExamTitle(exam.getTitle());
        result.setStudentId(session.getStudentId());
        User student = userService.getById(session.getStudentId());
        result.setStudentName(userService.getDisplayName(student));
        result.setStartedAt(session.getStartedAt());
        result.setSubmittedAt(session.getSubmittedAt());
        result.setObjectiveScore(objectiveScore);
        result.setSubjectiveScore(subjectiveScore);
        result.setTotalScore(session.getScore());
        result.setMaxScore(exam.getTotalScore());
        result.setGradingStatus(session.getGradingStatus());
        result.setAnswers(answerDetails);

        return result;
    }

    /**
     * 分页查询考试
     */
    public PageResult<Exam> page(PageRequest pageRequest, Long courseId, Long teacherId, String status,
                                  Long currentUserId, String currentUserRole) {
        refreshExamStatuses();
        Page<Exam> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();

        // 数据隔离：管理员与教师都只能查看自己创建的考试
        if (teacherId != null && !teacherId.equals(currentUserId)) {
            return PageResult.empty(pageRequest.getCurrent(), pageRequest.getSize());
        }
        wrapper.eq(Exam::getTeacherId, currentUserId);

        // 课程筛选
        if (courseId != null) {
            wrapper.eq(Exam::getCourseId, courseId);
        }

        // 状态筛选
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(Exam::getStatus, status);
        }

        // 排序
        applySorting(wrapper, pageRequest);

        Page<Exam> result = page(page, wrapper);
        fillExamDisplayFields(result.getRecords());
        return PageResult.from(result);
    }

    private void fillExamDisplayFields(List<Exam> exams) {
        if (exams == null || exams.isEmpty()) {
            return;
        }

        List<Long> courseIds = exams.stream()
                .map(Exam::getCourseId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> courseNameMap = courseService.listByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getName, (a, _b) -> a));

        List<Long> teacherIds = exams.stream()
                .map(Exam::getTeacherId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> teacherNameMap = userService.getDisplayNameMap(teacherIds);

        for (Exam exam : exams) {
            if (exam.getCourseId() != null) {
                exam.setCourseName(courseNameMap.get(exam.getCourseId()));
            }
            if (exam.getTeacherId() != null) {
                exam.setTeacherName(teacherNameMap.get(exam.getTeacherId()));
            }
        }
    }

    /**
     * 应用排序规则
     */
    private void applySorting(LambdaQueryWrapper<Exam> wrapper, PageRequest pageRequest) {
        if (StringUtils.isBlank(pageRequest.getOrderBy())) {
            // 默认按 ID 降序
            wrapper.orderByDesc(Exam::getId);
            return;
        }

        boolean isAsc = pageRequest.getAsc();
        String orderBy = pageRequest.getOrderBy().toLowerCase();

        switch (orderBy) {
            case "id" -> {
                if (isAsc) wrapper.orderByAsc(Exam::getId);
                else wrapper.orderByDesc(Exam::getId);
            }
            case "createtime", "created_at" -> {
                if (isAsc) wrapper.orderByAsc(Exam::getCreatedAt);
                else wrapper.orderByDesc(Exam::getCreatedAt);
            }
            case "startedat", "started_at", "starttime" -> {
                if (isAsc) wrapper.orderByAsc(Exam::getStartedAt);
                else wrapper.orderByDesc(Exam::getStartedAt);
            }
            case "endedat", "ended_at", "endtime" -> {
                if (isAsc) wrapper.orderByAsc(Exam::getEndedAt);
                else wrapper.orderByDesc(Exam::getEndedAt);
            }
            case "totalscore" -> {
                if (isAsc) wrapper.orderByAsc(Exam::getTotalScore);
                else wrapper.orderByDesc(Exam::getTotalScore);
            }
            case "passscore" -> {
                if (isAsc) wrapper.orderByAsc(Exam::getPassScore);
                else wrapper.orderByDesc(Exam::getPassScore);
            }
            case "duration" -> {
                if (isAsc) wrapper.orderByAsc(Exam::getDuration);
                else wrapper.orderByDesc(Exam::getDuration);
            }
            case "status" -> {
                if (isAsc) wrapper.orderByAsc(Exam::getStatus);
                else wrapper.orderByDesc(Exam::getStatus);
            }
            default -> wrapper.orderByDesc(Exam::getId); // 无效字段使用默认排序
        }
    }

    /**
     * 验证答案格式
     * 检查答案是否符合题目类型要求
     */
    private void validateAnswers(List<ExamSession.Answer> answers) {
        if (answers == null) {
            throw new BusinessException("答案数据不能为空");
        }

        // 支持交白卷：空答案列表视为有效提交
        if (answers.isEmpty()) {
            return;
        }

        for (ExamSession.Answer answer : answers) {
            if (answer.getQuestionId() == null) {
                throw new BusinessException("题目ID不能为空");
            }

            // 检查答案内容是否为空
            if (answer.getAnswer() == null || answer.getAnswer().trim().isEmpty()) {
                throw new BusinessException("题目 " + answer.getQuestionId() + " 的答案不能为空");
            }

            // 如果是多选题，验证答案是否为有效的JSON数组格式
            if (QuestionTypeEnum.MULTIPLE_CHOICE.getCode().equals(answer.getQuestionType())) {
                try {
                    List<String> options = JSONUtil.toList(answer.getAnswer(), String.class);
                    if (options.isEmpty()) {
                        throw new BusinessException("多选题答案不能为空数组");
                    }
                } catch (Exception e) {
                    throw new BusinessException("多选题答案格式错误，应为JSON数组格式，如：[\"A\", \"B\"]");
                }
            }
        }
    }

    private LocalDateTime sessionDeadline(ExamSession session, Integer durationMinutes) {
        // 给予30秒宽限时间，避免网络延迟导致的保存/提交误判
        return session.getStartedAt().plusMinutes(durationMinutes).plusSeconds(30);
    }

    private Set<Long> getPaperQuestionIdSet(Paper paper) {
        if (paper == null || paper.getQuestions() == null) {
            return Set.of();
        }
        return paper.getQuestions().stream()
                .map(Paper.PaperQuestion::getQuestionId)
                .collect(Collectors.toSet());
    }

    private void validateAnswerQuestionIds(List<ExamSession.Answer> answers, Set<Long> paperQuestionIds) {
        if (answers == null || answers.isEmpty()) {
            return;
        }
        if (paperQuestionIds.isEmpty()) {
            throw new BusinessException("考试试卷题目不存在，无法提交");
        }

        Set<Long> submittedQuestionIds = new HashSet<>();
        for (ExamSession.Answer answer : answers) {
            Long questionId = answer.getQuestionId();
            if (!submittedQuestionIds.add(questionId)) {
                throw new BusinessException("题目 " + questionId + " 重复提交");
            }
            if (!paperQuestionIds.contains(questionId)) {
                throw new BusinessException("题目 " + questionId + " 不属于当前考试");
            }
        }
    }

    private void refreshExamStatuses() {
        // 单元测试中的 Spy 场景下 baseMapper 可能未注入，直接跳过状态刷新
        if (this.baseMapper == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Exam> exams = lambdaQuery()
                .in(Exam::getStatus, ExamStatusEnum.PUBLISHED.getCode(), ExamStatusEnum.STARTED.getCode())
                .list();

        for (Exam exam : exams) {
            if (exam.getStartedAt() == null || exam.getEndedAt() == null) {
                continue;
            }

            String targetStatus = exam.getStatus();
            if (now.isAfter(exam.getEndedAt())) {
                targetStatus = ExamStatusEnum.ENDED.getCode();
            } else if (!now.isBefore(exam.getStartedAt())) {
                targetStatus = ExamStatusEnum.STARTED.getCode();
            } else {
                targetStatus = ExamStatusEnum.PUBLISHED.getCode();
            }

            if (!targetStatus.equals(exam.getStatus())) {
                Exam toUpdate = new Exam();
                toUpdate.setId(exam.getId());
                toUpdate.setStatus(targetStatus);
                updateById(toUpdate);
            }
        }
    }
}
