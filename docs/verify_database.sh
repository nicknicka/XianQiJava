#!/bin/bash

# ====================================
# 数据库验证脚本
# ====================================

echo "================================"
echo "校园二手交易平台 - 数据库验证"
echo "================================"
echo ""

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="Xianqi"
DB_USER="root"
DB_PASS="123456"

echo "1. 检查数据库连接..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS -e "SELECT 1;" > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "   ✅ 数据库连接成功"
else
    echo "   ❌ 数据库连接失败"
    echo "   请检查MySQL是否启动，用户名密码是否正确"
    exit 1
fi

echo ""
echo "2. 检查数据库是否存在..."
DB_EXISTS=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS -e "SHOW DATABASES LIKE '$DB_NAME';" | grep $DB_NAME)

if [ -n "$DB_EXISTS" ]; then
    echo "   ✅ 数据库 $DB_NAME 存在"
else
    echo "   ❌ 数据库 $DB_NAME 不存在"
    echo "   正在创建数据库..."
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo "   ✅ 数据库创建成功"
fi

echo ""
echo "3. 检查worker_node表..."
TABLE_EXISTS=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS -D $DB_NAME -e "SHOW TABLES LIKE 'worker_node';" | grep worker_node)

if [ -n "$TABLE_EXISTS" ]; then
    echo "   ✅ worker_node表存在"
else
    echo "   ❌ worker_node表不存在"
    echo "   请执行以下命令创建："
    echo "   mysql -u $DB_USER -p $DB_NAME < docs/uid_generator_worker_node.sql"
fi

echo ""
echo "4. 检查现有数据表数量..."
TABLE_COUNT=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS -D $DB_NAME -e "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = '$DB_NAME';" | tail -n 1)
echo "   当前表数量: $TABLE_COUNT"

echo ""
echo "================================"
echo "验证完成！"
echo "================================"
echo ""
echo "下一步："
echo "1. 如果worker_node表不存在，执行："
echo "   mysql -u $DB_USER -p $DB_NAME < docs/uid_generator_worker_node.sql"
echo ""
echo "2. 编译项目："
echo "   ./mvnw clean compile"
echo ""
echo "3. 运行测试："
echo "   ./mvnw test -Dtest=IdGeneratorServiceTest"
echo ""
