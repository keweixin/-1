-- 帖子推荐字段
ALTER TABLE community_post ADD COLUMN IF NOT EXISTS recommended INT DEFAULT 0 COMMENT '是否推荐 0-否 1-是';
