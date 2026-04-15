-- =====================================================
-- 南方职业学院在线考试系统 - 测试数据
-- =====================================================
-- 测试账号（密码统一为 123456）：
--   管理员: admin
--   教师:   teacher1
--   学生:   student1
-- =====================================================

USE exam_system;

START TRANSACTION;

-- =====================================================
-- 用户数据
-- =====================================================
-- 密码: 123456 (BCrypt加密)

INSERT INTO users (id, username, password, nickname, avatar, role, status, created_at, deleted) VALUES
(1, 'admin', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '系统管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin&backgroundColor=b6e3f4', 'ADMIN', 'ACTIVE', '2024-09-01 08:00:00', 0),
(2, 'teacher1', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '张老师', 'https://api.dicebear.com/7.x/avataaars/svg?seed=teacher&backgroundColor=ffd5dc', 'TEACHER', 'ACTIVE', '2024-09-01 08:00:00', 0),
(3, 'student1', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '李同学', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-01 08:00:00', 0);

COMMIT;
