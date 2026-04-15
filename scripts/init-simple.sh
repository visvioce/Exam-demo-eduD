#!/bin/bash

# 简单初始化脚本 - 只创建表结构，不插入数据
# 然后自己在系统里注册用户

echo "====================================="
echo "  简单初始化 - 只创建表结构"
echo "====================================="

PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
MYSQL_PASSWORD="123456"
MYSQL_CMD="mysql -u root -p$MYSQL_PASSWORD"

echo ""
echo "🗄️  删除旧数据库（如果存在）..."
$MYSQL_CMD -e "DROP DATABASE IF EXISTS exam_system;"

echo ""
echo "📝 创建新数据库和表..."
$MYSQL_CMD < "$PROJECT_ROOT/database/init.sql"

echo ""
echo "====================================="
echo "  ✅ 初始化完成！"
echo ""
echo "  📝 下一步："
echo "  1. 启动系统：./start.sh"
echo "  2. 访问：http://localhost:5173"
echo "  3. 自己注册账号（第一个注册的设为管理员）"
echo ""
echo "  🎯 或者用调试模式启动看实时输出"
echo "====================================="
