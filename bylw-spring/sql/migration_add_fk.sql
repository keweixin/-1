-- ============================================================
-- 外键约束迁移脚本（完整版，24 条外键）
-- 确保全部 24 张表之间关联都有数据库级约束
-- ============================================================

SET NAMES utf8mb4;
USE bylw;

-- ============================================================
-- 核心商品域
-- ============================================================

-- 1. 食品 → 商超门店
ALTER TABLE food
    ADD CONSTRAINT fk_food_merchant
    FOREIGN KEY (merchant_id) REFERENCES food_merchant(merchant_id)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 2. 食品 → 食品分类
ALTER TABLE food
    ADD CONSTRAINT fk_food_category
    FOREIGN KEY (category_id) REFERENCES food_category(category_id)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 3. 食品标签关联 → 食品
ALTER TABLE food_tag_relation
    ADD CONSTRAINT fk_tag_relation_food
    FOREIGN KEY (food_id) REFERENCES food(food_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 4. 食品标签关联 → 标签
ALTER TABLE food_tag_relation
    ADD CONSTRAINT fk_tag_relation_tag
    FOREIGN KEY (tag_id) REFERENCES food_tag(tag_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- ============================================================
-- 订单交易域
-- ============================================================

-- 5. 订单 → 用户
ALTER TABLE food_order
    ADD CONSTRAINT fk_order_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- 6. 订单明细 → 订单
ALTER TABLE food_order_item
    ADD CONSTRAINT fk_order_item_order
    FOREIGN KEY (order_id) REFERENCES food_order(order_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 7. 订单明细 → 食品
ALTER TABLE food_order_item
    ADD CONSTRAINT fk_order_item_food
    FOREIGN KEY (food_id) REFERENCES food(food_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- 8. 配送 → 订单
ALTER TABLE food_delivery
    ADD CONSTRAINT fk_delivery_order
    FOREIGN KEY (order_id) REFERENCES food_order(order_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 9. 售后 → 用户
ALTER TABLE food_after_sale
    ADD CONSTRAINT fk_aftersale_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- 10. 售后 → 订单
ALTER TABLE food_after_sale
    ADD CONSTRAINT fk_aftersale_order
    FOREIGN KEY (order_id) REFERENCES food_order(order_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- ============================================================
-- 用户与档案域
-- ============================================================

-- 11. 饮食档案 → 用户
ALTER TABLE food_profile
    ADD CONSTRAINT fk_profile_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 12. 用户行为 → 用户
ALTER TABLE user_behavior
    ADD CONSTRAINT fk_behavior_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 13. 推荐结果 → 用户
ALTER TABLE recommendation
    ADD CONSTRAINT fk_rec_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- ============================================================
-- 内容社区域
-- ============================================================

-- 14. 膳食百科 → 用户（发布者）
ALTER TABLE food_article
    ADD CONSTRAINT fk_article_publisher
    FOREIGN KEY (publisher_id) REFERENCES sys_user(user_id)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 15. 健康食谱 → 用户（发布者）
ALTER TABLE food_recipe
    ADD CONSTRAINT fk_recipe_publisher
    FOREIGN KEY (publisher_id) REFERENCES sys_user(user_id)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 16. 食谱收藏 → 用户
ALTER TABLE recipe_favorite
    ADD CONSTRAINT fk_favorite_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 17. 食谱收藏 → 食谱
ALTER TABLE recipe_favorite
    ADD CONSTRAINT fk_favorite_recipe
    FOREIGN KEY (recipe_id) REFERENCES food_recipe(recipe_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 18. 帖子 → 用户
ALTER TABLE community_post
    ADD CONSTRAINT fk_post_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- 19. 评论 → 用户
ALTER TABLE community_comment
    ADD CONSTRAINT fk_comment_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- 20. 评论 → 帖子
ALTER TABLE community_comment
    ADD CONSTRAINT fk_comment_post
    FOREIGN KEY (post_id) REFERENCES community_post(post_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- ============================================================
-- 积分域
-- ============================================================

-- 21. 积分记录 → 用户
ALTER TABLE points_record
    ADD CONSTRAINT fk_points_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- 22. 积分兑换 → 商品
ALTER TABLE points_exchange
    ADD CONSTRAINT fk_exchange_goods
    FOREIGN KEY (goods_id) REFERENCES points_goods(goods_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- 23. 积分兑换 → 用户
ALTER TABLE points_exchange
    ADD CONSTRAINT fk_exchange_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- ============================================================
-- 系统配置域
-- ============================================================

-- 24. 公告 → 用户（发布者）
ALTER TABLE sys_notice
    ADD CONSTRAINT fk_notice_publisher
    FOREIGN KEY (publisher_id) REFERENCES sys_user(user_id)
    ON DELETE SET NULL ON UPDATE CASCADE;

SELECT '外键约束全部完成（24条）' AS result;
