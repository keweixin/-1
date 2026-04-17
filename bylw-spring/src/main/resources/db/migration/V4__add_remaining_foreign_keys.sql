-- ============================================================
-- Flyway 迁移 V4：补齐剩余外键约束（幂等版本）
-- 使用 IF NOT EXISTS 避免与 V3 存储过程创建的约束冲突
-- ============================================================

-- 食品 → 商超门店
ALTER TABLE food
    ADD CONSTRAINT IF NOT EXISTS fk_food_merchant
    FOREIGN KEY (merchant_id) REFERENCES food_merchant(merchant_id)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 食品 → 食品分类
ALTER TABLE food
    ADD CONSTRAINT IF NOT EXISTS fk_food_category
    FOREIGN KEY (category_id) REFERENCES food_category(category_id)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 积分兑换 → 积分商品
ALTER TABLE points_exchange
    ADD CONSTRAINT IF NOT EXISTS fk_exchange_goods
    FOREIGN KEY (goods_id) REFERENCES points_goods(goods_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- 积分兑换 → 用户
ALTER TABLE points_exchange
    ADD CONSTRAINT IF NOT EXISTS fk_exchange_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE RESTRICT ON UPDATE CASCADE;

-- 食谱收藏 → 用户
ALTER TABLE recipe_favorite
    ADD CONSTRAINT IF NOT EXISTS fk_favorite_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 食谱收藏 → 食谱
ALTER TABLE recipe_favorite
    ADD CONSTRAINT IF NOT EXISTS fk_favorite_recipe
    FOREIGN KEY (recipe_id) REFERENCES food_recipe(recipe_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 用户行为 → 用户
ALTER TABLE user_behavior
    ADD CONSTRAINT IF NOT EXISTS fk_behavior_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 膳食百科 → 用户
ALTER TABLE food_article
    ADD CONSTRAINT IF NOT EXISTS fk_article_publisher
    FOREIGN KEY (publisher_id) REFERENCES sys_user(user_id)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 健康食谱 → 用户
ALTER TABLE food_recipe
    ADD CONSTRAINT IF NOT EXISTS fk_recipe_publisher
    FOREIGN KEY (publisher_id) REFERENCES sys_user(user_id)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 公告 → 用户
ALTER TABLE sys_notice
    ADD CONSTRAINT IF NOT EXISTS fk_notice_publisher
    FOREIGN KEY (publisher_id) REFERENCES sys_user(user_id)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 推荐结果 → 用户
ALTER TABLE recommendation
    ADD CONSTRAINT IF NOT EXISTS fk_rec_user
    FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE;
