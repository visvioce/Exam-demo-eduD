#!/bin/bash

# 南方职业学院在线考试系统 - 停止脚本

echo "====================================="
echo "  南方职业学院在线考试系统"
echo "  正在停止..."
echo "====================================="

# 获取项目根目录
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# 停止后端服务
if [ -f "$PROJECT_ROOT/logs/backend.pid" ]; then
    BACKEND_PID=$(cat "$PROJECT_ROOT/logs/backend.pid" 2>/dev/null)
    if [ -n "$BACKEND_PID" ] && kill -0 $BACKEND_PID 2>/dev/null; then
        echo "🛑 正在停止后端服务 (PID: $BACKEND_PID)..."
        kill $BACKEND_PID
        sleep 3
        # 强制终止
        if kill -0 $BACKEND_PID 2>/dev/null; then
            kill -9 $BACKEND_PID
        fi
        echo "✅ 后端服务已停止"
    fi
    rm -f "$PROJECT_ROOT/logs/backend.pid"
else
    echo "⚠️  未找到后端服务PID文件"
fi

# 停止前端服务
if [ -f "$PROJECT_ROOT/logs/frontend.pid" ]; then
    FRONTEND_PID=$(cat "$PROJECT_ROOT/logs/frontend.pid" 2>/dev/null)
    if [ -n "$FRONTEND_PID" ] && kill -0 $FRONTEND_PID 2>/dev/null; then
        echo "🛑 正在停止前端服务 (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID
        sleep 2
        # 强制终止
        if kill -0 $FRONTEND_PID 2>/dev/null; then
            kill -9 $FRONTEND_PID
        fi
        echo "✅ 前端服务已停止"
    fi
    rm -f "$PROJECT_ROOT/logs/frontend.pid"
else
    echo "⚠️  未找到前端服务PID文件"
fi

# 清理可能残留的Java和Node进程
echo ""
echo "🧹 清理残留进程..."
pkill -f "mvn.*spring-boot:run" 2>/dev/null
pkill -f "node.*vite" 2>/dev/null

echo ""
echo "====================================="
echo "  ✅ 所有服务已停止！"
echo "====================================="
