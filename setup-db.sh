#!/bin/bash
# ============================================================
#   城市临期食品智能分发系统 - 一键数据库初始化 (Linux/macOS)
# ============================================================

set -e

SQL_DIR="bylw-spring/sql"

echo "============================================================"
echo "  城市临期食品智能分发系统 - 一键数据库初始化"
echo "============================================================"
echo ""

# 检查 MySQL
if ! command -v mysql &> /dev/null; then
    echo "[错误] 找不到 mysql 命令，请先安装 MySQL"
    exit 1
fi

echo "[提示] 请输入 MySQL 连接信息 (默认: root / root / localhost:3306)"
echo ""
read -p "MySQL 用户名 (默认 root): " DB_USER
DB_USER=${DB_USER:-root}

read -sp "MySQL 密码 (默认 root): " DB_PASS
DB_PASS=${DB_PASS:-root}
echo ""

read -p "MySQL 地址 (默认 localhost): " DB_HOST
DB_HOST=${DB_HOST:-localhost}

read -p "MySQL 端口 (默认 3306): " DB_PORT
DB_PORT=${DB_PORT:-3306}

MYSQL_CMD="mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS"

echo ""
echo "============================================================"
echo "  开始初始化数据库"
echo "============================================================"

echo ""
echo "[1/7] 创建数据库和表结构..."
$MYSQL_CMD < "$SQL_DIR/schema.sql" || { echo "[错误] schema.sql 执行失败"; exit 1; }
echo "[1/7] 完成"

echo ""
echo "[2/7] 导入标签和食品数据..."
$MYSQL_CMD bylw < "$SQL_DIR/seed_data.sql"
echo "[2/7] 完成"

echo ""
echo "[3/7] 导入用户、文章、食谱、社区数据..."
$MYSQL_CMD bylw < "$SQL_DIR/seed_data_v2.sql"
echo "[3/7] 完成"

echo ""
echo "[4/7] 导入扩充食品数据..."
$MYSQL_CMD bylw < "$SQL_DIR/seed_data_v2_extra.sql"
echo "[4/7] 完成"

echo ""
echo "[5/7] 导入完整食品数据 (200条)..."
$MYSQL_CMD bylw < "$SQL_DIR/init_data_v2.sql"
echo "[5/7] 完成"

echo ""
echo "[6/7] 应用数据库迁移 (列、索引、触发器)..."
$MYSQL_CMD bylw < "$SQL_DIR/migration_comprehensive_v2.sql"
echo "[6/7] 完成"

echo ""
echo "[7/7] 应用增量迁移 (评论表、推荐字段等)..."
$MYSQL_CMD bylw < "$SQL_DIR/migration_business_optimize.sql"
$MYSQL_CMD bylw < "$SQL_DIR/migration_add_version.sql"
$MYSQL_CMD bylw < "$SQL_DIR/migration_food_review.sql"
$MYSQL_CMD bylw < "$SQL_DIR/migration_remaining.sql"
echo "[7/7] 完成"

echo ""
echo "============================================================"
echo "  数据库初始化完成!"
echo "============================================================"
echo ""
echo "数据库名: bylw"
echo "管理员账号: admin / 123456"
echo "测试用户: testuser / user123"
echo ""
echo "下一步:"
echo "  1. 启动后端: cd bylw-spring && mvn spring-boot:run"
echo "  2. 启动前端: cd bylw-vue && npm install && npm run dev"
echo ""
