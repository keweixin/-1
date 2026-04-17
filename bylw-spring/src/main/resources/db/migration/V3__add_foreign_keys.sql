-- ============================================================
-- Flyway V3：外键约束（幂等，逐条检查是否存在）
-- 先清理孤立数据，再建立约束
-- ============================================================

-- 清理孤立数据：food_tag_relation 中 tag_id 不存在于 food_tag
DELETE r FROM food_tag_relation r
    LEFT JOIN food_tag t ON r.tag_id = t.tag_id
    WHERE t.tag_id IS NULL;

-- 清理孤立数据：food_tag_relation 中 food_id 不存在于 food
DELETE r FROM food_tag_relation r
    LEFT JOIN food f ON r.food_id = f.food_id
    WHERE f.food_id IS NULL;

-- 清理孤立数据：recipe_favorite 中 recipe_id 不存在于 food_recipe
DELETE f FROM recipe_favorite f
    LEFT JOIN food_recipe r ON f.recipe_id = r.recipe_id
    WHERE r.recipe_id IS NULL;

-- 清理孤立数据：recipe_favorite 中 user_id 不存在于 sys_user
DELETE f FROM recipe_favorite f
    LEFT JOIN sys_user u ON f.user_id = u.user_id
    WHERE u.user_id IS NULL;

-- 清理孤立数据：points_exchange 中 goods_id 不存在于 points_goods
DELETE e FROM points_exchange e
    LEFT JOIN points_goods g ON e.goods_id = g.goods_id
    WHERE g.goods_id IS NULL;

-- 清理孤立数据：food_order_item 中 food_id 不存在于 food
DELETE i FROM food_order_item i
    LEFT JOIN food f ON i.food_id = f.food_id
    WHERE f.food_id IS NULL;

-- 清理孤立数据：food_order_item 中 order_id 不存在于 food_order
DELETE i FROM food_order_item i
    LEFT JOIN food_order o ON i.order_id = o.order_id
    WHERE o.order_id IS NULL;

-- 清理孤立数据：food_delivery 中 order_id 不存在于 food_order
DELETE d FROM food_delivery d
    LEFT JOIN food_order o ON d.order_id = o.order_id
    WHERE o.order_id IS NULL;

-- 清理孤立数据：food_after_sale 中 order_id 不存在于 food_order
DELETE a FROM food_after_sale a
    LEFT JOIN food_order o ON a.order_id = o.order_id
    WHERE o.order_id IS NULL;

-- 清理孤立数据：community_comment 中 post_id 不存在于 community_post
DELETE c FROM community_comment c
    LEFT JOIN community_post p ON c.post_id = p.post_id
    WHERE p.post_id IS NULL;

-- 清理孤立数据：community_comment 中 user_id 不存在于 sys_user
DELETE c FROM community_comment c
    LEFT JOIN sys_user u ON c.user_id = u.user_id
    WHERE u.user_id IS NULL;

-- 清理孤立数据：community_post 中 user_id 不存在于 sys_user
DELETE p FROM community_post p
    LEFT JOIN sys_user u ON p.user_id = u.user_id
    WHERE u.user_id IS NULL;

-- 清理孤立数据：food_order 中 user_id 不存在于 sys_user
DELETE o FROM food_order o
    LEFT JOIN sys_user u ON o.user_id = u.user_id
    WHERE u.user_id IS NULL;

-- 清理孤立数据：food_after_sale 中 user_id 不存在于 sys_user
DELETE a FROM food_after_sale a
    LEFT JOIN sys_user u ON a.user_id = u.user_id
    WHERE u.user_id IS NULL;

-- 清理孤立数据：points_record 中 user_id 不存在于 sys_user
DELETE p FROM points_record p
    LEFT JOIN sys_user u ON p.user_id = u.user_id
    WHERE u.user_id IS NULL;

-- 清理孤立数据：points_exchange 中 user_id 不存在于 sys_user
DELETE e FROM points_exchange e
    LEFT JOIN sys_user u ON e.user_id = u.user_id
    WHERE u.user_id IS NULL;

-- 清理孤立数据：user_behavior 中 user_id 不存在于 sys_user
DELETE b FROM user_behavior b
    LEFT JOIN sys_user u ON b.user_id = u.user_id
    WHERE u.user_id IS NULL;

-- 清理孤立数据：food 中 merchant_id 不存在于 food_merchant
UPDATE food SET merchant_id = NULL WHERE merchant_id IS NOT NULL AND merchant_id NOT IN (SELECT merchant_id FROM food_merchant);

-- 清理孤立数据：food 中 category_id 不存在于 food_category
UPDATE food SET category_id = NULL WHERE category_id IS NOT NULL AND category_id NOT IN (SELECT category_id FROM food_category);

DROP PROCEDURE IF EXISTS flyway_add_fk;
DELIMITER //
CREATE PROCEDURE flyway_add_fk(
    IN p_table VARCHAR(64),
    IN p_constraint VARCHAR(64),
    IN p_fk_def VARCHAR(512)
)
BEGIN
    DECLARE fk_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO fk_exists
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND CONSTRAINT_NAME = p_constraint
      AND CONSTRAINT_TYPE = 'FOREIGN KEY';
    IF fk_exists = 0 THEN
        SET @ddl = CONCAT('ALTER TABLE ', p_table, ' ADD CONSTRAINT ', p_constraint, ' ', p_fk_def);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- 核心商品域
CALL flyway_add_fk('food', 'fk_food_merchant', 'FOREIGN KEY (merchant_id) REFERENCES food_merchant(merchant_id) ON DELETE SET NULL ON UPDATE CASCADE');
CALL flyway_add_fk('food', 'fk_food_category', 'FOREIGN KEY (category_id) REFERENCES food_category(category_id) ON DELETE SET NULL ON UPDATE CASCADE');
CALL flyway_add_fk('food_tag_relation', 'fk_tag_relation_food', 'FOREIGN KEY (food_id) REFERENCES food(food_id) ON DELETE CASCADE ON UPDATE CASCADE');
CALL flyway_add_fk('food_tag_relation', 'fk_tag_relation_tag', 'FOREIGN KEY (tag_id) REFERENCES food_tag(tag_id) ON DELETE RESTRICT ON UPDATE CASCADE');

-- 订单交易域
CALL flyway_add_fk('food_order', 'fk_order_user', 'FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE RESTRICT ON UPDATE CASCADE');
CALL flyway_add_fk('food_order_item', 'fk_order_item_order', 'FOREIGN KEY (order_id) REFERENCES food_order(order_id) ON DELETE CASCADE ON UPDATE CASCADE');
CALL flyway_add_fk('food_order_item', 'fk_order_item_food', 'FOREIGN KEY (food_id) REFERENCES food(food_id) ON DELETE RESTRICT ON UPDATE CASCADE');
CALL flyway_add_fk('food_delivery', 'fk_delivery_order', 'FOREIGN KEY (order_id) REFERENCES food_order(order_id) ON DELETE CASCADE ON UPDATE CASCADE');
CALL flyway_add_fk('food_after_sale', 'fk_aftersale_user', 'FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE RESTRICT ON UPDATE CASCADE');
CALL flyway_add_fk('food_after_sale', 'fk_aftersale_order', 'FOREIGN KEY (order_id) REFERENCES food_order(order_id) ON DELETE RESTRICT ON UPDATE CASCADE');

-- 用户与档案域
CALL flyway_add_fk('food_profile', 'fk_profile_user', 'FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE ON UPDATE CASCADE');
CALL flyway_add_fk('user_behavior', 'fk_behavior_user', 'FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE ON UPDATE CASCADE');
CALL flyway_add_fk('recommendation', 'fk_rec_user', 'FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE ON UPDATE CASCADE');

-- 内容社区域
CALL flyway_add_fk('food_article', 'fk_article_publisher', 'FOREIGN KEY (publisher_id) REFERENCES sys_user(user_id) ON DELETE SET NULL ON UPDATE CASCADE');
CALL flyway_add_fk('food_recipe', 'fk_recipe_publisher', 'FOREIGN KEY (publisher_id) REFERENCES sys_user(user_id) ON DELETE SET NULL ON UPDATE CASCADE');
CALL flyway_add_fk('recipe_favorite', 'fk_favorite_user', 'FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE ON UPDATE CASCADE');
CALL flyway_add_fk('recipe_favorite', 'fk_favorite_recipe', 'FOREIGN KEY (recipe_id) REFERENCES food_recipe(recipe_id) ON DELETE CASCADE ON UPDATE CASCADE');
CALL flyway_add_fk('community_post', 'fk_post_user', 'FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE RESTRICT ON UPDATE CASCADE');
CALL flyway_add_fk('community_comment', 'fk_comment_user', 'FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE RESTRICT ON UPDATE CASCADE');
CALL flyway_add_fk('community_comment', 'fk_comment_post', 'FOREIGN KEY (post_id) REFERENCES community_post(post_id) ON DELETE CASCADE ON UPDATE CASCADE');

-- 积分域
CALL flyway_add_fk('points_record', 'fk_points_user', 'FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE RESTRICT ON UPDATE CASCADE');
CALL flyway_add_fk('points_exchange', 'fk_exchange_goods', 'FOREIGN KEY (goods_id) REFERENCES points_goods(goods_id) ON DELETE RESTRICT ON UPDATE CASCADE');
CALL flyway_add_fk('points_exchange', 'fk_exchange_user', 'FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE RESTRICT ON UPDATE CASCADE');

-- 系统配置域
CALL flyway_add_fk('sys_notice', 'fk_notice_publisher', 'FOREIGN KEY (publisher_id) REFERENCES sys_user(user_id) ON DELETE SET NULL ON UPDATE CASCADE');

DROP PROCEDURE IF EXISTS flyway_add_fk;
