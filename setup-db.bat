@echo off
chcp 65001 >nul 2>&1
echo ============================================================
echo   城市临期食品智能分发系统 - 一键数据库初始化
echo ============================================================
echo.

set "SQL_DIR=bylw-spring\sql"

:: 检查 MySQL 是否可用
where mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 找不到 mysql 命令，请确保 MySQL 已安装并添加到 PATH
    echo.
    echo 常见 MySQL 安装路径:
    echo   C:\Program Files\MySQL\MySQL Server 8.0\bin
    echo   C:\xampp\mysql\bin
    echo.
    echo 请将 MySQL 的 bin 目录添加到系统 PATH，或手动执行 SQL 文件
    pause
    exit /b 1
)

echo [提示] 请输入 MySQL 连接信息
echo 默认: root / root / localhost:3306
echo.
set /p DB_USER="MySQL 用户名 (默认 root): "
if "%DB_USER%"=="" set DB_USER=root

set /p DB_PASS="MySQL 密码 (默认 root): "
if "%DB_PASS%"=="" set DB_PASS=root

set /p DB_HOST="MySQL 地址 (默认 localhost): "
if "%DB_HOST%"=="" set DB_HOST=localhost

set /p DB_PORT="MySQL 端口 (默认 3306): "
if "%DB_PORT%"=="" set DB_PORT=3306

set MYSQL_CMD=mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS%

echo.
echo ============================================================
echo   开始初始化数据库
echo ============================================================

echo.
echo [1/7] 创建数据库和表结构...
%MYSQL_CMD% < "%SQL_DIR%\schema.sql"
if %errorlevel% neq 0 (
    echo [错误] schema.sql 执行失败，请检查用户名密码是否正确
    pause
    exit /b 1
)
echo [1/7] 完成

echo.
echo [2/7] 导入标签和食品数据...
%MYSQL_CMD% bylw < "%SQL_DIR%\seed_data.sql"
echo [2/7] 完成

echo.
echo [3/7] 导入用户、文章、食谱、社区数据...
%MYSQL_CMD% bylw < "%SQL_DIR%\seed_data_v2.sql"
echo [3/7] 完成

echo.
echo [4/7] 导入扩充食品数据...
%MYSQL_CMD% bylw < "%SQL_DIR%\seed_data_v2_extra.sql"
echo [4/7] 完成

echo.
echo [5/7] 导入完整食品数据 (200条)...
%MYSQL_CMD% bylw < "%SQL_DIR%\init_data_v2.sql"
echo [5/7] 完成

echo.
echo [6/7] 应用数据库迁移 (列、索引、触发器)...
%MYSQL_CMD% bylw < "%SQL_DIR%\migration_comprehensive_v2.sql"
echo [6/7] 完成

echo.
echo [7/7] 应用增量迁移 (评论表、推荐字段等)...
%MYSQL_CMD% bylw < "%SQL_DIR%\migration_business_optimize.sql"
%MYSQL_CMD% bylw < "%SQL_DIR%\migration_add_version.sql"
%MYSQL_CMD% bylw < "%SQL_DIR%\migration_food_review.sql"
%MYSQL_CMD% bylw < "%SQL_DIR%\migration_remaining.sql"
echo [7/7] 完成

echo.
echo ============================================================
echo   数据库初始化完成!
echo ============================================================
echo.
echo 数据库名: bylw
echo 管理员账号: admin / 123456
echo 测试用户: testuser / user123
echo.
echo 下一步:
echo   1. 用 IDEA 打开 bylw-spring，启动后端 (端口 8080)
echo   2. cd bylw-vue ^&^& npm install ^&^& npm run dev
echo.
pause
