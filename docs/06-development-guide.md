# 开发指南

## 一、环境准备

### 1.1 必需软件

| 软件 | 版本要求 | 检查命令 | 安装方式 |
|------|----------|----------|----------|
| Java JDK | 17+ | `java -version` | SDKMAN 或 官网下载 |
| Node.js | 18+ | `node -v` | nvm 或 官网下载 |
| MySQL | 8.0+ | `mysql --version` | Docker 或 官网下载 |
| Maven | 3.8+ | `mvn -v` | SDKMAN 或 官网下载 |

### 1.2 推荐 IDE

| 语言 | 推荐IDE | 插件 |
|------|---------|------|
| Java | IntelliJ IDEA | Lombok, MyBatisX |
| Vue/TS | VS Code | Volar, TypeScript Vue Plugin |

### 1.3 环境变量配置

```bash
# ~/.bashrc 或 ~/.zshrc
export JAVA_HOME=/path/to/jdk-17
export MAVEN_HOME=/path/to/maven
export NODE_HOME=/path/to/node
export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$NODE_HOME/bin:$PATH
```

---

## 二、快速启动

### 2.1 一键启动（推荐）

```bash
# 在项目根目录执行
./start.sh
```

**start.sh 做了什么？**

```bash
#!/bin/bash

# 1. 检查数据库
echo "检查MySQL服务..."
mysql -u root -p"$MYSQL_PASSWORD" -e "SELECT 1" || exit 1

# 2. 启动后端
echo "启动后端服务..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!

# 3. 启动前端
echo "启动前端服务..."
cd ../frontend
npm run dev &
FRONTEND_PID=$!

echo "后端 PID: $BACKEND_PID"
echo "前端 PID: $FRONTEND_PID"
echo "访问地址: http://localhost:5173"
```

### 2.2 手动启动

**启动后端**：

```bash
cd backend

# 方式1：Maven（开发推荐）
mvn spring-boot:run

# 方式2：打包后运行
mvn clean package -DskipTests
java -jar target/exam-backend-0.0.1-SNAPSHOT.jar
```

**启动前端**：

```bash
cd frontend

# 首次需要安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 2.3 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://localhost:5173 | 开发模式 |
| 后端API | http://localhost:8080/api | REST接口 |
| API文档 | http://localhost:8080/swagger-ui.html | Swagger UI |

---

## 三、数据库配置

### 3.1 初始化数据库

```bash
# 方式1：使用脚本
./scripts/init-db.sh

# 方式2：手动执行SQL
mysql -u root -p < database/init.sql
mysql -u root -p < database/data.sql
```

### 3.2 配置数据库连接

**修改 `backend/src/main/resources/application-dev.yml`**：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

**为什么要分 application.yml 和 application-dev.yml？**

| 文件 | 用途 |
|------|------|
| application.yml | 通用配置，生产环境 |
| application-dev.yml | 开发环境配置，覆盖通用配置 |

启动时指定环境：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3.3 测试数据

**默认账号（密码都是 123456）**：

| 用户名 | 角色 | 用途 |
|--------|------|------|
| admin | ADMIN | 管理员操作 |
| teacher1 | TEACHER | 教师操作 |
| student1 | STUDENT | 学生操作 |

---

## 四、开发流程

### 4.1 新增一个功能模块

以"新增留言功能"为例：

#### 步骤1：创建数据库表

```sql
-- database/migrations/add_messages.sql
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 步骤2：创建后端实体

```java
// backend/src/main/java/com/southcollege/exam/entity/Message.java
@Data
@TableName("messages")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
```

#### 步骤3：创建 Mapper

```java
// backend/src/main/java/com/southcollege/exam/mapper/MessageMapper.java
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
```

#### 步骤4：创建 Service

```java
// backend/src/main/java/com/southcollege/exam/service/MessageService.java
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    
    public List<Message> getAllMessages() {
        return messageMapper.selectList(null);
    }
    
    public Message createMessage(Long userId, String content) {
        Message message = new Message();
        message.setUserId(userId);
        message.setContent(content);
        messageMapper.insert(message);
        return message;
    }
}
```

#### 步骤5：创建 Controller

```java
// backend/src/main/java/com/southcollege/exam/controller/MessageController.java
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;
    
    @GetMapping
    public Result<List<Message>> getAll() {
        return Result.success(messageService.getAllMessages());
    }
    
    @PostMapping
    public Result<Message> create(@RequestBody MessageRequest request) {
        Long userId = getCurrentUserId();  // 从请求属性获取
        return Result.success(messageService.createMessage(userId, request.getContent()));
    }
}
```

#### 步骤6：创建前端 API

```typescript
// frontend/src/api/message.ts
import request from '@/utils/request'

export function getMessages() {
  return request.get('/messages')
}

export function createMessage(content: string) {
  return request.post('/messages', { content })
}
```

#### 步骤7：创建前端页面

```vue
<!-- frontend/src/views/message/MessageList.vue -->
<template>
  <div class="message-list">
    <el-input v-model="newMessage" placeholder="输入留言" />
    <el-button @click="submit">提交</el-button>
    
    <div v-for="msg in messages" :key="msg.id" class="message-item">
      {{ msg.content }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMessages, createMessage } from '@/api/message'

const messages = ref([])
const newMessage = ref('')

onMounted(async () => {
  const res = await getMessages()
  messages.value = res.data
})

async function submit() {
  await createMessage(newMessage.value)
  newMessage.value = ''
  // 刷新列表
}
</script>
```

#### 步骤8：添加路由

```typescript
// frontend/src/router/index.ts
{
  path: 'message',
  component: () => import('@/views/message/MessageList.vue')
}
```

### 4.2 开发顺序建议

```
数据库表 → Entity → Mapper → Service → Controller → 前端API → 前端页面 → 路由
```

**为什么这个顺序？**

1. 数据库是一切的基础
2. 后端自底向上，依赖关系清晰
3. 前端依赖后端接口，所以后端先完成

---

## 五、常见开发任务

### 5.1 添加新的 API 端点

```java
// 1. 在 Controller 添加方法
@GetMapping("/stats")
public Result<ExamStats> getStats(@PathVariable Long id) {
    return Result.success(examService.getStats(id));
}

// 2. 在 Service 实现业务逻辑
public ExamStats getStats(Long examId) {
    // 业务逻辑
}

// 3. 测试 API
// 访问 http://localhost:8080/swagger-ui.html 测试
```

### 5.2 添加权限控制

```java
// 方式1：整个 Controller 需要权限
@RequireRole({RoleEnum.ADMIN, RoleEnum.TEACHER})
@RestController
@RequestMapping("/api/questions")
public class QuestionController { ... }

// 方式2：单个方法需要权限
@RequireRole({RoleEnum.ADMIN})
@DeleteMapping("/{id}")
public Result<Void> delete(@PathVariable Long id) { ... }
```

### 5.3 添加参数校验

```java
// 1. 在 DTO 添加注解
@Data
public class QuestionRequest {
    @NotBlank(message = "题目内容不能为空")
    private String content;
    
    @NotNull(message = "题目类型不能为空")
    private QuestionType type;
    
    @Min(value = 1, message = "分值最小为1")
    private Integer score;
}

// 2. 在 Controller 使用 @Valid
@PostMapping
public Result<Question> create(@Valid @RequestBody QuestionRequest request) {
    // 校验失败会自动抛出异常
}
```

### 5.4 添加前端页面

```vue
<!-- 1. 创建组件文件 -->
<template>
  <div class="page-container">
    <!-- 页面内容 -->
  </div>
</template>

<script setup lang="ts">
// 导入依赖
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 定义响应式数据
const loading = ref(false)
const list = ref([])

// 生命周期
onMounted(() => {
  fetchData()
})

// 方法
async function fetchData() {
  loading.value = true
  // ...
}
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}
</style>
```

---

## 六、调试技巧

### 6.1 后端调试

**日志输出**：

```java
// 使用 Slf4j
@Slf4j
@Service
public class ExamService {
    public void someMethod() {
        log.debug("调试信息: {}", someVariable);
        log.info("普通信息");
        log.error("错误信息", exception);
    }
}
```

**开启 SQL 日志**：

```yaml
# application-dev.yml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

**IDE 断点调试**：

1. 在 IDEA 中打开 `ExamApplication.java`
2. 右键 → Debug 'ExamApplication'
3. 在代码行号处点击设置断点
4. 请求 API，自动停在断点处

### 6.2 前端调试

**Vue DevTools**：

- 安装 Chrome 插件 "Vue.js devtools"
- 可以查看组件树、状态、事件等

**控制台调试**：

```typescript
// 打印响应数据
console.log('响应:', res.data)

// 使用 debugger 断点
debugger  // 代码会停在这里

// 查看响应式数据
import { toRaw } from 'vue'
console.log(toRaw(reactiveObject))
```

**网络请求调试**：

- 浏览器 F12 → Network 标签
- 查看请求参数、响应内容、耗时等

### 6.3 数据库调试

**查看 SQL 执行计划**：

```sql
EXPLAIN SELECT * FROM exams WHERE status = 'STARTED';
```

**慢查询分析**：

```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;  -- 超过1秒记录
```

---

## 七、常见问题

### 7.1 后端启动失败

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| 端口被占用 | 8080 已被使用 | 杀掉占用进程或修改端口 |
| 数据库连接失败 | MySQL 未启动或密码错误 | 检查 MySQL 服务和配置 |
| Bean 创建失败 | 依赖缺失或配置错误 | 检查 pom.xml 和配置文件 |

**端口占用解决**：

```bash
# 查找占用进程
lsof -i :8080
# 或
netstat -tlnp | grep 8080

# 杀掉进程
kill -9 <PID>
```

### 7.2 前端启动失败

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| 依赖安装失败 | 网络问题 | 使用淘宝镜像 `npm config set registry https://registry.npmmirror.com` |
| 编译错误 | TypeScript 类型错误 | 检查类型定义，使用 `any` 临时绕过 |
| 代理失败 | 后端未启动 | 先启动后端 |

### 7.3 登录失败

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| 用户名或密码错误 | 密码不对或用户不存在 | 检查数据库数据 |
| Token 无效 | Token 过期或格式错误 | 清除 localStorage 重新登录 |
| 跨域问题 | CORS 配置错误 | 检查后端 CORS 配置 |

---

## 八、代码规范

### 8.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | 大驼峰 | `ExamService` |
| 方法名 | 小驼峰 | `getExamById` |
| 变量名 | 小驼峰 | `examList` |
| 常量名 | 全大写下划线 | `MAX_RETRY_COUNT` |
| 数据库表名 | 小写下划线 | `exam_sessions` |
| 数据库字段名 | 小写下划线 | `started_at` |

### 8.2 注释规范

```java
/**
 * 考试服务类
 * 处理考试相关的业务逻辑
 * 
 * @author 作者名
 * @since 1.0.0
 */
@Service
public class ExamService {
    
    /**
     * 提交考试
     * 
     * @param examId 考试ID
     * @param studentId 学生ID
     * @param request 提交请求
     * @return 考试记录
     * @throws BusinessException 当考试已结束或重复提交时抛出
     */
    public ExamSession submitExam(Long examId, Long studentId, SubmitExamRequest request) {
        // 实现...
    }
}
```

### 8.3 Git 提交规范

```
<type>(<scope>): <subject>

type:
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式
- refactor: 重构
- test: 测试
- chore: 构建/工具

示例:
feat(exam): 添加考试自动评分功能
fix(login): 修复Token过期后跳转登录页问题
docs: 更新API文档
```

---

## 九、部署指南

### 9.1 打包

**后端打包**：

```bash
cd backend
mvn clean package -DskipTests
# 产物: target/exam-backend-0.0.1-SNAPSHOT.jar
```

**前端打包**：

```bash
cd frontend
npm run build
# 产物: dist/
```

### 9.2 生产环境配置

**后端 application-prod.yml**：

```yaml
spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/exam_system
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    
jwt:
  secret: ${JWT_SECRET}  # 从环境变量读取
  expiration: 86400000

logging:
  level:
    root: INFO
```

**启动命令**：

```bash
java -jar exam-backend.jar --spring.profiles.active=prod
```

### 9.3 Nginx 配置

```nginx
server {
    listen 80;
    server_name exam.example.com;
    
    # 前端静态文件
    location / {
        root /var/www/exam/dist;
        try_files $uri $uri/ /index.html;
    }
    
    # API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 十、扩展学习

### 10.1 推荐学习路径

```
1. Spring Boot 基础 → 2. MyBatis-Plus → 3. Spring Security → 4. JWT
         ↓
5. Vue 3 基础 → 6. TypeScript → 7. Element Plus → 8. Pinia
         ↓
9. 前后端联调 → 10. 部署上线
```

### 10.2 学习资源

| 主题 | 推荐资源 |
|------|----------|
| Spring Boot | 官方文档、Spring Boot实战 |
| MyBatis-Plus | 官方文档 baomidou.com |
| Vue 3 | 官方文档 cn.vuejs.org |
| Element Plus | 官方文档 element-plus.org |
| JWT | jwt.io |

### 10.3 进阶方向

1. **性能优化**：添加缓存、数据库优化
2. **安全加固**：防止 SQL 注入、XSS 攻击
3. **功能扩展**：AI 智能出题、成绩分析图表
4. **架构升级**：微服务、容器化部署
