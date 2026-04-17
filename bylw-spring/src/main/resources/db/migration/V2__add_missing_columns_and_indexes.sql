-- ============================================================
-- Flyway V2：补齐缺失列、索引、触发器（幂等）
-- 每个操作先检查是否已存在再执行
-- ============================================================

DROP PROCEDURE IF EXISTS flyway_add_column;
DELIMITER //
CREATE PROCEDURE flyway_add_column(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_definition VARCHAR(256)
)
BEGIN
    DECLARE col_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO col_exists
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND COLUMN_NAME = p_column;
    IF col_exists = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_column, ' ', p_definition);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- 补齐缺失列
CALL flyway_add_column('sys_user', 'role_type', 'INT(1) DEFAULT 1 COMMENT ''角色类型: 1-普通用户, 2-骑手, 99-管理员'' AFTER role');
CALL flyway_add_column('food', 'version', 'INT(11) DEFAULT 0 COMMENT ''乐观锁版本号'' AFTER deleted');
CALL flyway_add_column('food_order', 'points_used', 'INT(11) DEFAULT 0 COMMENT ''使用积分数量'' AFTER pay_time');
CALL flyway_add_column('food_order', 'update_time', 'DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER create_time');
CALL flyway_add_column('food_delivery', 'update_time', 'DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER remark');
CALL flyway_add_column('food_after_sale', 'update_time', 'DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER handle_time');
CALL flyway_add_column('community_post', 'update_time', 'DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER create_time');
CALL flyway_add_column('community_comment', 'update_time', 'DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER create_time');
CALL flyway_add_column('user_behavior', 'update_time', 'DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER create_time');
CALL flyway_add_column('points_record', 'update_time', 'DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER create_time');
CALL flyway_add_column('food_tag_relation', 'update_time', 'DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER tag_id');

DROP PROCEDURE IF EXISTS flyway_add_column;

-- 索引（幂等）
DROP PROCEDURE IF EXISTS flyway_create_index;
DELIMITER //
CREATE PROCEDURE flyway_create_index(
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
        SET @ddl = CONCAT('CREATE INDEX ', p_index_name, ' ON ', p_table, p_index_def);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL flyway_create_index('food_order', 'idx_order_pay_time', '(pay_status, create_time)');
CALL flyway_create_index('food_order', 'idx_order_status_deleted', '(order_status, deleted)');
CALL flyway_create_index('food_order', 'idx_order_create_time', '(create_time)');
CALL flyway_create_index('food_order', 'idx_order_pay_time_range', '(pay_time, pay_status)');
CALL flyway_create_index('food_order_item', 'idx_order_item_food', '(food_id)');
CALL flyway_create_index('user_behavior', 'idx_behavior_user_type_time', '(user_id, target_type, create_time)');
CALL flyway_create_index('points_record', 'idx_points_signin', '(user_id, source_type, create_time)');
CALL flyway_create_index('points_record', 'idx_points_create_time', '(create_time)');
CALL flyway_create_index('food', 'idx_food_status_deleted', '(status, deleted)');
CALL flyway_create_index('food', 'idx_food_expire', '(expire_date, status, deleted)');
CALL flyway_create_index('community_post', 'idx_post_audit_deleted', '(audit_status, deleted)');
CALL flyway_create_index('community_post', 'idx_post_create_time', '(create_time)');
CALL flyway_create_index('points_goods', 'idx_points_goods_status_deleted', '(status, deleted)');
CALL flyway_create_index('points_exchange', 'idx_exchange_create_time', '(create_time)');
CALL flyway_create_index('food_delivery', 'idx_delivery_status', '(delivery_status)');
CALL flyway_create_index('food_after_sale', 'idx_aftersale_status', '(handle_status)');
CALL flyway_create_index('sys_user', 'idx_user_create_time', '(create_time)');
CALL flyway_create_index('sys_user', 'idx_user_deleted', '(deleted)');

DROP PROCEDURE IF EXISTS flyway_create_index;

-- 审计触发器（幂等，先 DROP 再 CREATE）
DELIMITER //

DROP TRIGGER IF EXISTS trg_order_update//
CREATE TRIGGER trg_order_update
BEFORE UPDATE ON food_order FOR EACH ROW
BEGIN SET NEW.update_time = NOW(); END//

DROP TRIGGER IF EXISTS trg_delivery_update//
CREATE TRIGGER trg_delivery_update
BEFORE UPDATE ON food_delivery FOR EACH ROW
BEGIN SET NEW.update_time = NOW(); END//

DROP TRIGGER IF EXISTS trg_aftersale_update//
CREATE TRIGGER trg_aftersale_update
BEFORE UPDATE ON food_after_sale FOR EACH ROW
BEGIN SET NEW.update_time = NOW(); END//

DROP TRIGGER IF EXISTS trg_post_update//
CREATE TRIGGER trg_post_update
BEFORE UPDATE ON community_post FOR EACH ROW
BEGIN SET NEW.update_time = NOW(); END//

DROP TRIGGER IF EXISTS trg_comment_update//
CREATE TRIGGER trg_comment_update
BEFORE UPDATE ON community_comment FOR EACH ROW
BEGIN SET NEW.update_time = NOW(); END//

DROP TRIGGER IF EXISTS trg_behavior_update//
CREATE TRIGGER trg_behavior_update
BEFORE UPDATE ON user_behavior FOR EACH ROW
BEGIN SET NEW.update_time = NOW(); END//

DROP TRIGGER IF EXISTS trg_points_update//
CREATE TRIGGER trg_points_update
BEFORE UPDATE ON points_record FOR EACH ROW
BEGIN SET NEW.update_time = NOW(); END//

DROP TRIGGER IF EXISTS trg_tag_relation_update//
CREATE TRIGGER trg_tag_relation_update
BEFORE UPDATE ON food_tag_relation FOR EACH ROW
BEGIN SET NEW.update_time = NOW(); END//

DELIMITER ;
