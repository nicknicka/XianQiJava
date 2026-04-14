@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: ============================================
:: XianQi 后端项目导入脚本（Windows 执行）
:: 作者：Claude
:: 日期：2026-04-12
:: 用途：在 Windows 机器上导入数据库和配置项目
:: ============================================

title XianQi 项目导入工具

echo.
echo ╔════════════════════════════════════════════╗
echo ║     XianQi 后端项目导入工具 (Windows)      ║
echo ║        校园二手交易与共享平台               ║
echo ╚════════════════════════════════════════════╝
echo.

:: ============================================
:: 配置区域 - 根据实际情况修改
:: ============================================
set DB_NAME=XianQi
set DB_USER=root
set DB_PASS=123456
set DB_HOST=localhost
set DB_PORT=3306

:: 项目配置
set PROJECT_PORT=8080
set UPLOAD_PATH=C:\xianqi-uploads

:: ============================================
:: 颜色定义
:: ============================================
:: Windows CMD 不支持颜色，使用符号代替

:: ============================================
:: 检查管理员权限
:: ============================================
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] 建议以管理员身份运行，以便创建目录
    echo.
)

:: ============================================
:: 步骤 1: 检查 Java 环境
:: ============================================
echo [1/6] 检查 Java 环境...
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] 错误: 未找到 Java
    echo.
    echo 请安装 JDK 17 或更高版本:
    echo   下载地址: https://adoptium.net/
    echo   或: https://www.oracle.com/java/technologies/downloads/
    goto :error_exit
)

:: 检查 Java 版本
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%i
    goto :check_java_done
)
:check_java_done
set JAVA_VERSION=%JAVA_VERSION:"=%
echo [√] Java 版本: %JAVA_VERSION%

:: ============================================
:: 步骤 2: 检查 MySQL
:: ============================================
echo.
echo [2/6] 检查 MySQL...
where mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] 错误: 未找到 mysql 命令
    echo.
    echo 请确保 MySQL 已安装并添加到系统 PATH
    echo.
    echo 常见 MySQL bin 目录:
    echo   - C:\Program Files\MySQL\MySQL Server 8.0\bin
    echo   - C:\xampp\mysql\bin
    echo   - C:\wamp64\bin\mysql\mysql8.0.x\bin
    echo.
    echo 如果已安装但未添加 PATH，请手动添加或使用完整路径
    goto :error_exit
)
echo [√] MySQL 已找到

:: 测试 MySQL 连接
echo.
echo 正在测试 MySQL 连接...
echo 数据库地址: %DB_HOST%:%DB_PORT%
echo 用户名: %DB_USER%

mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "SELECT 1;" >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] 使用默认密码连接失败
    echo.
    set /p DB_PASS="请输入 MySQL root 密码: "

    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "SELECT 1;" >nul 2>&1
    if %errorlevel% neq 0 (
        echo [X] MySQL 连接失败，请检查密码和 MySQL 服务状态
        goto :error_exit
    )
)
echo [√] MySQL 连接成功

:: ============================================
:: 步骤 3: 检查 Redis
:: ============================================
echo.
echo [3/6] 检查 Redis...
where redis-server >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] 警告: 未找到 Redis
    echo.
    echo Redis 用于缓存，建议安装:
    echo   下载地址: https://github.com/tporadowski/redis/releases
    echo   下载 Redis-x64-*.msi 安装包
    echo.
    echo 如果不需要缓存功能，可以在 application.yml 中禁用 Redis
    echo.
    set /p CONTINUE="是否继续安装（不安装 Redis）？[Y/N]: "
    if /i "!CONTINUE!" neq "Y" goto :error_exit
) else (
    echo [√] Redis 已找到
)

:: ============================================
:: 步骤 4: 创建数据库并导入数据
:: ============================================
echo.
echo [4/6] 创建数据库并导入数据...

:: 检查数据库文件是否存在
if not exist "database\xianqi_database.sql" (
    if not exist "..\database\xianqi_database.sql" (
        echo [X] 错误: 未找到数据库文件
        echo.
        echo 请确保以下文件存在:
        echo   database\xianqi_database.sql
        echo.
        echo 或者先在 Mac/Linux 上运行 export_for_migration.sh 导出
        goto :error_exit
    )
    set DB_FILE=..\database\xianqi_database.sql
) else (
    set DB_FILE=database\xianqi_database.sql
)

:: 创建数据库
echo 正在创建数据库 %DB_NAME%...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
if %errorlevel% neq 0 (
    echo [X] 创建数据库失败
    goto :error_exit
)
echo [√] 数据库创建成功

:: 导入数据
echo 正在导入数据库数据...
echo 这可能需要几分钟，请耐心等待...
echo.

mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < %DB_FILE% 2>nul
if %errorlevel% neq 0 (
    echo [X] 数据库导入失败
    echo.
    echo 可能的原因:
    echo   1. SQL 文件格式不正确
    echo   2. 数据库权限不足
    echo   3. MySQL 版本不兼容
    goto :error_exit
)
echo [√] 数据库导入成功

:: ============================================
:: 步骤 5: 配置项目
:: ============================================
echo.
echo [5/6] 配置项目...

:: 创建上传目录
if not exist "%UPLOAD_PATH%" (
    mkdir "%UPLOAD_PATH%"
    echo [√] 创建上传目录: %UPLOAD_PATH%
)

:: 检查项目文件
if exist "files\XianQiJava" (
    set PROJECT_DIR=files\XianQiJava
) else if exist "..\XianQiJava" (
    set PROJECT_DIR=..\XianQiJava
) else if exist "XianQiJava" (
    set PROJECT_DIR=XianQiJava
) else (
    echo [!] 警告: 未找到项目目录
    echo 请手动指定项目路径
    set /p PROJECT_DIR="请输入 XianQiJava 目录路径: "
)

:: 复制上传文件
if exist "files\uploads" (
    echo 正在复制上传文件...
    xcopy /E /I /Y "files\uploads\*" "%UPLOAD_PATH%\" >nul
    echo [√] 上传文件复制成功
)

:: 修改 application.yml 中的配置
echo.
echo 请根据实际情况修改配置文件:
echo   %PROJECT_DIR%\src\main\resources\application.yml
echo.
echo 需要检查的配置项:
echo   - 数据库密码 (spring.datasource.password)
echo   - Redis 密码 (spring.data.redis.password)
echo   - 文件上传路径 (file.upload.path)
echo   - 应用基础URL (app.base-url)

:: ============================================
:: 步骤 6: 生成启动脚本
:: ============================================
echo.
echo [6/6] 生成启动脚本...

:: 创建启动脚本
set START_SCRIPT=%PROJECT_DIR%\start.bat
(
    echo @echo off
    echo chcp 65001 ^>nul
    echo echo 正在启动 XianQi 后端服务...
    echo echo.
    echo cd /d "%%~dp0"
    echo.
    echo :: 检查是否已编译
    echo if not exist "target\XianQiJava-0.0.1-SNAPSHOT.jar" ^(
    echo     echo 首次运行，正在编译项目...
    echo     call mvnw.cmd clean package -DskipTests
    echo ^)
    echo.
    echo echo 启动服务...
    echo echo 访问地址: http://localhost:%PROJECT_PORT%/api/doc.html
    echo echo.
    echo java -jar target\XianQiJava-0.0.1-SNAPSHOT.jar
    echo pause
) > "%START_SCRIPT%"
echo [√] 启动脚本已生成: %START_SCRIPT%

:: 创建停止脚本
set STOP_SCRIPT=%PROJECT_DIR%\stop.bat
(
    echo @echo off
    echo echo 正在停止 XianQi 后端服务...
    echo taskkill /F /FI "WINDOWTITLE eq XianQi*" 2^>nul
    echo for /f "tokens=5" %%%%a in ^('netstat -aon ^| findstr :%PROJECT_PORT% ^| findstr LISTENING'^) do taskkill /F /PID %%%%a 2^>nul
    echo echo 服务已停止
    echo pause
) > "%STOP_SCRIPT%"
echo [√] 停止脚本已生成: %STOP_SCRIPT%

:: ============================================
:: 完成
:: ============================================
echo.
echo ╔════════════════════════════════════════════╗
echo ║              安装完成！                    ║
echo ╚════════════════════════════════════════════╝
echo.
echo ─────────────────────────────────────────────
echo  下一步操作:
echo ─────────────────────────────────────────────
echo.
echo  1. 修改配置文件（如果需要）:
echo     %PROJECT_DIR%\src\main\resources\application.yml
echo.
echo  2. 启动服务:
echo     方式一: 双击 %PROJECT_DIR%\start.bat
echo     方式二: 命令行运行
echo             cd %PROJECT_DIR%
echo             mvnw.cmd spring-boot:run
echo.
echo  3. 访问 API 文档:
echo     http://localhost:%PROJECT_PORT%/api/doc.html
echo.
echo  4. 停止服务:
echo     双击 %PROJECT_DIR%\stop.bat
echo     或按 Ctrl+C
echo.
echo ─────────────────────────────────────────────
echo  服务信息:
echo ─────────────────────────────────────────────
echo  数据库: %DB_NAME%
echo  端口: %PROJECT_PORT%
echo  上传目录: %UPLOAD_PATH%
echo ─────────────────────────────────────────────
echo.
pause
exit /b 0

:: ============================================
:: 错误退出
:: ============================================
:error_exit
echo.
echo ╔════════════════════════════════════════════╗
echo ║              安装失败                      ║
echo ╚════════════════════════════════════════════╝
echo.
echo 请检查错误信息并重试
echo 如有问题，请检查:
echo   1. MySQL 服务是否启动
echo   2. 数据库密码是否正确
echo   3. 端口 %PROJECT_PORT% 是否被占用
echo.
pause
exit /b 1
