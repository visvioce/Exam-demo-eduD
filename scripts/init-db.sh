#!/bin/bash

# 南方职业学院在线考试系统 - 数据库初始化脚本

echo "====================================="
echo "  南方职业学院在线考试系统"
echo "  数据库初始化"
echo "====================================="

# 获取项目根目录
PROJECT_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# MySQL配置
MYSQL_PASSWORD="123456"
MYSQL_CMD="mysql -u root -p$MYSQL_PASSWORD"

echo ""
echo "🔍 检查数据库连接..."
if ! $MYSQL_CMD -e "SELECT 1;" > /dev/null 2>&1; then
    echo "❌ 错误: 无法连接到MySQL数据库"
    echo "   请检查密码是否正确"
    exit 1
fi

echo "✅ 数据库连接成功"

echo ""
echo "🗄️  创建数据库..."
$MYSQL_CMD -e "CREATE DATABASE IF NOT EXISTS exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo "📝 执行初始化脚本..."
$MYSQL_CMD exam_system < "$PROJECT_ROOT/database/init.sql"

echo ""
echo "✅ 数据库初始化完成！"
echo ""
echo "📚 数据库名: exam_system"
echo "🔑 root 密码: 123456"
echo "====================================="
