-- =====================================================
-- 南方职业学院在线考试系统 - 测试数据（可直接用于联调）
-- =====================================================
--
-- 说明：
-- 1) 先执行 init.sql，再执行本文件
-- 2) 保留用户表测试账号不变
-- 3) 本文件会重建用户表之外的测试数据（便于反复回归测试）
-- 4) 密码统一为：123456
-- =====================================================

USE exam_system;

START TRANSACTION;

-- =====================================================
-- 清理非用户表数据（保留 users）
-- =====================================================
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM exam_sessions;
DELETE FROM exams;
DELETE FROM papers;
DELETE FROM questions;
DELETE FROM course_members;
DELETE FROM announcements;
DELETE FROM user_ai_configs;
DELETE FROM carousels;
DELETE FROM courses;

ALTER TABLE courses AUTO_INCREMENT = 1;
ALTER TABLE questions AUTO_INCREMENT = 1;
ALTER TABLE papers AUTO_INCREMENT = 1;
ALTER TABLE exams AUTO_INCREMENT = 1;
ALTER TABLE exam_sessions AUTO_INCREMENT = 1;
ALTER TABLE announcements AUTO_INCREMENT = 1;
ALTER TABLE user_ai_configs AUTO_INCREMENT = 1;
ALTER TABLE carousels AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 用户数据（保持不变）
-- =====================================================

-- 管理员 (密码: 123456)
INSERT INTO users (username, password, nickname, role, status) VALUES
('admin', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '系统管理员', 'ADMIN', 'ACTIVE')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), role = VALUES(role), status = VALUES(status);

-- 教师 (密码: 123456)
INSERT INTO users (username, password, nickname, role, status) VALUES
('teacher1', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '张老师', 'TEACHER', 'ACTIVE'),
('teacher2', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '李老师', 'TEACHER', 'ACTIVE'),
('teacher3', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '王老师', 'TEACHER', 'ACTIVE')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), role = VALUES(role), status = VALUES(status);

-- 学生 (密码: 123456)
INSERT INTO users (username, password, nickname, role, status) VALUES
('student1', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '张三', 'STUDENT', 'ACTIVE'),
('student2', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '李四', 'STUDENT', 'ACTIVE'),
('student3', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '王五', 'STUDENT', 'ACTIVE'),
('student4', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '赵六', 'STUDENT', 'ACTIVE'),
('student5', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '钱七', 'STUDENT', 'ACTIVE'),
('student6', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '孙八', 'STUDENT', 'ACTIVE')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), role = VALUES(role), status = VALUES(status);

-- 设置头像（缺失时补齐）
UPDATE users
SET avatar = CONCAT('https://picsum.photos/seed/avatar-', username, '/200/200')
WHERE avatar IS NULL OR avatar = '';

-- 为后续插入准备用户ID变量（避免依赖固定主键）
SET @admin_id    = (SELECT id FROM users WHERE username = 'admin'    LIMIT 1);
SET @teacher1_id = (SELECT id FROM users WHERE username = 'teacher1' LIMIT 1);
SET @teacher2_id = (SELECT id FROM users WHERE username = 'teacher2' LIMIT 1);
SET @teacher3_id = (SELECT id FROM users WHERE username = 'teacher3' LIMIT 1);
SET @student1_id = (SELECT id FROM users WHERE username = 'student1' LIMIT 1);
SET @student2_id = (SELECT id FROM users WHERE username = 'student2' LIMIT 1);
SET @student3_id = (SELECT id FROM users WHERE username = 'student3' LIMIT 1);
SET @student4_id = (SELECT id FROM users WHERE username = 'student4' LIMIT 1);
SET @student5_id = (SELECT id FROM users WHERE username = 'student5' LIMIT 1);
SET @student6_id = (SELECT id FROM users WHERE username = 'student6' LIMIT 1);

-- =====================================================
-- 课程数据
-- =====================================================
INSERT INTO courses (name, code, description, cover_url, teacher_id, credits, status, deadline) VALUES
('计算机组成原理', 'CS101', '计算机组成原理是计算机专业核心基础课程。', 'https://picsum.photos/seed/course-cs101/800/450', @teacher1_id, 3.0, 'ACTIVE', DATE_ADD(NOW(), INTERVAL 45 DAY)),
('数据结构与算法', 'CS102', '数据结构与算法课程，覆盖线性结构、树图和经典算法。', 'https://picsum.photos/seed/course-cs102/800/450', @teacher1_id, 4.0, 'ACTIVE', DATE_ADD(NOW(), INTERVAL 30 DAY)),
('操作系统', 'CS103', '操作系统原理与实践。', 'https://picsum.photos/seed/course-cs103/800/450', @teacher2_id, 3.5, 'ACTIVE', DATE_ADD(NOW(), INTERVAL 60 DAY)),
('数据库系统概论', 'CS104', '关系数据库原理、SQL 与数据库设计。', 'https://picsum.photos/seed/course-cs104/800/450', @teacher2_id, 3.0, 'ACTIVE', DATE_ADD(NOW(), INTERVAL 20 DAY)),
('软件工程', 'CS105', '软件生命周期、需求、设计、测试与维护。', 'https://picsum.photos/seed/course-cs105/800/450', @teacher3_id, 2.5, 'ACTIVE', DATE_ADD(NOW(), INTERVAL 50 DAY)),
('计算机网络', 'CS106', '网络体系结构、协议栈、网络安全基础。', 'https://picsum.photos/seed/course-cs106/800/450', @teacher3_id, 3.0, 'ACTIVE', DATE_ADD(NOW(), INTERVAL 40 DAY));

-- 课程ID变量
SET @course_cs101 = (SELECT id FROM courses WHERE code = 'CS101' LIMIT 1);
SET @course_cs102 = (SELECT id FROM courses WHERE code = 'CS102' LIMIT 1);
SET @course_cs103 = (SELECT id FROM courses WHERE code = 'CS103' LIMIT 1);
SET @course_cs104 = (SELECT id FROM courses WHERE code = 'CS104' LIMIT 1);
SET @course_cs105 = (SELECT id FROM courses WHERE code = 'CS105' LIMIT 1);
SET @course_cs106 = (SELECT id FROM courses WHERE code = 'CS106' LIMIT 1);

-- =====================================================
-- 学生选课数据
-- =====================================================
INSERT INTO course_members (course_id, student_id, joined_at) VALUES
(@course_cs101, @student1_id, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(@course_cs102, @student1_id, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(@course_cs103, @student1_id, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(@course_cs104, @student1_id, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(@course_cs105, @student1_id, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(@course_cs106, @student1_id, DATE_SUB(NOW(), INTERVAL 20 DAY)),

(@course_cs101, @student2_id, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(@course_cs102, @student2_id, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(@course_cs103, @student2_id, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(@course_cs104, @student2_id, DATE_SUB(NOW(), INTERVAL 18 DAY)),

(@course_cs101, @student3_id, DATE_SUB(NOW(), INTERVAL 16 DAY)),
(@course_cs102, @student3_id, DATE_SUB(NOW(), INTERVAL 16 DAY)),
(@course_cs105, @student3_id, DATE_SUB(NOW(), INTERVAL 16 DAY)),

(@course_cs102, @student4_id, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(@course_cs103, @student4_id, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(@course_cs104, @student4_id, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(@course_cs106, @student4_id, DATE_SUB(NOW(), INTERVAL 14 DAY)),

(@course_cs101, @student5_id, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(@course_cs102, @student5_id, DATE_SUB(NOW(), INTERVAL 12 DAY)),

(@course_cs103, @student6_id, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(@course_cs104, @student6_id, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(@course_cs106, @student6_id, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- =====================================================
-- 题目数据（JSON 字段统一）
-- =====================================================
INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('计算机的基本组成部分包括？', 'SINGLE_CHOICE', 'EASY', 2.0, @teacher1_id, '计算机组成原理',
 '[{"id":"A","text":"运算器、控制器、存储器、输入设备、输出设备"},{"id":"B","text":"CPU、内存、硬盘、显示器"},{"id":"C","text":"键盘、鼠标、显示器、打印机"},{"id":"D","text":"主板、电源、显卡、声卡"}]',
 '"A"', '冯·诺依曼结构由五大部件组成。'),

('CPU主要由哪两部分组成？', 'SINGLE_CHOICE', 'EASY', 2.0, @teacher1_id, '计算机组成原理',
 '[{"id":"A","text":"运算器和存储器"},{"id":"B","text":"运算器和控制器"},{"id":"C","text":"控制器和存储器"},{"id":"D","text":"运算器和输入设备"}]',
 '"B"', 'CPU主要由运算器和控制器组成。'),

('下列关于Cache的说法正确的是？', 'SINGLE_CHOICE', 'MEDIUM', 2.0, @teacher1_id, '计算机组成原理',
 '[{"id":"A","text":"Cache容量越大越好"},{"id":"B","text":"Cache用于缓解CPU与主存速度不匹配"},{"id":"C","text":"Cache可以完全替代主存"},{"id":"D","text":"Cache只能存储指令"}]',
 '"B"', 'Cache用于缓解CPU与主存速度差距。'),

('以下哪些属于计算机的性能指标？', 'MULTIPLE_CHOICE', 'MEDIUM', 3.0, @teacher1_id, '计算机组成原理',
 '[{"id":"A","text":"主频"},{"id":"B","text":"字长"},{"id":"C","text":"存储容量"},{"id":"D","text":"颜色"}]',
 '["A","B","C"]', '主频、字长、容量等是性能指标。'),

('二进制数1011转换为十进制是11。', 'TRUE_FALSE', 'EASY', 1.0, @teacher1_id, '计算机组成原理',
 NULL, 'true', '1011(2)=11(10)。'),

('CPU可以直接访问硬盘中的数据。', 'TRUE_FALSE', 'EASY', 1.0, @teacher1_id, '计算机组成原理',
 NULL, 'false', '必须先经由主存。'),

('计算机中，1KB等于____字节。', 'FILL_BLANK', 'EASY', 2.0, @teacher1_id, '计算机组成原理',
 NULL, '["1024"]', '1KB=1024B。'),

('存储器层次从快到慢：寄存器、____、主存、辅存。', 'FILL_BLANK', 'MEDIUM', 2.0, @teacher1_id, '计算机组成原理',
 NULL, '["Cache","高速缓存","缓存"]', '寄存器→Cache→主存→辅存。'),

('栈和队列的共同特点是？', 'SINGLE_CHOICE', 'EASY', 2.0, @teacher1_id, '数据结构',
 '[{"id":"A","text":"都是先进先出"},{"id":"B","text":"都是先进后出"},{"id":"C","text":"都只允许在端点处插入删除"},{"id":"D","text":"没有共同点"}]',
 '"C"', '都在端点处进行受限操作。'),

('栈是一种____数据结构。', 'FILL_BLANK', 'EASY', 2.0, @teacher1_id, '数据结构',
 NULL, '["后进先出","LIFO","先进后出","FILO"]', '栈是LIFO结构。'),

('以下哪些是线性结构？', 'MULTIPLE_CHOICE', 'MEDIUM', 3.0, @teacher1_id, '数据结构',
 '[{"id":"A","text":"栈"},{"id":"B","text":"队列"},{"id":"C","text":"数组"},{"id":"D","text":"树"}]',
 '["A","B","C"]', '树是非线性结构。'),

('队列是一种先进先出(FIFO)的数据结构。', 'TRUE_FALSE', 'EASY', 1.0, @teacher1_id, '数据结构',
 NULL, 'true', '队列是FIFO结构。'),

('操作系统的主要功能包括？', 'SINGLE_CHOICE', 'MEDIUM', 2.0, @teacher2_id, '操作系统',
 '[{"id":"A","text":"进程管理、内存管理、文件管理、设备管理"},{"id":"B","text":"只管理硬件"},{"id":"C","text":"只管理软件"},{"id":"D","text":"只管理内存"}]',
 '"A"', '操作系统核心是资源管理。'),

('进程的三种基本状态是？', 'MULTIPLE_CHOICE', 'MEDIUM', 3.0, @teacher2_id, '操作系统',
 '[{"id":"A","text":"就绪态"},{"id":"B","text":"运行态"},{"id":"C","text":"阻塞态"},{"id":"D","text":"死亡态"}]',
 '["A","B","C"]', '基本状态为就绪、运行、阻塞。'),

('死锁是多个进程因争夺资源而互相等待的现象。', 'TRUE_FALSE', 'MEDIUM', 1.0, @teacher2_id, '操作系统',
 NULL, 'true', '这是死锁定义。'),

('SQL中用于删除表的命令是？', 'SINGLE_CHOICE', 'EASY', 2.0, @teacher2_id, '数据库',
 '[{"id":"A","text":"DELETE TABLE"},{"id":"B","text":"DROP TABLE"},{"id":"C","text":"REMOVE TABLE"},{"id":"D","text":"TRUNCATE TABLE"}]',
 '"B"', 'DROP TABLE 删除表结构和数据。'),

('软件生命周期包括哪些阶段？', 'MULTIPLE_CHOICE', 'MEDIUM', 3.0, @teacher3_id, '软件工程',
 '[{"id":"A","text":"需求分析"},{"id":"B","text":"设计"},{"id":"C","text":"编码"},{"id":"D","text":"测试"}]',
 '["A","B","C","D"]', '生命周期通常包括需求、设计、开发、测试、维护。'),

('请简述什么是软件危机及其主要表现。', 'ESSAY', 'HARD', 10.0, @teacher3_id, '软件工程',
 NULL,
 NULL,
 '从成本、质量、进度、维护角度论述软件危机。');

-- 题目ID变量
SET @q1  = 1;
SET @q2  = 2;
SET @q3  = 3;
SET @q4  = 4;
SET @q5  = 5;
SET @q6  = 6;
SET @q7  = 7;
SET @q8  = 8;
SET @q9  = 9;
SET @q10 = 10;
SET @q11 = 11;
SET @q12 = 12;
SET @q13 = 13;
SET @q14 = 14;
SET @q15 = 15;
SET @q16 = 16;
SET @q17 = 17;
SET @q18 = 18;

-- 简答题评分标准单独更新（可读性更高）
UPDATE questions
SET scoring_criteria = '[{"point":"给出软件危机定义","score":3},{"point":"说明成本与进度失控","score":3},{"point":"说明质量问题","score":2},{"point":"说明维护困难","score":2}]'
WHERE id = @q18;

-- =====================================================
-- 试卷数据（questions JSON 统一使用 questionId）
-- =====================================================
INSERT INTO papers (name, description, course_id, teacher_id, questions, total_score, type, status) VALUES
('计算机组成原理期末试卷', '覆盖组成原理核心知识点。', @course_cs101, @teacher1_id,
 '[{"questionId":1,"score":10},{"questionId":2,"score":10},{"questionId":3,"score":10},{"questionId":4,"score":15},{"questionId":5,"score":5},{"questionId":6,"score":5},{"questionId":7,"score":10},{"questionId":8,"score":15}]',
 80.0, 'MANUAL', 'PUBLISHED'),

('数据结构期中试卷', '覆盖线性表、栈队列、基础判断。', @course_cs102, @teacher1_id,
 '[{"questionId":9,"score":10},{"questionId":10,"score":10},{"questionId":11,"score":15},{"questionId":12,"score":5}]',
 40.0, 'MANUAL', 'PUBLISHED'),

('操作系统综合测试', '操作系统核心概念测验。', @course_cs103, @teacher2_id,
 '[{"questionId":13,"score":10},{"questionId":14,"score":15},{"questionId":15,"score":5}]',
 30.0, 'MANUAL', 'PUBLISHED'),

('软件工程主观题测试', '用于演示主观题评分流程。', @course_cs105, @teacher3_id,
 '[{"questionId":18,"score":10}]',
 10.0, 'MANUAL', 'PUBLISHED');

SET @paper1 = (SELECT id FROM papers WHERE name = '计算机组成原理期末试卷' LIMIT 1);
SET @paper2 = (SELECT id FROM papers WHERE name = '数据结构期中试卷' LIMIT 1);
SET @paper3 = (SELECT id FROM papers WHERE name = '操作系统综合测试' LIMIT 1);
SET @paper4 = (SELECT id FROM papers WHERE name = '软件工程主观题测试' LIMIT 1);

-- =====================================================
-- 考试数据（时间与状态一致）
-- =====================================================
INSERT INTO exams (title, description, course_id, paper_id, teacher_id, started_at, ended_at, duration, total_score, pass_score, status) VALUES
('计算机组成原理期末考试', '进行中的考试，用于测试答题与自动保存。', @course_cs101, @paper1, @teacher1_id,
 DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_ADD(NOW(), INTERVAL 90 MINUTE), 120, 80.0, 48.0, 'STARTED'),

('数据结构期中考试', '已结束考试，用于测试成绩统计与考试回顾。', @course_cs102, @paper2, @teacher1_id,
 DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 1 HOUR, 60, 40.0, 24.0, 'ENDED'),

('操作系统期末考试', '未开始考试，用于测试发布态与倒计时。', @course_cs103, @paper3, @teacher2_id,
 DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY) + INTERVAL 45 MINUTE, 45, 30.0, 18.0, 'PUBLISHED'),

('软件工程主观题测验', '已提交待评分，用于测试教师评分流程。', @course_cs105, @paper4, @teacher3_id,
 DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 2 HOUR, 120, 10.0, 6.0, 'ENDED');

SET @exam1 = (SELECT id FROM exams WHERE title = '计算机组成原理期末考试' LIMIT 1);
SET @exam2 = (SELECT id FROM exams WHERE title = '数据结构期中考试' LIMIT 1);
SET @exam3 = (SELECT id FROM exams WHERE title = '操作系统期末考试' LIMIT 1);
SET @exam4 = (SELECT id FROM exams WHERE title = '软件工程主观题测验' LIMIT 1);

-- =====================================================
-- 考试记录（answers JSON 统一使用 questionId / questionType）
-- 说明：answer 字段统一存字符串；多选题为 JSON 字符串（如 "[\"A\",\"B\"]"）
-- =====================================================
INSERT INTO exam_sessions (exam_id, student_id, started_at, submitted_at, score, total_score, status, grading_status, answers) VALUES
(@exam2, @student1_id,
 DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 40 MINUTE,
 38.0, 40.0, 'GRADED', 'COMPLETED',
 '[{"questionId":9,"answer":"C","questionType":"SINGLE_CHOICE","isCorrect":true,"score":10,"gradingStatus":"GRADED"},{"questionId":10,"answer":"后进先出","questionType":"FILL_BLANK","isCorrect":true,"score":10,"gradingStatus":"GRADED"},{"questionId":11,"answer":"[\\\"A\\\",\\\"B\\\",\\\"C\\\"]","questionType":"MULTIPLE_CHOICE","isCorrect":true,"score":15,"gradingStatus":"GRADED"},{"questionId":12,"answer":"true","questionType":"TRUE_FALSE","isCorrect":true,"score":3,"gradingStatus":"GRADED"}]'),

(@exam2, @student2_id,
 DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 43 MINUTE,
 32.0, 40.0, 'GRADED', 'COMPLETED',
 '[{"questionId":9,"answer":"C","questionType":"SINGLE_CHOICE","isCorrect":true,"score":10,"gradingStatus":"GRADED"},{"questionId":10,"answer":"LIFO","questionType":"FILL_BLANK","isCorrect":true,"score":10,"gradingStatus":"GRADED"},{"questionId":11,"answer":"[\\\"A\\\",\\\"B\\\"]","questionType":"MULTIPLE_CHOICE","isCorrect":false,"score":8,"gradingStatus":"GRADED"},{"questionId":12,"answer":"true","questionType":"TRUE_FALSE","isCorrect":true,"score":4,"gradingStatus":"GRADED"}]'),

(@exam2, @student3_id,
 DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY) + INTERVAL 37 MINUTE,
 25.0, 40.0, 'GRADED', 'COMPLETED',
 '[{"questionId":9,"answer":"A","questionType":"SINGLE_CHOICE","isCorrect":false,"score":0,"gradingStatus":"GRADED"},{"questionId":10,"answer":"后进先出","questionType":"FILL_BLANK","isCorrect":true,"score":10,"gradingStatus":"GRADED"},{"questionId":11,"answer":"[\\\"A\\\",\\\"B\\\",\\\"C\\\"]","questionType":"MULTIPLE_CHOICE","isCorrect":true,"score":15,"gradingStatus":"GRADED"},{"questionId":12,"answer":"false","questionType":"TRUE_FALSE","isCorrect":false,"score":0,"gradingStatus":"GRADED"}]'),

(@exam4, @student1_id,
 DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 65 MINUTE,
 0.0, 10.0, 'SUBMITTED', 'PENDING',
 '[{"questionId":18,"answer":"软件危机是软件开发维护中出现的成本高、进度慢、质量差、维护难等系统性问题。","questionType":"ESSAY","isCorrect":null,"score":null,"gradingStatus":"PENDING","teacherComment":null}]');

-- =====================================================
-- AI 配置数据（models JSON + activeModel 对齐）
-- =====================================================
INSERT INTO user_ai_configs (user_id, name, base_url, api_key, models, active_model) VALUES
(@teacher1_id, '我的通义千问', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'TEST_DASHSCOPE_API_KEY',
 '["qwen-plus","qwen-turbo","qwen-max"]', 'qwen-plus'),
(@teacher2_id, 'DeepSeek配置', 'https://api.deepseek.com/v1', 'TEST_DEEPSEEK_API_KEY',
 '["deepseek-chat","deepseek-coder"]', 'deepseek-chat'),
(@teacher3_id, '智谱AI配置', 'https://open.bigmodel.cn/api/paas/v4', 'TEST_ZHIPU_API_KEY',
 '["glm-4","glm-4-flash","glm-4-plus"]', 'glm-4');

-- =====================================================
-- 公告数据
-- =====================================================
INSERT INTO announcements (title, content, type, priority, status, publisher_id, published_at) VALUES
('欢迎使用在线考试系统', '欢迎使用南方职业学院在线考试系统！支持在线考试、自动阅卷、成绩统计。', 'SYSTEM', 'HIGH', 'PUBLISHED', @admin_id, DATE_SUB(NOW(), INTERVAL 5 DAY)),
('计算机组成原理期末考试通知', '计算机组成原理期末考试正在进行，请按时参加并提交。', 'EXAM', 'HIGH', 'PUBLISHED', @teacher1_id, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('数据结构期中考试成绩公布', '数据结构期中考试成绩已发布，请同学们查看考试结果与解析。', 'EXAM', 'MEDIUM', 'PUBLISHED', @teacher1_id, DATE_SUB(NOW(), INTERVAL 9 DAY)),
('系统升级维护通知', '本周末凌晨将进行系统维护，请提前安排考试计划。', 'SYSTEM', 'MEDIUM', 'PUBLISHED', @admin_id, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('软件工程作业提醒', '软件工程课程阶段作业截止时间为下周三 23:59。', 'COURSE', 'LOW', 'PUBLISHED', @teacher3_id, DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- =====================================================
-- 轮播图数据（补齐 start_at / end_at）
-- =====================================================
INSERT INTO carousels (title, image_url, link_url, description, sort_order, status, start_at, end_at) VALUES
('在线考试系统上线', 'https://picsum.photos/seed/carousel-1/1200/400', '/', '南方职业学院在线考试系统正式上线。', 1, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 120 DAY)),
('欢迎使用AI出题功能', 'https://picsum.photos/seed/carousel-2/1200/400', '/aiconfig', 'AI智能出题功能已开放，支持多模型切换。', 2, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY)),
('课程与考试中心', 'https://picsum.photos/seed/carousel-3/1200/400', '/course', '查看课程、加入课程并参与考试。', 3, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 60 DAY));

COMMIT;

-- =====================================================
-- 快速验证建议（可选）
-- SELECT COUNT(*) FROM courses;
-- SELECT COUNT(*) FROM questions;
-- SELECT COUNT(*) FROM papers;
-- SELECT COUNT(*) FROM exams;
-- SELECT COUNT(*) FROM exam_sessions;
-- =====================================================
