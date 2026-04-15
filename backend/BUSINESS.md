# 南方职业学院在线考试系统 - 后端业务说明

## 项目概述

基于 Java + Spring Boot + MyBatis-Plus + MySQL 的在线考试系统后端，共 **59 个 Java 文件**，涵盖 10 个业务模块。

---

## 一、认证模块 (Auth)

**文件位置**: `controller/AuthController.java`, `service/UserService.java`

### 业务功能
| 接口 | 方法 | 说明 |
|------|------|------|
| POST /api/auth/login | login() | 用户登录，密码 MD5 加密验证，返回 JWT Token + 用户信息 |
| POST /api/auth/register | register() | 用户注册，支持学生/教师角色，密码加密存储 |
| GET /api/auth/me | getCurrentUser() | 获取当前登录用户信息 |
| POST /api/auth/change-password | changePassword() | 修改密码，需验证原密码 |

### 核心逻辑
- 密码使用 MD5 加密存储
- JWT Token 包含 userId、username、role
- Token 有效期 7 天

---

## 二、用户管理模块 (User)

**文件位置**: `controller/UserController.java`, `service/UserService.java`

### 业务功能
| 接口 | 说明 |
|------|------|
| GET /api/users | 获取用户列表 |
| GET /api/users/{id} | 获取用户详情 |
| POST /api/users | 创建用户（管理员） |
| PUT /api/users/{id} | 更新用户信息 |
| DELETE /api/users/{id} | 删除用户（逻辑删除） |

### 核心逻辑
- 支持按用户名查询
- 返回数据不包含密码字段

---

## 三、课程模块 (Course)

**文件位置**: `controller/CourseController.java`, `service/CourseService.java`

### 业务功能
| 接口 | 说明 |
|------|------|
| GET /api/courses | 获取所有课程 |
| GET /api/courses/active | 获取可加入的课程（未截止） |
| GET /api/courses/my | 获取我加入的课程（学生） |
| GET /api/courses/{id} | 获取课程详情 |
| POST /api/courses | 创建课程（教师） |
| PUT /api/courses/{id} | 更新课程 |
| DELETE /api/courses/{id} | 删除课程 |
| POST /api/courses/{id}/join | 加入课程（学生） |
| POST /api/courses/{id}/leave | 退出课程（学生） |
| GET /api/courses/{id}/members | 获取课程成员列表 |

### 核心逻辑
- 学生加入课程需检查：课程状态、选课截止时间、是否已加入
- 课程成员关联查询

---

## 四、课程成员模块 (CourseMember)

**文件位置**: `service/CourseMemberService.java`

### 业务功能
- 学生加入/退出课程
- 查询课程的学生列表
- 查询学生加入的课程列表

---

## 五、题目模块 (Question)

**文件位置**: `controller/QuestionController.java`, `service/QuestionService.java`

### 业务功能
| 接口 | 说明 |
|------|------|
| GET /api/questions | 获取题目列表 |
| GET /api/questions/{id} | 获取题目详情 |
| GET /api/questions/teacher/{teacherId} | 获取某教师的题目 |
| GET /api/questions/type/{type} | 按类型获取题目 |
| POST /api/questions | 创建题目 |
| PUT /api/questions/{id} | 更新题目 |
| DELETE /api/questions/{id} | 删除题目 |

### 题目类型
- SINGLE_CHOICE: 单选题
- MULTIPLE_CHOICE: 多选题
- JUDGE: 判断题
- FILL: 填空题
- ESSAY: 简答题

### 核心逻辑
- 题目包含 JSON 字段：options（选项）、correctAnswer（正确答案）、scoringCriteria（评分标准）

---

## 六、试卷模块 (Paper)

**文件位置**: `controller/PaperController.java`, `service/PaperService.java`

### 业务功能
| 接口 | 说明 |
|------|------|
| GET /api/papers | 获取试卷列表 |
| GET /api/papers/{id} | 获取试卷详情 |
| GET /api/papers/course/{courseId} | 获取课程的试卷 |
| POST /api/papers | 创建试卷 |
| PUT /api/papers/{id} | 更新试卷 |
| DELETE /api/papers/{id} | 删除试卷 |

### 核心逻辑
- 试卷包含 JSON 字段：questions（题目列表，含题目ID和分值）
- 支持从题库选题组卷

---

## 七、考试模块 (Exam)

**文件位置**: `controller/ExamController.java`, `service/ExamService.java`

### 业务功能
| 接口 | 说明 |
|------|------|
| GET /api/exams | 获取考试列表 |
| GET /api/exams/published | 获取已发布的考试 |
| GET /api/exams/my | 获取我的考试（学生） |
| GET /api/exams/{id} | 获取考试详情 |
| POST /api/exams | 创建考试（教师） |
| PUT /api/exams/{id} | 更新考试 |
| DELETE /api/exams/{id} | 删除考试 |
| POST /api/exams/{id}/publish | 发布考试 |
| POST /api/exams/{id}/cancel | 取消考试 |
| POST /api/exams/{id}/start | 开始考试（学生） |
| POST /api/exams/{id}/submit | 提交考试（学生） |

### 核心逻辑
- **开始考试**: 检查考试状态、时间，创建考试记录
- **提交考试**: 保存答案，客观题自动评分
- 考试状态：DRAFT(草稿)、PUBLISHED(已发布)、CANCELLED(已取消)

---

## 八、考试记录模块 (ExamSession)

**文件位置**: `controller/ExamSessionController.java`, `service/ExamSessionService.java`

### 业务功能
| 接口 | 说明 |
|------|------|
| GET /api/exam-sessions | 获取考试记录列表 |
| GET /api/exam-sessions/{id} | 获取记录详情 |
| GET /api/exam-sessions/exam/{examId} | 获取某考试的所有记录 |
| GET /api/exam-sessions/student/{studentId} | 获取某学生的所有记录 |
| POST /api/exam-sessions | 创建记录 |
| PUT /api/exam-sessions/{id} | 更新记录 |
| DELETE /api/exam-sessions/{id} | 删除记录 |

### 核心逻辑
- 记录包含 JSON 字段：answers（答题列表，含题目ID、答案、是否正确、得分）
- 状态：IN_PROGRESS(进行中)、SUBMITTED(已提交)

---

## 九、公告模块 (Announcement)

**文件位置**: `controller/AnnouncementController.java`, `service/AnnouncementService.java`

### 业务功能
| 接口 | 说明 |
|------|------|
| GET /api/announcements | 获取公告列表 |
| GET /api/announcements/{id} | 获取公告详情 |
| POST /api/announcements | 发布公告 |
| PUT /api/announcements/{id} | 更新公告 |
| DELETE /api/announcements/{id} | 删除公告 |

### 公告类型
- SYSTEM: 系统公告
- COURSE: 课程公告

---

## 十、AI 配置模块 (AiConfig)

**文件位置**: `controller/AiConfigController.java`, `service/AiConfigService.java`

### 业务功能
| 接口 | 说明 |
|------|------|
| GET /api/ai-configs | 获取配置列表 |
| GET /api/ai-configs/{id} | 获取配置详情 |
| GET /api/ai-configs/user/{userId} | 获取用户的配置 |
| GET /api/ai-configs/user/{userId}/active | 获取用户的默认配置 |
| POST /api/ai-configs | 添加配置 |
| PUT /api/ai-configs/{id} | 更新配置 |
| DELETE /api/ai-configs/{id} | 删除配置 |

### 配置字段
- name: 配置名称
- baseUrl: AI 服务地址
- apiKey: API 密钥
- model: 模型名称
- isActive: 是否为默认配置

---

## 十一、AI 出题模块 (AiQuestion)

**文件位置**: `controller/AiQuestionController.java`, `service/AiQuestionService.java`

### 业务功能
| 接口 | 说明 |
|------|------|
| POST /api/ai/generate-questions | AI 生成题目 |

### 请求参数
- subject: 科目
- type: 题目类型
- difficulty: 难度
- count: 数量
- requirements: 额外要求

### 核心逻辑
1. 构建 Prompt 模板，要求 AI 按 JSON 格式返回
2. 调用 AI API（OpenAI 格式）
3. 解析 AI 返回的文本为结构化数据
4. 返回题目列表给前端预览

---

## 十二、轮播图模块 (Carousel)

**文件位置**: `controller/CarouselController.java`, `service/CarouselService.java`

### 业务功能
| 接口 | 说明 |
|------|------|
| GET /api/carousels | 获取轮播图列表 |
| GET /api/carousels/{id} | 获取轮播图详情 |
| GET /api/carousels/active | 获取启用的轮播图 |
| POST /api/carousels | 添加轮播图 |
| PUT /api/carousels/{id} | 更新轮播图 |
| DELETE /api/carousels/{id} | 删除轮播图 |

---

## 技术架构

### 技术栈
- **Java**: 17
- **Spring Boot**: 3.2.x
- **MyBatis-Plus**: 3.5.x
- **MySQL**: 8.x
- **JWT**: 0.12.x
- **Lombok**: 1.18.x
- **Hutool**: 5.8.x

### 项目结构
```
backend/
├── src/main/java/com/southcollege/exam/
│   ├── ExamApplication.java          # 启动类
│   ├── config/                       # 配置类
│   │   ├── MyBatisPlusConfig.java    # MyBatis-Plus 配置
│   │   ├── JwtInterceptor.java       # JWT 拦截器
│   │   └── WebConfig.java            # Web 配置（跨域、拦截器）
│   ├── controller/                   # 控制器层（11个）
│   ├── service/                      # 业务逻辑层（10个）
│   ├── entity/                       # 实体类（10个）
│   ├── mapper/                       # 数据访问层（10个）
│   ├── dto/                          # 数据传输对象
│   │   ├── request/                  # 请求 DTO
│   │   └── response/                 # 响应 DTO
│   ├── utils/                        # 工具类
│   │   ├── JwtUtil.java              # JWT 工具
│   │   ├── PasswordUtil.java         # 密码加密工具
│   │   └── SecurityUtil.java         # 安全工具（获取当前用户）
│   └── exception/                    # 异常处理
│       ├── BusinessException.java    # 业务异常
│       └── GlobalExceptionHandler.java # 全局异常处理器
```

---

## 数据库表

| 表名 | 说明 | 逻辑删除 |
|------|------|----------|
| users | 用户表 | ✅ |
| courses | 课程表 | ✅ |
| course_members | 课程成员表 | ❌ |
| questions | 题目表 | ✅ |
| papers | 试卷表 | ✅ |
| exams | 考试表 | ✅ |
| exam_sessions | 考试记录表 | ❌ |
| announcements | 公告表 | ✅ |
| user_ai_configs | AI 配置表 | ❌ |
| carousels | 轮播图表 | ✅ |

---

## 安全机制

1. **JWT 认证**: 除 /api/auth/** 外，所有接口需携带 Authorization: Bearer {token}
2. **密码加密**: MD5 加密存储
3. **跨域支持**: 允许所有来源
4. **业务异常**: 统一处理，返回标准错误格式

---

## 响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

错误响应：
```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```
