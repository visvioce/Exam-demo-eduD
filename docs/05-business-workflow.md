# 业务流程详解

## 一、核心业务流程概览

本系统围绕"在线考试"这一核心场景，涉及以下主要流程：

```
用户认证 → 课程管理 → 题库建设 → 试卷组卷 → 考试发布 → 在线答题 → 自动评分 → 成绩查看
```

---

## 二、用户认证流程

### 2.1 登录流程详解

**为什么用 JWT 而不是 Session？**

| 对比项 | Session | JWT | 本项目选择 |
|--------|---------|-----|-----------|
| 存储位置 | 服务端内存/Redis | 客户端 | JWT |
| 扩展性 | 需要Session共享 | 无状态，天然支持分布式 | JWT |
| 跨域 | 需要额外配置 | 天然支持 | JWT |
| 安全性 | SessionID泄露风险 | Token泄露风险，可设置短期有效期 | JWT |

**完整登录流程**：

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  用户    │     │  前端    │     │  后端    │     │ 数据库   │
└────┬─────┘     └────┬─────┘     └────┬─────┘     └────┬─────┘
     │ 输入用户名密码  │                │                │
     │───────────────▶│                │                │
     │                │ POST /auth/login               │
     │                │───────────────▶│                │
     │                │                │ SELECT * FROM users
     │                │                │ WHERE username=?
     │                │                │───────────────▶│
     │                │                │◀───────────────│
     │                │                │                │
     │                │                │ BCrypt验证密码  │
     │                │                │ ┌────────────┐ │
     │                │                │ │密码哈希对比│ │
     │                │                │ │hash(输入) │ │
     │                │                │ │== stored  │ │
     │                │                │ └────────────┘ │
     │                │                │                │
     │                │                │ 生成JWT Token │
     │                │                │ ┌────────────┐ │
     │                │                │ │Header:算法 │ │
     │                │                │ │Payload:   │ │
     │                │                │ │ userId    │ │
     │                │                │ │ username  │ │
     │                │                │ │ role      │ │
     │                │                │ │ exp(过期) │ │
     │                │                │ │Signature  │ │
     │                │                │ └────────────┘ │
     │                │◀───────────────│                │
     │                │ { token, user }│                │
     │                │                │                │
     │                │ 存储到localStorage              │
     │                │ ┌────────────┐ │                │
     │◀───────────────│ │跳转首页    │ │                │
     │ 登录成功       │ └────────────┘ │                │
```

**后端代码解析**：

```java
public LoginResponse login(LoginRequest request) {
    // 1. 查询用户
    User user = userMapper.selectByUsername(request.getUsername());
    
    // 为什么先查用户再验证密码，而不是一条SQL？
    // 答：需要返回具体的错误信息（用户不存在 vs 密码错误）
    // 安全上：实际不会区分，统一返回"用户名或密码错误"
    
    if (user == null) {
        throw new BusinessException("用户名或密码错误");  // 不暴露具体原因
    }
    
    // 2. 密码验证 - 为什么用 BCrypt？
    // 答：BCrypt 自带盐值，每次加密结果不同，防止彩虹表攻击
    if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
        throw new BusinessException("用户名或密码错误");
    }
    
    // 3. 状态检查
    if (user.getStatus() != UserStatus.ACTIVE) {
        throw new BusinessException("账号已被禁用，请联系管理员");
    }
    
    // 4. 生成 JWT
    String token = JwtUtil.generateToken(
        user.getId(), 
        user.getUsername(), 
        user.getRole()
    );
    
    return new LoginResponse(token, UserInfoResponse.from(user));
}
```

### 2.2 Token 验证机制

**每次请求如何验证身份？**

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│  前端    │     │  拦截器  │     │ Controller│
└────┬─────┘     └────┬─────┘     └────┬─────┘
     │ 请求 + Authorization Header   │
     │ Authorization: Bearer xxx.xxx.xxx
     │───────────────▶│                │
     │                │ 1. 提取Token   │
     │                │ 2. 验证签名    │
     │                │ 3. 检查过期    │
     │                │ 4. 解析用户信息 │
     │                │                │
     │                │ 验证失败?      │
     │                │ ┌────┐        │
     │◀───────────────│ │401 │        │
     │                │ └────┘        │
     │                │                │
     │                │ 验证成功?      │
     │                │ 存入request.setAttribute()
     │                │───────────────▶│
     │                │                │ 处理业务
     │                │                │ 使用request.getAttribute("userId")
     │◀───────────────────────────────│
     │                │ 返回结果       │
```

**为什么用拦截器而不是过滤器？**

| 对比 | 过滤器(Filter) | 拦截器(Interceptor) |
|------|---------------|-------------------|
| 所属 | Servlet容器 | Spring框架 |
| 执行时机 | DispatcherServlet之前 | Controller之前 |
| 注入Bean | 不支持 | 支持 |
| 本项目 | 用拦截器 | 更灵活，可注入UserService等 |

---

## 三、课程管理流程

### 3.1 教师创建课程

```
教师 ──▶ 填写课程信息 ──▶ 提交 ──▶ 后端验证 ──▶ 保存数据库
                                  │
                                  ├─ 课程代码唯一性检查
                                  ├─ 教师身份验证
                                  └─ 字段格式验证
```

**为什么课程代码要唯一？**

- 课程代码是业务标识（如 `CS101`），便于记忆和引用
- 不同教师可能创建同名课程，但代码必须唯一
- 方便学生通过代码查找课程

### 3.2 学生选课流程

```
┌──────────┐                      ┌──────────┐
│  学生    │                      │  系统    │
└────┬─────┘                      └────┬─────┘
     │ 1. 浏览课程列表                 │
     │◀───────────────────────────────│
     │                                │
     │ 2. 选择课程，点击"加入"         │
     │───────────────────────────────▶│
     │                                │
     │                        ┌───────┴───────┐
     │                        │ 3. 验证       │
     │                        │ - 是否已加入  │
     │                        │ - 课程是否开放│
     │                        │ - 是否已截止  │
     │                        └───────┬───────┘
     │                                │
     │                        ┌───────┴───────┐
     │                        │ 4. 创建记录   │
     │                        │ INSERT INTO   │
     │                        │ course_members│
     │                        └───────┬───────┘
     │◀───────────────────────────────│
     │ 5. 选课成功                     │
```

**为什么选课要检查这么多条件？**

| 检查项 | 原因 |
|--------|------|
| 是否已加入 | 防止重复选课（数据库唯一键也会拦截） |
| 课程是否开放 | INACTIVE 的课程不应被选 |
| 是否已截止 | 超过 deadline 不允许选课 |

---

## 四、考试流程（核心）

### 4.1 考试生命周期

```
创建考试 ──▶ 发布考试 ──▶ 开始考试 ──▶ 结束考试
   │            │            │            │
   │            │            │            │
   ▼            ▼            ▼            ▼
 DRAFT      PUBLISHED     STARTED       ENDED
 (草稿)      (已发布)      (进行中)      (已结束)
   │            │
   │            └──────────▶ CANCELLED (取消)
   │
   └──────────▶ 可编辑/删除
```

**为什么要设计这么多状态？**

| 状态 | 允许的操作 | 设计原因 |
|------|-----------|----------|
| DRAFT | 编辑、删除、发布 | 草稿可以随意修改 |
| PUBLISHED | 取消 | 发布后不可修改，保证公平 |
| STARTED | 无 | 进行中任何人都不能操作 |
| ENDED | 查看结果 | 考试结束，只能查看 |
| CANCELLED | 无 | 取消后不能恢复 |

### 4.2 学生答题流程（详细）

```
┌─────────────────────────────────────────────────────────────────┐
│                         学生答题完整流程                          │
└─────────────────────────────────────────────────────────────────┘

1. 查看考试
   ┌─────┐     GET /exams/published     ┌─────┐
   │学生 │ ──────────────────────────▶  │后端  │
   └─────┘     返回可参加的考试列表      └─────┘

2. 开始考试
   ┌─────┐     POST /exams/{id}/start   ┌─────┐     ┌─────┐
   │学生 │ ──────────────────────────▶  │后端 │ ──▶ │数据库│
   └─────┘                              └─────┘     └─────┘
                                                   创建 exam_session
                                                   status = IN_PROGRESS
                                                   started_at = NOW()

3. 获取题目
   ┌─────┐     GET /exams/{id}/questions ┌─────┐
   │学生 │ ◀───────────────────────────  │后端  │
   └─────┘     返回题目（不含正确答案）    └─────┘
   
   为什么不含正确答案？
   → 防止学生通过接口获取答案作弊

4. 答题（自动保存）
   ┌─────┐     PUT /exams/{id}/auto-save ┌─────┐
   │学生 │ ──────────────────────────▶   │后端  │
   └─────┘     每分钟自动保存答案         └─────┘
   
   为什么自动保存？
   → 防止意外关闭浏览器/断电导致答案丢失
   → 考试时间到自动提交

5. 提交考试
   ┌─────┐     POST /exams/{id}/submit   ┌─────────────────────┐
   │学生 │ ──────────────────────────▶   │       后端          │
   └─────┘                               └──────────┬──────────┘
                                                   │
                              ┌────────────────────┼────────────────────┐
                              │                    │                    │
                              ▼                    ▼                    ▼
                        ┌──────────┐        ┌──────────┐        ┌──────────┐
                        │ 验证时间 │        │ 保存答案 │        │ 自动评分 │
                        │是否超时  │        │ 更新状态 │        │(客观题)  │
                        └──────────┘        └──────────┘        └──────────┘
```

### 4.3 自动评分算法详解

**为什么需要区分题型？**

不同题型的答案格式和匹配逻辑完全不同：

```java
private double calculateQuestionScore(Question question, Object userAnswer) {
    switch (question.getType()) {
        
        // ========== 单选题 ==========
        case SINGLE_CHOICE:
            // 用户答案: "A"
            // 正确答案: "B"
            // 直接字符串比较（忽略大小写）
            return userAnswer.toString().equalsIgnoreCase(
                correctAnswer.toString()
            ) ? question.getScore() : 0;
        
        // ========== 多选题 ==========
        case MULTIPLE_CHOICE:
            // 用户答案: ["A", "B", "C"]
            // 正确答案: ["A", "C", "D"]
            // 必须完全匹配（顺序无关）
            List<String> userAnswerList = sortAndConvert(userAnswer);
            List<String> correctAnswerList = sortAndConvert(correctAnswer);
            return userAnswerList.equals(correctAnswerList) 
                ? question.getScore() : 0;
            
            // 为什么多选题不采用"部分正确给部分分"？
            // 答：业务需求决定，当前设计是全对才给分
            // 如果需要部分给分，可以计算交集比例
            
        // ========== 判断题 ==========
        case TRUE_FALSE:
            // 用户答案可能是: "A" / "B" / "true" / "false"
            // 需要标准化后比较
            String ua = normalizeTrueFalseAnswer(userAnswer);
            String ca = normalizeTrueFalseAnswer(correctAnswer);
            return ua.equals(ca) ? question.getScore() : 0;
            
            // normalizeTrueFalseAnswer 逻辑:
            // "A" 或 "true" → "true"
            // "B" 或 "false" → "false"
        
        // ========== 填空题 ==========
        case FILL_BLANK:
            // 用户答案: ["答案1", "答案2"]
            // 正确答案: ["答案1", "备选答案1", "答案2", "备选答案2"]
            // 支持多个空、每个空多个可选答案
            return calculateFillBlankScore(question, userAnswer);
            
            // calculateFillBlankScore 逻辑:
            // 按空逐一匹配，每个空答对得部分分
            
        // ========== 简答题 ==========
        case ESSAY:
            // 主观题无法自动评分
            // 返回0分，等待教师评分
            return 0;
    }
}
```

### 4.4 乐观锁防并发提交

**问题场景**：

```
学生同时打开两个浏览器窗口答题
窗口A：答题到10题，点击提交
窗口B：答题到15题，点击提交

如果没有锁：两次提交都会成功，成绩可能混乱
```

**乐观锁解决方案**：

```java
@Transactional
public ExamSession submitExam(Long examId, Long studentId, SubmitExamRequest request) {
    // 1. 查询考试记录
    ExamSession session = examSessionMapper.selectByExamAndStudent(examId, studentId);
    
    // 此时 session.version = 0
    
    // 2. 准备更新数据
    session.setAnswers(request.getAnswers());
    session.setScore(calculatedScore);
    session.setVersion(session.getVersion() + 1);  // version = 1
    
    // 3. 更新（带版本检查）
    // SQL: UPDATE exam_sessions SET ..., version = 1 
    //      WHERE id = ? AND version = 0
    int rows = examSessionMapper.updateById(session);
    
    // 如果另一个请求先提交了，version已经变成1
    // 这个请求的 WHERE version = 0 条件不满足
    // rows = 0，更新失败
    
    if (rows == 0) {
        throw new BusinessException("提交失败，请刷新页面后重试");
    }
    
    return session;
}
```

**为什么用乐观锁而不是悲观锁？**

| 对比 | 悲观锁 | 乐观锁 |
|------|--------|--------|
| 实现 | SELECT FOR UPDATE | version 字段 |
| 性能 | 阻塞等待 | 失败重试 |
| 适用场景 | 冲突频繁 | 冲突较少 |
| 本项目 | - | 考试提交冲突很少，用乐观锁更合适 |

---

## 五、组卷流程

### 5.1 手动组卷

```
教师 ──▶ 选择课程 ──▶ 从题库选题 ──▶ 设置每题分值 ──▶ 保存试卷
```

**手动组卷的 questions JSON**：

```json
[
    { "questionId": 1, "score": 10 },  // 第1题，单选，10分
    { "questionId": 5, "score": 15 },  // 第2题，多选，15分
    { "questionId": 12, "score": 20 }  // 第3题，简答，20分
]
```

### 5.2 自动组卷算法

**Fisher-Yates 洗牌算法**：

```java
public Paper autoGeneratePaper(AutoPaperRequest request) {
    // 1. 筛选符合条件的题目
    List<Question> candidates = questionMapper.selectList(wrapper);
    
    // 2. Fisher-Yates 洗牌（为什么用这个算法？）
    // 答：保证每个题目被选中的概率相等，真正的随机
    for (int i = candidates.size() - 1; i > 0; i--) {
        int j = (int) (Math.random() * (i + 1));
        Collections.swap(candidates, i, j);  // 交换位置
    }
    
    // 3. 取前N道题
    List<Question> selected = candidates.subList(0, request.getQuestionCount());
    
    // 4. 组装试卷
    Paper paper = new Paper();
    paper.setQuestions(selected.stream()
        .map(q -> Map.of("questionId", q.getId(), "score", request.getScorePerQuestion()))
        .collect(Collectors.toList())
    );
    
    return paper;
}
```

**为什么用 Fisher-Yates 而不是 Collections.shuffle()？**

其实 `Collections.shuffle()` 内部就是 Fisher-Yates 算法！这里手动实现是为了：
1. 演示原理
2. 方便添加自定义逻辑（如按题型分组后洗牌）

---

## 六、权限控制流程

### 6.1 三级权限体系

```
┌─────────────────────────────────────────────────────┐
│                    ADMIN (管理员)                    │
│  所有权限 + 用户管理 + 系统配置                       │
└───────────────────────┬─────────────────────────────┘
                        │ 包含
┌───────────────────────┴─────────────────────────────┐
│                   TEACHER (教师)                     │
│  管理自己的课程/题目/试卷/考试 + 评分                 │
└───────────────────────┬─────────────────────────────┘
                        │ 包含
┌───────────────────────┴─────────────────────────────┐
│                   STUDENT (学生)                     │
│  选课 + 参加考试 + 查看成绩                          │
└─────────────────────────────────────────────────────┘
```

### 6.2 权限注解实现

**为什么用注解而不是硬编码？**

```java
// 方式1：硬编码（不好）
public Result<?> createQuestion(...) {
    String role = getCurrentUserRole();
    if (!"ADMIN".equals(role) && !"TEACHER".equals(role)) {
        return Result.error("权限不足");
    }
    // 业务逻辑
}

// 方式2：注解（推荐）
@RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
public Result<?> createQuestion(...) {
    // 直接写业务逻辑，权限由拦截器处理
}
```

**注解的好处**：
1. 代码简洁，业务逻辑不被权限检查干扰
2. 权限声明直观，一目了然
3. 统一管理，便于审计

### 6.3 数据隔离

**教师只能看到自己的数据**：

```java
// 课程列表
public List<Course> getTeacherCourses(Long teacherId) {
    return courseMapper.selectList(
        new LambdaQueryWrapper<Course>()
            .eq(Course::getTeacherId, teacherId)  // 只查自己的
    );
}

// 题目列表
public List<Question> getTeacherQuestions(Long teacherId) {
    return questionMapper.selectList(
        new LambdaQueryWrapper<Question>()
            .eq(Question::getTeacherId, teacherId)
    );
}
```

**为什么不用 SQL 过滤？**

其实就是在 SQL 层面过滤（通过 MyBatis-Plus 的条件构造器）。

如果用注解实现数据隔离会更复杂，当前方案简单有效。

---

## 七、异常处理流程

### 7.1 统一异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 业务异常（我们主动抛出的）
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getMessage());
    }
    
    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return Result.error(message);
    }
    
    // 其他异常（未预期的）
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统繁忙，请稍后重试");
    }
}
```

**为什么要区分异常类型？**

| 异常类型 | 返回信息 | 原因 |
|----------|----------|------|
| BusinessException | 异常消息 | 业务逻辑错误，需要告诉用户具体原因 |
| ValidationException | 字段错误列表 | 前端可以高亮错误字段 |
| 其他Exception | "系统繁忙" | 不暴露内部错误，安全考虑 |

---

## 八、缓存策略（未来优化方向）

### 8.1 为什么当前没有缓存？

1. 数据量不大，数据库压力可接受
2. 考试数据实时性要求高
3. 简化架构，降低复杂度

### 8.2 适合缓存的场景

| 数据 | 缓存策略 | 原因 |
|------|----------|------|
| 用户信息 | Redis，30分钟 | 登录后频繁读取 |
| 题库 | Redis，1小时 | 读取多，修改少 |
| 考试状态 | 不缓存 | 需要实时准确 |

### 8.3 实现示例（未来）

```java
@Cacheable(value = "questions", key = "#id")
public Question getQuestionById(Long id) {
    return questionMapper.selectById(id);
}

@CacheEvict(value = "questions", key = "#question.id")
public void updateQuestion(Question question) {
    questionMapper.updateById(question);
}
```
