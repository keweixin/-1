-- ============================================================
-- 综合数据库升级脚本 V2
-- 覆盖：缺失列、性能索引、审计触发器、安全加固
-- 适用于从 schema.sql 初始化后追加执行
-- ============================================================

SET NAMES utf8mb4;
USE bylw;

-- ============================================================
-- 第一部分：缺失列补齐
-- ============================================================

-- 1.1 sys_user 添加 role_type 列（角色类型：1-普通用户, 2-骑手, 99-管理员）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = 'role_type';
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN role_type INT(1) DEFAULT 1 COMMENT ''角色类型: 1-普通用户, 2-骑手, 99-管理员'' AFTER role',
    'SELECT ''sys_user.role_type already exists'' AS result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.2 food 添加 version 列（乐观锁）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food' AND COLUMN_NAME = 'version';
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE food ADD COLUMN version INT(11) DEFAULT 0 COMMENT ''乐观锁版本号'' AFTER deleted',
    'SELECT ''food.version already exists'' AS result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.3 food_order 添加 points_used 列（积分抵扣）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food_order' AND COLUMN_NAME = 'points_used';
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE food_order ADD COLUMN points_used INT(11) DEFAULT 0 COMMENT ''使用积分数量'' AFTER pay_time',
    'SELECT ''food_order.points_used already exists'' AS result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.4 food_order 添加 update_time 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food_order' AND COLUMN_NAME = 'update_time';
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE food_order ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER create_time',
    'SELECT ''food_order.update_time already exists'' AS result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.5 food_delivery 添加 update_time 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food_delivery' AND COLUMN_NAME = 'update_time';
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE food_delivery ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER remark',
    'SELECT ''food_delivery.update_time already exists'' AS result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.6 food_after_sale 添加 update_time 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food_after_sale' AND COLUMN_NAME = 'update_time';
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE food_after_sale ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER handle_time',
    'SELECT ''food_after_sale.update_time already exists'' AS result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.7 community_post 添加 update_time 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'community_post' AND COLUMN_NAME = 'update_time';
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE community_post ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER create_time',
    'SELECT ''community_post.update_time already exists'' AS result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.8 community_comment 添加 update_time 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'community_comment' AND COLUMN_NAME = 'update_time';
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE community_comment ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER create_time',
    'SELECT ''community_comment.update_time already exists'' AS result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.9 user_behavior 添加 update_time 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_behavior' AND COLUMN_NAME = 'update_time';
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE user_behavior ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER create_time',
    'SELECT ''user_behavior.update_time already exists'' AS result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.10 points_record 添加 update_time 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'points_record' AND COLUMN_NAME = 'update_time';
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE points_record ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER create_time',
    'SELECT ''points_record.update_time already exists'' AS result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.11 food_tag_relation 添加 update_time 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'food_tag_relation' AND COLUMN_NAME = 'update_time';
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE food_tag_relation ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT ''更新时间'' AFTER tag_id',
    'SELECT ''food_tag_relation.update_time already exists'' AS result');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '第一部分：缺失列补齐完成' AS result;

-- ============================================================
-- 第二部分：性能索引优化（幂等）
-- ============================================================

DROP PROCEDURE IF EXISTS create_index_if_not_exists;
DELIMITER //
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
        SET @ddl = CONCAT('CREATE INDEX ', p_index_name, ' ON ', p_table, p_index_def);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- 2.1 订单高频查询索引
CALL create_index_if_not_exists('food_order', 'idx_order_pay_time', '(pay_status, create_time)');
CALL create_index_if_not_exists('food_order', 'idx_order_status_deleted', '(order_status, deleted)');
CALL create_index_if_not_exists('food_order', 'idx_order_create_time', '(create_time)');
CALL create_index_if_not_exists('food_order', 'idx_order_pay_time_range', '(pay_time, pay_status)');

-- 2.2 订单明细查询索引
CALL create_index_if_not_exists('food_order_item', 'idx_order_item_food', '(food_id)');

-- 2.3 推荐算法行为查询覆盖索引
CALL create_index_if_not_exists('user_behavior', 'idx_behavior_user_type_time', '(user_id, target_type, create_time)');

-- 2.4 签到防刷复合索引
CALL create_index_if_not_exists('points_record', 'idx_points_signin', '(user_id, source_type, create_time)');
CALL create_index_if_not_exists('points_record', 'idx_points_create_time', '(create_time)');

-- 2.5 食品有效商品筛选覆盖索引
CALL create_index_if_not_exists('food', 'idx_food_status_deleted', '(status, deleted)');
CALL create_index_if_not_exists('food', 'idx_food_expire', '(expire_date, status, deleted)');

-- 2.6 社区帖子待审核索引
CALL create_index_if_not_exists('community_post', 'idx_post_audit_deleted', '(audit_status, deleted)');
CALL create_index_if_not_exists('community_post', 'idx_post_create_time', '(create_time)');

-- 2.7 积分兑换状态索引
CALL create_index_if_not_exists('points_goods', 'idx_points_goods_status_deleted', '(status, deleted)');
CALL create_index_if_not_exists('points_exchange', 'idx_exchange_create_time', '(create_time)');

-- 2.8 配送状态索引
CALL create_index_if_not_exists('food_delivery', 'idx_delivery_status', '(delivery_status)');

-- 2.9 售后查询索引
CALL create_index_if_not_exists('food_after_sale', 'idx_aftersale_status', '(handle_status)');

-- 2.10 文章/食谱时间查询索引
CALL create_index_if_not_exists('food_article', 'idx_article_status_deleted', '(status, deleted)');
CALL create_index_if_not_exists('food_recipe', 'idx_recipe_status_deleted', '(status, deleted)');

-- 2.11 用户查询索引
CALL create_index_if_not_exists('sys_user', 'idx_user_create_time', '(create_time)');
CALL create_index_if_not_exists('sys_user', 'idx_user_deleted', '(deleted)');

DROP PROCEDURE IF EXISTS create_index_if_not_exists;

SELECT '第二部分：性能索引优化完成' AS result;

-- ============================================================
-- 第三部分：审计触发器（自动维护 update_time）
-- ============================================================

DELIMITER //

-- 3.1 订单表更新触发器
DROP TRIGGER IF EXISTS trg_order_update//
CREATE TRIGGER trg_order_update
BEFORE UPDATE ON food_order
FOR EACH ROW
BEGIN
    SET NEW.update_time = NOW();
END//

-- 3.2 配送表更新触发器
DROP TRIGGER IF EXISTS trg_delivery_update//
CREATE TRIGGER trg_delivery_update
BEFORE UPDATE ON food_delivery
FOR EACH ROW
BEGIN
    SET NEW.update_time = NOW();
END//

-- 3.3 售后表更新触发器
DROP TRIGGER IF EXISTS trg_aftersale_update//
CREATE TRIGGER trg_aftersale_update
BEFORE UPDATE ON food_after_sale
FOR EACH ROW
BEGIN
    SET NEW.update_time = NOW();
END//

-- 3.4 社区帖子更新触发器
DROP TRIGGER IF EXISTS trg_post_update//
CREATE TRIGGER trg_post_update
BEFORE UPDATE ON community_post
FOR EACH ROW
BEGIN
    SET NEW.update_time = NOW();
END//

-- 3.5 社区评论更新触发器
DROP TRIGGER IF EXISTS trg_comment_update//
CREATE TRIGGER trg_comment_update
BEFORE UPDATE ON community_comment
FOR EACH ROW
BEGIN
    SET NEW.update_time = NOW();
END//

-- 3.6 用户行为更新触发器
DROP TRIGGER IF EXISTS trg_behavior_update//
CREATE TRIGGER trg_behavior_update
BEFORE UPDATE ON user_behavior
FOR EACH ROW
BEGIN
    SET NEW.update_time = NOW();
END//

-- 3.7 积分记录更新触发器
DROP TRIGGER IF EXISTS trg_points_update//
CREATE TRIGGER trg_points_update
BEFORE UPDATE ON points_record
FOR EACH ROW
BEGIN
    SET NEW.update_time = NOW();
END//

-- 3.8 食品标签关联更新触发器
DROP TRIGGER IF EXISTS trg_tag_relation_update//
CREATE TRIGGER trg_tag_relation_update
BEFORE UPDATE ON food_tag_relation
FOR EACH ROW
BEGIN
    SET NEW.update_time = NOW();
END//

DELIMITER ;

SELECT '第三部分：审计触发器完成' AS result;

-- ============================================================
-- 第四部分：字段注释补全（已存在列追加 COMMENT）
-- ============================================================

ALTER TABLE sys_user MODIFY COLUMN password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt/MD5)';
ALTER TABLE sys_user MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: admin/user';
ALTER TABLE sys_user MODIFY COLUMN status INT(1) NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用';
ALTER TABLE sys_user MODIFY COLUMN deleted INT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已删';

ALTER TABLE food MODIFY COLUMN order_status VARCHAR(20) NOT NULL COMMENT '订单状态: 待支付/待接单/待配送/配送中/已完成/已取消/已退款';
ALTER TABLE food_order MODIFY COLUMN order_status VARCHAR(20) NOT NULL COMMENT '订单状态: 待支付/待接单/待配送/配送中/已完成/已取消/已退款';
ALTER TABLE food_order MODIFY COLUMN pay_status VARCHAR(20) NOT NULL COMMENT '支付状态: 待支付/已支付';
ALTER TABLE food_order MODIFY COLUMN total_amount DECIMAL(10,2) COMMENT '实付金额(扣除积分后)';
ALTER TABLE food_order MODIFY COLUMN points_used INT(11) DEFAULT 0 COMMENT '使用积分数量(1积分=0.01元)';

ALTER TABLE food_delivery MODIFY COLUMN delivery_status VARCHAR(20) NOT NULL COMMENT '配送状态: 待派单/已派单/配送中/已完成/已拒绝';

ALTER TABLE food_after_sale MODIFY COLUMN handle_status VARCHAR(20) NOT NULL COMMENT '处理状态: 待处理/已处理/已拒绝';
ALTER TABLE food_after_sale MODIFY COLUMN reason_type VARCHAR(50) COMMENT '申诉类型: 质量问题/配送延迟/商品损坏/其他';

ALTER TABLE points_record MODIFY COLUMN change_type VARCHAR(20) NOT NULL COMMENT '变动类型: earn-获得, spend-消费';
ALTER TABLE points_record MODIFY COLUMN source_type VARCHAR(50) COMMENT '来源: env_reward-环保奖励, signin-签到, deduct-订单抵扣, exchange-兑换';

ALTER TABLE community_post MODIFY COLUMN audit_status VARCHAR(20) NOT NULL DEFAULT '待审核' COMMENT '审核状态: 待审核/已通过/已拒绝';

SELECT '第四部分：字段注释补全完成' AS result;

-- ============================================================
-- 最终验证
-- ============================================================

SELECT
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='bylw' AND COLUMN_NAME='update_time') AS update_time列数,
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA='bylw' AND INDEX_NAME != 'PRIMARY') AS 索引总数,
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA='bylw' AND CONSTRAINT_TYPE='FOREIGN KEY') AS 外键总数,
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TRIGGERS WHERE TRIGGER_SCHEMA='bylw') AS 触发器总数;

SELECT '========== 数据库升级 V2 全部完成 ==========' AS result;
