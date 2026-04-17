-- ============================================================
-- 乐观锁 version 字段迁移（MySQL 8.0 兼容版）
-- ============================================================

-- 1. 食品表乐观锁
SET @dbname = DATABASE();
SET @tablename = 'food';
SET @columnname = 'version';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE food ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号'' AFTER deleted'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. 积分商品表乐观锁
SET @tablename = 'points_goods';
SET @columnname = 'version';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE points_goods ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号'' AFTER deleted'
));
PREPARE alterIfNotExists2 FROM @preparedStatement;
EXECUTE alterIfNotExists2;
DEALLOCATE PREPARE alterIfNotExists2;

SELECT '乐观锁迁移完成' AS result;
