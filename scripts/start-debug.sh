#!/bin/bash

# 南方职业学院在线考试系统 - 调试启动脚本（前台运行）

echo "====================================="
echo "  南方职业学院在线考试系统"
echo "  调试模式启动"
echo "====================================="

# 获取项目根目录
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# 检查MySQL服务
echo ""
echo "📊 检查MySQL服务..."
if ! systemctl is-active --quiet mysql; then
    echo "⚠️  MySQL服务未运行，正在启动..."
    echo "2714" | sudo -S systemctl start mysql
fi

# 检查后端端口
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  端口 8080 已被占用，正在清理..."
    pkill -f "mvn.*spring-boot:run" 2>/dev/null
    sleep 3
fi

# 检查前端端口
if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "⚠️  端口 5173 已被占用，正在清理..."
    pkill -f "node.*vite" 2>/dev/null
    sleep 2
fi

echo ""
echo "====================================="
echo "  🎯 使用说明："
echo "  1. 先启动后端（新终端运行）"
echo "  2. 再启动前端（另一个新终端运行）"
echo ""
echo "  🚀 启动后端："
echo "     cd backend && mvn spring-boot:run"
echo ""
echo "  🚀 启动前端："
echo "     cd frontend && npm run dev"
echo ""
echo "  这样可以看到实时输出！"
echo "====================================="
