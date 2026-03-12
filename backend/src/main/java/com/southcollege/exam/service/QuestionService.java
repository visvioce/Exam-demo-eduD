package com.southcollege.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southcollege.exam.dto.request.PageRequest;
import com.southcollege.exam.dto.response.PageResult;
import com.southcollege.exam.entity.Paper;
import com.southcollege.exam.entity.Question;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.exception.BusinessException;
import com.southcollege.exam.mapper.QuestionMapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService extends ServiceImpl<QuestionMapper, Question> {

    @Autowired
    @Lazy
    private PaperService paperService;

    public List<Question> getByTeacherId(Long teacherId) {
        return baseMapper.selectByTeacherId(teacherId);
    }

    public List<Question> getByType(String type) {
        return baseMapper.selectByType(type);
    }

    /**
     * 分页查询题目
     */
    public PageResult<Question> page(PageRequest pageRequest, Long teacherId, String type, String subject, String difficulty,
                                      Long currentUserId, String currentUserRole) {
        Page<Question> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();

        boolean isAdmin = RoleEnum.ADMIN.getCode().equals(currentUserRole);
        if (!isAdmin && teacherId != null && !teacherId.equals(currentUserId)) {
            return PageResult.empty(pageRequest.getCurrent(), pageRequest.getSize());
        }
        if (teacherId != null) {
            wrapper.eq(Question::getTeacherId, teacherId);
        } else if (!isAdmin) {
            wrapper.eq(Question::getTeacherId, currentUserId);
        }

        // 题型筛选
        if (StringUtils.isNotBlank(type)) {
            wrapper.eq(Question::getType, type);
        }

        // 学科/分类筛选
        if (StringUtils.isNotBlank(subject)) {
            wrapper.like(Question::getSubject, subject);
        }

        // 难度筛选
        if (StringUtils.isNotBlank(difficulty)) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }

        // 排序
        applySorting(wrapper, pageRequest);

        Page<Question> result = page(page, wrapper);
        return PageResult.from(result);
    }

    /**
     * 应用排序规则
     */
    private void applySorting(LambdaQueryWrapper<Question> wrapper, PageRequest pageRequest) {
        if (StringUtils.isBlank(pageRequest.getOrderBy())) {
            // 默认按 ID 降序
            wrapper.orderByDesc(Question::getId);
            return;
        }

        boolean isAsc = pageRequest.getAsc();
        String orderBy = pageRequest.getOrderBy().toLowerCase();

        switch (orderBy) {
            case "id" -> {
                if (isAsc) wrapper.orderByAsc(Question::getId);
                else wrapper.orderByDesc(Question::getId);
            }
            case "score" -> {
                if (isAsc) wrapper.orderByAsc(Question::getScore);
                else wrapper.orderByDesc(Question::getScore);
            }
            case "difficulty" -> {
                if (isAsc) wrapper.orderByAsc(Question::getDifficulty);
                else wrapper.orderByDesc(Question::getDifficulty);
            }
            case "type" -> {
                if (isAsc) wrapper.orderByAsc(Question::getType);
                else wrapper.orderByDesc(Question::getType);
            }
            case "subject" -> {
                if (isAsc) wrapper.orderByAsc(Question::getSubject);
                else wrapper.orderByDesc(Question::getSubject);
            }
            default -> wrapper.orderByDesc(Question::getId); // 无效字段使用默认排序
        }
    }

    /**
     * 检查题目所有权
     */
    public void checkOwnership(Long questionId, Long userId, String userRole) {
        Question question = getById(questionId);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        if (RoleEnum.ADMIN.getCode().equals(userRole)) {
            return;
        }
        if (!question.getTeacherId().equals(userId)) {
            throw new BusinessException("无权操作该题目");
        }
    }

    /**
     * 检查题目是否可以删除
     * 被试卷引用的题目不能删除
     */
    public void checkCanDelete(Long questionId) {
        // 获取所有试卷，检查是否引用了该题目
        List<Paper> papers = paperService.list();
        for (Paper paper : papers) {
            if (paper.getQuestions() != null) {
                boolean isUsed = paper.getQuestions().stream()
                        .anyMatch(pq -> pq.getQuestionId().equals(questionId));
                if (isUsed) {
                    throw new BusinessException("该题目已被试卷「" + paper.getName() + "」引用，无法删除");
                }
            }
        }
    }

    /**
     * 获取所有学科列表（去重）
     */
    public List<String> getAllSubjects() {
        return baseMapper.selectAllSubjects();
    }
}
