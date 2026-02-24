#!/bin/bash

# MySQL自动安装脚本
# MySQL root密码: 2714

echo "====================================="
echo "  MySQL 自动安装"
echo "====================================="

# 1. 更新软件包
echo ""
echo "📦 更新软件包列表..."
echo "2714" | sudo -S apt update > /dev/null 2>&1

# 2. 安装MySQL
echo ""
echo "🔧 安装 MySQL Server..."
echo "2714" | sudo -S apt install -y mysql-server > /dev/null 2>&1

# 3. 启动MySQL服务
echo ""
echo "🚀 启动 MySQL 服务..."
echo "2714" | sudo -S systemctl start mysql
echo "2714" | sudo -S systemctl enable mysql > /dev/null 2>&1

# 4. 设置root密码为2714
echo ""
echo "🔐 设置 MySQL root 密码..."
echo "2714" | sudo -S mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '2714'; FLUSH PRIVILEGES;"

# 5. 验证
echo ""
echo "✅ 验证安装..."
if mysql -u root -p2714 -e "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ MySQL 安装成功！"
    echo ""
    echo "📊 MySQL 版本信息："
    mysql --version
    echo ""
    echo "🔑 root 密码: 2714"
    echo "====================================="
else
    echo "❌ MySQL 安装失败，请检查"
    exit 1
fi
