package com.southcollege.exam.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southcollege.exam.entity.CourseMember;
import com.southcollege.exam.mapper.CourseMemberMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseMemberService extends ServiceImpl<CourseMemberMapper, CourseMember> {

    public List<CourseMember> getByCourseId(Long courseId) {
        return baseMapper.selectByCourseId(courseId);
    }

    public List<CourseMember> getByStudentId(Long studentId) {
        return baseMapper.selectByStudentId(studentId);
    }
}
