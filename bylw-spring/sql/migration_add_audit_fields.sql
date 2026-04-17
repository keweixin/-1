-- ============================================================
-- 审计字段迁移脚本
-- 为关键表添加 update_time 字段
-- ============================================================

SET NAMES utf8mb4;

USE bylw;

-- 1. 订单表添加更新时间
ALTER TABLE food_order
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间'
    AFTER create_time;

-- 2. 配送表添加更新时间
ALTER TABLE food_delivery
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间'
    AFTER remark;

-- 3. 售后表添加更新时间
ALTER TABLE after_sale
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间'
    AFTER create_time;

-- 4. 社区帖子表添加更新时间
ALTER TABLE community_post
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间'
    AFTER create_time;

-- 5. 社区评论表添加更新时间
ALTER TABLE community_comment
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间'
    AFTER create_time;

-- 6. 用户行为表添加更新时间
ALTER TABLE user_behavior
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间'
    AFTER create_time;

-- 7. 积分记录表添加更新时间
ALTER TABLE points_record
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间'
    AFTER create_time;

-- 8. 食品标签关联表添加更新时间
ALTER TABLE food_tag_relation
    ADD COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间'
    AFTER tag_id;

-- 创建审计触发器示例（MySQL 8.0+）
DELIMITER //

-- 订单表更新触发器
DROP TRIGGER IF EXISTS trg_order_update//
CREATE TRIGGER trg_order_update
BEFORE UPDATE ON food_order
FOR EACH ROW
BEGIN
    SET NEW.update_time = NOW();
END//

-- 配送表更新触发器
DROP TRIGGER IF EXISTS trg_delivery_update//
CREATE TRIGGER trg_delivery_update
BEFORE UPDATE ON food_delivery
FOR EACH ROW
BEGIN
    SET NEW.update_time = NOW();
END//

DELIMITER ;

SELECT '审计字段迁移完成' AS result;