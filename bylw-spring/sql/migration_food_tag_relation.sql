-- ============================================================
-- 食品标签关联表 (food_tag_relation)
-- ============================================================
DROP TABLE IF EXISTS food_tag_relation;
CREATE TABLE food_tag_relation (
    relation_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '关联编号',
    food_id     INT(11) NOT NULL COMMENT '食品编号',
    tag_id      INT(11) NOT NULL COMMENT '标签编号',
    UNIQUE KEY uk_food_tag (food_id, tag_id),
    INDEX idx_food_id (food_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品与标签的关联表';
