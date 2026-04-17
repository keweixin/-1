-- ============================================================
-- 综合业务优化迁移（MySQL 8.0 兼容版）
-- ============================================================

-- ============================================================
-- 1. user_like 表（不存在则创建）+ 唯一索引
-- ============================================================
SET @tbl_exists = NULL;
SELECT COUNT(*) INTO @tbl_exists FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_like';
SET @create_sql = IF(@tbl_exists = 0,
    'CREATE TABLE user_like (like_id INT AUTO_INCREMENT PRIMARY KEY, user_id INT NOT NULL, target_type VARCHAR(32) NOT NULL, target_id INT NOT NULL, create_time DATETIME DEFAULT CURRENT_TIMESTAMP, INDEX idx_target (target_type, target_id), INDEX idx_user (user_id))',
    'SELECT ''Table already exists''');
PREPARE create_tbl FROM @create_sql;
EXECUTE create_tbl;
DEALLOCATE PREPARE create_tbl;

SET @idx_exists = NULL;
SELECT COUNT(*) INTO @idx_exists FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_like'
  AND INDEX_NAME = 'uk_user_like_target';
SET @sql = IF(@idx_exists = 0,
    'CREATE UNIQUE INDEX uk_user_like_target ON user_like(user_id, target_type, target_id)',
    'SELECT ''Index already exists''');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 2. recipe_favorite 表唯一索引（幂等，已存在则跳过）
-- ============================================================
SET @idx_exists = NULL;
SELECT COUNT(*) INTO @idx_exists FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'recipe_favorite'
  AND INDEX_NAME = 'uk_user_recipe';
SET @sql = IF(@idx_exists = 0,
    'CREATE UNIQUE INDEX uk_user_recipe ON recipe_favorite(user_id, recipe_id)',
    'SELECT ''Index already exists''');
PREPARE stmt2 FROM @sql;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- ============================================================
-- 3. food_order.points_used 列（幂等添加）
-- ============================================================
SET @col_exists = NULL;
SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food_order'
  AND COLUMN_NAME = 'points_used';
SET @alter_sql = IF(@col_exists = 0,
    'ALTER TABLE food_order ADD COLUMN points_used INT NOT NULL DEFAULT 0 COMMENT ''订单使用的积分数量'' AFTER total_amount',
    'SELECT ''Column already exists''');
PREPARE alter_stmt FROM @alter_sql;
EXECUTE alter_stmt;
DEALLOCATE PREPARE alter_stmt;

-- ============================================================
-- 4. points_goods.version 列（幂等添加）
-- ============================================================
SET @col_exists = NULL;
SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'points_goods'
  AND COLUMN_NAME = 'version';
SET @alter_sql = IF(@col_exists = 0,
    'ALTER TABLE points_goods ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号'' AFTER deleted',
    'SELECT ''Column already exists''');
PREPARE alter_stmt2 FROM @alter_sql;
EXECUTE alter_stmt2;
DEALLOCATE PREPARE alter_stmt2;

-- ============================================================
-- 5. user_behavior 表统计优化索引（按时间聚合查询）
-- ============================================================
SET @idx_exists = NULL;
SELECT COUNT(*) INTO @idx_exists FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_behavior'
  AND INDEX_NAME = 'idx_user_behavior_create';
SET @sql = IF(@idx_exists = 0,
    'CREATE INDEX idx_user_behavior_create ON user_behavior(create_time)',
    'SELECT ''Index already exists''');
PREPARE stmt3 FROM @sql;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

SELECT '综合迁移全部完成' AS result;
