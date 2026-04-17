-- ============================================================
-- 临期食品种子数据：42 条临期食品（覆盖全部 6 个分类）
-- 推荐系统配套数据：标签、标签关联、用户行为记录
-- 图片策略：Unsplash 真实食物摄影（免费商用授权）
-- ============================================================

-- ==================== 1. 标签数据 ====================
INSERT IGNORE INTO food_tag (tag_id, tag_name, tag_type, description, status, deleted) VALUES
(1, '甜', 'taste', '甜味', 1, 0),
(2, '酸', 'taste', '酸味', 1, 0),
(3, '咸', 'taste', '咸味', 1, 0),
(4, '辣', 'taste', '辣味', 1, 0),
(5, '鲜', 'taste', '鲜味', 1, 0),
(6, '清淡', 'taste', '清淡口味', 1, 0),
(7, '奶香', 'taste', '奶香味', 1, 0),
(8, '酥脆', 'taste', '酥脆口感', 1, 0),
(9, '高蛋白', 'nutrition', '富含蛋白质', 1, 0),
(10, '高纤维', 'nutrition', '富含膳食纤维', 1, 0),
(11, '低脂', 'nutrition', '低脂肪', 1, 0),
(12, '低糖', 'nutrition', '低糖/无糖', 1, 0),
(13, '高钙', 'nutrition', '富含钙质', 1, 0),
(14, '富含维生素C', 'nutrition', '富含维生素C', 1, 0),
(15, '富含铁', 'nutrition', '富含铁元素', 1, 0),
(16, '富含花青素', 'nutrition', '富含花青素', 1, 0),
(17, '益生菌', 'nutrition', '含活性益生菌', 1, 0),
(18, '不饱和脂肪酸', 'nutrition', '富含不饱和脂肪酸', 1, 0),
(19, '健身', 'crowd', '适合健身人群', 1, 0),
(20, '减脂', 'crowd', '适合减脂人群', 1, 0),
(21, '儿童', 'crowd', '适合儿童', 1, 0),
(22, '老人', 'crowd', '适合老年人', 1, 0),
(23, '孕妇', 'crowd', '适合孕妇', 1, 0),
(24, '上班族', 'crowd', '适合上班族', 1, 0),
(25, '学生', 'crowd', '适合学生', 1, 0),
(26, '三高人群', 'crowd', '适合三高人群', 1, 0),
(27, '减肥', 'crowd', '适合减肥人群', 1, 0),
(28, '熬夜', 'crowd', '适合熬夜人群', 1, 0);

-- ==================== 2. 临期食品数据（42 条） ====================
-- 分类：1-新鲜果蔬, 2-肉禽蛋奶, 3-休闲零食, 4-粮油调味, 5-速食冷冻, 6-酒水饮料

DELETE FROM food WHERE food_id > 0;

INSERT INTO food (food_id, merchant_id, category_id, food_name, origin_price, discount_price, stock_qty, expire_date, nutrition_desc, suitable_people, allergen_info, taste_desc, cover_img, description, status, deleted, create_time, update_time) VALUES

-- ===== 新鲜果蔬 (category_id=1)：8 条 =====
(1, 1, 1, '有机小番茄 500g', 12.8, 6.4, 80, DATE_ADD(NOW(), INTERVAL 3 DAY), '富含番茄红素、维生素C，高纤维低热量', '减肥人群、上班族', '无', '酸甜多汁，皮薄肉嫩', '/images/foods/1_有机小番茄500g.jpg', '来自云南有机种植基地，自然成熟，口感酸甜，番茄红素含量高，可生吃可熟食。', 1, 0, NOW(), NOW()),
(2, 1, 1, '新鲜西蓝花 约400g', 9.9, 4.95, 60, DATE_ADD(NOW(), INTERVAL 5 DAY), '高维生素C、高纤维，含萝卜硫素', '健身人群、三高人群', '无', '清脆爽口，营养丰富', '/images/foods/2_新鲜西蓝花约400g.jpg', '西蓝花富含维生素K、C和叶酸，萝卜硫素具有抗氧化作用，清蒸或清炒均可。', 1, 0, NOW(), NOW()),
(3, 2, 1, '云南高原蓝莓 125g*2盒', 35.6, 17.8, 40, DATE_ADD(NOW(), INTERVAL 2 DAY), '富含花青素、维生素E，保护视力', '学生、上班族、爱美人士', '无', '果香浓郁，颗颗饱满', '/images/foods/3_云南高原蓝莓125gx2盒.jpg', '云南高原种植，花青素含量极高，每天一小盒保护眼睛健康。', 1, 0, NOW(), NOW()),
(4, 2, 1, '应季青苹果 4个装', 18, 9, 50, DATE_ADD(NOW(), INTERVAL 7 DAY), '富含果胶、维生素C，促进消化', '所有人群', '无', '酸甜清脆，多汁爽口', '/images/foods/4_应季青苹果4个装.jpg', '当季新鲜青苹果，果肉紧实，酸甜可口，富含膳食纤维。', 1, 0, NOW(), NOW()),
(5, 3, 1, '农家土鸡蛋 10枚装', 22, 11, 70, DATE_ADD(NOW(), INTERVAL 8 DAY), '高蛋白、富含卵磷脂和DHA', '孕妇、儿童、老人', '鸡蛋过敏者禁食', '蛋黄橙红，蛋香浓郁', '/images/foods/5_农家土鸡蛋10枚装.jpg', '农家放养土鸡所产，蛋黄橙红饱满，蛋香浓郁，营养价值远高于普通鸡蛋。', 1, 0, NOW(), NOW()),
(6, 1, 1, '新鲜胡萝卜 2根装', 6.5, 3.25, 90, DATE_ADD(NOW(), INTERVAL 10 DAY), '富含β-胡萝卜素，护眼养颜', '儿童、视力疲劳人群', '无', '脆甜爽口，自然清甜', '/images/foods/6_新鲜胡萝卜2根装.jpg', '当季新鲜胡萝卜，笔直饱满，颜色橙红，β-胡萝卜素含量高，口感脆甜。', 1, 0, NOW(), NOW()),
(7, 2, 1, '海南金煌芒果 2个', 28, 14, 45, DATE_ADD(NOW(), INTERVAL 3 DAY), '富含维生素A、C，膳食纤维丰富', '所有人群', '芒果过敏者禁食', '果肉金黄，香甜软糯', '/images/foods/7_海南金煌芒果2个.jpg', '海南金煌芒果，果肉金黄无纤维，香甜软糯，入口即化，老少皆宜。', 1, 0, NOW(), NOW()),
(8, 3, 1, '新鲜菠菜 300g', 8.8, 4.4, 65, DATE_ADD(NOW(), INTERVAL 2 DAY), '富含铁、叶酸和维生素K', '贫血人群、孕妇', '草酸结石患者慎食', '嫩绿清甜，口感柔滑', '/images/foods/8_新鲜菠菜300g.jpg', '新鲜嫩菠菜，叶片翠绿饱满，富含铁元素和叶酸，适合清炒或做汤。', 1, 0, NOW(), NOW()),

-- ===== 肉禽蛋奶 (category_id=2)：8 条 =====
(9, 1, 2, '冷鲜五花肉 300g', 32, 16, 35, DATE_ADD(NOW(), INTERVAL 3 DAY), '富含蛋白质和B族维生素', '所有人群', '无', '肥瘦相间，肉质鲜嫩', '/images/foods/9_冷鲜五花肉300g.jpg', '冷鲜五花肉，肥瘦相间三七开，肉质细腻，适合红烧、烤肉、做回锅肉。', 1, 0, NOW(), NOW()),
(10, 1, 2, '德青源鲜鸡蛋 12枚', 19.8, 9.9, 55, DATE_ADD(NOW(), INTERVAL 10 DAY), '高蛋白、DHA、硒元素', '全家适用', '鸡蛋过敏者禁食', '蛋黄饱满，蛋香浓郁', '/images/foods/10_德青源鲜鸡蛋12枚.jpg', '德青源A级鲜鸡蛋，蛋黄饱满橙红，蛋香浓郁，适合水煮、炒蛋、蒸蛋。', 1, 0, NOW(), NOW()),
(11, 2, 2, '即食鸡胸肉 100g*3袋', 42, 21, 40, DATE_ADD(NOW(), INTERVAL 6 DAY), '高蛋白低脂肪，每100g含蛋白质25g', '健身人群、减脂人群', '无', '肉质细嫩，低脂健康', '/images/foods/11_即食鸡胸肉100gx3袋.jpg', '开袋即食鸡胸肉，低脂高蛋白，口感细嫩不柴，健身减脂必备。', 1, 0, NOW(), NOW()),
(12, 2, 2, '蒙牛纯牛奶 250ml*16盒', 49.6, 24.8, 60, DATE_ADD(NOW(), INTERVAL 12 DAY), '富含钙质和优质乳蛋白', '儿童、青少年、中老年人', '乳糖不耐受者慎食', '奶香纯正，口感丝滑', '/images/foods/12_蒙牛纯牛奶250mlx16盒.jpg', '蒙牛纯牛奶，每100ml含钙120mg、蛋白质3.2g，适合早餐或睡前饮用。', 1, 0, NOW(), NOW()),
(13, 3, 2, '科尔沁牛肉馅 250g', 28, 14, 30, DATE_ADD(NOW(), INTERVAL 2 DAY), '高蛋白低脂肪，铁锌含量丰富', '所有人群', '无', '肉质细嫩，纹理清晰', '/images/foods/13_科尔沁牛肉馅250g.jpg', '科尔沁草原牛肉手工绞制，肥瘦比例适中，适合做肉丸、饺子馅、牛肉饼。', 1, 0, NOW(), NOW()),
(14, 1, 2, '光明原味酸奶 180g*4杯', 24, 12, 50, DATE_ADD(NOW(), INTERVAL 5 DAY), '富含益生菌、钙质和蛋白质', '便秘人群、减肥人群', '乳制品过敏者禁食', '酸甜适中，质地浓稠', '/images/foods/14_光明原味酸奶180gx4杯.jpg', '光明原味酸奶，添加活性益生菌，口感浓稠顺滑，0添加明胶。', 1, 0, NOW(), NOW()),
(15, 3, 2, '南京盐水鸭即食 300g', 38, 19, 25, DATE_ADD(NOW(), INTERVAL 8 DAY), '富含蛋白质，脂肪含量低', '所有人群', '无', '皮白肉嫩，咸香适中', '/images/foods/15_南京盐水鸭即食300g.jpg', '正宗南京盐水鸭，皮白肉嫩，咸香适口，开袋即食，冷热皆宜。', 1, 0, NOW(), NOW()),
(16, 2, 2, '鹌鹑蛋 20枚装', 16, 8, 45, DATE_ADD(NOW(), INTERVAL 10 DAY), '富含卵磷脂、维生素A、铁', '儿童、贫血人群', '鸡蛋过敏者禁食', '小巧圆润，蛋黄饱满', '/images/foods/16_鹌鹑蛋20枚装.jpg', '新鲜鹌鹑蛋，个头小巧精致，蛋黄比例大，营养价值高于鸡蛋，适合卤制或红烧。', 1, 0, NOW(), NOW()),

-- ===== 休闲零食 (category_id=3)：7 条 =====
(17, 1, 3, '三只松鼠夏威夷果 260g', 36, 18, 50, DATE_ADD(NOW(), INTERVAL 20 DAY), '富含不饱和脂肪酸和维生素E', '白领、零食爱好者', '坚果过敏者禁食', '奶香浓郁，果仁饱满', '/images/foods/17_三只松鼠夏威夷果260g.jpg', '精选优质坚果，奶香浓郁，颗颗饱满，是追剧休闲的最佳伴侣。', 1, 0, NOW(), NOW()),
(18, 1, 3, '奥利奥夹心饼干 388g', 19.8, 9.9, 70, DATE_ADD(NOW(), INTERVAL 25 DAY), '碳水化合物，提供能量', '儿童、零食爱好者', '小麦、乳制品过敏者禁食', '香甜酥脆，入口即化', '/images/foods/18_奥利奥夹心饼干388g.jpg', '经典夹心饼干，巧克力味浓郁，搭配牛奶风味更佳。', 1, 0, NOW(), NOW()),
(19, 2, 3, '旺旺雪饼 400g', 16, 8, 80, DATE_ADD(NOW(), INTERVAL 30 DAY), '米制品，碳水化合物来源', '儿童、上班族', '无', '米香清甜，入口即化', '/images/foods/19_旺旺雪饼400g.jpg', '经典大米膨化食品，香脆可口，是很多人童年记忆中的零食。', 1, 0, NOW(), NOW()),
(20, 2, 3, '洽洽香瓜子 308g', 15.8, 7.9, 65, DATE_ADD(NOW(), INTERVAL 28 DAY), '富含维生素E和不饱和脂肪酸', '聚会人群、影视爱好者', '无', '颗粒饱满，香气扑鼻', '/images/foods/20_洽洽香瓜子308g.jpg', '经典香瓜子，颗粒饱满，嗑起来香脆无比，是追剧聊天的必备零食。', 1, 0, NOW(), NOW()),
(21, 3, 3, '良品铺子猪肉脯 200g', 38, 19, 40, DATE_ADD(NOW(), INTERVAL 15 DAY), '高蛋白，富含B族维生素', '所有人群', '无', '肉香浓郁，越嚼越香', '/images/foods/21_良品铺子猪肉脯200g.jpg', '精选猪后腿肉，慢火烘烤，肉香浓郁，甜中带咸，越嚼越有滋味。', 1, 0, NOW(), NOW()),
(22, 1, 3, '上好佳虾条 60g*6包', 18, 9, 75, DATE_ADD(NOW(), INTERVAL 22 DAY), '米制品，碳水化合物', '儿童、学生', '虾制品过敏者慎食', '鲜香酥脆，虾味浓郁', '/images/foods/22_上好佳虾条60gx6包.jpg', '经典虾条，酥脆可口，虾味浓郁，是深受孩子喜爱的休闲零食。', 1, 0, NOW(), NOW()),
(23, 3, 3, '德芙巧克力 252g', 42, 21, 35, DATE_ADD(NOW(), INTERVAL 18 DAY), '富含可可多酚和能量', '甜食爱好者', '可可过敏者慎食', '丝滑香浓，入口即化', '/images/foods/23_德芙巧克力252g.jpg', '经典牛奶巧克力，丝滑香浓，入口即化，送给心爱的人最甜蜜的选择。', 1, 0, NOW(), NOW()),

-- ===== 粮油调味 (category_id=4)：6 条 =====
(24, 1, 4, '金龙鱼一级大豆油 5L', 65, 32.5, 30, DATE_ADD(NOW(), INTERVAL 60 DAY), '富含维生素E，不含胆固醇', '所有人群', '大豆过敏者慎食', '色泽金黄，油质清亮', '/images/foods/24_金龙鱼一级大豆油5L.jpg', '精选优质大豆压榨，油质清亮，适合煎炒烹炸各种烹饪方式。', 1, 0, NOW(), NOW()),
(25, 1, 4, '海天金标生抽 500ml', 12.8, 6.4, 60, DATE_ADD(NOW(), INTERVAL 45 DAY), '含氨基酸态氮，天然发酵', '烹饪人群', '大豆过敏者慎食', '色泽红亮，鲜香浓郁', '/images/foods/25_海天金标生抽500ml.jpg', '天然晒制，氨基酸态氮含量高，色泽红亮，适合凉拌和炒菜提鲜。', 1, 0, NOW(), NOW()),
(26, 2, 4, '五常大米 5kg', 68, 34, 40, DATE_ADD(NOW(), INTERVAL 90 DAY), '富含碳水化合物和B族维生素', '所有人群', '无', '米粒晶莹，软糯香甜', '/images/foods/26_五常大米5kg.jpg', '正宗黑龙江五常大米，米粒细长晶莹，蒸熟后软糯香甜，饭香浓郁，隔夜不回生。', 1, 0, NOW(), NOW()),
(27, 2, 4, '李锦记蚝油 510g', 9.9, 4.95, 55, DATE_ADD(NOW(), INTERVAL 50 DAY), '富含氨基酸和锌元素', '烹饪人群', '无', '色泽红褐，鲜香爽滑', '/images/foods/27_李锦记蚝油510g.jpg', '选用优质鲜蚝熬制，色泽红褐，挂汁均匀，是炒菜提鲜的法宝。', 1, 0, NOW(), NOW()),
(28, 3, 4, '鲁花花生油 5S压榨 5L', 158, 79, 20, DATE_ADD(NOW(), INTERVAL 80 DAY), '富含油酸和维生素E', '注重健康人群', '花生过敏者禁食', '金黄透亮，花生香气浓郁', '/images/foods/28_鲁花花生油5S压榨5L.jpg', '采用5S纯物理压榨工艺，油质金黄透亮，花生香气浓郁持久。', 1, 0, NOW(), NOW()),
(29, 1, 4, '千禾零添加酱油 500ml', 18.8, 9.4, 45, DATE_ADD(NOW(), INTERVAL 55 DAY), '零添加，富含氨基酸', '注重健康人群', '无', '豆香浓郁，口感醇厚', '/images/foods/29_千禾零添加酱油500ml.jpg', '不添加味精、色素、防腐剂，天然酿造，豆香浓郁，醇厚回甘。', 1, 0, NOW(), NOW()),

-- ===== 速食冷冻 (category_id=5)：7 条 =====
(30, 1, 5, '三全水饺 猪肉白菜 1kg', 25.8, 12.9, 45, DATE_ADD(NOW(), INTERVAL 10 DAY), '富含蛋白质和碳水化合物', '快节奏人群', '小麦、猪肉过敏者禁食', '皮薄馅大，汤汁饱满', '/images/foods/30_三全水饺猪肉白菜1kg.jpg', '精选猪肉白菜馅，皮薄馅大，汤汁饱满，只需水煮8分钟即可享用。', 1, 0, NOW(), NOW()),
(31, 1, 5, '湾仔码头手工水饺 540g', 32, 16, 35, DATE_ADD(NOW(), INTERVAL 8 DAY), '高蛋白，手工包制', '快节奏人群', '小麦、猪肉过敏者禁食', '皮薄馅大，爽滑多汁', '/images/foods/31_湾仔码头手工水饺540g.jpg', '精选手工包制，皮薄馅大，咬一口汤汁四溢，方便又美味。', 1, 0, NOW(), NOW()),
(32, 2, 5, '海底捞自热火锅 麻辣牛肉 400g', 39.8, 19.9, 40, DATE_ADD(NOW(), INTERVAL 15 DAY), '提供蛋白质、膳食纤维和热量', '户外人群、懒人', '对辣味敏感者慎食', '麻辣鲜香，内容丰富', '/images/foods/32_海底捞自热火锅麻辣牛肉400g.jpg', '无需火电，15分钟即享麻辣鲜香，牛肉软糯，蔬菜脆爽，随时随地吃火锅。', 1, 0, NOW(), NOW()),
(33, 2, 5, '思念猪肉灌汤水饺 450g', 18.8, 9.4, 50, DATE_ADD(NOW(), INTERVAL 7 DAY), '富含蛋白质，汤汁充盈', '快节奏人群', '小麦、猪肉过敏者禁食', '皮薄馅多，汤汁鲜美', '/images/foods/33_思念猪肉灌汤水饺450g.jpg', '皮薄如纸，馅大饱满，咬开有满满汤汁，蒸煮煎炸皆可。', 1, 0, NOW(), NOW()),
(34, 1, 5, '康师傅红烧牛肉面 127g*5包', 29.5, 14.75, 65, DATE_ADD(NOW(), INTERVAL 20 DAY), '碳水化合物主要来源，速食方便', '学生、上班族', '小麦过敏者禁食', '汤鲜面劲，麻辣够味', '/images/foods/34_康师傅红烧牛肉面127gx5包.jpg', '精选优质小麦，口感劲道有弹性，红烧汤底鲜香浓郁，3分钟即食。', 1, 0, NOW(), NOW()),
(35, 3, 5, '好侍咖喱块 98g*2盒', 26, 13, 35, DATE_ADD(NOW(), INTERVAL 30 DAY), '富含香辛料，促进食欲', '家庭主妇、烹饪爱好者', '无', '咖喱香浓，口感醇厚', '/images/foods/35_好侍咖喱块98gx2盒.jpg', '好侍百梦多咖喱块，无需调味，切块加入即可，咖喱香浓，口感醇厚，老少皆宜。', 1, 0, NOW(), NOW()),
(36, 1, 5, '桂格即食燕麦片 800g', 32, 16, 40, DATE_ADD(NOW(), INTERVAL 25 DAY), '富含膳食纤维和β-葡聚糖', '减肥人群、三高人群', '无', '软糯可口，麦香自然', '/images/foods/36_桂格即食燕麦片800g.jpg', '无需煮沸，热水冲泡即可，富含膳食纤维，是健康的早餐选择。', 1, 0, NOW(), NOW()),

-- ===== 酒水饮料 (category_id=6)：6 条 =====
(37, 1, 6, '农夫山泉矿泉水 550ml*24瓶', 36, 18, 100, DATE_ADD(NOW(), INTERVAL 180 DAY), '天然弱碱性水，含矿物质', '所有人群', '无', '清冽甘甜，口感柔和', '/images/foods/37_农夫山泉矿泉水550mlx24瓶.jpg', '取自千岛湖深层水源，天然弱碱性，富含钾、钙、钠、镁等矿物质。', 1, 0, NOW(), NOW()),
(38, 2, 6, '汇源100%橙汁 1L*2盒', 29.8, 14.9, 50, DATE_ADD(NOW(), INTERVAL 8 DAY), '富含维生素C和天然果糖', '儿童、上班族', '无', '酸甜可口，橙香浓郁', '/images/foods/38_汇源100pct橙汁1Lx2盒.jpg', '100%纯橙汁，不添加糖和防腐剂，鲜榨口感，每100ml含维生素C约30mg。', 1, 0, NOW(), NOW()),
(39, 2, 6, '可口可乐 330ml*12罐', 39.6, 19.8, 80, DATE_ADD(NOW(), INTERVAL 60 DAY), '碳水化合物，提供能量', '年轻人、聚会人群', '无', '气泡丰富，甜爽刺激', '/images/foods/39_可口可乐330mlx12罐.jpg', '经典可口可乐，气泡丰富，甜爽刺激，冰镇后口感更佳，是聚会派对的必备饮品。', 1, 0, NOW(), NOW()),
(40, 3, 6, '王老吉凉茶 310ml*6罐', 24, 12, 60, DATE_ADD(NOW(), INTERVAL 45 DAY), '含草本植物提取物', '上火人群、熬夜人群', '无', '甘甜清凉，草本清香', '/images/foods/40_王老吉凉茶310mlx6罐.jpg', '传承百年配方，甘甜清凉，怕上火喝王老吉，适合夏季和熬夜人群。', 1, 0, NOW(), NOW()),
(41, 1, 6, '维他柠檬茶 250ml*6盒', 21, 10.5, 55, DATE_ADD(NOW(), INTERVAL 30 DAY), '含维生素C和茶多酚', '年轻人、上班族', '无', '柠檬清香，茶味浓郁', '/images/foods/41_维他柠檬茶250mlx6盒.jpg', '真茶+真柠檬，清爽不涩口，是下午茶和解腻的最佳选择。', 1, 0, NOW(), NOW()),
(42, 3, 6, '青岛啤酒 500ml*12罐', 72, 36, 40, DATE_ADD(NOW(), INTERVAL 20 DAY), '含麦芽糖和少量蛋白质', '成年人、聚会人群', '麸质过敏者禁食', '麦香浓郁，清爽杀口', '/images/foods/42_青岛啤酒500mlx12罐.jpg', '青岛啤酒经典款，麦芽香气浓郁，泡沫细腻持久，口感清爽杀口，是朋友聚会的必备。', 1, 0, NOW(), NOW());

-- ==================== 3. 标签关联数据 ====================
DELETE FROM food_tag_relation WHERE food_id > 0;

INSERT INTO food_tag_relation (food_id, tag_id) VALUES
-- 新鲜果蔬
(1, 2), (1, 1), (1, 14), (1, 27), (1, 24),
(2, 14), (2, 10), (2, 19), (2, 26),
(3, 16), (3, 14), (3, 25), (3, 24),
(4, 2), (4, 14), (4, 10),
(5, 9), (5, 23), (5, 21), (5, 22),
(6, 14), (6, 21),
(7, 1), (7, 14),
(8, 15), (8, 23), (8, 10),

-- 肉禽蛋奶
(9, 9),
(10, 9), (10, 21), (10, 22),
(11, 9), (11, 11), (11, 19), (11, 20),
(12, 13), (12, 9), (12, 21), (12, 22),
(13, 9), (13, 15), (13, 11),
(14, 17), (14, 13), (14, 20), (14, 27),
(15, 9), (15, 11), (15, 3),
(16, 15), (16, 9), (16, 21),

-- 休闲零食
(17, 18), (17, 7), (17, 24),
(18, 1), (18, 8), (18, 21),
(19, 1), (19, 8), (19, 24),
(20, 18), (20, 8),
(21, 9), (21, 5),
(22, 8), (22, 25), (22, 21),
(23, 1), (23, 7),

-- 粮油调味
(24, 14),
(25, 5), (25, 3),
(26, 6),
(27, 5), (27, 3),
(28, 14), (28, 18),
(29, 6), (29, 5),

-- 速食冷冻
(30, 9), (30, 24),
(31, 9), (31, 24),
(32, 4), (32, 9),
(33, 9), (33, 24),
(34, 4), (34, 25), (34, 24),
(35, 6),
(36, 10), (36, 20), (36, 27), (36, 26),

-- 酒水饮料
(37, 6),
(38, 14), (38, 1), (38, 21),
(39, 1), (39, 24),
(40, 6), (40, 28),
(41, 14), (41, 6), (41, 24),
(42, 3), (42, 24);

-- ==================== 4. 用户行为记录（推荐系统用） ====================
-- 模拟多个用户对不同食品的行为，让协同过滤有交叉数据

DELETE FROM user_behavior WHERE behavior_id > 0;

-- testuser (user_id=2)：健康饮食爱好者，喜欢果蔬、健身食品
INSERT INTO user_behavior (user_id, target_type, target_id, behavior_type, weight_score, create_time) VALUES
(2, 'food', 1, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'food', 1, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 'food', 2, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 'food', 2, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 'food', 3, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'food', 3, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 'food', 6, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 'food', 8, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'food', 8, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'food', 11, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 'food', 11, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 'food', 14, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, 'food', 14, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, 'food', 36, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, 'food', 36, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, 'food', 38, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(2, 'food', 38, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(2, 'food', 17, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 'food', 26, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(2, 'food', 26, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 11 DAY));

-- 用户A（user_id=100）：零食爱好者，喜欢甜食、坚果
INSERT INTO user_behavior (user_id, target_type, target_id, behavior_type, weight_score, create_time) VALUES
(100, 'food', 17, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(100, 'food', 17, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(100, 'food', 17, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(100, 'food', 18, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(100, 'food', 18, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(100, 'food', 19, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(100, 'food', 19, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(100, 'food', 20, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(100, 'food', 20, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(100, 'food', 21, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(100, 'food', 21, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(100, 'food', 22, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(100, 'food', 23, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(100, 'food', 23, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(100, 'food', 39, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(100, 'food', 39, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(100, 'food', 7, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(100, 'food', 1, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- 用户B（user_id=101）：家庭用户，喜欢粮油调味、速食
INSERT INTO user_behavior (user_id, target_type, target_id, behavior_type, weight_score, create_time) VALUES
(101, 'food', 24, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(101, 'food', 24, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(101, 'food', 25, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(101, 'food', 25, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(101, 'food', 26, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(101, 'food', 26, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(101, 'food', 27, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(101, 'food', 28, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(101, 'food', 28, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(101, 'food', 29, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(101, 'food', 29, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(101, 'food', 30, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(101, 'food', 30, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(101, 'food', 31, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(101, 'food', 33, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(101, 'food', 34, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(101, 'food', 34, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(101, 'food', 35, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(101, 'food', 5, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(101, 'food', 5, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 12 DAY));

-- 用户C（user_id=102）：健身达人，高蛋白低脂食品
INSERT INTO user_behavior (user_id, target_type, target_id, behavior_type, weight_score, create_time) VALUES
(102, 'food', 11, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(102, 'food', 11, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(102, 'food', 11, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(102, 'food', 2, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(102, 'food', 2, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(102, 'food', 14, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(102, 'food', 14, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(102, 'food', 15, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(102, 'food', 15, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(102, 'food', 36, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(102, 'food', 36, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(102, 'food', 36, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(102, 'food', 13, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(102, 'food', 13, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(102, 'food', 1, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(102, 'food', 1, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(102, 'food', 8, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(102, 'food', 37, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(102, 'food', 37, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 9 DAY));

-- 用户D（user_id=103）：学生党，喜欢速食、饮料、零食
INSERT INTO user_behavior (user_id, target_type, target_id, behavior_type, weight_score, create_time) VALUES
(103, 'food', 34, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(103, 'food', 34, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(103, 'food', 34, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(103, 'food', 32, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(103, 'food', 32, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(103, 'food', 39, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(103, 'food', 39, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(103, 'food', 39, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(103, 'food', 40, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(103, 'food', 40, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(103, 'food', 41, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(103, 'food', 41, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(103, 'food', 18, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(103, 'food', 18, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(103, 'food', 22, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(103, 'food', 22, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(103, 'food', 30, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(103, 'food', 30, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(103, 'food', 33, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(103, 'food', 23, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(103, 'food', 23, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 10 DAY));

-- 用户E（user_id=104）：宝妈，喜欢儿童食品、营养品
INSERT INTO user_behavior (user_id, target_type, target_id, behavior_type, weight_score, create_time) VALUES
(104, 'food', 5, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(104, 'food', 5, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(104, 'food', 10, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(104, 'food', 10, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(104, 'food', 10, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(104, 'food', 12, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(104, 'food', 12, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(104, 'food', 12, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(104, 'food', 16, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(104, 'food', 16, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(104, 'food', 6, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(104, 'food', 6, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(104, 'food', 3, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(104, 'food', 3, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(104, 'food', 38, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(104, 'food', 38, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(104, 'food', 22, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(104, 'food', 22, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(104, 'food', 31, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(104, 'food', 31, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 9 DAY));

-- 用户F（user_id=105）：上班族，喜欢便捷、健康食品
INSERT INTO user_behavior (user_id, target_type, target_id, behavior_type, weight_score, create_time) VALUES
(105, 'food', 1, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(105, 'food', 1, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(105, 'food', 3, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(105, 'food', 3, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(105, 'food', 11, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(105, 'food', 11, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(105, 'food', 14, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(105, 'food', 14, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(105, 'food', 19, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(105, 'food', 19, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(105, 'food', 34, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(105, 'food', 34, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(105, 'food', 36, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(105, 'food', 36, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(105, 'food', 41, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(105, 'food', 41, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(105, 'food', 38, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(105, 'food', 38, 'favorite', 4.0, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(105, 'food', 26, 'view', 1.0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(105, 'food', 26, 'purchase', 5.0, DATE_SUB(NOW(), INTERVAL 10 DAY));
