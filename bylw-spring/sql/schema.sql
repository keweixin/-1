-- ============================================================
-- 城市临期食品分发与膳食个性化推荐系统 - 数据库初始化脚本
-- 数据库名: bylw
-- ============================================================

CREATE DATABASE IF NOT EXISTS bylw DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bylw;

-- ============================================================
-- 1. 用户表
-- ============================================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    user_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '用户编号',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(MD5)',
    nickname VARCHAR(50) COMMENT '昵称',
    gender VARCHAR(10) COMMENT '性别',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(50) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像',
    address VARCHAR(255) COMMENT '地址',
    role VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色: admin / user',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删, 1-已删',
    create_time DATETIME COMMENT '注册时间',
    update_time DATETIME COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 饮食档案表
-- ============================================================
DROP TABLE IF EXISTS food_profile;
CREATE TABLE food_profile (
    profile_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '档案编号',
    user_id INT(11) NOT NULL COMMENT '用户编号',
    taste_preference VARCHAR(200) COMMENT '口味偏好',
    allergen_info VARCHAR(255) COMMENT '过敏原信息',
    chronic_disease VARCHAR(255) COMMENT '慢病史',
    taboo_info VARCHAR(255) COMMENT '忌口信息',
    remark VARCHAR(255) COMMENT '备注',
    update_time DATETIME COMMENT '更新时间',
    UNIQUE INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食档案表';

-- ============================================================
-- 3. 商超门店表
-- ============================================================
DROP TABLE IF EXISTS food_merchant;
CREATE TABLE food_merchant (
    merchant_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '门店编号',
    merchant_name VARCHAR(100) NOT NULL COMMENT '门店名称',
    contact_name VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    license_no VARCHAR(100) COMMENT '资质编号',
    address VARCHAR(255) COMMENT '地址',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商超门店表';

-- ============================================================
-- 4. 食品分类表
-- ============================================================
DROP TABLE IF EXISTS food_category;
CREATE TABLE food_category (
    category_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '分类编号',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述',
    sort_no INT(11) COMMENT '排序号',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品分类表';

-- ============================================================
-- 5. 食品标签表
-- ============================================================
DROP TABLE IF EXISTS food_tag;
CREATE TABLE food_tag (
    tag_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '标签编号',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tag_type VARCHAR(50) COMMENT '标签类型: nutrition/taste/crowd',
    description VARCHAR(200) COMMENT '描述',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品标签表';

-- ============================================================
-- 5b. 食品标签关联表
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

-- ============================================================
-- 6. 临期食品表
-- ============================================================
DROP TABLE IF EXISTS food;
CREATE TABLE food (
    food_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '食品编号',
    merchant_id INT(11) COMMENT '门店编号',
    category_id INT(11) COMMENT '分类编号',
    food_name VARCHAR(100) NOT NULL COMMENT '食品名称',
    origin_price DECIMAL(10,2) COMMENT '原价',
    discount_price DECIMAL(10,2) COMMENT '折扣价',
    stock_qty INT(11) DEFAULT 0 COMMENT '库存数量',
    expire_date DATETIME COMMENT '保质截止日期',
    nutrition_desc VARCHAR(500) COMMENT '营养说明',
    suitable_people VARCHAR(255) COMMENT '适宜人群',
    allergen_info VARCHAR(255) COMMENT '过敏原信息',
    taste_desc VARCHAR(255) COMMENT '味道特征: 酸/甜/苦/辣/咸/鲜',
    cover_img VARCHAR(255) COMMENT '封面图片',
    description VARCHAR(500) COMMENT '商品描述',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    INDEX idx_category (category_id),
    INDEX idx_merchant (merchant_id),
    INDEX idx_status (status),
    INDEX idx_expire (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='临期食品表';

-- ============================================================
-- 7. 订单表
-- ============================================================
DROP TABLE IF EXISTS food_order;
CREATE TABLE food_order (
    order_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '订单编号',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    user_id INT(11) NOT NULL COMMENT '用户编号',
    total_amount DECIMAL(10,2) COMMENT '订单金额',
    order_status VARCHAR(20) NOT NULL COMMENT '订单状态',
    pay_status VARCHAR(20) NOT NULL COMMENT '支付状态',
    receiver_name VARCHAR(50) COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) COMMENT '联系电话',
    receiver_address VARCHAR(255) COMMENT '收货地址',
    remark VARCHAR(255) COMMENT '备注',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME COMMENT '创建时间',
    pay_time DATETIME COMMENT '支付时间',
    INDEX idx_user (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================================
-- 8. 订单明细表
-- ============================================================
DROP TABLE IF EXISTS food_order_item;
CREATE TABLE food_order_item (
    item_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '明细编号',
    order_id INT(11) NOT NULL COMMENT '订单编号',
    food_id INT(11) NOT NULL COMMENT '食品编号',
    food_name VARCHAR(100) COMMENT '食品名称',
    price DECIMAL(10,2) COMMENT '单价',
    quantity INT(11) COMMENT '数量',
    subtotal DECIMAL(10,2) COMMENT '小计金额',
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ============================================================
-- 9. 配送表
-- ============================================================
DROP TABLE IF EXISTS food_delivery;
CREATE TABLE food_delivery (
    delivery_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '配送编号',
    order_id INT(11) NOT NULL COMMENT '订单编号',
    courier_name VARCHAR(50) COMMENT '配送员姓名',
    courier_phone VARCHAR(20) COMMENT '配送员电话',
    delivery_status VARCHAR(20) NOT NULL COMMENT '配送状态',
    dispatch_time DATETIME COMMENT '派单时间',
    finish_time DATETIME COMMENT '完成时间',
    remark VARCHAR(255) COMMENT '备注',
    UNIQUE INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送表';

-- ============================================================
-- 10. 售后申诉表
-- ============================================================
DROP TABLE IF EXISTS food_after_sale;
CREATE TABLE food_after_sale (
    after_sale_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '申诉编号',
    order_id INT(11) NOT NULL COMMENT '订单编号',
    user_id INT(11) NOT NULL COMMENT '用户编号',
    reason_type VARCHAR(50) COMMENT '申诉类型',
    reason_desc VARCHAR(500) COMMENT '申诉原因',
    evidence_img VARCHAR(255) COMMENT '凭证图片',
    handle_status VARCHAR(20) NOT NULL COMMENT '处理状态',
    handle_result VARCHAR(255) COMMENT '处理结果',
    apply_time DATETIME COMMENT '申请时间',
    handle_time DATETIME COMMENT '处理时间',
    INDEX idx_order (order_id),
    INDEX idx_user (user_id),
    INDEX idx_status (handle_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后申诉表';

-- ============================================================
-- 11. 膳食百科表
-- ============================================================
DROP TABLE IF EXISTS food_article;
CREATE TABLE food_article (
    article_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '文章编号',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    summary VARCHAR(300) COMMENT '摘要',
    content TEXT COMMENT '正文内容',
    cover_img VARCHAR(255) COMMENT '封面图',
    publisher_id INT(11) COMMENT '发布者编号',
    read_count INT(11) DEFAULT 0 COMMENT '浏览量',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    publish_time DATETIME COMMENT '发布时间',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='膳食百科表';

-- ============================================================
-- 12. 健康食谱表
-- ============================================================
DROP TABLE IF EXISTS food_recipe;
CREATE TABLE food_recipe (
    recipe_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '食谱编号',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    summary VARCHAR(300) COMMENT '摘要',
    content TEXT COMMENT '内容',
    suitable_people VARCHAR(255) COMMENT '适宜人群',
    cover_img VARCHAR(255) COMMENT '封面图',
    publisher_id INT(11) COMMENT '发布者编号',
    collect_count INT(11) DEFAULT 0 COMMENT '收藏量',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    publish_time DATETIME COMMENT '发布时间',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康食谱表';

-- ============================================================
-- 13. 食谱收藏表
-- ============================================================
DROP TABLE IF EXISTS recipe_favorite;
CREATE TABLE recipe_favorite (
    favorite_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '收藏编号',
    user_id INT(11) NOT NULL COMMENT '用户编号',
    recipe_id INT(11) NOT NULL COMMENT '食谱编号',
    create_time DATETIME COMMENT '收藏时间',
    UNIQUE INDEX idx_user_recipe (user_id, recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食谱收藏表';

-- ============================================================
-- 14. 社区帖子表
-- ============================================================
DROP TABLE IF EXISTS community_post;
CREATE TABLE community_post (
    post_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '帖子编号',
    user_id INT(11) NOT NULL COMMENT '用户编号',
    title VARCHAR(100) COMMENT '标题',
    content TEXT COMMENT '正文',
    img_list VARCHAR(500) COMMENT '图片列表(JSON)',
    like_count INT(11) DEFAULT 0 COMMENT '点赞数',
    comment_count INT(11) DEFAULT 0 COMMENT '评论数',
    audit_status VARCHAR(20) NOT NULL DEFAULT '待审核' COMMENT '审核状态',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME COMMENT '发布时间',
    INDEX idx_user (user_id),
    INDEX idx_audit (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

-- ============================================================
-- 15. 社区评论表
-- ============================================================
DROP TABLE IF EXISTS community_comment;
CREATE TABLE community_comment (
    comment_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '评论编号',
    post_id INT(11) NOT NULL COMMENT '帖子编号',
    user_id INT(11) NOT NULL COMMENT '用户编号',
    content VARCHAR(500) COMMENT '评论内容',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME COMMENT '发布时间',
    INDEX idx_post (post_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区评论表';

-- ============================================================
-- 16. 积分记录表
-- ============================================================
DROP TABLE IF EXISTS points_record;
CREATE TABLE points_record (
    record_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '记录编号',
    user_id INT(11) NOT NULL COMMENT '用户编号',
    change_type VARCHAR(20) NOT NULL COMMENT '变动类型: earn/spend',
    change_value INT(11) NOT NULL COMMENT '变动值',
    source_type VARCHAR(50) COMMENT '来源类型',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_type (change_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- ============================================================
-- 17. 积分商品表
-- ============================================================
DROP TABLE IF EXISTS points_goods;
CREATE TABLE points_goods (
    goods_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '商品编号',
    goods_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    point_cost INT(11) NOT NULL COMMENT '所需积分',
    stock_qty INT(11) DEFAULT 0 COMMENT '库存数量',
    cover_img VARCHAR(255) COMMENT '封面图',
    description VARCHAR(500) COMMENT '商品描述',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商品表';

-- ============================================================
-- 18. 积分兑换表
-- ============================================================
DROP TABLE IF EXISTS points_exchange;
CREATE TABLE points_exchange (
    exchange_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '兑换编号',
    goods_id INT(11) NOT NULL COMMENT '商品编号',
    user_id INT(11) NOT NULL COMMENT '用户编号',
    exchange_status VARCHAR(20) NOT NULL COMMENT '兑换状态',
    create_time DATETIME COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_goods (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换表';

-- ============================================================
-- 19. 公告表
-- ============================================================
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
    notice_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '公告编号',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    publisher_id INT(11) COMMENT '发布者编号',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    publish_time DATETIME COMMENT '发布时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ============================================================
-- 20. 轮播图表
-- ============================================================
DROP TABLE IF EXISTS sys_banner;
CREATE TABLE sys_banner (
    banner_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '轮播图编号',
    title VARCHAR(100) COMMENT '标题',
    img_url VARCHAR(255) NOT NULL COMMENT '图片地址',
    link_url VARCHAR(255) COMMENT '跳转地址',
    sort_no INT(11) DEFAULT 0 COMMENT '排序号',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- ============================================================
-- 21. 友情链接表
-- ============================================================
DROP TABLE IF EXISTS sys_friend_link;
CREATE TABLE sys_friend_link (
    link_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '链接编号',
    link_name VARCHAR(100) NOT NULL COMMENT '名称',
    link_url VARCHAR(255) NOT NULL COMMENT '链接地址',
    sort_no INT(11) DEFAULT 0 COMMENT '排序号',
    status INT(1) NOT NULL DEFAULT 1 COMMENT '状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='友情链接表';

-- ============================================================
-- 22. 关于我们表
-- ============================================================
DROP TABLE IF EXISTS sys_about_us;
CREATE TABLE sys_about_us (
    about_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '编号',
    platform_name VARCHAR(100) COMMENT '平台名称',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(50) COMMENT '邮箱',
    address VARCHAR(255) COMMENT '地址',
    intro_text VARCHAR(500) COMMENT '简介',
    cover_img VARCHAR(255) COMMENT '图片',
    update_time DATETIME COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关于我们表';

-- ============================================================
-- 23. 用户行为记录表
-- ============================================================
DROP TABLE IF EXISTS user_behavior;
CREATE TABLE user_behavior (
    behavior_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '行为编号',
    user_id INT(11) NOT NULL COMMENT '用户编号',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型: food/article/recipe',
    target_id INT(11) NOT NULL COMMENT '目标编号',
    behavior_type VARCHAR(20) NOT NULL COMMENT '行为类型: view/favorite/like/purchase/comment',
    weight_score DECIMAL(8,2) COMMENT '行为权重',
    create_time DATETIME COMMENT '行为时间',
    INDEX idx_user (user_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_type (behavior_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为记录表';

-- ============================================================
-- 24. 推荐结果表
-- ============================================================
DROP TABLE IF EXISTS recommendation;
CREATE TABLE recommendation (
    rec_id INT(11) AUTO_INCREMENT PRIMARY KEY COMMENT '推荐编号',
    user_id INT(11) NOT NULL COMMENT '用户编号',
    target_type VARCHAR(20) NOT NULL COMMENT '推荐目标类型',
    target_id INT(11) NOT NULL COMMENT '推荐目标编号',
    score_value DECIMAL(8,4) COMMENT '推荐得分',
    rec_reason VARCHAR(255) COMMENT '推荐原因',
    deleted INT(1) NOT NULL DEFAULT 0 COMMENT '删除标记',
    create_time DATETIME COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐结果表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 插入管理员账户 (密码: admin123 -> MD5)
INSERT INTO sys_user (username, password, nickname, role, create_time, deleted) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'admin', NOW(), 0);

-- 插入普通用户账户 (密码: user123 -> MD5)
INSERT INTO sys_user (username, password, nickname, phone, role, create_time, deleted) VALUES
('testuser', '49ba59abbe56e057f20f883e', '测试用户', '13800138000', 'user', NOW(), 0);

-- 插入食品分类
INSERT INTO food_category (category_name, description, sort_no, status, deleted) VALUES
('新鲜果蔬', '新鲜蔬菜和水果', 1, 1, 0),
('肉禽蛋奶', '肉类、禽类、蛋类、奶制品', 2, 1, 0),
('休闲零食', '零食小吃', 3, 1, 0),
('粮油调味', '粮油与调味品', 4, 1, 0),
('速食冷冻', '速食和冷冻食品', 5, 1, 0),
('酒水饮料', '酒类和饮料', 6, 1, 0);

-- 插入商超门店
INSERT INTO food_merchant (merchant_name, contact_name, contact_phone, address, status, create_time, deleted) VALUES
('绿色生活超市', '张经理', '13800001001', '北京市朝阳区建国路88号', 1, NOW(), 0),
('晨曦乳业门店', '李经理', '13800001002', '北京市海淀区中关村大街1号', 1, NOW(), 0),
('生鲜大本营', '王经理', '13800001003', '北京市丰台区南三环西路16号', 1, NOW(), 0),
('便利蜂', '赵经理', '13800001004', '北京市东城区王府井大街1号', 1, NOW(), 0);

-- 插入临期食品
INSERT INTO food (merchant_id, category_id, food_name, origin_price, discount_price, stock_qty, expire_date, nutrition_desc, suitable_people, allergen_info, description, cover_img, status, create_time, deleted) VALUES
(1, 1, '有机红富士苹果 1kg', 19.80, 9.90, 25, DATE_ADD(NOW(), INTERVAL 3 DAY), '热量: 52kcal/100g, 纤维素: 2.4g', '适合所有人', '无', '产自烟台，口感脆甜，富含维生素C。', '/images/placeholders/schema-food-01.jpg', 1, NOW(), 0),
(2, 2, '全脂纯牛奶 1L', 12.00, 4.50, 15, DATE_ADD(NOW(), INTERVAL 2 DAY), '蛋白质: 3.2g/100ml, 钙: 110mg/100ml', '青少年、成年人', '乳制品', '优质牧场奶源，巴氏杀菌，保留更多营养。', '/images/placeholders/schema-food-02.jpg', 1, NOW(), 0),
(3, 5, '全麦吐司面包 400g', 15.00, 6.80, 8, DATE_ADD(NOW(), INTERVAL 1 DAY), '热量: 245kcal/100g, 膳食纤维: 6.5g', '健身人群、控糖人群', '麸质', '全麦粉含量>50%，无添加蔗糖，饱腹感强。', '/images/placeholders/schema-food-03.jpg', 1, NOW(), 0),
(4, 2, '冷冻鸡胸肉 500g', 22.00, 12.00, 40, DATE_ADD(NOW(), INTERVAL 15 DAY), '蛋白质: 24g/100g, 脂肪: 1.9g', '减脂健身人群', '无', '去皮去油脂，肉质鲜嫩，适合多种烹饪方式。', '/images/placeholders/schema-food-04.jpg', 1, NOW(), 0),
(1, 1, '新鲜草莓 500g', 29.90, 15.90, 10, DATE_ADD(NOW(), INTERVAL 1 DAY), '热量: 32kcal/100g, 维生素C: 58.8mg/100g', '适合所有人', '无', '当季采摘，酸甜可口，富含维生素C。', '/images/placeholders/schema-food-05.jpg', 1, NOW(), 0),
(2, 2, '无糖燕麦酸奶 200g', 8.00, 3.50, 30, DATE_ADD(NOW(), INTERVAL 3 DAY), '蛋白质: 4.5g/100g, 碳水: 8g/100g', '减脂人群、肠胃不佳者', '乳制品', '添加真实燕麦颗粒，饱腹感强，促进肠道蠕动。', '/images/placeholders/schema-food-06.jpg', 1, NOW(), 0),
(1, 3, '混合坚果仁 250g', 39.90, 19.90, 50, DATE_ADD(NOW(), INTERVAL 30 DAY), '热量: 600kcal/100g, 脂肪: 50g/100g', '学生、脑力劳动者', '坚果', '包含核桃、腰果、巴旦木等多种坚果，营养丰富。', '/images/placeholders/schema-food-07.jpg', 1, NOW(), 0),
(4, 4, '特级初榨橄榄油 500ml', 59.90, 29.90, 20, DATE_ADD(NOW(), INTERVAL 60 DAY), '脂肪: 99g/100ml', '追求健康饮食的人群', '无', '冷压初榨，保留更多营养，适合凉拌和轻度烹饪。', '/images/placeholders/schema-food-08.jpg', 1, NOW(), 0);

-- 插入轮播图
INSERT INTO sys_banner (title, img_url, link_url, sort_no, status) VALUES
('临期食品专区', '/images/banners/banner-1.jpg', '/food-hall', 1, 1),
('健康食谱推荐', '/images/banners/banner-2.jpg', '/recipes', 2, 1),
('零浪费社区', '/images/banners/banner-3.jpg', '/community', 3, 1);

-- 插入关于我们
INSERT INTO sys_about_us (platform_name, phone, email, address, intro_text, update_time) VALUES
('城市临期食品分发与膳食个性化推荐系统', '400-888-8888', 'contact@bylw.com', '北京市海淀区中关村大街88号', '我们致力于减少食品浪费，通过智能推荐帮助用户发现临期但仍安全优质的食品，同时提供个性化的膳食建议，让每一次消费都更加环保和健康。', NOW());
