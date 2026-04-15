package com.southcollege.exam.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.southcollege.exam.entity.Announcement;
import com.southcollege.exam.enums.RoleEnum;
import com.southcollege.exam.exception.BusinessException;
import com.southcollege.exam.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public List<Announcement> listVisibleAnnouncements(Long userId, String userRole) {
        if (RoleEnum.ADMIN.getCode().equals(userRole)) {
            return lambdaQuery()
                    .orderByDesc(Announcement::getPublishedAt)
                    .orderByDesc(Announcement::getId)
                    .list();
        }

        if (RoleEnum.TEACHER.getCode().equals(userRole)) {
            return lambdaQuery()
                    .and(wrapper -> wrapper
                            .eq(Announcement::getStatus, "PUBLISHED")
                            .or()
                            .eq(Announcement::getPublisherId, userId))
                    .orderByDesc(Announcement::getPublishedAt)
                    .orderByDesc(Announcement::getId)
                    .list();
        }

        return getPublishedAnnouncements();
    }

    public Announcement getVisibleAnnouncementById(Long id, Long userId, String userRole) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            return null;
        }

        if (RoleEnum.ADMIN.getCode().equals(userRole)) {
            return announcement;
        }

        boolean published = "PUBLISHED".equals(announcement.getStatus());
        boolean owner = announcement.getPublisherId() != null && announcement.getPublisherId().equals(userId);

        if (published || owner) {
            return announcement;
        }

        throw new BusinessException("无权查看该公告");
    }

    public Announcement prepareForCreate(Announcement announcement, Long publisherId) {
        announcement.setPublisherId(publisherId);
        if (announcement.getStatus() == null || announcement.getStatus().isBlank()) {
            announcement.setStatus("DRAFT");
        }
        announcement.setPublishedAt(resolvePublishedAt(announcement.getStatus(), null));
        return announcement;
    }

    public Announcement prepareForUpdate(Long id, Announcement announcement, Long userId, String userRole) {
        Announcement existing = getById(id);
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }

        checkOwnership(id, userId, userRole);

        announcement.setId(id);
        announcement.setPublisherId(existing.getPublisherId());
        announcement.setPublishedAt(resolvePublishedAt(announcement.getStatus(), existing.getPublishedAt()));
        return announcement;
    }

    private LocalDateTime resolvePublishedAt(String status, LocalDateTime existingPublishedAt) {
        if (!"PUBLISHED".equals(status)) {
            return null;
        }
        return existingPublishedAt != null ? existingPublishedAt : LocalDateTime.now();
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
