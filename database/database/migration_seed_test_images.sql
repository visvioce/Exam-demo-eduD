USE exam_system;

-- 先执行 migration_add_course_cover.sql，再执行本脚本

-- 头像：为空时补测试图
UPDATE users
SET avatar = CONCAT('https://picsum.photos/seed/avatar-', username, '/200/200')
WHERE avatar IS NULL OR avatar = '';

-- 课程封面：为空时补测试图
UPDATE courses
SET cover_url = CONCAT('https://picsum.photos/seed/course-', LOWER(code), '/800/450')
WHERE cover_url IS NULL OR cover_url = '';
