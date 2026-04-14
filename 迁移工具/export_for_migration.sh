#!/bin/bash
# ============================================
# XianQi 后端项目迁移导出脚本（Mac/Linux 执行）
# 作者：Claude
# 日期：2026-04-12
# ============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
BACKUP_DIR="${SCRIPT_DIR}/migration_backup"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="xianqi_backup_${TIMESTAMP}"

# 数据库配置
DB_NAME="XianQi"
DB_USER="root"
DB_PASS="123456"
DB_HOST="localhost"

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}   XianQi 后端项目迁移导出工具${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""

# 创建备份目录
echo -e "${YELLOW}[1/5] 创建备份目录...${NC}"
mkdir -p "${BACKUP_DIR}"
mkdir -p "${BACKUP_DIR}/database"
mkdir -p "${BACKUP_DIR}/files"

# 导出数据库
echo -e "${YELLOW}[2/5] 导出数据库...${NC}"
echo "数据库名: ${DB_NAME}"
echo "用户: ${DB_USER}"

# 检查 MySQL 是否可用
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}错误: 未找到 mysql 命令，请确保 MySQL 已安装${NC}"
    exit 1
fi

# 导出数据库结构和数据
mysqldump -h"${DB_HOST}" -u"${DB_USER}" -p"${DB_PASS}" \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    --default-character-set=utf8mb4 \
    "${DB_NAME}" > "${BACKUP_DIR}/database/xianqi_database.sql"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 数据库导出成功${NC}"
    echo "  文件: ${BACKUP_DIR}/database/xianqi_database.sql"
    echo "  大小: $(du -h "${BACKUP_DIR}/database/xianqi_database.sql" | cut -f1)"
else
    echo -e "${RED}✗ 数据库导出失败${NC}"
    exit 1
fi

# 复制项目代码
echo -e "${YELLOW}[3/5] 复制项目代码...${NC}"

# 排除不需要的目录
EXCLUDE_DIRS=(
    "--exclude=target"
    "--exclude=.idea"
    "--exclude=.git"
    "--exclude=logs"
    "--exclude=*.log"
    "--exclude=.DS_Store"
    "--exclude=migration_backup"
    "--exclude=.mvn"
    "--exclude=scripts"
)

# 创建目标目录
mkdir -p "${BACKUP_DIR}/files/XianQiJava"

# 复制项目代码到 files/XianQiJava
rsync -av "${EXCLUDE_DIRS[@]}" "${PROJECT_DIR}/" "${BACKUP_DIR}/files/XianQiJava/"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 项目代码复制成功${NC}"
else
    echo -e "${RED}✗ 项目代码复制失败${NC}"
    exit 1
fi

# 复制上传文件目录（如果存在）
echo -e "${YELLOW}[4/5] 检查上传文件...${NC}"

# 检查多个可能的上传目录位置
UPLOAD_DIRS=(
    "${PROJECT_DIR}/../uploads"
    "${PROJECT_DIR}/uploads"
    "/tmp/xianqi-uploads"
)

FOUND_UPLOAD=false
for UPLOAD_DIR in "${UPLOAD_DIRS[@]}"; do
    if [ -d "${UPLOAD_DIR}" ]; then
        cp -r "${UPLOAD_DIR}" "${BACKUP_DIR}/files/uploads"
        echo -e "${GREEN}✓ 上传文件复制成功${NC}"
        echo "  源目录: ${UPLOAD_DIR}"
        echo "  大小: $(du -sh "${BACKUP_DIR}/files/uploads" | cut -f1)"
        FOUND_UPLOAD=true
        break
    fi
done

if [ "$FOUND_UPLOAD" = false ]; then
    echo -e "${YELLOW}! 未找到上传文件目录，跳过${NC}"
fi

# 打包备份文件
echo -e "${YELLOW}[5/5] 打包备份文件...${NC}"
cd "${PROJECT_DIR}"
tar -czvf "${BACKUP_FILE}.tar.gz" -C "${BACKUP_DIR}" .

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 打包完成${NC}"
    echo "  文件: ${PROJECT_DIR}/${BACKUP_FILE}.tar.gz"
    echo "  大小: $(du -h "${PROJECT_DIR}/${BACKUP_FILE}.tar.gz" | cut -f1)"
else
    echo -e "${RED}✗ 打包失败${NC}"
    exit 1
fi

# 生成导入脚本（Windows 版）
echo -e "${YELLOW}生成 Windows 导入脚本...${NC}"
cat > "${BACKUP_DIR}/import_windows.bat" << 'EOF'
@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: ============================================
:: XianQi 后端项目导入脚本（Windows 执行）
:: ============================================

echo ============================================
echo    XianQi 后端项目导入工具 (Windows)
echo ============================================
echo.

:: 配置变量
set DB_NAME=XianQi
set DB_USER=root
set DB_PASS=123456
set DB_HOST=localhost

:: 检查 MySQL
echo [1/5] 检查 MySQL...
where mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到 mysql 命令
    echo 请确保 MySQL 已安装并添加到系统 PATH
    echo.
    echo 常见 MySQL 路径:
    echo   C:\Program Files\MySQL\MySQL Server 8.0\bin
    echo   C:\xampp\mysql\bin
    pause
    exit /b 1
)
echo ✓ MySQL 已找到

:: 检查 Redis
echo [2/5] 检查 Redis...
where redis-server >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告: 未找到 redis-server 命令
    echo 请确保 Redis 已安装并添加到系统 PATH
    echo.
    echo 下载地址: https://github.com/tporadowski/redis/releases
    echo.
) else (
    echo ✓ Redis 已找到
)

:: 创建数据库
echo [3/5] 创建数据库...
mysql -h%DB_HOST% -u%DB_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
if %errorlevel% neq 0 (
    echo 数据库密码可能不正确，请手动输入:
    set /p DB_PASS="请输入 MySQL root 密码: "
    mysql -h%DB_HOST% -u%DB_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
)
echo ✓ 数据库创建成功

:: 导入数据
echo [4/5] 导入数据库数据...
if exist "database\xianqi_database.sql" (
    mysql -h%DB_HOST% -u%DB_USER% -p%DB_PASS% %DB_NAME% < database\xianqi_database.sql
    if %errorlevel% neq 0 (
        echo 错误: 数据库导入失败
        pause
        exit /b 1
    )
    echo ✓ 数据库导入成功
) else (
    echo 错误: 未找到数据库文件 database\xianqi_database.sql
    pause
    exit /b 1
)

:: 复制上传文件
echo [5/5] 复制上传文件...
if exist "files\uploads" (
    if not exist "C:\uploads" mkdir "C:\uploads"
    xcopy /E /I /Y "files\uploads\*" "C:\uploads\"
    echo ✓ 上传文件复制成功
) else (
    echo ! 未找到上传文件目录，跳过
)

echo.
echo ============================================
echo    导入完成！
echo ============================================
echo.
echo 下一步操作:
echo 1. 进入 files\XianQiJava 目录
echo 2. 修改 application.yml 中的数据库密码（如果不同）
echo 3. 运行: mvnw.cmd spring-boot:run
echo.
echo 或者打包后运行:
echo    mvnw.cmd clean package
echo    java -jar target\XianQiJava-0.0.1-SNAPSHOT.jar
echo.
pause
EOF

# 将导入脚本也打包进去
tar -czvf "${BACKUP_FILE}.tar.gz" -C "${BACKUP_DIR}" .

# 清理临时目录
echo -e "${YELLOW}清理临时文件...${NC}"
rm -rf "${BACKUP_DIR}"

echo ""
echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}   导出完成！${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""
echo "备份文件: ${PROJECT_DIR}/${BACKUP_FILE}.tar.gz"
echo ""
echo -e "${YELLOW}迁移步骤:${NC}"
echo "1. 将 ${BACKUP_FILE}.tar.gz 复制到 Windows 机器"
echo "2. 解压到任意目录"
echo "3. 双击运行 import_windows.bat"
echo ""
