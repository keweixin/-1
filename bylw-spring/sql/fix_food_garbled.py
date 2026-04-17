"""
Fix garbled food description data in database.
174 food entries (IDs 43-216) have corrupted Chinese text in:
  nutrition_desc, taste_desc, suitable_people, allergen_info, description

This script:
1. Reads all food data from DB
2. Generates proper Chinese descriptions for corrupted entries
3. Updates the DB directly
4. Exports corrected SQL file
"""

import pymysql
import os
import re

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "root",
    "database": "bylw",
    "charset": "utf8mb4",
}

CATEGORY_TEMPLATES = {
    # 1: 新鲜果蔬
    1: {
        "allergen_default": "无",
        "suitable_default": "所有人群",
        "nutrition": [
            "富含维生素C、膳食纤维，促进消化",
            "富含维生素A、β-胡萝卜素，护眼养颜",
            "高纤维低热量，适合减肥人群",
            "富含叶酸、铁元素，适合贫血人群",
            "富含花青素、维生素E，抗氧化",
            "富含钾元素、膳食纤维，调节血压",
        ],
        "taste": [
            "清脆爽口，汁多味甜",
            "酸甜可口，果香浓郁",
            "肉质细腻，清甜多汁",
            "脆嫩爽口，自然清甜",
            "口感绵密，入口即化",
            "果香四溢，颗颗饱满",
        ],
        "desc": [
            "当季新鲜采摘，自然成熟，口感更佳。",
            "产地直发，新鲜保障，冷链配送。",
            "精选优质品种，个大饱满，新鲜直达。",
            "人工筛选，颗颗精品，坏果包赔。",
        ],
    },
    # 2: 肉禽蛋奶
    2: {
        "allergen_default": "无",
        "suitable_default": "所有人群",
        "nutrition": [
            "高蛋白、低脂肪，健身人群首选",
            "富含优质蛋白和B族维生素",
            "高蛋白、富含卵磷脂和DHA",
            "富含钙质和优质乳蛋白",
            "高蛋白低脂肪，富含虾青素",
            "富含益生菌，有助肠道健康",
        ],
        "taste": [
            "肉质鲜嫩，口感细腻",
            "肥瘦相间，味道鲜美",
            "皮脆肉嫩，咸香可口",
            "蛋香浓郁，蛋黄饱满",
            "奶香纯正，口感丝滑",
            "肉质紧实，鲜甜可口",
        ],
        "desc": [
            "精选食材，冷鲜保存，确保新鲜口感。",
            "源头直供，品质保障，营养丰富。",
            "工厂直发，新鲜速递，食品安全。",
            "严格质检，品质把控，吃得放心。",
        ],
    },
    # 3: 休闲零食
    3: {
        "allergen_default": "无",
        "suitable_default": "所有人群",
        "nutrition": [
            "富含不饱和脂肪酸，健脑益智",
            "碳水化合物，快速补充能量",
            "非油炸，富含植物蛋白",
            "富含可可多酚和能量",
            "高蛋白，富含B族维生素",
            "富含维生素E，抗氧化",
        ],
        "taste": [
            "香甜酥脆，入口即化",
            "奶香浓郁，口感酥脆",
            "咸香可口，越嚼越香",
            "果香浓郁，回味悠长",
            "香脆可口，虾味十足",
            "丝滑香浓，甜而不腻",
        ],
        "desc": [
            "精选原料，匠心制作，品质保证。",
            "独立小包装，方便携带，随时享用。",
            "品牌正品，厂家授权，品质保障。",
            "口感丰富，老少皆宜，送礼自用两相宜。",
        ],
    },
    # 4: 粮油调味
    4: {
        "allergen_default": "无",
        "suitable_default": "所有人群",
        "nutrition": [
            "富含维生素E，不含胆固醇",
            "含氨基酸态氮，天然发酵",
            "优质碳水化合物来源",
            "富含油酸和维生素E",
            "富含矿物质，天然酿造",
            "零添加，富含氨基酸",
        ],
        "taste": [
            "色泽金黄，油质清亮",
            "色泽红亮，鲜香浓郁",
            "米粒晶莹，软糯香甜",
            "酸味柔和，陈香醇厚",
            "豆香浓郁，口感醇厚",
            "香气扑鼻，味道醇厚",
        ],
        "desc": [
            "传统工艺，天然酿造，品质上乘。",
            "精选原料，严格品控，安全健康。",
            "知名品牌，品质保证，值得信赖。",
            "用途广泛，煎炒烹炸皆宜。",
        ],
    },
    # 5: 速食冷冻
    5: {
        "allergen_default": "无",
        "suitable_default": "快节奏人群",
        "nutrition": [
            "高蛋白，富含膳食纤维",
            "富含蛋白质和碳水化合物",
            "非油炸，低热量健康之选",
            "高蛋白低碳水，营养均衡",
            "快速加热即食，方便快捷",
        ],
        "taste": [
            "皮薄馅大，汤汁饱满",
            "爽滑多汁，口感丰富",
            "香脆可口，方便美味",
            "热气腾腾，风味十足",
            "鲜香可口，老少皆宜",
        ],
        "desc": [
            "工厂速冻，锁住新鲜，加热即食。",
            "精选食材，配料丰富，方便快捷。",
            "品质保障，食品安全，冷链配送。",
            "微波炉、水煮皆可，适合上班族。",
        ],
    },
    # 6: 酒水饮料
    6: {
        "allergen_default": "无",
        "suitable_default": "所有人群",
        "nutrition": [
            "富含维生素C，补充水分",
            "低糖配方，健康饮品",
            "天然果汁，不添加香精",
            "富含茶多酚，抗氧化",
            "零脂肪，清爽解渴",
        ],
        "taste": [
            "酸甜可口，清爽解渴",
            "茶香浓郁，回味甘甜",
            "奶香醇厚，口感丝滑",
            "果味清新，冰凉解暑",
            "清冽爽口，沁人心脾",
        ],
        "desc": [
            "精选原料，科学配比，品质上乘。",
            "冷链配送，确保新鲜口感。",
            "品牌正品，厂家授权，放心饮用。",
            "独立瓶装，方便携带，随时享用。",
        ],
    },
}


def normalize_text(text: str) -> str:
    """Remove garbled ? and extra whitespace from text."""
    if not text:
        return ""
    # Replace ? with empty or keep if it looks intentional
    text = re.sub(r'\?+', '', text).strip()
    return text


def generate_descriptions(food_name: str, category_id: int) -> dict:
    """Generate Chinese descriptions for a food item."""
    tpl = CATEGORY_TEMPLATES.get(category_id, CATEGORY_TEMPLATES[1])

    # Select based on food name keywords
    name_lower = food_name.lower()

    # Pick nutrition based on keywords
    if any(k in name_lower for k in ["蓝莓", "葡萄", "石榴", "山楂"]):
        nutrition = "富含花青素、维生素C，抗氧化、保护视力"
    elif any(k in name_lower for k in ["番茄", "番茄"]):
        nutrition = "富含番茄红素、维生素C，高纤维低热量"
    elif any(k in name_lower for k in ["鸡", "鸡翅", "鸡腿", "鸡肉", "鸡胸"]):
        nutrition = "高蛋白低脂肪，富含氨基酸，肉质细嫩"
    elif any(k in name_lower for k in ["牛", "牛肉", "牛排", "肥牛"]):
        nutrition = "高蛋白低脂肪，富含铁、锌元素"
    elif any(k in name_lower for k in ["牛奶", "酸奶", "奶", "乳"]):
        nutrition = "富含钙质和优质乳蛋白"
    elif any(k in name_lower for k in ["蛋", "鸡蛋", "土鸡蛋", "鹌鹑"]):
        nutrition = "高蛋白、富含卵磷脂和DHA"
    elif any(k in name_lower for k in ["鱼", "虾", "蟹", "海鲜"]):
        nutrition = "高蛋白低脂肪，富含虾青素和矿物质"
    elif any(k in name_lower for k in ["油", "油 5"]):
        nutrition = "富含维生素E，不含胆固醇"
    elif any(k in name_lower for k in ["酱油", "生抽", "蚝油", "醋"]):
        nutrition = "天然发酵，富含氨基酸和矿物质"
    elif any(k in name_lower for k in ["米", "稻"]):
        nutrition = "优质碳水化合物来源，富含B族维生素"
    elif any(k in name_lower for k in ["饼", "饼干", "雪饼", "威化"]):
        nutrition = "碳水化合物，快速补充能量"
    elif any(k in name_lower for k in ["巧克力", "糖", "奶糖"]):
        nutrition = "富含可可多酚，提供能量"
    elif any(k in name_lower for k in ["坚果", "果仁", "瓜子", "花生"]):
        nutrition = "富含不饱和脂肪酸和维生素E"
    elif any(k in name_lower for k in ["薯片", "虾条", "膨化"]):
        nutrition = "碳水化合物，松脆可口，注意适量"
    elif any(k in name_lower for k in ["水饺", "饺子", "包子", "馄饨", "汤圆"]):
        nutrition = "富含蛋白质和碳水化合物"
    elif any(k in name_lower for k in ["饮料", "茶", "果汁", "酸奶饮", "奶茶"]):
        nutrition = "低糖配方，清爽解渴，补充水分"
    elif any(k in name_lower for k in ["啤酒", "白酒", "红酒", "酒"]):
        nutrition = "适量饮用，活跃气氛，勿过量"
    elif any(k in name_lower for k in ["豆浆", "豆奶", "豆"]):
        nutrition = "植物蛋白，非转基因，零乳糖"
    else:
        nutrition = tpl["nutrition"][hash(food_name) % len(tpl["nutrition"])]

    # Pick taste
    if any(k in name_lower for k in ["酸", "酸甜"]):
        taste = "酸甜可口，清爽开胃"
    elif any(k in name_lower for k in ["甜", "蜜"]):
        taste = "甘甜多汁，蜜香浓郁"
    elif any(k in name_lower for k in ["辣", "麻辣", "香辣"]):
        taste = "麻辣鲜香，口感丰富"
    elif any(k in name_lower for k in ["咸", "盐", "腊"]):
        taste = "咸香可口，味道浓郁"
    elif any(k in name_lower for k in ["奶", "奶香", "酸奶", "芝士"]):
        taste = "奶香浓郁，口感丝滑"
    elif any(k in name_lower for k in ["果", "果香", "水果"]):
        taste = "果香四溢，清甜可口"
    elif any(k in name_lower for k in ["脆", "酥", "脆"]):
        taste = "香脆可口，入口即化"
    elif any(k in name_lower for k in ["鲜", "鲜美", "鲜嫩"]):
        taste = "鲜嫩多汁，口感细腻"
    else:
        taste = tpl["taste"][hash(food_name + "taste") % len(tpl["taste"])]

    # Suitable people
    if any(k in name_lower for k in ["减肥", "纤体", "低脂", "代餐"]):
        suitable = "减肥人群、健身人群"
    elif any(k in name_lower for k in ["儿童", "宝宝", "小孩"]):
        suitable = "儿童、成长发育期人群"
    elif any(k in name_lower for k in ["老人", "老年", "养生"]):
        suitable = "老人、养生人群"
    elif any(k in name_lower for k in ["孕妇", "孕"]):
        suitable = "孕妇、备孕人群"
    elif any(k in name_lower for k in ["健身", "蛋白", "增肌"]):
        suitable = "健身人群、运动爱好者"
    elif any(k in name_lower for k in ["糖尿病", "无糖", "低糖"]):
        suitable = "控糖人群、糖尿病患者"
    elif any(k in name_lower for k in ["婴幼儿", "奶粉", "辅食"]):
        suitable = "婴幼儿、成长阶段儿童"
    else:
        suitable = tpl["suitable_default"]

    # Allergen
    allergen = tpl["allergen_default"]
    if any(k in name_lower for k in ["坚果", "杏仁", "核桃", "花生", "果仁"]):
        allergen = "坚果过敏者禁食"
    elif any(k in name_lower for k in ["牛奶", "酸奶", "乳", "奶酪", "芝士", "奶粉"]):
        allergen = "乳制品过敏者慎食"
    elif any(k in name_lower for k in ["鸡蛋", "蛋", "蛋黄"]):
        allergen = "鸡蛋过敏者禁食"
    elif any(k in name_lower for k in ["小麦", "面粉", "面条", "馒头", "包子", "饼干", "面包"]):
        allergen = "小麦过敏者慎食"
    elif any(k in name_lower for k in ["大豆", "豆", "豆浆", "豆奶", "豆腐"]):
        allergen = "大豆过敏者慎食"
    elif any(k in name_lower for k in ["虾", "蟹", "鱼", "海鲜"]):
        allergen = "海鲜过敏者禁食"
    elif any(k in name_lower for k in ["芒果", "菠萝", "桃子", "水果"]):
        allergen = "水果过敏者慎食"
    elif any(k in name_lower for k in ["酒", "啤酒", "白酒", "红酒"]):
        allergen = "酒精过敏者禁食，孕妇儿童禁酒"

    # Description
    desc = food_name + "，"
    if any(k in name_lower for k in ["有机", "绿色", "生态"]):
        desc += "有机种植，天然健康，品质保证。"
    elif any(k in name_lower for k in ["进口", "日本", "韩国", "泰国", "法国"]):
        desc += "原装进口，品质优良，口感独特。"
    elif any(k in name_lower for k in ["临期", "折扣", "优惠"]):
        desc += "临期特惠，性价比高，购买请留意保质期。"
    elif any(k in name_lower for k in ["手工", "自制", "现做"]):
        desc += "手工制作，匠心独运，口感地道。"
    elif any(k in name_lower for k in ["品牌", "名牌", "知名"]):
        desc += "知名品牌，品质保障，值得信赖。"
    elif any(k in name_lower for k in ["冷鲜", "冷藏", "冷冻"]):
        desc += "冷链配送，确保新鲜，收到请及时冷藏。"
    else:
        d = tpl["desc"][hash(food_name + "desc") % len(tpl["desc"])]
        desc += d

    return {
        "nutrition_desc": nutrition,
        "taste_desc": taste,
        "suitable_people": suitable,
        "allergen_info": allergen,
        "description": desc,
    }


def fix_food_data():
    conn = pymysql.connect(**DB_CONFIG)
    cur = conn.cursor()

    def esc(s):
        return str(s).replace("'", "\\'")

    # Find garbled entries
    cur.execute("""
        SELECT food_id, food_name, category_id,
               nutrition_desc, taste_desc, suitable_people, allergen_info, description
        FROM food
        WHERE nutrition_desc LIKE '%?%'
           OR nutrition_desc = ''
           OR nutrition_desc IS NULL
        ORDER BY food_id
    """)
    garbled = cur.fetchall()
    print(f"Found {len(garbled)} garbled food entries")

    if not garbled:
        print("No garbled data found - already fixed!")
        conn.close()
        return

    # Generate and update
    updates = []
    for row in garbled:
        food_id, food_name, category_id = row[0], row[1], row[2]
        new_data = generate_descriptions(food_name, category_id)

        sql = """UPDATE food SET
    nutrition_desc = '{}',
    taste_desc = '{}',
    suitable_people = '{}',
    allergen_info = '{}',
    description = '{}'
WHERE food_id = {};""".format(
            esc(new_data['nutrition_desc']),
            esc(new_data['taste_desc']),
            esc(new_data['suitable_people']),
            esc(new_data['allergen_info']),
            esc(new_data['description']),
            food_id,
        )
        updates.append(sql)
        cur.execute(sql)
        print(f"  Updated food_id={food_id}: {food_name[:20]}")

    conn.commit()

    # Export SQL fix file
    sql_out = "E:/毕业论文/bylw-spring/sql/fix_food_garbled.sql"
    with open(sql_out, "w", encoding="utf-8") as f:
        f.write("-- ============================================================\n")
        f.write("-- Fix garbled food description data\n")
        f.write("-- Generated: 2026-04-11\n")
        f.write("-- ============================================================\n\n")
        for sql in updates:
            f.write(sql + "\n\n")
    print(f"\nSQL fix file written to: {sql_out}")

    # Verify
    cur.execute("SELECT COUNT(*) FROM food WHERE nutrition_desc NOT LIKE '%?%' AND nutrition_desc != '' AND nutrition_desc IS NOT NULL")
    good_count = cur.fetchone()[0]
    print(f"\nVerification: {good_count}/216 foods now have valid descriptions")

    conn.close()
    print("\nDone!")


if __name__ == "__main__":
    fix_food_data()
