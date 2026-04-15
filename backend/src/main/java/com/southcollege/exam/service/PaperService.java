package com.southcollege.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southcollege.exam.dto.request.AutoGeneratePaperRequest;
import com.southcollege.exam.dto.request.TypeConfig;
import com.southcollege.exam.entity.Course;
import com.southcollege.exam.entity.Exam;
import com.southcollege.exam.entity.Paper;
import com.southcollege.exam.entity.Question;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.exception.BusinessException;
import com.southcollege.exam.mapper.PaperMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 试卷服务类
 * <p>
 * 负责试卷的增删改查、自动组卷等核心业务逻辑。
 * 自动组卷功能根据配置的题型、数量、难度等参数，从题库中随机选取题目生成试卷。
 * </p>
 *
 * @author South College Exam Team
 * @version 1.0
 * @since 2024
 */
@Service
public class PaperService extends ServiceImpl<PaperMapper, Paper> {

    @Autowired
    @Lazy
    private ExamService examService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private QuestionService questionService;

    public List<Paper> getByCourseId(Long courseId) {
        List<Paper> papers = baseMapper.selectByCourseId(courseId);
        fillCourseNames(papers);
        return papers;
    }

    public List<Paper> getByTeacherId(Long teacherId) {
        List<Paper> papers = baseMapper.selectByTeacherId(teacherId);
        fillCourseNames(papers);
        return papers;
    }

    public List<Paper> listWithCourseNames() {
        List<Paper> papers = list();
        fillCourseNames(papers);
        return papers;
    }

    public Paper getByIdWithCourseName(Long id) {
        Paper paper = getById(id);
        if (paper == null) {
            return null;
        }
        fillCourseNames(List.of(paper));
        return paper;
    }

    public com.southcollege.exam.dto.response.PageResult<Paper> pageWithCourseNames(
            com.southcollege.exam.dto.request.PageRequest pageRequest,
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Paper> wrapper
    ) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Paper> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Paper> result = page(page, wrapper);
        fillCourseNames(result.getRecords());
        return com.southcollege.exam.dto.response.PageResult.from(result);
    }

    /**
     * 检查试卷所有权
     */
    public void checkOwnership(Long paperId, Long userId, String userRole) {
        Paper paper = getById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        if (RoleEnum.ADMIN.getCode().equals(userRole)) {
            return;
        }
        if (!paper.getTeacherId().equals(userId)) {
            throw new BusinessException("无权操作该试卷");
        }
    }

    /**
     * 检查试卷是否可以删除
     * 已被考试引用的试卷不能删除
     */
    public void checkCanDelete(Long paperId) {
        List<Exam> exams = examService.getByPaperId(paperId);
        if (!exams.isEmpty()) {
            throw new BusinessException("该试卷已被 " + exams.size() + " 场考试引用，无法删除");
        }
    }

    private void fillCourseNames(List<Paper> papers) {
        if (papers == null || papers.isEmpty()) {
            return;
        }
        List<Long> courseIds = papers.stream()
                .map(Paper::getCourseId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        Map<Long, String> courseNameMap = courseService.listByIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getName, (a, _b) -> a));
        for (Paper paper : papers) {
            if (paper.getCourseId() != null) {
                paper.setCourseName(courseNameMap.get(paper.getCourseId()));
            }
        }
    }

    /**
     * 自动组卷
     * <p>
     * 根据请求参数自动从题库中选取题目生成试卷。
     * 组卷策略：
     * 1. 支持按题型配置（单选、多选、判断、填空、简答）
     * 2. 支持按难度筛选（简单、中等、困难）
     * 3. 支持按学科筛选
     * 4. 采用随机抽取算法，保证试卷的多样性
     * 5. 支持两种配置方式：详细配置对象或简单数量配置
     * </p>
     *
     * @param request  组卷请求参数，包含题型配置、难度、学科等信息
     * @param teacherId 教师ID，用于数据隔离，教师只能使用自己创建的题目
     * @return 生成的试卷对象，包含题目列表和总分
     * @throws BusinessException 当题库中符合条件的题目数量不足时抛出异常
     */
    public Paper autoGenerate(AutoGeneratePaperRequest request, Long teacherId) {
        List<Paper.PaperQuestion> questions = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;

        // 题型配置：[类型, 详细配置, 简单数量, 简单分数]
        totalScore = processTypeConfig(questions, totalScore, request,
                "SINGLE_CHOICE", request.getSingleChoice(),
                request.getSingleChoiceCount(), request.getSingleChoiceScore(), teacherId);
        totalScore = processTypeConfig(questions, totalScore, request,
                "MULTIPLE_CHOICE", request.getMultipleChoice(),
                request.getMultipleChoiceCount(), request.getMultipleChoiceScore(), teacherId);
        totalScore = processTypeConfig(questions, totalScore, request,
                "TRUE_FALSE", request.getTrueFalse(),
                request.getTrueFalseCount(), request.getTrueFalseScore(), teacherId);
        totalScore = processTypeConfig(questions, totalScore, request,
                "FILL_BLANK", request.getFillBlank(),
                request.getFillBlankCount(), request.getFillBlankScore(), teacherId);
        totalScore = processTypeConfig(questions, totalScore, request,
                "ESSAY", request.getEssay(),
                request.getEssayCount(), request.getEssayScore(), teacherId);

        if (questions.isEmpty()) {
            throw new BusinessException("请至少选择一种题型并设置数量");
        }

        Paper paper = new Paper();
        paper.setName(request.getName());
        paper.setDescription(request.getDescription());
        paper.setCourseId(request.getCourseId());
        paper.setTeacherId(teacherId);
        paper.setType("AUTO");
        paper.setStatus("DRAFT");
        paper.setQuestions(questions);
        paper.setTotalScore(totalScore);

        save(paper);
        return paper;
    }

    /**
     * 随机选取题目
     * <p>
     * 根据条件从题库中随机选取指定数量的题目。
     * 算法步骤：
     * 1. 构建查询条件：题型、教师ID（数据隔离）、学科（可选）、难度（可选）
     * 2. 查询符合条件的所有题目
     * 3. 检查题目数量是否足够
     * 4. 使用 Fisher-Yates 洗牌算法（Collections.shuffle）随机打乱题目顺序
     * 5. 返回前 count 个题目
     * </p>
     *
     * @param type      题目类型（SINGLE_CHOICE/MULTIPLE_CHOICE/TRUE_FALSE/FILL_BLANK/ESSAY）
     * @param subject   学科，支持模糊匹配，可为 null
     * @param difficulty 难度（EASY/MEDIUM/HARD），可为 null
     * @param count     需要选取的题目数量
     * @param teacherId 教师ID，用于数据隔离
     * @return 随机选取的题目列表
     * @throws BusinessException 当符合条件的题目数量不足时抛出异常
     */
    private List<Question> selectRandomQuestions(String type, String subject, String difficulty, int count, Long teacherId) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getType, type);
        wrapper.eq(Question::getTeacherId, teacherId);

        if (subject != null && !subject.isEmpty()) {
            wrapper.like(Question::getSubject, subject);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }

        List<Question> allQuestions = questionService.list(wrapper);
        
        if (allQuestions.size() < count) {
            throw new BusinessException("题库中" + type + "类型题目不足，当前只有" + allQuestions.size() + "题，需要" + count + "题");
        }

        // 使用 Fisher-Yates 洗牌算法随机打乱题目顺序
        Collections.shuffle(allQuestions);
        return allQuestions.subList(0, count);
    }

    private void addQuestionsToList(List<Paper.PaperQuestion> questions, List<Question> selectedQuestions, BigDecimal score) {
        for (Question q : selectedQuestions) {
            Paper.PaperQuestion pq = new Paper.PaperQuestion();
            pq.setQuestionId(q.getId());
            pq.setScore(score);
            questions.add(pq);
        }
    }

    /**
     * 处理单个题型的组卷配置（消除重复代码）
     */
    private BigDecimal processTypeConfig(List<Paper.PaperQuestion> questions, BigDecimal totalScore,
                                         AutoGeneratePaperRequest request,
                                         String type, TypeConfig config,
                                         Integer simpleCount, BigDecimal simpleScore,
                                         Long teacherId) {
        if (config != null && config.getCount() != null && config.getCount() > 0) {
            List<Question> selected = selectRandomQuestions(
                    type,
                    config.getSubject() != null ? config.getSubject() : request.getSubject(),
                    config.getDifficulty() != null ? config.getDifficulty() : request.getDifficulty(),
                    config.getCount(),
                    teacherId
            );
            addQuestionsToList(questions, selected, config.getScore());
            return totalScore.add(config.getScore().multiply(BigDecimal.valueOf(config.getCount())));
        } else if (simpleCount != null && simpleCount > 0) {
            List<Question> selected = selectRandomQuestions(
                    type,
                    request.getSubject(),
                    request.getDifficulty(),
                    simpleCount,
                    teacherId
            );
            addQuestionsToList(questions, selected, simpleScore);
            return totalScore.add(simpleScore.multiply(BigDecimal.valueOf(simpleCount)));
        }
        return totalScore;
    }
}
