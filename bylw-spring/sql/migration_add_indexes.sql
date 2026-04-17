-- ============================================================
-- 性能优化索引迁移（MySQL 8.0 兼容版）
-- 使用存储过程自动判断索引是否存在
-- ============================================================

-- 工具：创建索引（如果不存在）
DELIMITER //

DROP PROCEDURE IF EXISTS create_index_if_not_exists //
CREATE PROCEDURE create_index_if_not_exists(
    IN p_table VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_def VARCHAR(256)
)
BEGIN
    DECLARE idx_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO idx_exists
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND INDEX_NAME = p_index_name;

    IF idx_exists = 0 THEN
        SET @sql = CONCAT('CREATE INDEX ', p_index_name, ' ON ', p_table, p_index_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('Created: ', p_index_name) AS result;
    ELSE
        SELECT CONCAT('Exists:  ', p_index_name) AS result;
    END IF;
END //

DELIMITER ;

-- 1. 订单超时查询优化
CALL create_index_if_not_exists('food_order', 'idx_order_pay_time', '(pay_status, create_time)');
CALL create_index_if_not_exists('food_order', 'idx_order_status_deleted', '(order_status, deleted)');

-- 2. 推荐算法用户行为查询
CALL create_index_if_not_exists('user_behavior', 'idx_behavior_user_type_time', '(user_id, target_type, create_time)');

-- 3. 签到防刷
CALL create_index_if_not_exists('points_record', 'idx_points_signin', '(user_id, source_type, create_time)');

-- 4. 食品有效商品筛选
CALL create_index_if_not_exists('food', 'idx_food_status_deleted', '(status, deleted)');

-- 5. 社区帖子待审核
CALL create_index_if_not_exists('community_post', 'idx_post_audit_deleted', '(audit_status, deleted)');

-- 6. 积分兑换商品有效商品筛选
CALL create_index_if_not_exists('points_goods', 'idx_points_goods_status_deleted', '(status, deleted)');

-- 7. 配送记录状态
CALL create_index_if_not_exists('food_delivery', 'idx_delivery_status', '(delivery_status)');

SELECT '索引迁移全部完成' AS result;
