#!/bin/bash
# 快速关闭脚本 - 南方职业学院在线考试系统

echo "🛑 停止服务..."

# 停止后端
if pgrep -f "exam-system-1.0.0.jar" > /dev/null; then
    echo "📌 停止后端..."
    pkill -f "exam-system-1.0.0.jar"
fi

# 停止前端
if pgrep -f "vite" > /dev/null; then
    echo "📌 停止前端..."
    pkill -f "vite"
fi

echo "✅ 服务已停止"
