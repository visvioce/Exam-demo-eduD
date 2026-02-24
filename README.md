# 南方学院在线考试系统

## 📁 项目结构

```
exam/
├── backend/           # Spring Boot 后端
├── frontend/          # Vue 3 前端
├── database/          # 数据库脚本
├── logs/              # 日志目录
├── scripts/           # 辅助脚本
├── start.sh           # 🚀 启动服务（后台运行）
├── stop.sh            # 🛑 停止服务
└── README.md          # 本文档
```

## 🚀 快速开始

### 启动系统
```bash
./start.sh
```

### 停止系统
```bash
./stop.sh
```

## 🌐 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端 | http://localhost:8080 |
| API文档 | http://localhost:8080/swagger-ui.html |

## 📝 日志查看

```bash
# 查看后端日志
tail -f logs/backend.log

# 查看前端日志
tail -f logs/frontend.log
```

## 🔧 辅助脚本（scripts/）

| 脚本 | 说明 |
|------|------|
| install-mysql.sh | MySQL 安装（可选） |
| init-db.sh | 数据库初始化 |
| init-simple.sh | 简化初始化 |
| start-debug.sh | 调试模式说明 |

## 📋 系统要求

- Java 17+
- Maven 3.x
- Node.js 18+
- MySQL 8.x
