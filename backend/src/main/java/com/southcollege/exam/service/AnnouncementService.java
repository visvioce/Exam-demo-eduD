package com.southcollege.exam.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southcollege.exam.entity.Announcement;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.exception.BusinessException;
import com.southcollege.exam.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementService extends ServiceImpl<AnnouncementMapper, Announcement> {

    public List<Announcement> getPublishedByType(String type) {
        return baseMapper.selectPublishedByType(type);
    }

    public List<Announcement> getPublishedAnnouncements() {
        return lambdaQuery()
                .eq(Announcement::getStatus, "PUBLISHED")
                .orderByDesc(Announcement::getPublishedAt)
                .list();
    }

    /**
     * 检查公告所有权
     */
    public void checkOwnership(Long announcementId, Long userId, String userRole) {
        Announcement announcement = getById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        if (!RoleEnum.ADMIN.getCode().equals(userRole) && !announcement.getPublisherId().equals(userId)) {
            throw new BusinessException("无权操作该公告");
        }
    }
}
