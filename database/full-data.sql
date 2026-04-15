-- =====================================================
-- 南方职业学院在线考试系统 - 完整测试数据
-- =====================================================
-- 生成时间：2026-04-08
-- 说明：包含系统运行所需的完整测试数据
-- 使用方法：mysql -u root -p exam_system < full-data.sql
-- =====================================================

USE exam_system;

-- 禁用外键检查（导入数据时）
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 用户数据
-- =====================================================
TRUNCATE TABLE users;
INSERT INTO users (id, username, password, nickname, avatar, role, status, created_at, deleted) VALUES
-- 管理员
(1, 'admin', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '系统管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin&backgroundColor=b6e3f4', 'ADMIN', 'ACTIVE', '2024-09-01 08:00:00', 0),
-- 教师
(2, 'teacher1', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '张老师', 'https://api.dicebear.com/7.x/avataaars/svg?seed=teacher1&backgroundColor=ffd5dc', 'TEACHER', 'ACTIVE', '2024-09-01 08:00:00', 0),
(3, 'teacher2', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '李老师', 'https://api.dicebear.com/7.x/avataaars/svg?seed=teacher2&backgroundColor=c0aede', 'TEACHER', 'ACTIVE', '2024-09-02 09:00:00', 0),
(4, 'teacher3', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '王老师', 'https://api.dicebear.com/7.x/avataaars/svg?seed=teacher3&backgroundColor=d1d4f9', 'TEACHER', 'ACTIVE', '2024-09-03 10:00:00', 0),
-- 学生
(5, 'student1', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '赵小明', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student1&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-01 08:00:00', 0),
(6, 'student2', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '钱小红', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student2&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-01 08:00:00', 0),
(7, 'student3', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '孙小华', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student3&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-02 09:00:00', 0),
(8, 'student4', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '周小丽', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student4&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-02 09:00:00', 0),
(9, 'student5', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '吴小强', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student5&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-03 10:00:00', 0),
(10, 'student6', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '郑小美', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student6&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-03 10:00:00', 0),
-- 额外教师
(11, 'teacher4', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '刘老师', 'https://api.dicebear.com/7.x/avataaars/svg?seed=teacher4&backgroundColor=c0aede', 'TEACHER', 'ACTIVE', '2024-09-04 08:00:00', 0),
(12, 'teacher5', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '陈老师', 'https://api.dicebear.com/7.x/avataaars/svg?seed=teacher5&backgroundColor=d1d4f9', 'TEACHER', 'ACTIVE', '2024-09-04 09:00:00', 0),
-- 额外学生
(13, 'student7', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '林小杰', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student7&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-04 08:00:00', 0),
(14, 'student8', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '杨小芳', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student8&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-04 08:00:00', 0),
(15, 'student9', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '黄小军', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student9&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-04 09:00:00', 0),
(16, 'student10', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '许小燕', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student10&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-04 09:00:00', 0),
(17, 'student11', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '何小伟', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student11&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-05 10:00:00', 0),
(18, 'student12', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '马小玲', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student12&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-05 10:00:00', 0),
(19, 'student13', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '罗小鹏', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student13&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-05 11:00:00', 0),
(20, 'student14', '$2a$10$CtPfaJ07wDeCSUCp6LgigeYEVZxOU4xOcEnaig3CqqKFphhOiiv.G', '梁小敏', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student14&backgroundColor=ffdfbf', 'STUDENT', 'ACTIVE', '2024-09-05 11:00:00', 0);

-- =====================================================
-- 2. 课程数据
-- =====================================================
TRUNCATE TABLE courses;
INSERT INTO courses (id, name, code, description, cover_url, teacher_id, credits, status, created_at, deadline, deleted) VALUES
(1, 'Web 开发基础教程', 'WEB101', '学习 Web 前端开发基础知识，包括 HTML、CSS、JavaScript 等', 'https://picsum.photos/800/600', 2, 3.0, 'ACTIVE', '2026-04-01 09:00:00', null, 0),
(2, '数据结构与算法', 'CS201', '学习常见数据结构和算法设计与分析方法', 'https://picsum.photos/800/600?random=2', 2, 4.0, 'ACTIVE', '2026-04-01 09:00:00', null, 0),
(3, '计算机网络基础', 'CS301', '学习计算机网络的基本原理和协议', 'https://picsum.photos/800/600?random=3', 3, 3.0, 'ACTIVE', '2026-04-02 10:00:00', null, 0),
(4, '数据库系统原理', 'CS401', '学习关系数据库的设计、管理和优化', 'https://picsum.photos/800/600?random=4', 3, 4.0, 'ACTIVE', '2026-04-02 10:00:00', null, 0),
(5, '操作系统原理', 'CS501', '学习操作系统的基本原理和实现技术', 'https://picsum.photos/800/600?random=5', 4, 4.0, 'ACTIVE', '2026-04-03 11:00:00', null, 0),
(6, 'Java 程序设计', 'CS102', '学习 Java 编程语言基础和面向对象编程', 'https://picsum.photos/800/600?random=6', 4, 3.0, 'ACTIVE', '2026-04-03 11:00:00', null, 0),
(7, 'Python 数据分析', 'DS201', '学习 Python 数据分析和可视化技术', 'https://picsum.photos/800/600?random=7', 11, 3.0, 'ACTIVE', '2026-04-04 09:00:00', null, 0),
(8, '人工智能导论', 'AI101', '学习人工智能基础概念和应用', 'https://picsum.photos/800/600?random=8', 11, 4.0, 'ACTIVE', '2026-04-04 10:00:00', null, 0),
(9, '软件工程', 'SE301', '学习软件开发流程和项目管理', 'https://picsum.photos/800/600?random=9', 12, 3.0, 'ACTIVE', '2026-04-05 09:00:00', null, 0),
(10, '网络安全基础', 'SEC201', '学习网络安全基础知识和防护技术', 'https://picsum.photos/800/600?random=10', 12, 4.0, 'ACTIVE', '2026-04-05 10:00:00', null, 0);

-- =====================================================
-- 3. 课程成员数据 (course_members)
-- =====================================================
TRUNCATE TABLE course_members;
INSERT INTO course_members (id, course_id, student_id, joined_at) VALUES
-- 课程1: Web开发基础教程
(1, 1, 5, '2026-04-04 10:00:00'),
(2, 1, 6, '2026-04-04 10:05:00'),
(3, 1, 7, '2026-04-04 10:10:00'),
(4, 1, 8, '2026-04-04 10:15:00'),
-- 课程2: 数据结构与算法
(5, 2, 5, '2026-04-04 11:00:00'),
(6, 2, 6, '2026-04-04 11:05:00'),
(7, 2, 9, '2026-04-04 11:10:00'),
-- 课程3: 计算机网络基础
(8, 3, 7, '2026-04-05 09:00:00'),
(9, 3, 8, '2026-04-05 09:05:00'),
(10, 3, 10, '2026-04-05 09:10:00'),
-- 课程4: 数据库系统原理
(11, 4, 5, '2026-04-05 10:00:00'),
(12, 4, 9, '2026-04-05 10:05:00'),
(13, 4, 10, '2026-04-05 10:10:00'),
-- 课程5: 操作系统原理
(14, 5, 6, '2026-04-06 09:00:00'),
(15, 5, 7, '2026-04-06 09:05:00'),
-- 课程6: Java程序设计
(16, 6, 8, '2026-04-06 10:00:00'),
(17, 6, 9, '2026-04-06 10:05:00'),
(18, 6, 10, '2026-04-06 10:10:00'),
-- 课程7: Python数据分析
(19, 7, 13, '2026-04-06 11:00:00'),
(20, 7, 14, '2026-04-06 11:05:00'),
(21, 7, 15, '2026-04-06 11:10:00'),
(22, 7, 5, '2026-04-06 11:15:00'),
-- 课程8: 人工智能导论
(23, 8, 16, '2026-04-07 09:00:00'),
(24, 8, 17, '2026-04-07 09:05:00'),
(25, 8, 18, '2026-04-07 09:10:00'),
(26, 8, 6, '2026-04-07 09:15:00'),
-- 课程9: 软件工程
(27, 9, 19, '2026-04-07 10:00:00'),
(28, 9, 20, '2026-04-07 10:05:00'),
(29, 9, 7, '2026-04-07 10:10:00'),
(30, 9, 9, '2026-04-07 10:15:00'),
-- 课程10: 网络安全基础
(31, 10, 13, '2026-04-08 09:00:00'),
(32, 10, 15, '2026-04-08 09:05:00'),
(33, 10, 17, '2026-04-08 09:10:00'),
(34, 10, 19, '2026-04-08 09:15:00');

-- =====================================================
-- 4. 题目数据 - 精简版
-- =====================================================
TRUNCATE TABLE questions;
INSERT INTO questions (id, content, type, difficulty, score, teacher_id, subject, options, correct_answer, scoring_criteria, explanation, deleted) VALUES
-- 单选题 (1-5)
(1, '已知函数 f(x) = ln(x^2 + 1)，则该函数的导数 f''(x) 为：', 'SINGLE_CHOICE', 'MEDIUM', 5.00, 2, '数学', '[{"id": "A", "text": "f''(x) = 1 / (x^2 + 1)"}, {"id": "B", "text": "f''(x) = 2x / (x^2 + 1)"}, {"id": "C", "text": "f''(x) = 2 / (x^2 + 1)"}, {"id": "D", "text": "f''(x) = x / (x^2 + 1)"}]', '"B"', null, '根据复合函数求导法则（链式法则），设 u = x^2 + 1，则 f(x) = ln(u)。导数为 f''(x) = (1/u) * u''。由于 u'' = (x^2 + 1)'' = 2x，因此 f''(x) = (1 / (x^2 + 1)) * 2x = 2x / (x^2 + 1)。', 0),
(2, '已知二次函数 f(x) = x² - 4x + 3，求该函数的最小值为多少？', 'SINGLE_CHOICE', 'MEDIUM', 5.00, 2, '数学', '[{"id": "A", "text": "-1"}, {"id": "B", "text": "0"}, {"id": "C", "text": "1"}, {"id": "D", "text": "2"}]', '"A"', '[{"point": "正确配方为f(x)=(x-2)²-1", "score": 2}, {"point": "理解顶点坐标为(2,-1)", "score": 2}, {"point": "得出最小值为-1", "score": 1}]', '将函数配方得 f(x) = (x-2)² - 1，因此最小值为-1，出现在x=2处。', 0),
(3, '已知等差数列{aₙ}中，a₃ + a₇ = 20，求a₅的值为多少？', 'SINGLE_CHOICE', 'MEDIUM', 5.00, 2, '数学', '[{"id": "A", "text": "5"}, {"id": "B", "text": "10"}, {"id": "C", "text": "12"}, {"id": "D", "text": "15"}]', '"B"', '[{"point": "识别出下标关系3+7=2×5", "score": 2}, {"point": "应用等差数列对称性质", "score": 2}, {"point": "正确计算出a₅=10", "score": 1}]', '在等差数列中，若m+n=2k，则aₘ + aₙ = 2aₖ。这里3+7=2×5，所以a₃ + a₇ = 2a₅ = 20，因此a₅=10。', 0),
(4, '抛掷三枚公平硬币，至少出现一个正面的概率是多少？', 'SINGLE_CHOICE', 'MEDIUM', 5.00, 2, '数学', '[{"id": "A", "text": "7/8"}, {"id": "B", "text": "3/4"}, {"id": "C", "text": "1/2"}, {"id": "D", "text": "1/8"}]', '"A"', '[{"point": "选择正确答案", "score": 5}]', '计算对立事件更简便：P(无正面)=1/8（只有TTT一种情况）。因此P(至少一个正面)=1-1/8=7/8。', 0),
(5, '等差数列前三项是 2, 5, 8，求第20项的值。', 'SINGLE_CHOICE', 'MEDIUM', 5.00, 2, '数学', '[{"id": "A", "text": "59"}, {"id": "B", "text": "60"}, {"id": "C", "text": "61"}, {"id": "D", "text": "58"}]', '"A"', '[{"point": "选择正确答案", "score": 5}]', '公差d=5-2=3。通项公式aₙ=a₁+(n-1)d=2+(20-1)×3=2+57=59。', 0),
-- 判断题 (6-10)
(6, '圆内接四边形的对角互补。', 'TRUE_FALSE', 'MEDIUM', 5.00, 2, '数学', '[{"id": "A", "text": "正确"}, {"id": "B", "text": "错误"}]', '"A"', '[{"point": "判断正确", "score": 5}]', '圆内接四边形的重要性质：对角之和等于180°（互补）。这是圆内接四边形的判定定理和性质定理。', 0),
(7, '二次方程ax²+bx+c=0有两个正根时，系数必须满足b<0且c>0（设a>0）。', 'TRUE_FALSE', 'MEDIUM', 5.00, 2, '数学', '[{"id": "A", "text": "正确"}, {"id": "B", "text": "错误"}]', '"A"', '[{"point": "判断正确", "score": 5}]', '根据韦达定理，若ax²+bx+c=0（a>0）有两个正根x₁,x₂，则x₁+x₂=-b/a>0 ⇒ b<0；x₁x₂=c/a>0 ⇒ c>0。', 0),
(8, '在任意三角形中，cosA+cosB+cosC ≤ 1。', 'TRUE_FALSE', 'MEDIUM', 5.00, 2, '数学', '[{"id": "A", "text": "正确"}, {"id": "B", "text": "错误"}]', '"B"', '[{"point": "判断正确", "score": 5}]', '在任意三角形中，cosA+cosB+cosC > 1恒成立，且最大值为3/2（当且仅当等边三角形时取得）。因此cosA+cosB+cosC ≤ 1是错误的。', 0),
(9, '矩阵的秩在转置后不变。', 'TRUE_FALSE', 'MEDIUM', 5.00, 3, '数学', '[{"id": "A", "text": "正确"}, {"id": "B", "text": "错误"}]', '"A"', '[{"point": "判断正确", "score": 5}]', '矩阵的秩等于其行秩也等于其列秩。转置操作交换行和列，因此rank(A)=rank(Aᵀ)。', 0),
(10, '泊松分布的期望等于方差。', 'TRUE_FALSE', 'MEDIUM', 5.00, 3, '数学', '[{"id": "A", "text": "正确"}, {"id": "B", "text": "错误"}]', '"A"', '[{"point": "判断正确", "score": 5}]', '泊松分布P(λ)的重要性质：期望值E(X)=λ，方差Var(X)=λ。两者相等是该分布的特征之一。', 0),
-- 多选题 (11-13)
(11, '已知方程 x² - 5x + 6 = 0 的两个根是 a 和 b，那么 (a+1)(b+1) 的值是多少？', 'MULTIPLE_CHOICE', 'MEDIUM', 5.00, 2, '数学', '[{"id": "A", "text": "12"}, {"id": "B", "text": "10"}, {"id": "C", "text": "8"}, {"id": "D", "text": "6"}]', '"A"', '[{"point": "选择正确答案", "score": 5}]', '方程x²-5x+6=0可分解为(x-2)(x-3)=0，所以a=2，b=3。计算得(a+1)(b+1)=(2+1)(3+1)=3×4=12。', 0),
(12, '从5本不同的数学书和3本不同的物理书中选出2本数学书和1本物理书，有多少种选法？', 'MULTIPLE_CHOICE', 'MEDIUM', 5.00, 2, '数学', '[{"id": "A", "text": "30"}, {"id": "B", "text": "15"}, {"id": "C", "text": "20"}, {"id": "D", "text": "45"}]', '"A"', '[{"point": "选择正确答案", "score": 5}]', '选2本数学书有C(5,2)=10种，选1本物理书有C(3,1)=3种。根据乘法原理，总选法=10×3=30种。', 0),
(13, '求100和75的最大公约数。', 'MULTIPLE_CHOICE', 'MEDIUM', 5.00, 3, '数学', '[{"id": "A", "text": "25"}, {"id": "B", "text": "15"}, {"id": "C", "text": "5"}, {"id": "D", "text": "50"}]', '"A"', '[{"point": "选择正确答案", "score": 5}]', '分解质因数：100=2²×5²，75=3×5²。最大公约数是公共质因数的最小指数幂的乘积，即5²=25。', 0),
-- 填空题 (14-17)
(14, '已知二次方程x² - 5x + k = 0的一个根是2，那么k = ___。', 'FILL_BLANK', 'MEDIUM', 5.00, 2, '数学', null, '["6"]', '[{"point": "正确代入x=2", "score": 2}, {"point": "正确化简计算", "score": 2}, {"point": "得出k=6", "score": 1}]', '将x=2代入方程得：2² - 5×2 + k = 0 → 4 - 10 + k = 0 → k = 6。', 0),
(15, '在直角三角形ABC中，∠C=90°，AC=3，BC=4，则斜边AB上的高为 ___。', 'FILL_BLANK', 'MEDIUM', 5.00, 2, '数学', null, '["12/5", "2.4"]', '[{"point": "正确求出斜边长", "score": 2}, {"point": "利用面积相等建立方程", "score": 2}, {"point": "得出正确答案", "score": 1}]', '斜边AB=5（勾股定理）。利用面积相等：AC×BC/2 = AB×h/2 → 12/2 = 5h/2 → h=12/5=2.4。', 0),
(16, 'sin(π/6) + cos(π/3) = ___。', 'FILL_BLANK', 'MEDIUM', 5.00, 3, '数学', null, '["1"]', '[{"point": "正确回忆特殊角三角函数值", "score": 3}, {"point": "准确计算结果", "score": 2}]', 'sin(π/6)=1/2，cos(π/3)=1/2，两者之和为1。', 0),
(17, '计算：log₂8 + log₃9 = ___。', 'FILL_BLANK', 'MEDIUM', 5.00, 3, '数学', null, '["5"]', '[{"point": "正确拆分对数", "score": 2}, {"point": "准确计算结果", "score": 3}]', 'log₂8 = log₂(2³) = 3，log₃9 = log₃(3²) = 2，所以和为5。', 0),
-- 简答题 (18-20)
(18, '已知等差数列{aₙ}的前n项和为Sₙ，且S₅=35，S₁₀=120。请详细推导求出该数列的通项公式aₙ和前n项和公式Sₙ，并计算S₂₀的值。请展示完整的代数推导过程。', 'ESSAY', 'MEDIUM', 10.00, 2, '数学', null, '""', '[{"point": "正确设立未知数并列出方程组", "score": 3}, {"point": "正确求解方程组得到首项和公差", "score": 3}, {"point": "正确写出通项公式aₙ和前n项和公式Sₙ", "score": 2}, {"point": "正确计算S₂₀并展示完整推导过程", "score": 2}]', '设等差数列首项为a₁，公差为d。根据Sₙ=n/2[2a₁+(n-1)d]，得方程组：5/2[2a₁+4d]=35→a₁+2d=7；10/2[2a₁+9d]=120→2a₁+9d=24。解得a₁=3，d=2。因此aₙ=3+2(n-1)=2n+1，Sₙ=n/2[2×3+(n-1)×2]=n(n+2)。S₂₀=20×22=440。', 0),
(19, '在平面直角坐标系中，已知圆C的方程为x²+y²-4x+6y-12=0。请通过配方法求出圆心坐标和半径，并求过点P(5,2)且与圆C相切的直线方程。请写出所有可能的切线方程。', 'ESSAY', 'MEDIUM', 10.00, 3, '数学', null, '""', '[{"point": "正确通过配方求出圆心和半径", "score": 2}, {"point": "正确判断点P与圆的位置关系", "score": 2}, {"point": "分类讨论斜率存在和不存在两种情况", "score": 3}, {"point": "正确求出所有切线方程并验证", "score": 3}]', '配方得(x²-4x+4)+(y²+6y+9)=12+4+9→(x-2)²+(y+3)²=25，圆心C(2,-3)，半径r=5。点P(5,2)在圆外。分两种情况：1)斜率k存在，设切线y-2=k(x-5)，利用圆心到直线距离等于半径，解得k=4/3，得切线4x-3y-14=0。2)斜率不存在，x=5也满足。', 0),
(20, '证明题：证明√2是无理数。请使用反证法，假设√2是有理数，即√2=p/q（p,q为互质正整数），然后通过代数变形和数论知识推导出矛盾。请详细写出每一步的推理过程。', 'ESSAY', 'MEDIUM', 10.00, 3, '数学', null, '""', '[{"point": "正确设立反证法假设（√2=p/q，p,q互质）", "score": 2}, {"point": "正确推导出p²=2q²并证明p为偶数", "score": 3}, {"point": "正确推导出q也为偶数", "score": 3}, {"point": "明确指出矛盾并得出结论，逻辑严密", "score": 2}]', '反证法：假设√2=p/q（p,q互质，q≠0）。平方得2=p²/q²→p²=2q²，故p²为偶数，p必为偶数。设p=2k，则(2k)²=2q²→4k²=2q²→q²=2k²，故q²为偶数，q必为偶数。则p,q均为偶数，有公因数2，与''互质''矛盾。故假设不成立，√2是无理数。', 0),
-- 计算机类单选题 (21-25)
(21, '在Java中，以下哪个关键字用于定义一个类？', 'SINGLE_CHOICE', 'EASY', 5.00, 4, 'Java', '[{"id": "A", "text": "class"}, {"id": "B", "text": "struct"}, {"id": "C", "text": "define"}, {"id": "D", "text": "type"}]', '"A"', null, 'Java使用class关键字定义类。', 0),
(22, 'SQL语句中，用于删除表中所有数据但保留表结构的是？', 'SINGLE_CHOICE', 'MEDIUM', 5.00, 3, '数据库', '[{"id": "A", "text": "DELETE"}, {"id": "B", "text": "DROP"}, {"id": "C", "text": "TRUNCATE"}, {"id": "D", "text": "REMOVE"}]', '"C"', null, 'TRUNCATE删除所有数据但保留表结构，比DELETE更快。', 0),
(23, 'TCP/IP模型中，HTTP协议位于哪一层？', 'SINGLE_CHOICE', 'MEDIUM', 5.00, 3, '计算机网络', '[{"id": "A", "text": "网络层"}, {"id": "B", "text": "传输层"}, {"id": "C", "text": "应用层"}, {"id": "D", "text": "数据链路层"}]', '"C"', null, 'HTTP是应用层协议，用于Web通信。', 0),
(24, '以下哪种排序算法的平均时间复杂度最低？', 'SINGLE_CHOICE', 'HARD', 5.00, 2, '数据结构', '[{"id": "A", "text": "冒泡排序"}, {"id": "B", "text": "快速排序"}, {"id": "C", "text": "插入排序"}, {"id": "D", "text": "选择排序"}]', '"B"', null, '快速排序平均O(nlogn)，其他为O(n²)。', 0),
(25, 'Vue.js中，用于双向数据绑定的指令是？', 'SINGLE_CHOICE', 'EASY', 5.00, 2, '前端开发', '[{"id": "A", "text": "v-bind"}, {"id": "B", "text": "v-model"}, {"id": "C", "text": "v-for"}, {"id": "D", "text": "v-if"}]', '"B"', null, 'v-model用于表单元素的双向数据绑定。', 0),
-- 计算机类判断题 (26-30)
(26, 'Java是一种面向对象的编程语言。', 'TRUE_FALSE', 'EASY', 5.00, 4, 'Java', '[{"id": "A", "text": "正确"}, {"id": "B", "text": "错误"}]', '"A"', null, 'Java是纯面向对象编程语言。', 0),
(27, 'MySQL是一种关系型数据库管理系统。', 'TRUE_FALSE', 'EASY', 5.00, 3, '数据库', '[{"id": "A", "text": "正确"}, {"id": "B", "text": "错误"}]', '"A"', null, 'MySQL是开源的关系型数据库。', 0),
(28, 'HTTP是无状态协议。', 'TRUE_FALSE', 'MEDIUM', 5.00, 3, '计算机网络', '[{"id": "A", "text": "正确"}, {"id": "B", "text": "错误"}]', '"A"', null, 'HTTP协议本身不保存通信状态。', 0),
(29, '栈是一种先进先出(FIFO)的数据结构。', 'TRUE_FALSE', 'MEDIUM', 5.00, 2, '数据结构', '[{"id": "A", "text": "正确"}, {"id": "B", "text": "错误"}]', '"B"', null, '栈是先进后出(FILO)，队列才是先进先出。', 0),
(30, 'Spring Boot可以自动配置Spring应用。', 'TRUE_FALSE', 'MEDIUM', 5.00, 4, 'Java', '[{"id": "A", "text": "正确"}, {"id": "B", "text": "错误"}]', '"A"', null, 'Spring Boot的核心特性之一就是自动配置。', 0),
-- 填空题 (31-35)
(31, 'Java中，用于输出内容到控制台的方法是System.out.___()。', 'FILL_BLANK', 'EASY', 5.00, 4, 'Java', null, '["println", "print"]', null, 'System.out.println()或print()用于控制台输出。', 0),
(32, 'SQL查询语句中，用于条件筛选的关键字是___。', 'FILL_BLANK', 'EASY', 5.00, 3, '数据库', null, '["WHERE", "where"]', null, 'WHERE子句用于过滤记录。', 0),
(33, 'TCP协议建立连接需要___次握手。', 'FILL_BLANK', 'MEDIUM', 5.00, 3, '计算机网络', null, '["3", "三"]', null, 'TCP三次握手：SYN、SYN-ACK、ACK。', 0),
(34, '二叉树的第i层最多有___个节点。', 'FILL_BLANK', 'MEDIUM', 5.00, 2, '数据结构', null, '["2^(i-1)", "2的i-1次方"]', null, '二叉树每层最大节点数为2^(i-1)。', 0),
(35, 'HTML中，用于创建超链接的标签是___。', 'FILL_BLANK', 'EASY', 5.00, 2, '前端开发', null, '["<a>", "a", "<a></a>"]', null, '<a>标签用于创建超链接。', 0),
-- 简答题 (36-40)
(36, '请简述面向对象编程的三大特性，并分别举例说明。', 'ESSAY', 'MEDIUM', 10.00, 4, 'Java', null, '""', '[{"point": "正确列出封装、继承、多态", "score": 3}, {"point": "封装举例合理", "score": 2}, {"point": "继承举例合理", "score": 2}, {"point": "多态举例合理", "score": 3}]', '三大特性：封装（隐藏实现细节，如private成员）、继承（代码复用，如子类继承父类）、多态（同一接口不同实现，如方法重写）。', 0),
(37, '什么是数据库索引？请说明索引的优缺点。', 'ESSAY', 'MEDIUM', 10.00, 3, '数据库', null, '""', '[{"point": "正确定义索引", "score": 3}, {"point": "说明优点（查询速度）", "score": 3}, {"point": "说明缺点（空间、更新开销）", "score": 4}]', '索引是数据库中提高查询效率的数据结构。优点：加快数据检索速度。缺点：占用存储空间、降低数据更新速度。', 0),
(38, '请解释HTTP和HTTPS的区别。', 'ESSAY', 'MEDIUM', 10.00, 3, '计算机网络', null, '""', '[{"point": "说明HTTP是明文传输", "score": 3}, {"point": "说明HTTPS是加密传输", "score": 3}, {"point": "提到SSL/TLS证书", "score": 4}]', 'HTTP是明文传输协议，数据不安全；HTTPS在HTTP基础上加入SSL/TLS加密层，保证数据传输安全，需要数字证书。', 0),
(39, '什么是时间复杂度？请分析冒泡排序的时间复杂度。', 'ESSAY', 'MEDIUM', 10.00, 2, '数据结构', null, '""', '[{"point": "正确定义时间复杂度", "score": 3}, {"point": "分析冒泡排序最好情况O(n)", "score": 3}, {"point": "分析冒泡排序最坏/平均情况O(n²)", "score": 4}]', '时间复杂度是算法执行时间与输入规模的关系。冒泡排序最好O(n)，最坏和平均O(n²)。', 0),
(40, '请描述Vue.js的生命周期，列出至少4个钩子函数及其作用。', 'ESSAY', 'MEDIUM', 10.00, 2, '前端开发', null, '""', '[{"point": "列出created钩子及作用", "score": 2}, {"point": "列出mounted钩子及作用", "score": 2}, {"point": "列出updated钩子及作用", "score": 3}, {"point": "列出destroyed/unmounted钩子及作用", "score": 3}]', 'Vue生命周期钩子：created（实例创建完成）、mounted（DOM挂载完成）、updated（数据更新后）、destroyed/unmounted（实例销毁后）。', 0);

-- =====================================================
-- 5. 试卷数据
-- =====================================================
TRUNCATE TABLE papers;
INSERT INTO papers (id, name, description, course_id, teacher_id, questions, total_score, type, status, created_at, deleted) VALUES
(1, '数学基础测试卷', '测试数学基础知识', 2, 2, '[{"score": 5, "questionId": 1}, {"score": 5, "questionId": 2}, {"score": 5, "questionId": 3}, {"score": 5, "questionId": 4}, {"score": 5, "questionId": 5}, {"score": 5, "questionId": 6}, {"score": 5, "questionId": 7}, {"score": 5, "questionId": 8}, {"score": 5, "questionId": 9}, {"score": 5, "questionId": 10}, {"score": 5, "questionId": 11}, {"score": 5, "questionId": 12}, {"score": 5, "questionId": 13}, {"score": 5, "questionId": 14}, {"score": 5, "questionId": 15}, {"score": 5, "questionId": 16}, {"score": 5, "questionId": 17}, {"score": 10, "questionId": 18}, {"score": 10, "questionId": 19}, {"score": 10, "questionId": 20}]', 105.00, 'MANUAL', 'DRAFT', '2026-04-06 10:00:00', 0),
(2, '数学快速测验', '简短测验', 2, 2, '[{"score": 5, "questionId": 1}, {"score": 5, "questionId": 6}, {"score": 5, "questionId": 11}, {"score": 5, "questionId": 14}, {"score": 10, "questionId": 18}]', 30.00, 'AUTO', 'DRAFT', '2026-04-07 14:00:00', 0),
(3, '高等数学综合测试', '综合性测试', 3, 3, '[{"score": 5, "questionId": 2}, {"score": 5, "questionId": 3}, {"score": 5, "questionId": 7}, {"score": 5, "questionId": 9}, {"score": 5, "questionId": 10}, {"score": 5, "questionId": 15}, {"score": 5, "questionId": 16}, {"score": 10, "questionId": 19}, {"score": 10, "questionId": 20}]', 55.00, 'MANUAL', 'DRAFT', '2026-04-07 16:00:00', 0),
(4, 'Java程序设计测试', 'Java基础知识测试', 6, 4, '[{"score": 5, "questionId": 21}, {"score": 5, "questionId": 26}, {"score": 5, "questionId": 31}, {"score": 10, "questionId": 36}]', 25.00, 'AUTO', 'DRAFT', '2026-04-08 10:00:00', 0),
(5, '计算机网络测试', '网络基础知识测试', 3, 3, '[{"score": 5, "questionId": 23}, {"score": 5, "questionId": 28}, {"score": 5, "questionId": 33}, {"score": 10, "questionId": 38}]', 25.00, 'AUTO', 'DRAFT', '2026-04-08 11:00:00', 0),
(6, '综合能力测试', '全学科综合测试', 2, 2, '[{"score": 5, "questionId": 21}, {"score": 5, "questionId": 22}, {"score": 5, "questionId": 23}, {"score": 5, "questionId": 24}, {"score": 5, "questionId": 25}, {"score": 5, "questionId": 26}, {"score": 5, "questionId": 27}, {"score": 5, "questionId": 28}, {"score": 5, "questionId": 29}, {"score": 5, "questionId": 30}]', 50.00, 'MANUAL', 'DRAFT', '2026-04-09 09:00:00', 0);

-- =====================================================
-- 6. 考试数据
-- =====================================================
TRUNCATE TABLE exams;
INSERT INTO exams (id, title, description, course_id, paper_id, teacher_id, started_at, ended_at, duration, total_score, pass_score, status, created_at, deleted) VALUES
(1, '期中数学测验', '数据结构与算法课程期中测验', 2, 1, 2, '2026-04-10 09:00:00', '2026-04-15 18:00:00', 120, 105.00, 60.00, 'PUBLISHED', '2026-04-06 10:30:00', 0),
(2, '课堂小测', '随堂快速测验', 2, 2, 2, '2026-04-08 14:00:00', '2026-04-08 16:00:00', 30, 30.00, 60.00, 'PUBLISHED', '2026-04-07 14:30:00', 0),
(3, '高等数学期末考试', '计算机网络课程期末考试', 3, 3, 3, '2026-04-12 09:00:00', '2026-04-12 12:00:00', 180, 55.00, 60.00, 'PUBLISHED', '2026-04-07 16:30:00', 0),
(4, '期末综合测试', '数据库课程期末测试', 4, 1, 3, '2026-04-14 14:00:00', '2026-04-14 17:00:00', 180, 105.00, 60.00, 'PUBLISHED', '2026-04-08 09:00:00', 0),
(5, 'Java程序设计测验', 'Java基础能力测试', 6, 4, 4, '2026-04-11 09:00:00', '2026-04-11 11:00:00', 60, 25.00, 60.00, 'PUBLISHED', '2026-04-08 14:00:00', 0),
(6, '计算机网络测验', '网络基础能力测试', 3, 5, 3, '2026-04-11 14:00:00', '2026-04-11 16:00:00', 60, 25.00, 60.00, 'PUBLISHED', '2026-04-08 15:00:00', 0),
(7, '全学科综合考试', '期中综合能力测试', 2, 6, 2, '2026-04-16 09:00:00', '2026-04-16 12:00:00', 90, 50.00, 60.00, 'PUBLISHED', '2026-04-09 09:00:00', 0);

-- =====================================================
-- 7. 考试记录数据 (exam_sessions)
-- =====================================================
TRUNCATE TABLE exam_sessions;
-- 学生5参加考试2的记录
INSERT INTO exam_sessions (id, exam_id, student_id, started_at, submitted_at, score, total_score, status, grading_status, answers, version) VALUES
(1, 2, 5, '2026-04-08 14:05:00', '2026-04-08 14:25:00', 20.00, 30.00, 'GRADED', 'COMPLETED', '[{"score": 5, "answer": "B", "isCorrect": true, "questionId": 1, "questionType": "SINGLE_CHOICE", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "正确", "isCorrect": true, "questionId": 6, "questionType": "TRUE_FALSE", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "A", "isCorrect": true, "questionId": 11, "questionType": "MULTIPLE_CHOICE", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 0, "answer": "5", "isCorrect": false, "questionId": 14, "questionType": "FILL_BLANK", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "正确答案", "isCorrect": true, "questionId": 18, "questionType": "ESSAY", "gradingStatus": "GRADED", "teacherComment": "答题思路清晰"}]', 1),
-- 学生6参加考试2的记录
(2, 2, 6, '2026-04-08 14:10:00', '2026-04-08 14:35:00', 15.00, 30.00, 'GRADED', 'COMPLETED', '[{"score": 0, "answer": "A", "isCorrect": false, "questionId": 1, "questionType": "SINGLE_CHOICE", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "正确", "isCorrect": true, "questionId": 6, "questionType": "TRUE_FALSE", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "A", "isCorrect": true, "questionId": 11, "questionType": "MULTIPLE_CHOICE", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "6", "isCorrect": true, "questionId": 14, "questionType": "FILL_BLANK", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 0, "answer": "不会做", "isCorrect": false, "questionId": 18, "questionType": "ESSAY", "gradingStatus": "GRADED", "teacherComment": "需要加强练习"}]', 1),
-- 学生7参加考试3的记录
(3, 3, 7, '2026-04-12 09:05:00', '2026-04-12 11:30:00', 35.00, 55.00, 'GRADED', 'COMPLETED', '[{"score": 5, "answer": "A", "isCorrect": true, "questionId": 2, "questionType": "SINGLE_CHOICE", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "B", "isCorrect": true, "questionId": 3, "questionType": "SINGLE_CHOICE", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "正确", "isCorrect": true, "questionId": 7, "questionType": "TRUE_FALSE", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "正确", "isCorrect": true, "questionId": 9, "questionType": "TRUE_FALSE", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 0, "answer": "正确", "isCorrect": false, "questionId": 10, "questionType": "TRUE_FALSE", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "2.4", "isCorrect": true, "questionId": 15, "questionType": "FILL_BLANK", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "1", "isCorrect": true, "questionId": 16, "questionType": "FILL_BLANK", "gradingStatus": "GRADED", "teacherComment": null}, {"score": 5, "answer": "解答过程", "isCorrect": true, "questionId": 19, "questionType": "ESSAY", "gradingStatus": "GRADED", "teacherComment": "基本正确"}, {"score": 0, "answer": "不会", "isCorrect": false, "questionId": 20, "questionType": "ESSAY", "gradingStatus": "GRADED", "teacherComment": null}]', 1),
-- 学生8参加考试2的记录（已提交未批改）
(4, 2, 8, '2026-04-08 14:15:00', '2026-04-08 14:40:00', null, 30.00, 'SUBMITTED', 'PENDING', '[{"score": null, "answer": "B", "isCorrect": null, "questionId": 1, "questionType": "SINGLE_CHOICE", "gradingStatus": "PENDING", "teacherComment": null}, {"score": null, "answer": "错误", "isCorrect": null, "questionId": 6, "questionType": "TRUE_FALSE", "gradingStatus": "PENDING", "teacherComment": null}, {"score": null, "answer": "B", "isCorrect": null, "questionId": 11, "questionType": "MULTIPLE_CHOICE", "gradingStatus": "PENDING", "teacherComment": null}, {"score": null, "answer": "6", "isCorrect": null, "questionId": 14, "questionType": "FILL_BLANK", "gradingStatus": "PENDING", "teacherComment": null}, {"score": null, "answer": "学生答案", "isCorrect": null, "questionId": 18, "questionType": "ESSAY", "gradingStatus": "PENDING", "teacherComment": null}]', 1),
-- 学生9参加考试1的记录（进行中）
(5, 1, 9, '2026-04-10 09:30:00', null, null, 105.00, 'IN_PROGRESS', 'PENDING', '[]', 1),
-- 学生10参加考试1的记录（已提交未批改）
(6, 1, 10, '2026-04-10 10:00:00', '2026-04-10 11:30:00', null, 105.00, 'SUBMITTED', 'PENDING', '[{"score": null, "answer": "B", "isCorrect": null, "questionId": 1, "questionType": "SINGLE_CHOICE", "gradingStatus": "PENDING", "teacherComment": null}]', 1),
-- 新增学生的考试记录
(7, 2, 13, '2026-04-08 14:20:00', '2026-04-08 14:45:00', 25.00, 30.00, 'GRADED', 'COMPLETED', '[{"score": 5, "answer": "B", "isCorrect": true, "questionId": 1, "questionType": "SINGLE_CHOICE"}, {"score": 5, "answer": "正确", "isCorrect": true, "questionId": 6, "questionType": "TRUE_FALSE"}, {"score": 5, "answer": "A", "isCorrect": true, "questionId": 11, "questionType": "MULTIPLE_CHOICE"}, {"score": 5, "answer": "6", "isCorrect": true, "questionId": 14, "questionType": "FILL_BLANK"}, {"score": 5, "answer": "答得好", "isCorrect": true, "questionId": 18, "questionType": "ESSAY"}]', 1),
(8, 2, 14, '2026-04-08 14:25:00', '2026-04-08 14:50:00', 18.00, 30.00, 'GRADED', 'COMPLETED', '[{"score": 5, "answer": "B", "isCorrect": true, "questionId": 1, "questionType": "SINGLE_CHOICE"}, {"score": 5, "answer": "正确", "isCorrect": true, "questionId": 6, "questionType": "TRUE_FALSE"}, {"score": 0, "answer": "B", "isCorrect": false, "questionId": 11, "questionType": "MULTIPLE_CHOICE"}, {"score": 3, "answer": "5", "isCorrect": false, "questionId": 14, "questionType": "FILL_BLANK"}, {"score": 5, "answer": "回答正确", "isCorrect": true, "questionId": 18, "questionType": "ESSAY"}]', 1),
(9, 5, 8, '2026-04-11 09:10:00', '2026-04-11 10:00:00', 20.00, 25.00, 'GRADED', 'COMPLETED', '[{"score": 5, "answer": "A", "isCorrect": true, "questionId": 21, "questionType": "SINGLE_CHOICE"}, {"score": 5, "answer": "正确", "isCorrect": true, "questionId": 26, "questionType": "TRUE_FALSE"}, {"score": 5, "answer": "println", "isCorrect": true, "questionId": 31, "questionType": "FILL_BLANK"}, {"score": 5, "answer": "答得好", "isCorrect": true, "questionId": 36, "questionType": "ESSAY"}]', 1),
(10, 5, 9, '2026-04-11 09:15:00', '2026-04-11 10:05:00', 15.00, 25.00, 'GRADED', 'COMPLETED', '[{"score": 5, "answer": "A", "isCorrect": true, "questionId": 21, "questionType": "SINGLE_CHOICE"}, {"score": 5, "answer": "正确", "isCorrect": true, "questionId": 26, "questionType": "TRUE_FALSE"}, {"score": 0, "answer": "output", "isCorrect": false, "questionId": 31, "questionType": "FILL_BLANK"}, {"score": 5, "answer": "基本正确", "isCorrect": true, "questionId": 36, "questionType": "ESSAY"}]', 1),
(11, 6, 7, '2026-04-11 14:10:00', '2026-04-11 15:00:00', 20.00, 25.00, 'GRADED', 'COMPLETED', '[{"score": 5, "answer": "C", "isCorrect": true, "questionId": 23, "questionType": "SINGLE_CHOICE"}, {"score": 5, "answer": "正确", "isCorrect": true, "questionId": 28, "questionType": "TRUE_FALSE"}, {"score": 5, "answer": "3", "isCorrect": true, "questionId": 33, "questionType": "FILL_BLANK"}, {"score": 5, "answer": "正确", "isCorrect": true, "questionId": 38, "questionType": "ESSAY"}]', 1),
(12, 7, 5, '2026-04-16 09:10:00', null, null, 50.00, 'IN_PROGRESS', 'PENDING', '[]', 1),
(13, 7, 6, '2026-04-16 09:15:00', '2026-04-16 10:30:00', null, 50.00, 'SUBMITTED', 'PENDING', '[{"score": null, "answer": "A", "isCorrect": null, "questionId": 21, "questionType": "SINGLE_CHOICE"}]', 1),
(14, 7, 13, '2026-04-16 09:20:00', '2026-04-16 10:45:00', null, 50.00, 'SUBMITTED', 'PENDING', '[{"score": null, "answer": "A", "isCorrect": null, "questionId": 21, "questionType": "SINGLE_CHOICE"}]', 1);

-- =====================================================
-- 8. 公告数据
-- =====================================================
TRUNCATE TABLE announcements;
INSERT INTO announcements (id, title, content, type, priority, status, publisher_id, published_at, deleted) VALUES
(1, '系统维护通知', '尊敬的各位师生：\n\n为了提供更好的服务体验，系统将于本周末进行例行维护升级。维护期间，部分功能可能暂时无法使用。\n\n维护时间：2026年4月10日（周六）凌晨2:00 - 6:00\n\n请各位师生提前做好工作安排，给您带来的不便敬请谅解。\n\n南方职业学院信息中心\n2026年4月8日', 'SYSTEM', 'HIGH', 'PUBLISHED', 1, '2026-04-08 08:00:00', 0),
(2, '期中考试安排通知', '各位师生：\n\n2026年春季学期期中考试将于下周开始，请各位考生提前做好复习准备。\n\n考试时间安排：\n- 4月15日（周一）：数据结构与算法\n- 4月16日（周二）：计算机网络、数据库系统\n- 4月17日（周三）：操作系统、Java程序设计\n\n具体考场安排请登录系统查看准考证。\n\n教务处\n2026年4月8日', 'SYSTEM', 'HIGH', 'PUBLISHED', 1, '2026-04-08 09:00:00', 0),
(3, '新课程上线通知', '各位师生：\n\n本学期新增多门优质课程现已正式上线，欢迎大家选修学习。\n\n新增课程列表：\n1. Web开发基础教程（WEB101）\n2. 数据结构与算法（CS201）\n3. 计算机网络基础（CS301）\n4. 数据库系统原理（CS401）\n5. 操作系统原理（CS501）\n6. Java程序设计（CS102）\n\n祝学习愉快！\n\n教务处\n2026年4月8日', 'SYSTEM', 'MEDIUM', 'PUBLISHED', 1, '2026-04-08 10:00:00', 0),
(4, '成绩查询通知', '各位师生：\n\n本学期第一次月考成绩现已公布，请各位同学及时登录系统查询。\n\n查询时间：2026年4月9日起\n\n注意事项：\n- 如对成绩有异议，可在4月15日前申请复查\n- 复查结果将于3个工作日内反馈\n\n教务处\n2026年4月8日', 'SYSTEM', 'MEDIUM', 'PUBLISHED', 1, '2026-04-08 11:00:00', 0),
(5, '校园活动通知', '各位师生：\n\n为丰富校园文化生活，本学期将举办一系列精彩活动：\n\n1. 学术讲座：每周五下午3:00-5:00\n2. 篮球联赛：4月20日开幕\n3. 文艺汇演：4月30日晚7:00\n\n欢迎各位同学积极参与！\n\n学生工作处\n2026年4月8日', 'SYSTEM', 'LOW', 'PUBLISHED', 1, '2026-04-08 12:00:00', 0),
(6, '教师培训通知', '各位教师：\n\n为提升教学质量，学校将举办在线教学平台使用培训。\n\n培训时间：2026年4月15日（周一）下午2:00-4:00\n培训地点：信息中心多媒体教室\n培训内容：\n1. 在线考试系统操作指南\n2. 试卷组卷技巧\n3. 成绩分析方法\n\n请各位教师准时参加。\n\n教务处\n2026年4月9日', 'SYSTEM', 'MEDIUM', 'PUBLISHED', 1, '2026-04-09 08:00:00', 0),
(7, '系统升级完成通知', '尊敬的各位师生：\n\n系统维护升级已顺利完成！本次升级新增了以下功能：\n\n1. AI智能出题功能\n2. 成绩统计分析优化\n3. 界面响应速度提升\n4. 移动端适配优化\n\n感谢大家的支持与配合！\n\n南方职业学院信息中心\n2026年4月10日', 'SYSTEM', 'HIGH', 'PUBLISHED', 1, '2026-04-10 07:00:00', 0),
(8, '补考安排通知', '各位同学：\n\n2025-2026学年第一学期补考将于下周进行，请相关同学做好准备。\n\n补考时间：2026年4月18日-19日\n补考科目：请登录系统查看个人补考安排\n\n注意事项：\n- 携带学生证和身份证\n- 提前15分钟到达考场\n- 遵守考场纪律\n\n教务处\n2026年4月10日', 'SYSTEM', 'HIGH', 'PUBLISHED', 1, '2026-04-10 09:00:00', 0),
(9, '暑期课程报名通知', '各位同学：\n\n2026年暑期选修课程现已开放报名，欢迎同学们踊跃参加！\n\n开设课程：\n1. Python数据分析实战\n2. Web全栈开发入门\n3. 人工智能应用基础\n4. 网络安全攻防实践\n\n报名截止时间：2026年5月15日\n报名方式：登录系统在线选课\n\n教务处\n2026年4月11日', 'SYSTEM', 'MEDIUM', 'PUBLISHED', 1, '2026-04-11 08:00:00', 0),
(10, '毕业生论文答辩安排', '各位毕业生：\n\n2026届毕业论文答辩安排如下：\n\n答辩时间：2026年5月20日-25日\n答辩地点：各系教学楼（具体安排见附件）\n\n准备材料：\n1. 毕业论文纸质版3份\n2. 答辩PPT\n3. 查重报告\n\n请按时参加答辩，祝答辩顺利！\n\n教务处\n2026年4月12日', 'SYSTEM', 'HIGH', 'PUBLISHED', 1, '2026-04-12 08:00:00', 0);

-- =====================================================
-- 9. 轮播图数据
-- =====================================================
TRUNCATE TABLE carousels;
INSERT INTO carousels (id, title, image_url, link_url, description, sort_order, status, start_at, end_at, deleted) VALUES
(1, '欢迎使用南方职业学院在线考试系统', 'https://picsum.photos/1920/600', '/course', '展示最新课程和考试信息', 1, 'ACTIVE', null, null, 0),
(2, '优质课程推荐', 'https://picsum.photos/1920/600?random=2', '/course', '发现更多优质学习资源', 2, 'ACTIVE', null, null, 0),
(3, '考试安排通知', 'https://picsum.photos/1920/600?random=3', '/exam', '查看最新考试安排和时间表', 3, 'ACTIVE', null, null, 0),
(4, '学习资源中心', 'https://picsum.photos/1920/600?random=4', '/course', '访问丰富的学习资料', 4, 'ACTIVE', null, null, 0);

-- =====================================================
-- 10. AI配置数据 (user_ai_configs)
-- =====================================================
TRUNCATE TABLE user_ai_configs;
INSERT INTO user_ai_configs (id, user_id, name, base_url, api_key, models, active_model, created_at) VALUES
(1, 1, '默认AI配置', 'https://integrate.api.nvidia.com/v1', '9400437cbb58729cead1253d02d5e33944989917cdd3008271456576b5ca26708305c6bea909729f82882aa17a621082e51d9486b56a470a25248bf948dbbc9e8f2a77d2d1d63cc6800d667896489bdc', '["z-ai/glm4.7", "google/gemma-4-31b-it"]', 'z-ai/glm4.7', '2026-04-01 09:00:00'),
(2, 2, '张老师AI配置', 'https://integrate.api.nvidia.com/v1', '9400437cbb58729cead1253d02d5e33944989917cdd3008271456576b5ca26708305c6bea909729f82882aa17a621082e51d9486b56a470a25248bf948dbbc9e8f2a77d2d1d63cc6800d667896489bdc', '["z-ai/glm4.7"]', 'z-ai/glm4.7', '2026-04-01 10:00:00');

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 完成
-- =====================================================
-- 数据导入完成！
-- 统计：
--   用户: 20 条 (1管理员 + 5教师 + 14学生)
--   课程: 10 条
--   课程成员: 34 条
--   题目: 40 条 (10单选 + 10判断 + 3多选 + 9填空 + 8简答)
--   试卷: 6 条
--   考试: 7 条
--   考试记录: 14 条
--   公告: 10 条
--   轮播图: 4 条
--   AI配置: 2 条
-- =====================================================