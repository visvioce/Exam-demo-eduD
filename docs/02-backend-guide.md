# 后端技术详解

## 一、技术栈概览

### 核心框架与依赖

后端基于 **Spring Boot 3.3.5** 构建，采用分层架构设计。

| 依赖 | 版本 | 用途 |
|------|------|------|
| spring-boot-starter-web | 3.3.5 | Web MVC框架 |
| spring-boot-starter-security | - | 安全框架 |
| mybatis-plus-spring-boot3-starter | 3.5.7 | ORM框架（MyBatis增强版） |
| mysql-connector-j | - | MySQL驱动 |
| jjwt-api / jjwt-impl / jjwt-jackson | 0.12.3 | JWT Token处理 |
| hutool-all | 5.8.23 | Java工具库 |
| springdoc-openapi-starter-webmvc-ui | 2.6.0 | API文档（Swagger UI） |
| lombok | - | 代码简化 |

### Maven 配置 (`pom.xml`)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.5</version>
</parent>

<properties>
    <java.version>17</java.version>
    <mybatis-plus.version>3.5.7</mybatis-plus.version>
    <jjwt.version>0.12.3</jjwt.version>
    <hutool.version>5.8.23</hutool.version>
</properties>
```

---

## 二、项目结构

```
backend/src/main/java/com/southcollege/exam/
├── ExamApplication.java          # 应用入口
│
├── controller/                   # 控制器层 - 处理HTTP请求
│   ├── AuthController.java       # 认证（登录/注册）
│   ├── UserController.java       # 用户管理
│   ├── CourseController.java     # 课程管理
│   ├── QuestionController.java   # 题目管理
│   ├── PaperController.java      # 试卷管理
│   ├── ExamController.java       # 考试管理
│   ├── ExamSessionController.java# 考试记录
│   ├── AnnouncementController.java# 公告管理
│   ├── AiConfigController.java   # AI配置
│   └── AiQuestionController.java # AI出题
│
├── service/                      # 服务层 - 业务逻辑
│   ├── UserService.java          # 用户服务
│   ├── CourseService.java        # 课程服务
│   ├── QuestionService.java      # 题目服务
│   ├── PaperService.java         # 试卷服务
│   ├── ExamService.java          # 考试服务
│   ├── ExamSessionService.java   # 考试记录服务
│   ├── AnnouncementService.java  # 公告服务
│   ├── AiConfigService.java      # AI配置服务
│   ├── AiQuestionService.java    # AI出题服务
│   ├── CarouselService.java      # 轮播图服务
│   └── CourseMemberService.java  # 选课服务
│
├── mapper/                       # 数据访问层 - 数据库操作
│   ├── UserMapper.java
│   ├── CourseMapper.java
│   ├── QuestionMapper.java
│   ├── PaperMapper.java
│   ├── ExamMapper.java
│   ├── ExamSessionMapper.java
│   ├── AnnouncementMapper.java
│   ├── AiConfigMapper.java
│   ├── CarouselMapper.java
│   └── CourseMemberMapper.java
│
├── entity/                       # 实体类 - 数据库表映射
│   ├── User.java
│   ├── Course.java
│   ├── Question.java
│   ├── Paper.java
│   ├── Exam.java
│   ├── ExamSession.java
│   ├── Announcement.java
│   ├── AiConfig.java
│   ├── Carousel.java
│   └── CourseMember.java
│
├── dto/                          # 数据传输对象
│   ├── request/                  # 请求DTO
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── ChangePasswordRequest.java
│   │   ├── CourseRequest.java
│   │   ├── QuestionRequest.java
│   │   ├── PaperRequest.java
│   │   ├── ExamRequest.java
│   │   ├── SubmitExamRequest.java
│   │   ├── GradeAnswerRequest.java
│   │   ├── AnnouncementRequest.java
│   │   └── AiConfigRequest.java
│   └── response/                 # 响应DTO
│       ├── LoginResponse.java
│       ├── UserInfoResponse.java
│       ├── ExamDetailResponse.java
│       ├── ExamResultResponse.java
│       ├── QuestionResponse.java
│       ├── PaperDetailResponse.java
│       ├── CourseDetailResponse.java
│       └── AnnouncementResponse.java
│
├── config/                       # 配置类
│   ├── SecurityConfig.java       # Spring Security配置
│   ├── WebConfig.java            # Web配置（CORS等）
│   ├── MyBatisPlusConfig.java    # MyBatis-Plus配置
│   ├── JwtInterceptor.java       # JWT拦截器
│   ├── RoleInterceptor.java      # 角色权限拦截器
│   ├── WebMvcConfig.java         # MVC配置（注册拦截器）
│   └── OpenApiConfig.java        # OpenAPI配置
│
├── enums/                        # 枚举类
│   ├── RoleEnum.java             # 用户角色
│   ├── UserStatus.java           # 用户状态
│   ├── QuestionType.java         # 题目类型
│   ├── Difficulty.java           # 难度等级
│   ├── ExamStatus.java           # 考试状态
│   └── SessionStatus.java        # 考试记录状态
│
├── utils/                        # 工具类
│   ├── JwtUtil.java              # JWT工具
│   ├── PasswordUtil.java         # 密码加密工具
│   ├── JsonUtil.java             # JSON处理工具
│   ├── TimeUtil.java             # 时间工具
│   └── Result.java               # 统一响应封装
│
├── annotation/                   # 自定义注解
│   └── RequireRole.java          # 角色权限注解
│
└── exception/                    # 异常处理
    ├── BusinessException.java    # 业务异常
    └── GlobalExceptionHandler.java# 全局异常处理
```

---

## 三、实体类详解

### 3.1 User (用户实体)

**文件位置**: `backend/src/main/java/com/southcollege/exam/entity/User.java`

```java
@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;      // 用户名，唯一，3-20字符
    private String password;      // 密码（BCrypt加密）
    private String nickname;      // 昵称
    private String avatar;        // 头像URL
    
    @TableField(typeHandler = EnumTypeHandler.class)
    private RoleEnum role;        // 角色：ADMIN/TEACHER/STUDENT
    
    @TableField(typeHandler = EnumTypeHandler.class)
    private UserStatus status;    // 状态：ACTIVE/INACTIVE/SUSPENDED
    
    @TableLogic
    private Boolean deleted;      // 逻辑删除标识
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 3.2 Question (题目实体)

**文件位置**: `backend/src/main/java/com/southcollege/exam/entity/Question.java`

```java
@Data
@TableName("questions")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String content;       // 题目内容
    
    @TableField(typeHandler = EnumTypeHandler.class)
    private QuestionType type;    // 题型：SINGLE_CHOICE/MULTIPLE_CHOICE/TRUE_FALSE/FILL_BLANK/ESSAY
    
    @TableField(typeHandler = EnumTypeHandler.class)
    private Difficulty difficulty;// 难度：EASY/MEDIUM/HARD
    
    private Integer score;        // 分值
    private Long teacherId;       // 创建者ID
    private String subject;       // 学科
    
    // JSON字段（使用TypeHandler处理）
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> options; // 选项列表（选择题/判断题）
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object correctAnswer; // 正确答案（支持多种格式）
    
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object scoringCriteria; // 评分标准（主观题）
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**题型说明**:

| 题型 | 枚举值 | correctAnswer格式 | 说明 |
|------|--------|------------------|------|
| 单选题 | SINGLE_CHOICE | `"A"` 或 `"B"` | 字符串，表示正确选项 |
| 多选题 | MULTIPLE_CHOICE | `["A", "B", "C"]` | JSON数组，多个正确选项 |
| 判断题 | TRUE_FALSE | `"A"` 或 `"true"` | A=正确，B=错误；或 true/false |
| 填空题 | FILL_BLANK | `["答案1", "答案2"]` | JSON数组，支持多个空、多个答案 |
| 简答题 | ESSAY | 无 | 使用 scoringCriteria 评分标准 |

### 3.3 Paper (试卷实体)

**文件位置**: `backend/src/main/java/com/southcollege/exam/entity/Paper.java`

```java
@Data
@TableName("papers")
public class Paper {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;          // 试卷名称
    private Long courseId;        // 所属课程
    private Long teacherId;       // 创建者ID
    private Integer totalScore;   // 总分
    
    @TableField(typeHandler = EnumTypeHandler.class)
    private PaperType type;       // 组卷方式：MANUAL/AUTO
    
    // 题目配置：[{questionId: 1, score: 10}, {questionId: 2, score: 15}, ...]
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> questions;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 3.4 Exam (考试实体)

**文件位置**: `backend/src/main/java/com/southcollege/exam/entity/Exam.java`

```java
@Data
@TableName("exams")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;         // 考试标题
    private Long courseId;        // 所属课程
    private Long paperId;         // 关联试卷
    private Long teacherId;       // 创建者ID
    
    private LocalDateTime startedAt;  // 开始时间
    private LocalDateTime endedAt;    // 结束时间
    private Integer duration;         // 时长（分钟）
    private Integer totalScore;       // 总分
    
    @TableField(typeHandler = EnumTypeHandler.class)
    private ExamStatus status;    // 状态：DRAFT/PUBLISHED/STARTED/ENDED/CANCELLED
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**考试状态流转**:

```
DRAFT (草稿) ──发布──▶ PUBLISHED (已发布) ──到达开始时间──▶ STARTED (进行中)
     │                                                        │
     │                                                        ▼
     └──取消──▶ CANCELLED (已取消) ◀──────────────────── ENDED (已结束)
```

### 3.5 ExamSession (考试记录实体)

**文件位置**: `backend/src/main/java/com/southcollege/exam/entity/ExamSession.java`

```java
@Data
@TableName("exam_sessions")
public class ExamSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long examId;          // 考试ID
    private Long studentId;       // 学生ID
    
    private LocalDateTime startedAt;   // 开始答题时间
    private LocalDateTime submittedAt; // 提交时间
    private Double score;              // 得分
    
    // 答案：{questionId: {answer: "A", score: 10, comment: "正确"}, ...}
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> answers;
    
    @TableField(typeHandler = EnumTypeHandler.class)
    private SessionStatus status; // 状态：NOT_STARTED/IN_PROGRESS/SUBMITTED/GRADED
    
    @TableField(typeHandler = EnumTypeHandler.class)
    private GradingStatus gradingStatus; // 评分状态：PENDING/GRADING/COMPLETED
    
    @Version
    private Integer version;      // 乐观锁版本号（防止并发提交）
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 四、Controller层 - API端点

### 4.1 API 端点总览

| Controller | 基础路径 | 主要功能 |
|------------|----------|----------|
| AuthController | `/api/auth` | 登录、注册、密码管理 |
| UserController | `/api/users` | 用户CRUD |
| CourseController | `/api/courses` | 课程管理、选课 |
| QuestionController | `/api/questions` | 题目管理 |
| PaperController | `/api/papers` | 试卷管理、自动组卷 |
| ExamController | `/api/exams` | 考试管理、参加考试 |
| ExamSessionController | `/api/exam-sessions` | 考试记录、评分 |
| AnnouncementController | `/api/announcements` | 公告管理 |
| AiConfigController | `/api/ai-configs` | AI配置管理 |
| AiQuestionController | `/api/ai` | AI出题 |

### 4.2 认证接口 (AuthController)

**文件位置**: `backend/src/main/java/com/southcollege/exam/controller/AuthController.java`

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    // 用户登录
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request);
    
    // 用户注册（强制学生角色）
    @PostMapping("/register")
    public Result<UserInfoResponse> register(@RequestBody RegisterRequest request);
    
    // 获取当前用户信息
    @GetMapping("/me")
    public Result<UserInfoResponse> getCurrentUser();
    
    // 修改密码
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request);
    
    // 更新个人资料
    @PutMapping("/profile")
    public Result<UserInfoResponse> updateProfile(@RequestBody UpdateProfileRequest request);
}
```

### 4.3 考试接口 (ExamController)

**文件位置**: `backend/src/main/java/com/southcollege/exam/controller/ExamController.java`

```java
@RestController
@RequestMapping("/api/exams")
public class ExamController {
    
    // ========== 管理员/教师接口 ==========
    
    @GetMapping("/page")
    public Result<PageResult<Exam>> getExamPage(...);  // 分页查询
    
    @GetMapping
    public Result<List<Exam>> getAllExams();           // 获取全部
    
    @GetMapping("/{id}")
    public Result<ExamDetailResponse> getExamDetail(@PathVariable Long id);
    
    @PostMapping
    public Result<Exam> createExam(@RequestBody ExamRequest request);  // 创建
    
    @PutMapping("/{id}")
    public Result<Exam> updateExam(@PathVariable Long id, @RequestBody ExamRequest request);
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteExam(@PathVariable Long id);
    
    @PostMapping("/{id}/publish")
    public Result<Exam> publishExam(@PathVariable Long id);  // 发布
    
    @PostMapping("/{id}/cancel")
    public Result<Exam> cancelExam(@PathVariable Long id);   // 取消
    
    // ========== 学生接口 ==========
    
    @GetMapping("/published")
    public Result<List<Exam>> getPublishedExams();     // 已发布考试
    
    @GetMapping("/my")
    public Result<List<Exam>> getMyExams();            // 我的考试
    
    @PostMapping("/{id}/start")
    public Result<ExamSession> startExam(@PathVariable Long id);  // 开始考试
    
    @PostMapping("/{id}/submit")
    public Result<ExamSession> submitExam(@PathVariable Long id, @RequestBody SubmitExamRequest request);
    
    @PutMapping("/{id}/auto-save")
    public Result<Void> autoSave(@PathVariable Long id, @RequestBody AutoSaveRequest request);
    
    @GetMapping("/{id}/questions")
    public Result<List<QuestionResponse>> getExamQuestions(@PathVariable Long id);
}
```

### 4.4 统一响应格式

所有API返回统一的 `Result<T>` 格式：

```java
@Data
public class Result<T> {
    private Integer code;    // 状态码：200成功，其他失败
    private String message;  // 提示信息
    private T data;          // 响应数据
}
```

**示例响应**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "username": "admin",
        "role": "ADMIN"
    }
}
```

---

## 五、Service层 - 业务逻辑

### 5.1 用户服务 (UserService)

**文件位置**: `backend/src/main/java/com/southcollege/exam/service/UserService.java`

核心方法：

```java
@Service
public class UserService {
    
    // 用户登录
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 2. 验证密码（BCrypt）
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 3. 检查状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("账号已被禁用");
        }
        
        // 4. 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        return new LoginResponse(token, UserInfoResponse.from(user));
    }
    
    // 用户注册
    public UserInfoResponse register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 2. 创建用户（密码加密）
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword())); // BCrypt加密
        user.setNickname(request.getNickname());
        user.setRole(RoleEnum.STUDENT);  // 强制学生角色
        user.setStatus(UserStatus.ACTIVE);
        
        userMapper.insert(user);
        return UserInfoResponse.from(user);
    }
}
```

### 5.2 考试服务 (ExamService)

**文件位置**: `backend/src/main/java/com/southcollege/exam/service/ExamService.java`

核心业务逻辑：

#### 5.2.1 自动评分算法

```java
// 计算单题得分
private double calculateQuestionScore(Question question, Object userAnswer) {
    QuestionType type = question.getType();
    Object correctAnswer = question.getCorrectAnswer();
    
    switch (type) {
        case SINGLE_CHOICE:
            // 单选：字符串比较（忽略大小写）
            return userAnswer.toString().equalsIgnoreCase(correctAnswer.toString()) 
                ? question.getScore() : 0;
                
        case MULTIPLE_CHOICE:
            // 多选：JSON数组排序后比较
            List<String> userAnswerList = sortAndConvert(userAnswer);
            List<String> correctAnswerList = sortAndConvert(correctAnswer);
            return userAnswerList.equals(correctAnswerList) 
                ? question.getScore() : 0;
                
        case TRUE_FALSE:
            // 判断：支持 A/B 和 true/false 两种格式
            String ua = normalizeTrueFalseAnswer(userAnswer);
            String ca = normalizeTrueFalseAnswer(correctAnswer);
            return ua.equals(ca) ? question.getScore() : 0;
            
        case FILL_BLANK:
            // 填空：支持多答案匹配
            return calculateFillBlankScore(question, userAnswer);
            
        case ESSAY:
            // 简答：主观题，需要教师评分
            return 0;  // 默认0分，等待教师评分
            
        default:
            return 0;
    }
}
```

#### 5.2.2 提交考试（乐观锁）

```java
@Transactional
public ExamSession submitExam(Long examId, Long studentId, SubmitExamRequest request) {
    // 1. 获取考试记录
    ExamSession session = examSessionMapper.selectByExamAndStudent(examId, studentId);
    
    if (session == null || session.getStatus() != SessionStatus.IN_PROGRESS) {
        throw new BusinessException("考试记录不存在或状态异常");
    }
    
    // 2. 检查考试时间
    Exam exam = examMapper.selectById(examId);
    if (LocalDateTime.now().isAfter(exam.getEndedAt())) {
        throw new BusinessException("考试已结束，无法提交");
    }
    
    // 3. 保存答案并计算客观题分数
    Map<String, Object> answers = new HashMap<>();
    double totalScore = 0;
    boolean hasSubjective = false;
    
    for (AnswerItem item : request.getAnswers()) {
        Question question = questionMapper.selectById(item.getQuestionId());
        double score = calculateQuestionScore(question, item.getAnswer());
        
        if (question.getType() == QuestionType.ESSAY || question.getType() == QuestionType.FILL_BLANK) {
            hasSubjective = true;
        } else {
            totalScore += score;
        }
        
        answers.put(item.getQuestionId().toString(), Map.of(
            "answer", item.getAnswer(),
            "score", score,
            "comment", ""
        ));
    }
    
    // 4. 更新考试记录（乐观锁）
    session.setAnswers(answers);
    session.setScore(totalScore);
    session.setSubmittedAt(LocalDateTime.now());
    session.setStatus(SessionStatus.SUBMITTED);
    session.setGradingStatus(hasSubjective ? GradingStatus.PENDING : GradingStatus.COMPLETED);
    
    // 乐观锁更新：如果version不匹配，更新失败
    int rows = examSessionMapper.updateById(session);
    if (rows == 0) {
        throw new BusinessException("提交失败，请重试（并发冲突）");
    }
    
    return session;
}
```

### 5.3 试卷服务 (PaperService)

**文件位置**: `backend/src/main/java/com/southcollege/exam/service/PaperService.java`

自动组卷算法：

```java
public Paper autoGeneratePaper(AutoPaperRequest request) {
    // 1. 按条件筛选题目
    LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(request.getCourseId() != null, Question::getCourseId, request.getCourseId())
           .eq(request.getDifficulty() != null, Question::getDifficulty, request.getDifficulty())
           .eq(request.getSubject() != null, Question::getSubject, request.getSubject())
           .eq(request.getType() != null, Question::getType, request.getType());
    
    List<Question> candidates = questionMapper.selectList(wrapper);
    
    // 2. Fisher-Yates 洗牌算法随机打乱
    for (int i = candidates.size() - 1; i > 0; i--) {
        int j = (int) (Math.random() * (i + 1));
        Collections.swap(candidates, i, j);
    }
    
    // 3. 取前N道题
    List<Question> selected = candidates.stream()
        .limit(request.getQuestionCount())
        .collect(Collectors.toList());
    
    // 4. 组装试卷
    Paper paper = new Paper();
    paper.setName(request.getName());
    paper.setType(PaperType.AUTO);
    
    List<Map<String, Object>> questions = selected.stream()
        .map(q -> Map.of(
            "questionId", q.getId(),
            "score", request.getScorePerQuestion()
        ))
        .collect(Collectors.toList());
    
    paper.setQuestions(questions);
    paper.setTotalScore(request.getQuestionCount() * request.getScorePerQuestion());
    
    paperMapper.insert(paper);
    return paper;
}
```

---

## 六、安全配置

### 6.1 Spring Security 配置

**文件位置**: `backend/src/main/java/com/southcollege/exam/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（使用JWT，不需要CSRF保护）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 允许所有请求（由JWT拦截器处理认证）
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            
            // 禁用Session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### 6.2 JWT 拦截器

**文件位置**: `backend/src/main/java/com/southcollege/exam/config/JwtInterceptor.java`

```java
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取Authorization Header
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return false;
        }
        
        // 2. 提取并验证Token
        String token = authHeader.substring(7);
        
        try {
            // 3. 解析Token获取用户信息
            Claims claims = JwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.get("username", String.class);
            String role = claims.get("role", String.class);
            
            // 4. 存入请求属性
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);
            
            return true;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }
    }
}
```

### 6.3 角色权限注解

**文件位置**: `backend/src/main/java/com/southcollege/exam/annotation/RequireRole.java`

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    RoleEnum[] value();
}
```

**使用示例**:

```java
@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    
    @RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
    @PostMapping
    public Result<Question> createQuestion(@RequestBody QuestionRequest request) {
        // 只有管理员和教师可以创建题目
    }
}
```

**拦截器实现**:

**文件位置**: `backend/src/main/java/com/southcollege/exam/config/RoleInterceptor.java`

```java
@Component
public class RoleInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
            
            if (annotation != null) {
                String userRole = (String) request.getAttribute("role");
                RoleEnum userRoleEnum = RoleEnum.valueOf(userRole);
                
                boolean hasRole = Arrays.stream(annotation.value())
                    .anyMatch(role -> role == userRoleEnum);
                
                if (!hasRole) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
                    return false;
                }
            }
        }
        return true;
    }
}
```

---

## 七、配置文件

### application.yml

**文件位置**: `backend/src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: your-secret-key-at-least-256-bits-long
  expiration: 86400000  # 24小时
```

---

## 八、API文档

启动项目后访问 Swagger UI：

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI 文档：

```
http://localhost:8080/v3/api-docs
```
