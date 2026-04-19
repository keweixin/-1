-- 食品评论表
CREATE TABLE IF NOT EXISTS food_review (
  review_id INT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  food_id INT NOT NULL COMMENT '食品ID',
  user_id INT NOT NULL COMMENT '用户ID',
  content VARCHAR(500) NOT NULL COMMENT '评论内容',
  rating INT NOT NULL DEFAULT 5 COMMENT '评分(1-5)',
  deleted INT DEFAULT 0 COMMENT '是否删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (review_id),
  KEY idx_food_id (food_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品评论表';
