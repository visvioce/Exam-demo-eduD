# 南方职业学院在线考试系统 - 后端

## 项目简介

基于 Java + Spring Boot + MyBatis-Plus + MySQL 的在线考试系统后端。

## 技术栈

- **Java**: 17+
- **Spring Boot**: 3.2.x
- **MyBatis-Plus**: 3.5.x
- **MySQL**: 8.x
- **JWT**: 0.12.x
- **Lombok**: 1.18.x
- **Hutool**: 5.8.x

## 项目结构

```
backend/
├── src/main/java/com/southcollege/exam/
│   ├── ExamApplication.java          # 启动类
│   ├── config/                       # 配置类
│   ├── controller/                   # 控制器层
│   ├── service/                      # 业务逻辑层
│   ├── entity/                       # 实体类
│   ├── mapper/                       # 数据访问层
│   ├── dto/                          # 数据传输对象
│   ├── utils/                        # 工具类
│   └── exception/                    # 异常处理
├── src/main/resources/
│   ├── mapper/                       # XML 映射文件
│   ├── application.yml               # 主配置
│   └── application-dev.yml           # 开发环境
├── docs/                             # 文档
└── pom.xml                           # Maven 配置
```

## 快速开始

### 1. 安装依赖

```bash
cd backend
mvn clean install
```

### 2. 配置数据库

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/exam_system
    username: root
    password: your_password
```

### 3. 初始化数据库

```bash
mysql -u root -p < ../database/init.sql
```

### 4. 启动项目

```bash
mvn spring-boot:run
```

或者运行 `ExamApplication` 主类。

服务器将在 http://localhost:8080 启动

## API 文档

基础路径: `http://localhost:8080/api`

响应格式:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

主要模块:
- `/auth` - 认证模块
- `/users` - 用户模块
- `/courses` - 课程模块
- `/questions` - 题目模块
- `/papers` - 试卷模块
- `/exams` - 考试模块
- `/exam-sessions` - 考试记录模块
- `/announcements` - 公告模块
- `/ai-configs` - AI配置模块

## 开发文档

详见 [docs/开发文档.md](./docs/开发文档.md)

## 注意事项

1. 确保 MySQL 服务已启动
2. 首次运行前需要初始化数据库
3. 修改 `application-dev.yml` 中的数据库密码
4. 生产环境请修改 JWT 密钥
