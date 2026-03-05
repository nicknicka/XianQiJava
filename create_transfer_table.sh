#!/bin/bash
# 执行 transfer_record 表创建脚本

# 数据库配置
DB_NAME="Xianqi"
DB_USER="root"
DB_PASS="123456"

# SQL 文件路径
SQL_FILE="$(dirname "$0")/src/main/resources/db/migration/V34__add_transfer_record_table.sql"

echo "正在创建 transfer_record 表..."
echo "数据库: $DB_NAME"
echo "SQL文件: $SQL_FILE"
echo ""

# 执行 SQL 脚本
mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$SQL_FILE"

if [ $? -eq 0 ]; then
    echo "✓ transfer_record 表创建成功！"
    echo ""
    echo "请执行以下命令重启后端服务："
    echo "  cd $(dirname "$0")"
    echo "  ./mvnw spring-boot:run"
else
    echo "✗ 表创建失败，请检查数据库连接和权限"
    exit 1
fi
