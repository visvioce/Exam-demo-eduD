#!/bin/bash

# 南方职业学院在线考试系统 - 启动脚本（改进版）

echo "====================================="
echo "  南方职业学院在线考试系统"
echo "  正在启动..."
echo "====================================="

# 检查Java
if ! command -v java &> /dev/null; then
    echo "❌ 错误: Java 未安装，请先安装 Java 17+"
    exit 1
fi

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: Maven 未安装，请先安装 Maven"
    exit 1
fi

# 检查Node.js
if ! command -v node &> /dev/null; then
    echo "❌ 错误: Node.js 未安装，请先安装 Node.js"
    exit 1
fi

# 检查MySQL服务
echo ""
echo "📊 检查MySQL服务..."
if ! systemctl is-active --quiet mysql; then
    echo "⚠️  MySQL服务未运行，正在启动..."
    echo "2714" | sudo -S systemctl start mysql
fi

# 获取项目根目录
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# 创建logs目录
mkdir -p "$PROJECT_ROOT/logs"

# 清理旧日志
rm -f "$PROJECT_ROOT/logs/backend.log"
rm -f "$PROJECT_ROOT/logs/frontend.log"

echo ""
echo "🚀 正在启动后端服务..."
cd "$PROJECT_ROOT/backend"

# 检查端口占用
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  端口 8080 已被占用，正在清理..."
    pkill -f "mvn.*spring-boot:run" 2>/dev/null
    sleep 3
fi

# 启动后端（后台运行）
echo "📦 编译并启动后端..."
nohup mvn spring-boot:run > "$PROJECT_ROOT/logs/backend.log" 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > "$PROJECT_ROOT/logs/backend.pid"
echo "✅ 后端服务正在启动 (PID: $BACKEND_PID)"
echo "📝 后端日志: $PROJECT_ROOT/logs/backend.log"

# 等待后端启动 - 检查日志而不是固定时间
echo ""
echo "⏳ 等待后端服务启动（最多等待60秒）..."
BACKEND_READY=0
for i in {1..60}; do
    if grep -q "Started ExamApplication" "$PROJECT_ROOT/logs/backend.log" 2>/dev/null; then
        echo "✅ 后端服务启动成功！"
        BACKEND_READY=1
        break
    fi
    if grep -q "ERROR\|Exception" "$PROJECT_ROOT/logs/backend.log" 2>/dev/null; then
        echo "❌ 后端启动出错，请查看日志："
        tail -20 "$PROJECT_ROOT/logs/backend.log"
        exit 1
    fi
    echo -n "."
    sleep 1
done

if [ $BACKEND_READY -eq 0 ]; then
    echo ""
    echo "⚠️  后端可能还在启动中，继续启动前端..."
    echo "📝 请稍后查看后端日志确认状态"
fi

echo ""
echo "🚀 正在启动前端服务..."
cd "$PROJECT_ROOT/frontend"

# 检查端口占用
if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  端口 5173 已被占用，正在清理..."
    pkill -f "node.*vite" 2>/dev/null
    sleep 2
fi

# 启动前端（后台运行）
nohup npm run dev > "$PROJECT_ROOT/logs/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > "$PROJECT_ROOT/logs/frontend.pid"
echo "✅ 前端服务正在启动 (PID: $FRONTEND_PID)"
echo "📝 前端日志: $PROJECT_ROOT/logs/frontend.log"

# 等待前端启动
echo ""
echo "⏳ 等待前端服务启动..."
sleep 5

echo ""
echo "====================================="
echo "  ✅ 系统启动完成！"
echo ""
echo "  📱 前端地址: http://localhost:5173"
echo "  🔧 后端地址: http://localhost:8080"
echo "  📚 API文档: http://localhost:8080/swagger-ui.html"
echo ""
echo "  📝 查看日志："
echo "     后端: tail -f logs/backend.log"
echo "     前端: tail -f logs/frontend.log"
echo ""
echo "  🛑 停止服务: ./stop.sh"
echo "  🔍 调试模式: ./start-debug.sh"
echo "====================================="
