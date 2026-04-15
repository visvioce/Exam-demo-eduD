#!/bin/bash
# 快速启动脚本 - 南方职业学院在线考试系统

cd "$(dirname "$0")"

echo "🚀 启动服务..."

# 先停止旧进程
echo "📌 停止旧进程..."
pkill -f "exam-system-1.0.0.jar" 2>/dev/null
pkill -f "vite" 2>/dev/null
sleep 2

# 检查 MySQL
if ! pgrep -x mysqld > /dev/null; then
 echo "📌 启动 MySQL..."
 sudo -S -p '' /usr/sbin/mysqld --user=mysql &
 sleep 3
fi

# 编译后端（确保使用最新代码）
echo "🔨 编译后端..."
cd backend
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH
export JWT_SECRET="exam_system_jwt_secret_key_256_bits_for_graduation_project_2024"
if [ -f .env ]; then
  export $(cat .env | grep -v '^#' | xargs)
fi
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
 echo "❌ 编译失败，请检查代码"
 exit 1
fi
echo "✅ 编译完成"

# 启动后端
echo "📌 启动后端 (Spring Boot)..."
$JAVA_HOME/bin/java -jar target/exam-system-1.0.0.jar --spring.profiles.active=dev > /tmp/backend.log 2>&1 &
BACKEND_PID=$!
cd ..

# 启动前端
echo "📌 启动前端 (Vue + Vite)..."
cd frontend
PATH=/usr/bin:$PATH npm run dev > /tmp/frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 8

# 检查服务状态
if curl -s http://localhost:8080/api/ai-configs/my -H "Authorization: Bearer test" > /dev/null 2>&1; then
 echo "✅ 后端运行中: http://localhost:8080"
else
 echo "⚠️ 后端可能还在启动中..."
fi

if curl -s http://localhost:5173 > /dev/null 2>&1; then
 echo "✅ 前端运行中: http://localhost:5173"
else
 echo "⚠️ 前端可能还在启动中..."
fi

echo ""
echo "=========================================="
echo " 服务已启动"
echo " 前端: http://localhost:5173"
echo " 后端: http://localhost:8080"
echo " 账号: admin / 123456"
echo "=========================================="
