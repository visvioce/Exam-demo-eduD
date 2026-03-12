# 数据库设计详解

## 一、设计原则

### 1.1 为什么这样设计？

本项目的数据库设计遵循以下核心原则：

| 原则 | 说明 | 为什么要这样做？ |
|------|------|-----------------|
| **第三范式(3NF)** | 消除传递依赖，每个非主属性都直接依赖于主键 | 避免数据冗余，减少更新异常 |
| **逻辑删除** | 使用 `deleted` 字段标记删除，而非物理删除 | 保留历史数据，支持数据恢复，便于审计追溯 |
| **外键约束** | 表之间通过外键关联 | 保证数据一致性，防止孤立数据 |
| **JSON字段** | 灵活数据（如选项、答案）使用JSON存储 | 题目类型多样，JSON可以灵活适应不同题型 |
| **乐观锁** | 考试记录使用 `version` 字段 | 防止学生重复提交或并发冲突 |

### 1.2 数据库选择

**为什么选择 MySQL？**

1. **成熟稳定** - MySQL 是最流行的开源关系数据库，社区活跃
2. **Spring Boot 集成良好** - 官方提供完善的 JDBC 支持和连接池
3. **支持 JSON 类型** - MySQL 5.7+ 原生支持 JSON，适合存储灵活数据
4. **事务支持** - 完整的 ACID 事务支持，考试系统需要数据一致性
5. **学习成本低** - 大多数开发者都熟悉 MySQL

---

## 二、表结构详解

### 2.1 ER图

```
                                    ┌─────────────────┐
                                    │     users       │
                                    │─────────────────│
                                    │ id (PK)         │
                                    │ username        │
                                    │ password        │
                                    │ role            │
                                    └────────┬────────┘
                                             │
              ┌──────────────────────────────┼──────────────────────────────┐
              │                              │                              │
              │ 1:N                          │ N:M                          │ 1:N
              ▼                              ▼                              ▼
    ┌─────────────────┐           ┌─────────────────┐            ┌─────────────────┐
    │    courses      │           │ course_members  │            │   questions     │
    │─────────────────│           │─────────────────│            │─────────────────│
    │ id (PK)         │           │ id (PK)         │            │ id (PK)         │
    │ teacher_id (FK) │◀──────────│ course_id (FK)  │            │ teacher_id (FK) │
    │ name            │           │ student_id (FK) │            │ content         │
    │ code            │           └─────────────────┘            │ type            │
    └────────┬────────┘                                          │ options (JSON)  │
             │                                                   │ correct_answer  │
             │ 1:N                                               └─────────────────┘
             ▼
    ┌─────────────────┐           ┌─────────────────┐
    │    papers       │           │     exams       │
    │─────────────────│           │─────────────────│
    │ id (PK)         │◀──────────│ id (PK)         │
    │ course_id (FK)  │           │ paper_id (FK)   │
    │ questions (JSON)│           │ course_id (FK)  │
    │ total_score     │           │ teacher_id (FK) │
    └─────────────────┘           │ started_at      │
                                  │ ended_at        │
                                  └────────┬────────┘
                                           │
                                           │ 1:N
                                           ▼
                                  ┌─────────────────┐
                                  │  exam_sessions  │
                                  │─────────────────│
                                  │ id (PK)         │
                                  │ exam_id (FK)    │
                                  │ student_id (FK) │
                                  │ answers (JSON)  │
                                  │ score           │
                                  │ version         │ ← 乐观锁
                                  └─────────────────┘
```

---

### 2.2 用户表 (users)

**为什么这样设计用户表？**

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(100) NOT NULL COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL DEFAULT 'STUDENT',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识'
);
```

| 字段 | 设计原因 |
|------|----------|
| `username VARCHAR(50)` | 50字符足够，支持中英文用户名，添加唯一索引防止重复 |
| `password VARCHAR(255)` | BCrypt 加密后的密码固定60字符，留255是为了未来可能的算法变更 |
| `role ENUM(...)` | 使用 ENUM 而非 INT，更直观，数据库层面限制取值范围 |
| `status ENUM(...)` | 三种状态：ACTIVE(正常)、INACTIVE(未激活)、SUSPENDED(已停用) |
| `deleted TINYINT` | 0=未删除，1=已删除，配合 MyBatis-Plus 的逻辑删除功能 |

**为什么不把角色设计成多对多？**

虽然可以用 `user_roles` 表实现一个用户多个角色，但在教育系统中：
- 一个人的身份通常是固定的（要么是老师，要么是学生）
- 单角色设计更简单，查询更高效
- 如果未来需要多角色，可以迁移到关联表

---

### 2.3 课程表 (courses)

```sql
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '课程代码',
    description TEXT COMMENT '课程描述',
    cover_url VARCHAR(500) COMMENT '课程封面URL',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
    credits DECIMAL(3,1) NOT NULL DEFAULT 1.0 COMMENT '学分',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deadline TIMESTAMP NULL COMMENT '选课截止时间',
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);
```

| 字段 | 设计原因 |
|------|----------|
| `code VARCHAR(20) UNIQUE` | 课程代码是业务唯一标识，比ID更易记忆和展示 |
| `teacher_id` 外键 | 确保每门课程都有对应的教师，数据完整性 |
| `credits DECIMAL(3,1)` | 支持小数学分，如 1.5 学分 |
| `deadline TIMESTAMP NULL` | NULL 表示不限时选课，有值则为截止时间 |

**为什么课程和教师是 N:1 而不是 N:N？**

- 当前设计：一门课程只有一个主讲教师
- 如果需要多教师（如助教），可以增加 `course_teachers` 关联表
- 简单场景优先使用简单设计

---

### 2.4 课程成员表 (course_members)

**为什么要单独设计选课关联表？**

```sql
CREATE TABLE course_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_course_student (course_id, student_id)
);
```

这是典型的**多对多关系**设计：

```
一个课程可以有多个学生选课（1:N）
一个学生可以选多门课程（1:N）
所以课程-学生是 N:M 关系
```

| 设计要点 | 原因 |
|----------|------|
| `UNIQUE KEY (course_id, student_id)` | 防止重复选课 |
| `ON DELETE CASCADE` | 删除课程或学生时，自动清理关联记录 |
| `joined_at` | 记录选课时间，可用于统计、排序 |

**为什么不在 users 表加 course_ids 字段？**

- 违反第一范式（字段值不原子）
- 无法高效查询"某课程的所有学生"
- 无法存储选课时间等附加信息

---

### 2.5 题目表 (questions)

```sql
CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL COMMENT '题目内容',
    type ENUM('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE', 'FILL_BLANK', 'ESSAY'),
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL DEFAULT 'MEDIUM',
    score DECIMAL(5,2) NOT NULL DEFAULT 1.0 COMMENT '分值',
    teacher_id BIGINT NOT NULL COMMENT '创建教师ID',
    subject VARCHAR(100) COMMENT '学科',
    options JSON COMMENT '选项（选择题）',
    correct_answer JSON COMMENT '正确答案',
    scoring_criteria JSON COMMENT '评分标准（简答题）',
    explanation TEXT COMMENT '答案解析',
    deleted TINYINT DEFAULT 0,
    FOREIGN KEY (teacher_id) REFERENCES users(id)
);
```

**为什么使用 JSON 存储选项和答案？**

不同题型的数据结构完全不同：

| 题型 | options 示例 | correct_answer 示例 |
|------|-------------|-------------------|
| 单选题 | `["A. 北京", "B. 上海", "C. 广州", "D. 深圳"]` | `"A"` |
| 多选题 | `["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"]` | `["A", "C"]` |
| 判断题 | `["A. 正确", "B. 错误"]` | `"A"` 或 `"true"` |
| 填空题 | 无 | `["答案1", "答案2"]` (支持多空) |
| 简答题 | 无 | 无 (使用 scoring_criteria) |

**为什么不拆成多张表（如选择题表、填空题表）？**

1. **查询效率**：一张表统一查询，无需 JOIN 多表
2. **代码简洁**：一个 Question 实体类处理所有题型
3. **灵活性**：未来新增题型只需扩展 JSON 结构
4. **MySQL JSON 支持**：MySQL 5.7+ 原生支持 JSON 类型和函数

**JSON 字段的索引问题？**

JSON 字段不能直接建索引，但可以：
1. 提取常用字段建立虚拟列索引
2. 在应用层缓存热点数据
3. 题库通常不需要复杂查询，性能影响可接受

---

### 2.6 试卷表 (papers)

```sql
CREATE TABLE papers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    questions JSON COMMENT '题目配置',
    total_score DECIMAL(5,2) NOT NULL DEFAULT 100.0,
    type ENUM('MANUAL', 'AUTO') NOT NULL DEFAULT 'MANUAL',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);
```

**questions JSON 结构解析**：

```json
[
    { "questionId": 1, "score": 10 },
    { "questionId": 2, "score": 15 },
    { "questionId": 5, "score": 20 }
]
```

**为什么这样设计？**

- 不直接存储题目内容，只存 ID 引用 → 题目可独立修改
- 每道题可单独设置分值 → 同一题目在不同试卷可有不同分值
- 顺序就是数组顺序 → 便于排序和展示

**为什么不设计 paper_questions 关联表？**

| 方案 | 优点 | 缺点 |
|------|------|------|
| JSON 字段 | 查询快、顺序明确、代码简单 | 无法 SQL 查询"包含某题目的试卷" |
| 关联表 | 支持复杂 SQL 查询 | 需要 JOIN、顺序需额外字段 |

本项目选择 JSON，因为：
- 试卷查询场景明确，通常整张试卷一起加载
- "包含某题目的试卷" 这种查询很少见
- 代码更简洁，一次查询获取全部信息

---

### 2.7 考试表 (exams)

```sql
CREATE TABLE exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    course_id BIGINT NOT NULL,
    paper_id BIGINT NULL,
    teacher_id BIGINT NOT NULL,
    started_at TIMESTAMP NOT NULL COMMENT '开始时间',
    ended_at TIMESTAMP NOT NULL COMMENT '结束时间',
    duration INT NOT NULL COMMENT '考试时长(分钟)',
    total_score DECIMAL(5,2) NOT NULL DEFAULT 100.0,
    pass_score DECIMAL(5,2) NOT NULL DEFAULT 60.0,
    status ENUM('DRAFT', 'PUBLISHED', 'STARTED', 'ENDED', 'CANCELLED'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);
```

**为什么考试和试卷分开？**

| 概念 | 说明 |
|------|------|
| 试卷(Paper) | 题目的集合，可复用，是"静态"的 |
| 考试(Exam) | 特定时间、特定课程的考试安排，是"动态"的 |

**分离的好处**：

1. **试卷复用**：同一份试卷可以在不同学期、不同班级多次使用
2. **独立管理**：教师可以提前准备试卷库，考试时再安排时间
3. **历史追溯**：即使试卷被修改，历史考试的题目快照仍然保留

**paper_id 为什么允许 NULL？**

- 支持不使用试卷，直接创建考试（未来扩展）
- 当前版本必须关联试卷

---

### 2.8 考试记录表 (exam_sessions) - 重点！

```sql
CREATE TABLE exam_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    submitted_at TIMESTAMP NULL,
    score DECIMAL(5,2) NULL,
    total_score DECIMAL(5,2) NOT NULL,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'SUBMITTED', 'GRADED'),
    grading_status ENUM('PENDING', 'GRADING', 'COMPLETED') DEFAULT 'PENDING',
    answers JSON COMMENT '答题记录',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    FOREIGN KEY (exam_id) REFERENCES exams(id),
    FOREIGN KEY (student_id) REFERENCES users(id),
    UNIQUE KEY uk_exam_student (exam_id, student_id)
);
```

**核心设计要点**：

#### 1. 为什么用乐观锁 (version 字段)？

**场景**：学生可能在多个设备上同时答题，或网络抖动导致重复提交。

```java
// 更新时自动检查版本
UPDATE exam_sessions 
SET answers = ?, score = ?, version = version + 1 
WHERE id = ? AND version = ?
```

如果 version 不匹配（已被其他人更新），更新失败，防止：
- 答案覆盖
- 重复计分
- 数据不一致

#### 2. 为什么有两个状态字段？

| 字段 | 含义 | 为什么分开？ |
|------|------|-------------|
| `status` | 考试流程状态 | 答题进行中 → 已提交 → 已评分 |
| `grading_status` | 评分状态 | 客观题自动评分 → 主观题待评 → 评分完成 |

**分开的好处**：
- 学生提交后，客观题已经评分，但主观题需要教师评分
- 两个状态独立流转，逻辑更清晰

#### 3. answers JSON 结构

```json
{
    "1": { "answer": "A", "score": 10, "comment": "" },
    "2": { "answer": ["A", "C"], "score": 0, "comment": "多选错误" },
    "5": { "answer": "学生答案...", "score": null, "comment": "" }
}
```

**为什么这样设计？**

- Key 是题目ID，便于快速查找
- Value 包含答案、得分、评语（主观题）
- score 为 null 表示未评分（主观题）

#### 4. UNIQUE KEY 的意义

```sql
UNIQUE KEY uk_exam_student (exam_id, student_id)
```

**确保一个学生对于一场考试只有一条记录**，防止：
- 重复考试
- 成绩统计错误

---

## 三、索引设计

### 3.1 为什么需要这些索引？

```sql
CREATE INDEX idx_users_username ON users(username);
-- 登录时通过用户名查询，最频繁的操作

CREATE INDEX idx_users_role ON users(role);
-- 按角色筛选用户（如查询所有教师）

CREATE INDEX idx_exams_status ON exams(status);
-- 查询"进行中"的考试，首页展示

CREATE INDEX idx_exam_sessions_status ON exam_sessions(status);
-- 教师查看待评分的考试记录

CREATE INDEX idx_questions_type ON questions(type);
-- 按题型筛选题目（组卷时使用）
```

### 3.2 索引使用原则

| 原则 | 说明 |
|------|------|
| 外键自动建索引 | MySQL InnoDB 会自动为外键创建索引 |
| 查询条件字段建索引 | WHERE、JOIN ON 后面的字段 |
| 组合索引注意顺序 | 遵循最左前缀原则 |
| 不过度索引 | 索引占用空间，影响写入性能 |

---

## 四、视图设计

### 4.1 为什么使用视图？

视图是**预定义的查询**，可以简化复杂查询、统一数据访问接口。

### 4.2 系统统计视图

```sql
CREATE VIEW v_system_stats AS
SELECT
    (SELECT COUNT(*) FROM users WHERE deleted = 0) AS total_users,
    (SELECT COUNT(*) FROM courses WHERE deleted = 0) AS total_courses,
    ...
```

**用途**：首页仪表盘一次性获取所有统计数据

**为什么用子查询而不是 JOIN？**

- 各表之间无关联关系
- 子查询更直观，性能可接受（数据量不大）

### 4.3 考试成绩统计视图

```sql
CREATE VIEW v_exam_statistics AS
SELECT
    es.exam_id,
    COUNT(*) AS total_participants,
    AVG(es.score) AS avg_score,
    MAX(es.score) AS max_score,
    MIN(es.score) AS min_score
FROM exam_sessions es
GROUP BY es.exam_id;
```

**用途**：教师查看考试统计，无需手写复杂聚合查询

### 4.4 学生成绩排名视图

```sql
CREATE VIEW v_student_rankings AS
SELECT
    es.exam_id,
    u.nickname,
    es.score,
    RANK() OVER (PARTITION BY es.exam_id ORDER BY es.score DESC) AS rank_position
FROM exam_sessions es
JOIN users u ON es.student_id = u.id
WHERE es.status = 'GRADED';
```

**使用了窗口函数 RANK()**：

- `PARTITION BY es.exam_id` - 按考试分组排名
- `ORDER BY es.score DESC` - 按分数降序
- RANK() - 相同分数排名相同，下一排名跳过（如：1,1,3）

---

## 五、数据安全

### 5.1 逻辑删除

所有核心表都有 `deleted` 字段：

```sql
deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识'
```

**MyBatis-Plus 配置**：

```java
@TableLogic
private Boolean deleted;
```

**效果**：
- `DELETE` 操作自动转为 `UPDATE SET deleted = 1`
- `SELECT` 自动过滤 `deleted = 1` 的记录

**为什么不用物理删除？**

1. 数据可恢复
2. 审计追溯需要
3. 外键约束不会因删除而失效

### 5.2 外键级联

```sql
FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
```

**CASCADE 的含义**：删除课程时，自动删除所有选课记录

**为什么不全部用 CASCADE？**

| 级联方式 | 适用场景 |
|----------|----------|
| CASCADE | 从属数据（选课记录） |
| RESTRICT | 核心数据（有考试的课程不能删除） |
| SET NULL | 可选关联（考试可以不关联试卷） |

---

## 六、性能考虑

### 6.1 JSON 字段性能

| 操作 | 性能影响 | 建议 |
|------|----------|------|
| 写入 | 略慢于普通字段 | 可接受 |
| 读取 | 同普通字段 | 可接受 |
| 查询(JSON内字段) | 较慢 | 避免在WHERE中使用JSON字段 |

### 6.2 分页查询

所有列表查询都支持分页，避免一次性加载大量数据：

```sql
SELECT * FROM questions LIMIT 10 OFFSET 0;
```

### 6.3 连接池配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

---

## 七、数据库初始化

### 初始化顺序

```bash
# 1. 创建数据库和表结构
mysql -u root -p < database/init.sql

# 2. 导入测试数据
mysql -u root -p < database/data.sql
```

**为什么分成两个文件？**

- `init.sql` - 表结构，生产环境使用
- `data.sql` - 测试数据，仅开发环境使用
