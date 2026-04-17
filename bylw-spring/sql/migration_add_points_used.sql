-- ============================================================
-- 积分抵扣字段迁移：points_used 列（MySQL 8.0 兼容版）
-- 修复 Order.pointsUsed 持久化问题
-- ============================================================

SET @dbname = DATABASE();
SET @tablename = 'food_order';
SET @columnname = 'points_used';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
    'SELECT 1',
    'ALTER TABLE food_order ADD COLUMN points_used INT NOT NULL DEFAULT 0 COMMENT ''订单使用的积分数量'' AFTER total_amount'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SELECT 'points_used 列迁移完成' AS result;
