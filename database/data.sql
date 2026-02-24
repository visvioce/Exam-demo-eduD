-- =====================================================
-- 南方学院在线考试系统 - 测试数据
-- =====================================================
--
-- 说明：此文件包含完整的测试数据，可体验系统全部功能
-- 密码统一为：123456
--
-- 执行顺序：先执行 init.sql，再执行此文件
-- =====================================================

USE exam_system;

-- =====================================================
-- 用户数据
-- =====================================================

-- 管理员 (密码: 123456)
INSERT INTO users (username, password, nickname, role, status) VALUES
('admin', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '系统管理员', 'ADMIN', 'ACTIVE');

-- 教师 (密码: 123456)
INSERT INTO users (username, password, nickname, role, status) VALUES
('teacher1', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '张老师', 'TEACHER', 'ACTIVE'),
('teacher2', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '李老师', 'TEACHER', 'ACTIVE'),
('teacher3', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '王老师', 'TEACHER', 'ACTIVE');

-- 学生 (密码: 123456)
INSERT INTO users (username, password, nickname, role, status) VALUES
('student1', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '张三', 'STUDENT', 'ACTIVE'),
('student2', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '李四', 'STUDENT', 'ACTIVE'),
('student3', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '王五', 'STUDENT', 'ACTIVE'),
('student4', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '赵六', 'STUDENT', 'ACTIVE'),
('student5', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '钱七', 'STUDENT', 'ACTIVE'),
('student6', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '孙八', 'STUDENT', 'ACTIVE');

-- =====================================================
-- 课程数据
-- =====================================================

INSERT INTO courses (name, code, description, teacher_id, credits, status) VALUES
('计算机组成原理', 'CS101', '计算机组成原理是计算机专业的核心基础课程，主要讲解计算机硬件系统的基本组成和工作原理。', 2, 3.0, 'ACTIVE'),
('数据结构与算法', 'CS102', '数据结构课程介绍各种数据结构的组织、管理和存储格式，以及常用算法的设计与分析。', 2, 4.0, 'ACTIVE'),
('操作系统', 'CS103', '操作系统课程介绍计算机操作系统的基本概念、原理和设计方法。', 3, 3.5, 'ACTIVE'),
('数据库系统概论', 'CS104', '数据库系统课程介绍关系数据库的基本概念、SQL语言和数据库设计方法。', 3, 3.0, 'ACTIVE'),
('软件工程', 'CS105', '软件工程课程介绍软件开发的方法论、过程管理和质量保证。', 4, 2.5, 'ACTIVE'),
('计算机网络', 'CS106', '计算机网络课程介绍网络体系结构、协议和网络安全知识。', 4, 3.0, 'ACTIVE');

-- =====================================================
-- 学生选课数据
-- =====================================================

-- 张三选了所有课
INSERT INTO course_members (course_id, student_id) VALUES
(1, 5), (2, 5), (3, 5), (4, 5), (5, 5), (6, 5);
-- 李四选了4门课
INSERT INTO course_members (course_id, student_id) VALUES
(1, 6), (2, 6), (3, 6), (4, 6);
-- 王五选了3门课
INSERT INTO course_members (course_id, student_id) VALUES
(1, 7), (2, 7), (5, 7);
-- 赵六选了4门课
INSERT INTO course_members (course_id, student_id) VALUES
(2, 8), (3, 8), (4, 8), (6, 8);
-- 钱七选了2门课
INSERT INTO course_members (course_id, student_id) VALUES
(1, 9), (2, 9);
-- 孙八选了3门课
INSERT INTO course_members (course_id, student_id) VALUES
(3, 10), (4, 10), (6, 10);

-- =====================================================
-- 题目数据（涵盖所有题型）
-- =====================================================

-- ===== 计算机组成原理题目 =====

-- 单选题
INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('计算机的基本组成部分包括？', 'SINGLE_CHOICE', 'EASY', 2.0, 2, '计算机组成原理',
 '[{"id":"A","text":"运算器、控制器、存储器、输入设备、输出设备"},{"id":"B","text":"CPU、内存、硬盘、显示器"},{"id":"C","text":"键盘、鼠标、显示器、打印机"},{"id":"D","text":"主板、电源、显卡、声卡"}]',
 '"A"', '冯·诺依曼计算机由运算器、控制器、存储器、输入设备和输出设备五大部件组成。');

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('CPU主要由哪两部分组成？', 'SINGLE_CHOICE', 'EASY', 2.0, 2, '计算机组成原理',
 '[{"id":"A","text":"运算器和存储器"},{"id":"B","text":"运算器和控制器"},{"id":"C","text":"控制器和存储器"},{"id":"D","text":"运算器和输入设备"}]',
 '"B"', 'CPU（中央处理器）主要由运算器和控制器两部分组成。');

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('下列关于Cache的说法正确的是？', 'SINGLE_CHOICE', 'MEDIUM', 2.0, 2, '计算机组成原理',
 '[{"id":"A","text":"Cache容量越大越好"},{"id":"B","text":"Cache是为了解决CPU与主存速度不匹配的问题"},{"id":"C","text":"Cache可以完全替代主存"},{"id":"D","text":"Cache只能存储指令"}]',
 '"B"', 'Cache（高速缓存）的引入是为了解决CPU速度快而主存速度慢的矛盾。');

-- 多选题
INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('以下哪些属于计算机的性能指标？', 'MULTIPLE_CHOICE', 'MEDIUM', 3.0, 2, '计算机组成原理',
 '[{"id":"A","text":"主频"},{"id":"B","text":"字长"},{"id":"C","text":"存储容量"},{"id":"D","text":"颜色"}]',
 '["A","B","C"]', '计算机的主要性能指标包括：主频、字长、存储容量、运算速度等。');

-- 判断题
INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, correct_answer, explanation) VALUES
('二进制数1011转换为十进制是11。', 'TRUE_FALSE', 'EASY', 1.0, 2, '计算机组成原理',
 'true', '1×2³ + 0×2² + 1×2¹ + 1×2⁰ = 8 + 0 + 2 + 1 = 11');

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, correct_answer, explanation) VALUES
('CPU可以直接访问硬盘中的数据。', 'TRUE_FALSE', 'EASY', 1.0, 2, '计算机组成原理',
 'false', 'CPU不能直接访问外存（硬盘），必须先将数据调入内存才能被CPU访问。');

-- 填空题
INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, correct_answer, explanation) VALUES
('计算机中，1KB等于____字节。', 'FILL_BLANK', 'EASY', 2.0, 2, '计算机组成原理',
 '["1024"]', '1KB = 1024B = 2¹⁰字节');

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, correct_answer, explanation) VALUES
('存储器的层次结构从快到慢依次为：寄存器、____、主存、辅存。', 'FILL_BLANK', 'MEDIUM', 2.0, 2, '计算机组成原理',
 '["Cache","高速缓存","缓存"]', '存储器层次结构：寄存器 → Cache → 主存 → 辅存');

-- ===== 数据结构题目 =====

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('栈和队列的共同特点是？', 'SINGLE_CHOICE', 'EASY', 2.0, 2, '数据结构',
 '[{"id":"A","text":"都是先进先出"},{"id":"B","text":"都是先进后出"},{"id":"C","text":"只允许在端点处插入和删除元素"},{"id":"D","text":"没有共同点"}]',
 '"C"', '栈和队列都是线性表，都只允许在端点处进行插入和删除操作。');

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, correct_answer, explanation) VALUES
('栈是一种____数据结构。', 'FILL_BLANK', 'EASY', 2.0, 2, '数据结构',
 '["后进先出","LIFO","先进后出","FILO"]', '栈是一种后进先出(LIFO, Last In First Out)的数据结构。');

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('以下哪些是线性结构？', 'MULTIPLE_CHOICE', 'MEDIUM', 3.0, 2, '数据结构',
 '[{"id":"A","text":"栈"},{"id":"B","text":"队列"},{"id":"C","text":"数组"},{"id":"D","text":"树"}]',
 '["A","B","C"]', '栈、队列、数组、链表都是线性结构，树是非线性结构。');

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, correct_answer, explanation) VALUES
('队列是一种先进先出(FIFO)的数据结构。', 'TRUE_FALSE', 'EASY', 1.0, 2, '数据结构',
 'true', '队列是一种先进先出(FIFO, First In First Out)的数据结构。');

-- ===== 操作系统题目 =====

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('操作系统的主要功能包括？', 'SINGLE_CHOICE', 'MEDIUM', 2.0, 3, '操作系统',
 '[{"id":"A","text":"进程管理、内存管理、文件管理、设备管理"},{"id":"B","text":"只管理硬件"},{"id":"C","text":"只管理软件"},{"id":"D","text":"只管理内存"}]',
 '"A"', '操作系统的主要功能包括：进程管理、内存管理、文件管理、设备管理。');

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('进程的三种基本状态是？', 'MULTIPLE_CHOICE', 'MEDIUM', 3.0, 3, '操作系统',
 '[{"id":"A","text":"就绪态"},{"id":"B","text":"运行态"},{"id":"C","text":"阻塞态"},{"id":"D","text":"死亡态"}]',
 '["A","B","C"]', '进程的三种基本状态是：就绪态、运行态、阻塞态。');

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, correct_answer, explanation) VALUES
('死锁是指两个或两个以上的进程在执行过程中，因争夺资源而造成的一种互相等待的现象。', 'TRUE_FALSE', 'MEDIUM', 1.0, 3, '操作系统',
 'true', '死锁是指多个进程因竞争资源而造成的一种僵局，若无外力作用，这些进程都将无法向前推进。');

-- ===== 数据库题目 =====

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('SQL中用于删除表的命令是？', 'SINGLE_CHOICE', 'EASY', 2.0, 3, '数据库',
 '[{"id":"A","text":"DELETE TABLE"},{"id":"B","text":"DROP TABLE"},{"id":"C","text":"REMOVE TABLE"},{"id":"D","text":"TRUNCATE TABLE"}]',
 '"B"', 'DROP TABLE 用于删除整个表结构和数据；DELETE 用于删除数据但保留表结构。');

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('关系数据库的三大范式包括？', 'MULTIPLE_CHOICE', 'HARD', 3.0, 3, '数据库',
 '[{"id":"A","text":"第一范式（属性不可再分）"},{"id":"B","text":"第二范式（消除非主属性对码的部分依赖）"},{"id":"C","text":"第三范式（消除非主属性对码的传递依赖）"},{"id":"D","text":"第四范式（消除多值依赖）"}]',
 '["A","B","C"]', '关系数据库的三大范式是：1NF、2NF、3NF。');

-- ===== 软件工程题目 =====

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('软件生命周期包括哪些阶段？', 'MULTIPLE_CHOICE', 'MEDIUM', 3.0, 4, '软件工程',
 '[{"id":"A","text":"需求分析"},{"id":"B","text":"设计"},{"id":"C","text":"编码"},{"id":"D","text":"测试"}]',
 '["A","B","C","D"]', '软件生命周期包括：需求分析、设计、编码、测试、维护等阶段。');

-- 简答题
INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, scoring_criteria, explanation) VALUES
('请简述什么是软件危机，以及其主要表现。', 'ESSAY', 'HARD', 10.0, 4, '软件工程',
 '[{"point":"软件危机的定义","score":3},{"point":"开发成本和进度难以控制","score":2},{"point":"软件质量难以保证","score":2},{"point":"软件维护困难","score":3}]',
 '软件危机是指在计算机软件的开发和维护过程中所遇到的一系列严重问题。主要表现为：开发成本和进度难以估计和控制，软件质量难以保证，软件的可维护性差等。');

-- ===== 计算机网络题目 =====

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, options, correct_answer, explanation) VALUES
('OSI参考模型共有几层？', 'SINGLE_CHOICE', 'EASY', 2.0, 4, '计算机网络',
 '[{"id":"A","text":"4层"},{"id":"B","text":"5层"},{"id":"C","text":"6层"},{"id":"D","text":"7层"}]',
 '"D"', 'OSI参考模型分为7层：物理层、数据链路层、网络层、传输层、会话层、表示层、应用层。');

INSERT INTO questions (content, type, difficulty, score, teacher_id, subject, correct_answer, explanation) VALUES
('TCP协议是一种面向连接的、可靠的传输层协议。', 'TRUE_FALSE', 'EASY', 1.0, 4, '计算机网络',
 'true', 'TCP是面向连接的、可靠的传输层协议；UDP是无连接的、不可靠的传输层协议。');

-- =====================================================
-- 试卷数据
-- =====================================================

INSERT INTO papers (name, description, course_id, teacher_id, questions, total_score, type, status) VALUES
('计算机组成原理期末试卷', '计算机组成原理课程期末考试试卷，涵盖基础知识与综合应用。', 1, 2,
 '[{"question_id":1,"score":10},{"question_id":2,"score":10},{"question_id":3,"score":10},{"question_id":4,"score":15},{"question_id":5,"score":5},{"question_id":6,"score":5},{"question_id":7,"score":10},{"question_id":8,"score":15}]',
 80, 'MANUAL', 'PUBLISHED');

INSERT INTO papers (name, description, course_id, teacher_id, questions, total_score, type, status) VALUES
('数据结构期中试卷', '数据结构课程期中考试试卷。', 2, 2,
 '[{"question_id":9,"score":10},{"question_id":10,"score":10},{"question_id":11,"score":15},{"question_id":12,"score":5}]',
 40, 'MANUAL', 'PUBLISHED');

INSERT INTO papers (name, description, course_id, teacher_id, questions, total_score, type, status) VALUES
('操作系统综合测试', '操作系统课程综合测试试卷。', 3, 3,
 '[{"question_id":13,"score":10},{"question_id":14,"score":15},{"question_id":15,"score":5}]',
 30, 'MANUAL', 'PUBLISHED');

-- =====================================================
-- 考试数据
-- =====================================================

-- 正在进行的考试
INSERT INTO exams (title, description, course_id, paper_id, teacher_id, started_at, ended_at, duration, total_score, pass_score, status) VALUES
('计算机组成原理期末考试', '2026年春季学期期末考试', 1, 1, 2,
 DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY), 90, 80.0, 48.0, 'STARTED');

-- 已结束的考试
INSERT INTO exams (title, description, course_id, paper_id, teacher_id, started_at, ended_at, duration, total_score, pass_score, status) VALUES
('数据结构期中考试', '2026年春季学期期中考试', 2, 2, 2,
 DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), 60, 40.0, 24.0, 'ENDED');

-- 未开始的考试
INSERT INTO exams (title, description, course_id, paper_id, teacher_id, started_at, ended_at, duration, total_score, pass_score, status) VALUES
('操作系统期末考试', '2026年春季学期期末考试', 3, 3, 3,
 DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 45, 30.0, 18.0, 'PUBLISHED');

-- =====================================================
-- 考试记录数据（用于测试成绩统计）
-- =====================================================

-- 数据结构期中考试的答题记录
INSERT INTO exam_sessions (exam_id, student_id, started_at, submitted_at, score, total_score, status, grading_status, answers) VALUES
(2, 5, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), 38.0, 40.0, 'GRADED', 'COMPLETED',
 '[{"question_id":9,"answer":"C","is_correct":true,"score":10},{"question_id":10,"answer":"后进先出","is_correct":true,"score":10},{"question_id":11,"answer":["A","B","C"],"is_correct":true,"score":15},{"question_id":12,"answer":"true","is_correct":true,"score":3}]'),
(2, 6, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), 32.0, 40.0, 'GRADED', 'COMPLETED',
 '[{"question_id":9,"answer":"C","is_correct":true,"score":10},{"question_id":10,"answer":"LIFO","is_correct":true,"score":10},{"question_id":11,"answer":["A","B"],"is_correct":false,"score":8},{"question_id":12,"answer":"true","is_correct":true,"score":4}]'),
(2, 7, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), 25.0, 40.0, 'GRADED', 'COMPLETED',
 '[{"question_id":9,"answer":"A","is_correct":false,"score":0},{"question_id":10,"answer":"后进先出","is_correct":true,"score":10},{"question_id":11,"answer":["A","B","C"],"is_correct":true,"score":15},{"question_id":12,"answer":"false","is_correct":false,"score":0}]'),
(2, 8, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), 35.0, 40.0, 'GRADED', 'COMPLETED',
 '[{"question_id":9,"answer":"C","is_correct":true,"score":10},{"question_id":10,"answer":"先进后出","is_correct":true,"score":10},{"question_id":11,"answer":["A","B","C"],"is_correct":true,"score":15},{"question_id":12,"answer":"true","is_correct":false,"score":0}]');

-- =====================================================
-- AI配置数据（重要：用于AI出题功能）
-- =====================================================

-- 为教师配置AI（密码: 123456 对应的加密key需要替换为实际的）
-- 注意：api_key 字段需要使用后端的 AES 工具加密后存储
-- 这里使用明文存储，后端会自动处理加密解密

-- 张老师的通义千问配置
INSERT INTO user_ai_configs (user_id, name, base_url, api_key, models, active_model) VALUES
(2, '我的通义千问', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'YOUR_DASHSCOPE_API_KEY',
 '["qwen-plus", "qwen-turbo", "qwen-max"]', 'qwen-plus');

-- 李老师的DeepSeek配置
INSERT INTO user_ai_configs (user_id, name, base_url, api_key, models, active_model) VALUES
(3, 'DeepSeek配置', 'https://api.deepseek.com/v1', 'YOUR_DEEPSEEK_API_KEY',
 '["deepseek-chat", "deepseek-coder"]', 'deepseek-chat');

-- 王老师的智谱AI配置
INSERT INTO user_ai_configs (user_id, name, base_url, api_key, models, active_model) VALUES
(4, '智谱AI配置', 'https://open.bigmodel.cn/api/paas/v4', 'YOUR_ZHIPU_API_KEY',
 '["glm-4", "glm-4-flash", "glm-4-plus"]', 'glm-4');

-- =====================================================
-- 公告数据
-- =====================================================

INSERT INTO announcements (title, content, type, priority, status, publisher_id, published_at) VALUES
('欢迎使用在线考试系统', '欢迎使用南方学院在线考试系统！本系统支持在线考试、自动阅卷、成绩统计等功能。', 'SYSTEM', 'HIGH', 'PUBLISHED', 1, NOW()),
('计算机组成原理期末考试通知', '计算机组成原理期末考试将于指定时间进行，请同学们做好准备。考试时长90分钟，请提前复习相关知识。', 'EXAM', 'HIGH', 'PUBLISHED', 2, NOW()),
('数据结构期中考试成绩公布', '数据结构期中考试成绩已公布，请同学们登录系统查看。', 'EXAM', 'MEDIUM', 'PUBLISHED', 2, NOW()),
('系统升级维护通知', '系统将于本周末进行升级维护，届时系统将暂停服务，请提前安排好考试时间。', 'SYSTEM', 'MEDIUM', 'PUBLISHED', 1, NOW()),
('软件工程课程作业提醒', '请各位同学注意软件工程课程作业的截止时间，请按时提交。', 'COURSE', 'LOW', 'PUBLISHED', 4, NOW());

-- =====================================================
-- 轮播图数据
-- =====================================================

INSERT INTO carousels (title, image_url, link_url, description, sort_order, status) VALUES
('在线考试系统上线', 'https://picsum.photos/1200/400?random=1', '/', '南方学院在线考试系统正式上线', 1, 'ACTIVE'),
('欢迎使用AI出题功能', 'https://picsum.photos/1200/400?random=2', '/ai-config', 'AI智能出题功能已开放，快来体验吧！', 2, 'ACTIVE'),
('数据结构课程火热进行中', 'https://picsum.photos/1200/400?random=3', '/courses', '数据结构与算法课程正在进行中', 3, 'ACTIVE');

COMMIT;

-- =====================================================
-- 使用说明
-- =====================================================
--
-- 账号信息（密码统一为：123456）：
--
-- 管理员：
--   admin - 系统管理员
--
-- 教师：
--   teacher1 - 张老师（有AI配置）
--   teacher2 - 李老师（有AI配置）
--   teacher3 - 王老师（有AI配置）
--
-- 学生：
--   student1 - 张三
--   student2 - 李四
--   student3 - 王五
--   student4 - 赵六
--   student5 - 钱七
--   student6 - 孙八
--
-- AI配置说明：
--   需要将 YOUR_DASHSCOPE_API_KEY 等替换为真实的API Key
--   通义千问API获取地址：https://dashscope.console.aliyun.com/
--   DeepSeek API获取地址：https://platform.deepseek.com/
--   智谱AI API获取地址：https://open.bigmodel.cn/
--
-- =====================================================
