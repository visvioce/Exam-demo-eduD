-- =====================================================
-- 南方职业学院在线考试系统数据库初始化脚本
-- =====================================================
--
-- 说明：此文件仅包含表结构定义，不含示例数据
-- 示例数据请执行 data.sql
--
-- 最后更新：2026-02-14
-- =====================================================

DROP DATABASE IF EXISTS exam_system;
CREATE DATABASE exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE exam_system;

-- =====================================================
-- 核心表
-- =====================================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(100) NOT NULL COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL DEFAULT 'STUDENT' COMMENT '角色',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 课程表
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '课程代码',
    description TEXT COMMENT '课程描述',
    cover_url VARCHAR(500) COMMENT '课程封面URL',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
    credits DECIMAL(3,1) NOT NULL DEFAULT 1.0 COMMENT '学分',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deadline TIMESTAMP NULL COMMENT '课程截止时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    FOREIGN KEY (teacher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 3. 课程成员表（学生选课关联）
CREATE TABLE IF NOT EXISTS course_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_course_student (course_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程成员表';

-- 4. 题目表
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL COMMENT '题目内容',
    type ENUM('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE', 'FILL_BLANK', 'ESSAY') NOT NULL COMMENT '题目类型',
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL DEFAULT 'MEDIUM' COMMENT '难度',
    score DECIMAL(5,2) NOT NULL DEFAULT 1.0 COMMENT '分值',
    teacher_id BIGINT NOT NULL COMMENT '创建教师ID',
    subject VARCHAR(100) COMMENT '学科',
    options JSON COMMENT '选项（选择题）',
    correct_answer JSON COMMENT '正确答案',
    scoring_criteria JSON COMMENT '评分标准（简答题）',
    explanation TEXT COMMENT '答案解析',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    FOREIGN KEY (teacher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- 5. 试卷表
CREATE TABLE IF NOT EXISTS papers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '试卷名称',
    description TEXT COMMENT '试卷描述',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id BIGINT NOT NULL COMMENT '创建教师ID',
    questions JSON COMMENT '题目配置',
    total_score DECIMAL(5,2) NOT NULL DEFAULT 100.0 COMMENT '总分',
    type ENUM('MANUAL', 'AUTO') NOT NULL DEFAULT 'MANUAL' COMMENT '组卷方式：MANUAL手动组卷，AUTO自动组卷',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷表';

-- 6. 考试表
CREATE TABLE IF NOT EXISTS exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '考试标题',
    description TEXT COMMENT '考试描述',
    course_id BIGINT NOT NULL COMMENT '所属课程ID',
    paper_id BIGINT NULL COMMENT '关联试卷ID',
    teacher_id BIGINT NOT NULL COMMENT '创建教师ID',
    started_at TIMESTAMP NOT NULL COMMENT '开始时间',
    ended_at TIMESTAMP NOT NULL COMMENT '结束时间',
    duration INT NOT NULL COMMENT '考试时长(分钟)',
    total_score DECIMAL(5,2) NOT NULL DEFAULT 100.0 COMMENT '总分',
    pass_score DECIMAL(5,2) NOT NULL DEFAULT 60.0 COMMENT '及格分',
    status ENUM('DRAFT', 'PUBLISHED', 'STARTED', 'ENDED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (paper_id) REFERENCES papers(id),
    FOREIGN KEY (teacher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试表';

-- 7. 学生考试记录表
CREATE TABLE IF NOT EXISTS exam_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL COMMENT '考试ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    started_at TIMESTAMP NOT NULL COMMENT '开始时间',
    submitted_at TIMESTAMP NULL COMMENT '提交时间',
    score DECIMAL(5,2) NULL COMMENT '得分',
    total_score DECIMAL(5,2) NOT NULL COMMENT '总分',
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'SUBMITTED', 'GRADED') NOT NULL DEFAULT 'NOT_STARTED' COMMENT '状态',
    grading_status ENUM('PENDING', 'GRADING', 'COMPLETED') DEFAULT 'PENDING' COMMENT '评分状态',
    answers JSON COMMENT '答题记录',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    FOREIGN KEY (exam_id) REFERENCES exams(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    UNIQUE KEY uk_exam_student (exam_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生考试记录表';

-- 8. 公告表
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    type ENUM('SYSTEM', 'EXAM', 'COURSE') NOT NULL DEFAULT 'SYSTEM' COMMENT '公告类型',
    priority ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级',
    status ENUM('DRAFT', 'PUBLISHED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    publisher_id BIGINT NOT NULL COMMENT '发布者ID',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    FOREIGN KEY (publisher_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- 9. AI配置表
CREATE TABLE IF NOT EXISTS user_ai_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '配置名称',
    base_url VARCHAR(500) NOT NULL COMMENT 'API基础地址',
    api_key VARCHAR(500) NOT NULL COMMENT 'API密钥（加密存储）',
    models JSON NOT NULL COMMENT '模型列表（JSON数组）',
    active_model VARCHAR(100) COMMENT '当前激活的模型',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户AI配置表';

-- 10. 轮播图表
CREATE TABLE IF NOT EXISTS carousels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(500) COMMENT '跳转链接',
    description TEXT COMMENT '描述',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    start_at TIMESTAMP NULL COMMENT '展示开始时间',
    end_at TIMESTAMP NULL COMMENT '展示结束时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- =====================================================
-- 索引
-- =====================================================

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_courses_teacher ON courses(teacher_id);
CREATE INDEX idx_exams_course ON exams(course_id);
CREATE INDEX idx_exams_teacher ON exams(teacher_id);
CREATE INDEX idx_exams_status ON exams(status);
CREATE INDEX idx_questions_teacher ON questions(teacher_id);
CREATE INDEX idx_questions_type ON questions(type);
CREATE INDEX idx_exam_sessions_exam ON exam_sessions(exam_id);
CREATE INDEX idx_exam_sessions_student ON exam_sessions(student_id);
CREATE INDEX idx_exam_sessions_status ON exam_sessions(status);
CREATE INDEX idx_user_ai_configs_user ON user_ai_configs(user_id);
CREATE INDEX idx_user_ai_configs_active_model ON user_ai_configs(active_model);

-- =====================================================
-- 视图
-- =====================================================

-- 系统统计视图
CREATE OR REPLACE VIEW v_system_stats AS
SELECT
    (SELECT COUNT(*) FROM users WHERE deleted = 0) AS total_users,
    (SELECT COUNT(*) FROM users WHERE role = 'TEACHER' AND deleted = 0) AS total_teachers,
    (SELECT COUNT(*) FROM users WHERE role = 'STUDENT' AND deleted = 0) AS total_students,
    (SELECT COUNT(*) FROM courses WHERE deleted = 0) AS total_courses,
    (SELECT COUNT(*) FROM exams WHERE deleted = 0) AS total_exams,
    (SELECT COUNT(*) FROM questions WHERE deleted = 0) AS total_questions,
    (SELECT COUNT(*) FROM papers WHERE deleted = 0) AS total_papers,
    (SELECT COUNT(*) FROM exam_sessions) AS total_sessions,
    (SELECT COUNT(*) FROM announcements WHERE deleted = 0) AS total_announcements,
    CURRENT_TIMESTAMP AS last_updated;

-- 考试成绩统计视图
CREATE OR REPLACE VIEW v_exam_statistics AS
SELECT
    es.exam_id,
    e.title AS exam_title,
    c.name AS course_name,
    COUNT(*) AS total_participants,
    COUNT(es.score) AS graded_count,
    AVG(es.score) AS avg_score,
    MAX(es.score) AS max_score,
    MIN(es.score) AS min_score
FROM exam_sessions es
LEFT JOIN exams e ON es.exam_id = e.id
LEFT JOIN courses c ON e.course_id = c.id
GROUP BY es.exam_id, e.title, c.name;

-- 学生成绩排名视图
CREATE OR REPLACE VIEW v_student_rankings AS
SELECT
    es.exam_id,
    es.student_id,
    u.username,
    u.nickname,
    es.score,
    es.total_score,
    ROUND(es.score / es.total_score * 100, 2) AS percentage,
    RANK() OVER (PARTITION BY es.exam_id ORDER BY es.score DESC) AS rank_position
FROM exam_sessions es
JOIN users u ON es.student_id = u.id
WHERE es.status = 'GRADED';

-- 试卷信息视图
CREATE OR REPLACE VIEW v_paper_info AS
SELECT
    p.id AS paper_id,
    p.name AS paper_name,
    p.description,
    c.name AS course_name,
    u.nickname AS teacher_name,
    p.total_score,
    JSON_LENGTH(p.questions) AS question_count,
    p.type,
    p.status,
    p.created_at
FROM papers p
LEFT JOIN courses c ON p.course_id = c.id
LEFT JOIN users u ON p.teacher_id = u.id
WHERE p.deleted = 0;
