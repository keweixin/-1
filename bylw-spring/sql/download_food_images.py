"""Download matching local images from Baidu and normalize SQL image paths."""
from __future__ import annotations
import html, re, time
from dataclasses import dataclass
from io import BytesIO
from pathlib import Path
import requests
from PIL import Image, ImageDraw, ImageFont, ImageOps
try:
    import pymysql  # type: ignore
except Exception:
    pymysql = None

ROOT = Path(__file__).resolve().parents[1]
SQL_DIR = ROOT / "sql"
STATIC_DIR = ROOT / "src/main/resources" / "static" / "images"
FOODS_DIR = STATIC_DIR / "foods"
RECIPES_DIR = STATIC_DIR / "recipes"
ARTICLES_DIR = STATIC_DIR / "articles"
COMMUNITY_DIR = STATIC_DIR / "community"
POINTS_DIR = STATIC_DIR / "points"
BANNERS_DIR = STATIC_DIR / "banners"
PLACEHOLDERS_DIR = STATIC_DIR / "placeholders"
SQL_FILES = sorted(path for path in SQL_DIR.glob("*.sql") if path.name not in {"update_local_image_paths.sql", "update_food_images_fixed.sql"})
DB_CONFIG = {"host": "localhost", "port": 3306, "user": "root", "password": "root", "database": "bylw", "charset": "utf8mb4"}
TABLE_ID_COLUMN = {"food": "food_id", "food_recipe": "recipe_id", "food_article": "article_id", "community_post": "post_id", "points_goods": "goods_id"}
IMAGE_COLUMN_BY_TABLE = {"food": "cover_img", "food_recipe": "cover_img", "food_article": "cover_img", "community_post": "img_list", "points_goods": "cover_img", "sys_banner": "img_url"}
TABLE_COUNTER_DEFAULTS = {"food_recipe": 1, "food_article": 1, "community_post": 1, "points_goods": 1, "sys_banner": 1, "schema_food": 1}
FONT_CANDIDATES = [("C:/Windows/Fonts/msyhbd.ttc", True), ("C:/Windows/Fonts/msyh.ttc", False), ("C:/Windows/Fonts/simhei.ttf", False)]
BAIDU_HEADERS = {"User-Agent": "Mozilla/5.0", "Referer": "https://image.baidu.com/"}
BAIDU_SEARCH_URL = "https://image.baidu.com/search/acjson"
BAIDU_RESULT_PATTERN = re.compile(r'"thumbURL":"(?P<thumb>https://[^\"]+)".*?"width":(?P<width>\d+),\s*"height":(?P<height>\d+).*?"fromPageTitle":"(?P<title>.*?)"', re.S)

@dataclass
class CoverSpec:
    relative_path: str
    title: str
    subtitle: str
    label: str
    kind: str
    size: tuple[int, int]

@dataclass
class Stats:
    downloaded: int = 0
    generated: int = 0
    removed: int = 0


def ensure_dirs() -> None:
    for path in (FOODS_DIR, RECIPES_DIR, ARTICLES_DIR, COMMUNITY_DIR, POINTS_DIR, BANNERS_DIR, PLACEHOLDERS_DIR):
        path.mkdir(parents=True, exist_ok=True)


def load_font(size: int, bold: bool = False):
    for font_path, is_bold in FONT_CANDIDATES:
        if bold and not is_bold:
            continue
        try:
            return ImageFont.truetype(font_path, size=size)
        except OSError:
            continue
    for font_path, _ in FONT_CANDIDATES:
        try:
            return ImageFont.truetype(font_path, size=size)
        except OSError:
            continue
    return ImageFont.load_default()


def clean_text(value: str) -> str:
    value = value.replace("\\n", " ").replace("\r", " ").replace("\n", " ")
    return re.sub(r"\s+", " ", value).strip()


def short_text(value: str, limit: int) -> str:
    value = clean_text(value)
    return value if len(value) <= limit else value[: limit - 3] + "..."


def split_sql_values(row_text: str) -> list[str]:
    content = row_text.strip().rstrip(",;").strip()
    if content.startswith("(") and content.endswith(")"):
        content = content[1:-1]
    values, current, in_quote, depth, i = [], [], False, 0, 0
    while i < len(content):
        ch = content[i]
        if ch == "'":
            current.append(ch)
            if in_quote and i + 1 < len(content) and content[i + 1] == "'":
                current.append(content[i + 1])
                i += 1
            else:
                in_quote = not in_quote
        elif ch == "," and not in_quote and depth == 0:
            values.append("".join(current).strip())
            current = []
        else:
            if not in_quote:
                if ch == "(": depth += 1
                elif ch == ")" and depth > 0: depth -= 1
            current.append(ch)
        i += 1
    if current:
        values.append("".join(current).strip())
    return values


def sql_literal_to_value(value: str) -> str:
    value = value.strip()
    return value[1:-1].replace("''", "'") if value.startswith("'") and value.endswith("'") else value


def sql_quote(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def rebuild_row(line: str, values: list[str]) -> str:
    stripped = line.strip()
    suffix = "," if stripped.endswith(",") else ";" if stripped.endswith(";") else ""
    indent = line[: len(line) - len(line.lstrip())]
    return f"{indent}({', '.join(values)}){suffix}\n"


def sanitize_filename(text: str) -> str:
    value = clean_text(text)
    for old, new in {"*": "x", "?": "x", "%": "pct", "?": "", "/": "_", "\\": "_"}.items():
        value = value.replace(old, new)
    value = re.sub(r"[<>:\"|?*]", "", value)
    value = re.sub(r"[??()????!???:?;????'\"??\-\s]+", "", value)
    return value or "item"


def iter_insert_rows(sql_path: Path):
    insert_pattern = re.compile(r"INSERT INTO\s+([a-zA-Z_]+)\s*\((.*?)\)\s*VALUES", re.IGNORECASE)
    current_table, current_columns = "", []
    for line in sql_path.read_text(encoding="utf-8", errors="ignore").splitlines():
        stripped = line.strip()
        match = insert_pattern.match(stripped)
        if match:
            current_table = match.group(1).lower()
            current_columns = [column.strip().strip("`") for column in match.group(2).split(",")]
            continue
        if current_table and stripped.startswith("("):
            values = split_sql_values(stripped)
            if len(values) == len(current_columns):
                data = {column: sql_literal_to_value(value) for column, value in zip(current_columns, values)}
                yield current_table, current_columns, data
            if stripped.endswith(";"):
                current_table, current_columns = "", []
            continue
        if current_table and stripped.endswith(";"):
            current_table, current_columns = "", []


def parse_food_filename_map() -> dict[int, str]:
    mapping: dict[int, str] = {}
    for sql_file in SQL_FILES:
        for table, _, data in iter_insert_rows(sql_file):
            if table != "food":
                continue
            food_id, food_name = data.get("food_id", ""), data.get("food_name", "")
            if food_id.isdigit() and food_name:
                mapping[int(food_id)] = f"{int(food_id)}_{sanitize_filename(food_name)}.jpg"
    if mapping:
        return mapping
    for path in FOODS_DIR.glob("*.jpg"):
        match = re.match(r"(\d+)_", path.name)
        if match:
            mapping[int(match.group(1))] = path.name
    return mapping


def build_default_specs() -> dict[str, CoverSpec]:
    defaults = [
        ("placeholders/food-default.jpg", "??????", "?????????????????", "????", "food", (1200, 900)),
        ("placeholders/food-fresh.jpg", "????", "??????????", "????", "food", (1200, 900)),
        ("placeholders/food-meat.jpg", "????", "?????????????", "????", "food", (1200, 900)),
        ("placeholders/food-snack.jpg", "????", "?????????", "????", "food", (1200, 900)),
        ("placeholders/food-grain.jpg", "????", "?????????", "????", "food", (1200, 900)),
        ("placeholders/food-frozen.jpg", "????", "????????????", "????", "food", (1200, 900)),
        ("placeholders/food-drink.jpg", "????", "???????", "????", "food", (1200, 900)),
        ("placeholders/recipe-cover.jpg", "????", "????????", "????", "recipe", (1280, 720)),
        ("placeholders/article-cover.jpg", "????", "??????????", "????", "article", (1280, 720)),
        ("placeholders/community-cover.jpg", "????", "?????????", "????", "community", (1280, 720)),
        ("placeholders/points-cover.jpg", "????", "?????????", "????", "points", (1200, 1200)),
        ("banners/home-hero.jpg", "??????????", "??????????????????", "????", "banner", (1600, 900)),
    ]
    return {relative_path: CoverSpec(relative_path, title, subtitle, label, kind, size) for relative_path, title, subtitle, label, kind, size in defaults}


def ensure_spec(specs: dict[str, CoverSpec], relative_path: str, title: str, subtitle: str, label: str, kind: str, size: tuple[int, int]) -> None:
    if relative_path not in specs:
        specs[relative_path] = CoverSpec(relative_path, title, subtitle, label, kind, size)


def make_table_counters() -> dict[str, int]:
    return dict(TABLE_COUNTER_DEFAULTS)


def get_record_id(table: str, data: dict[str, str], counters: dict[str, int]) -> int:
    id_column = TABLE_ID_COLUMN.get(table)
    if id_column and data.get(id_column, "").isdigit():
        return int(data[id_column])
    current = counters[table]
    counters[table] += 1
    return current


def get_known_id(table: str, data: dict[str, str], counters: dict[str, int]) -> int | None:
    id_column = TABLE_ID_COLUMN.get(table)
    if id_column and data.get(id_column, "").isdigit():
        return int(data[id_column])
    return counters.get(table, 1) - 1


def build_local_path(table: str, data: dict[str, str], food_filename_map: dict[int, str], specs: dict[str, CoverSpec], counters: dict[str, int]) -> str | None:
    if table == "food":
        food_id_raw = data.get("food_id", "")
        if food_id_raw.isdigit() and int(food_id_raw) in food_filename_map:
            return f"/images/foods/{food_filename_map[int(food_id_raw)]}"
        path = f"/images/placeholders/schema-food-{counters['schema_food']:02d}.jpg"
        counters["schema_food"] += 1
        ensure_spec(specs, path.removeprefix("/images/"), data.get("food_name", "????"), data.get("description", data.get("nutrition_desc", "??????")), "????", "food", (1200, 900))
        return path
    if table == "food_recipe":
        record_id = get_record_id(table, data, counters)
        path = f"/images/recipes/{record_id:03d}.jpg"
        ensure_spec(specs, path.removeprefix("/images/"), data.get("title", "????"), data.get("summary", data.get("content", "")), "????", "recipe", (1280, 720))
        return path
    if table == "food_article":
        record_id = get_record_id(table, data, counters)
        path = f"/images/articles/{record_id:03d}.jpg"
        ensure_spec(specs, path.removeprefix("/images/"), data.get("title", "????"), data.get("summary", data.get("content", "")), "????", "article", (1280, 720))
        return path
    if table == "community_post":
        record_id = get_record_id(table, data, counters)
        path = f"/images/community/{record_id:03d}.jpg"
        ensure_spec(specs, path.removeprefix("/images/"), data.get("title", "????"), data.get("content", ""), "????", "community", (1280, 720))
        return path
    if table == "points_goods":
        record_id = get_record_id(table, data, counters)
        path = f"/images/points/{record_id:03d}.jpg"
        ensure_spec(specs, path.removeprefix("/images/"), data.get("goods_name", "????"), data.get("description", ""), "????", "points", (1200, 1200))
        return path
    if table == "sys_banner":
        record_id = get_record_id(table, data, counters)
        path = f"/images/banners/banner-{record_id}.jpg"
        ensure_spec(specs, path.removeprefix("/images/"), data.get("title", "????"), data.get("link_url", data.get("target_url", "")), "????", "banner", (1600, 900))
        return path
    return None


def rewrite_sql_file(sql_path: Path, food_filename_map: dict[int, str], specs: dict[str, CoverSpec], db_updates: dict[str, dict[int, str]], banner_updates: dict[str, str]) -> None:
    original = sql_path.read_text(encoding="utf-8", errors="ignore")
    lines = original.splitlines(keepends=True)
    output_lines: list[str] = []
    current_table, current_columns = "", []
    counters = make_table_counters()
    insert_pattern = re.compile(r"INSERT INTO\s+([a-zA-Z_]+)\s*\((.*?)\)\s*VALUES", re.IGNORECASE)
    for line in lines:
        stripped = line.strip()
        insert_match = insert_pattern.match(stripped)
        if insert_match:
            current_table = insert_match.group(1).lower()
            current_columns = [column.strip().strip("`") for column in insert_match.group(2).split(",")]
            output_lines.append(line)
            continue
        if current_table and stripped.startswith("("):
            values = split_sql_values(stripped)
            if len(values) == len(current_columns):
                data = {column: sql_literal_to_value(value) for column, value in zip(current_columns, values)}
                local_path = build_local_path(current_table, data, food_filename_map, specs, counters)
                image_column = IMAGE_COLUMN_BY_TABLE.get(current_table)
                if local_path and image_column and image_column in current_columns:
                    values[current_columns.index(image_column)] = sql_quote(local_path)
                    line = rebuild_row(line, values)
                if current_table == "food":
                    food_id = data.get("food_id", "")
                    if food_id.isdigit() and int(food_id) in food_filename_map:
                        db_updates.setdefault("food", {})[int(food_id)] = f"/images/foods/{food_filename_map[int(food_id)]}"
                elif current_table in ("food_recipe", "food_article", "community_post", "points_goods"):
                    record_id = get_known_id(current_table, data, counters)
                    if record_id is not None and local_path:
                        db_updates.setdefault(current_table, {})[record_id] = local_path
                elif current_table == "sys_banner" and local_path:
                    banner_updates[data.get("title", "")] = local_path
            output_lines.append(line)
            if stripped.endswith(";"):
                current_table, current_columns = "", []
            continue
        output_lines.append(line)
    rewritten = "".join(output_lines).replace("/static/images/foods/", "/images/foods/")
    if rewritten != original:
        sql_path.write_text(rewritten, encoding="utf-8")


def write_update_sql(food_filename_map: dict[int, str], db_updates: dict[str, dict[int, str]], banner_updates: dict[str, str]) -> None:
    output = ["-- ??????????", "-- ????: 2026-04-13", ""]
    for food_id in sorted(food_filename_map):
        local_path = f"/images/foods/{food_filename_map[food_id]}"
        output.append(f"UPDATE food SET cover_img = '{local_path}' WHERE food_id = {food_id};")
    for recipe_id, local_path in sorted(db_updates.get("food_recipe", {}).items()):
        output.append(f"UPDATE food_recipe SET cover_img = '{local_path}' WHERE recipe_id = {recipe_id};")
    for article_id, local_path in sorted(db_updates.get("food_article", {}).items()):
        output.append(f"UPDATE food_article SET cover_img = '{local_path}' WHERE article_id = {article_id};")
    for post_id, local_path in sorted(db_updates.get("community_post", {}).items()):
        output.append(f"UPDATE community_post SET img_list = '{local_path}' WHERE post_id = {post_id};")
    for goods_id, local_path in sorted(db_updates.get("points_goods", {}).items()):
        output.append(f"UPDATE points_goods SET cover_img = '{local_path}' WHERE goods_id = {goods_id};")
    for title, local_path in sorted(banner_updates.items()):
        escaped_title = title.replace("'", "''")
        output.append(f"UPDATE sys_banner SET img_url = '{local_path}' WHERE title = '{escaped_title}';")
    output.append("")
    (SQL_DIR / "update_local_image_paths.sql").write_text("\n".join(output), encoding="utf-8")


def palette(kind: str) -> tuple[str, str, str]:
    colors = {
        "food": ("#0f766e", "#ccfbf1", "#134e4a"),
        "recipe": ("#9a3412", "#ffedd5", "#7c2d12"),
        "article": ("#1d4ed8", "#dbeafe", "#1e3a8a"),
        "community": ("#7c3aed", "#ede9fe", "#5b21b6"),
        "points": ("#a16207", "#fef3c7", "#854d0e"),
        "banner": ("#0f766e", "#dcfce7", "#14532d"),
    }
    return colors.get(kind, ("#334155", "#e2e8f0", "#0f172a"))


def draw_cover(output_path: Path, title: str, subtitle: str, label: str, kind: str, size: tuple[int, int]) -> None:
    bg, panel, fg = palette(kind)
    image = Image.new("RGB", size, bg)
    draw = ImageDraw.Draw(image)
    w, h = size
    pad = max(24, min(w, h) // 16)
    draw.rounded_rectangle((pad, pad, w - pad, h - pad), radius=28, fill=panel)
    title_font = load_font(max(30, h // 11), bold=True)
    body_font = load_font(max(18, h // 26))
    tag_font = load_font(max(16, h // 32), bold=True)
    draw.rounded_rectangle((pad * 2, pad * 2, pad * 2 + w // 5, pad * 2 + h // 12), radius=18, fill=fg)
    draw.text((pad * 2 + 18, pad * 2 + h // 24), label, fill="white", font=tag_font, anchor="lm")
    x, y, max_width = pad * 2, pad * 2 + h // 9, w - pad * 4
    for line in wrap_text(draw, short_text(title, 28), title_font, max_width, 3):
        draw.text((x, y), line, fill=fg, font=title_font)
        y += title_font.size + 8
    y += 8
    for line in wrap_text(draw, short_text(subtitle or title, 64), body_font, max_width, 4):
        draw.text((x, y), line, fill=fg, font=body_font)
        y += body_font.size + 6
    output_path.parent.mkdir(parents=True, exist_ok=True)
    image.save(output_path, quality=92)


def wrap_text(draw: ImageDraw.ImageDraw, text: str, font, max_width: int, max_lines: int) -> list[str]:
    text = clean_text(text)
    if not text:
        return []
    lines, current = [], ""
    for char in text:
        trial = current + char
        bbox = draw.textbbox((0, 0), trial, font=font)
        if bbox[2] - bbox[0] <= max_width:
            current = trial
        else:
            if current:
                lines.append(current)
            current = char
            if len(lines) == max_lines:
                break
    if current and len(lines) < max_lines:
        lines.append(current)
    if len(lines) == max_lines and "".join(lines) != text:
        lines[-1] = short_text(lines[-1], max(4, len(lines[-1]) - 1))
    return lines


def write_avatar_svg(output_path: Path) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256"><defs><linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" stop-color="#1f8f62"/><stop offset="100%" stop-color="#69c39a"/></linearGradient></defs><rect width="256" height="256" rx="48" fill="url(#g)"/><circle cx="128" cy="98" r="46" fill="#f6fffb" opacity="0.94"/><path d="M62 214c12-40 40-60 66-60s54 20 66 60" fill="#f6fffb" opacity="0.94"/></svg>', encoding='utf-8')


def strip_tags(value: str) -> str:
    value = value.replace("\\/", "/")
    value = html.unescape(value)
    if "\\u" in value:
        try:
            value = value.encode("utf-8").decode("unicode_escape")
        except Exception:
            pass
    return clean_text(re.sub(r"<[^>]+>", "", value))


def normalized_match_text(value: str) -> str:
    return re.sub(r"[^0-9a-z\u4e00-\u9fff]+", "", strip_tags(value).lower())


def longest_common_substring(a: str, b: str) -> int:
    if not a or not b:
        return 0
    dp, best = [0] * (len(b) + 1), 0
    for char_a in a:
        prev = 0
        for index, char_b in enumerate(b, start=1):
            current = dp[index]
            if char_a == char_b:
                dp[index] = prev + 1
                best = max(best, dp[index])
            else:
                dp[index] = 0
            prev = current
    return best


def build_search_queries(title: str, kind: str, subtitle: str = "", extras: Iterable[str] | None = None) -> list[str]:
    suffix_map = {
        "food": ["???", "???"],
        "recipe": ["???", "???"],
        "article": ["????", "????"],
        "community": ["????", "????"],
        "points": ["???", "???"],
        "banner": ["??????", "??????"],
    }
    queries = [f"{clean_text(title)} {suffix}" for suffix in suffix_map.get(kind, ["??"])]
    if subtitle:
        queries.append(f"{clean_text(title)} {short_text(subtitle, 18)}")
    if extras:
        queries.extend(clean_text(item) for item in extras if item)
    seen, result = set(), []
    for query in queries:
        query = clean_text(query)
        if query and query not in seen:
            seen.add(query)
            result.append(query)
    return result[:4]


def candidate_score(query_title: str, candidate_title: str, width: int, height: int, target_size: tuple[int, int]) -> float:
    query_norm = normalized_match_text(query_title)
    title_norm = normalized_match_text(candidate_title)
    score = float(longest_common_substring(query_norm, title_norm) * 10)
    for token in re.findall(r"[0-9]+(?:ml|g|kg|l)?", query_norm):
        if token and token in title_norm:
            score += 6
    if query_norm[:4] and title_norm.startswith(query_norm[:4]):
        score += 8
    if query_norm[:2] and query_norm[:2] in title_norm:
        score += 5
    score += min(width, height) / 120
    score -= abs(target_size[0] / max(1, target_size[1]) - width / max(1, height)) * 10
    return score


def save_resized_image(content: bytes, output_path: Path, size: tuple[int, int]) -> None:
    with Image.open(BytesIO(content)) as image:
        fitted = ImageOps.fit(image.convert("RGB"), size, method=Image.LANCZOS, centering=(0.5, 0.5))
        output_path.parent.mkdir(parents=True, exist_ok=True)
        fitted.save(output_path, quality=92, subsampling=0)


def fetch_baidu_candidates(session: requests.Session, query: str) -> list[dict[str, str | int]]:
    params = {"tn": "resultjson_com", "ipn": "rj", "ct": "201326592", "fp": "result", "word": query, "queryWord": query, "cl": "2", "lm": "-1", "ie": "utf-8", "oe": "utf-8", "st": "-1", "ic": "0", "hd": "1", "latest": "0", "copyright": "0", "rn": "18", "pn": "0"}
    response = session.get(BAIDU_SEARCH_URL, params=params, headers=BAIDU_HEADERS, timeout=20)
    response.raise_for_status()
    candidates = []
    for match in BAIDU_RESULT_PATTERN.finditer(response.text):
        thumb = match.group("thumb").replace("\\/", "/")
        title = strip_tags(match.group("title"))
        width, height = int(match.group("width")), int(match.group("height"))
        if width < 260 or height < 260:
            continue
        candidates.append({"thumb": thumb, "title": title, "width": width, "height": height})
        if len(candidates) >= 12:
            break
    return candidates


def download_best_image(session: requests.Session, output_path: Path, title: str, subtitle: str, kind: str, size: tuple[int, int], extras: Iterable[str] | None = None) -> bool:
    best_score, best_content = -10000.0, None
    for query in build_search_queries(title, kind, subtitle, extras):
        try:
            candidates = fetch_baidu_candidates(session, query)
        except Exception:
            time.sleep(0.2)
            continue
        for candidate in candidates:
            score = candidate_score(title, str(candidate["title"]), int(candidate["width"]), int(candidate["height"]), size)
            if score < 18:
                continue
            try:
                image_response = session.get(str(candidate["thumb"]), headers=BAIDU_HEADERS, timeout=20)
                image_response.raise_for_status()
                with Image.open(BytesIO(image_response.content)) as image:
                    image.verify()
                if score > best_score:
                    best_score, best_content = score, image_response.content
            except Exception:
                continue
        if best_content is not None and best_score >= 30:
            break
        time.sleep(0.15)
    if best_content is None:
        return False
    save_resized_image(best_content, output_path, size)
    return True


def render_food_images(food_filename_map: dict[int, str], stats: Stats) -> None:
    session = requests.Session()
    for _, filename in sorted(food_filename_map.items()):
        title = Path(filename).stem.split("_", 1)[1] if "_" in Path(filename).stem else Path(filename).stem
        output_path = FOODS_DIR / filename
        if download_best_image(session, output_path, title, title, "food", (1200, 900)):
            stats.downloaded += 1
        else:
            draw_cover(output_path, title, "?????????????????????", "????", "food", (1200, 900))
            stats.generated += 1


def render_spec_images(specs: Iterable[CoverSpec], stats: Stats) -> None:
    session = requests.Session()
    generated_defaults = {"placeholders/food-default.jpg", "placeholders/food-fresh.jpg", "placeholders/food-meat.jpg", "placeholders/food-snack.jpg", "placeholders/food-grain.jpg", "placeholders/food-frozen.jpg", "placeholders/food-drink.jpg", "placeholders/recipe-cover.jpg", "placeholders/article-cover.jpg", "placeholders/community-cover.jpg", "placeholders/points-cover.jpg"}
    generic_title = {
        "recipe": "\u5bb6\u5e38\u83dc",
        "article": "\u65b0\u9c9c\u98df\u6750",
        "community": "\u7f8e\u98df\u5206\u4eab",
        "banner": "\u8d85\u5e02\u98df\u54c1\u8d27\u67b6",
    }
    extra_queries = {
        "banners/home-hero.jpg": [
            "\u4e34\u671f\u98df\u54c1 \u8d85\u5e02 \u8d2d\u7269 \u6a2a\u5e45",
            "\u98df\u54c1 \u8d85\u5e02 \u8d27\u67b6 \u6a2a\u5e45",
        ]
    }
    for spec in specs:
        output_path = STATIC_DIR / spec.relative_path
        if spec.relative_path in generated_defaults:
            draw_cover(output_path, spec.title, short_text(spec.subtitle or spec.title, 64), spec.label, spec.kind, spec.size)
            stats.generated += 1
            continue
        search_title = generic_title.get(spec.kind, spec.title)
        if download_best_image(session, output_path, search_title, spec.subtitle, spec.kind, spec.size, extra_queries.get(spec.relative_path)):
            stats.downloaded += 1
        else:
            draw_cover(output_path, search_title, short_text(spec.subtitle or spec.title, 64), spec.label, spec.kind, spec.size)
            stats.generated += 1


def cleanup_unused_images(food_filename_map: dict[int, str], specs: dict[str, CoverSpec], stats: Stats) -> None:
    expected = {Path("foods") / filename for filename in food_filename_map.values()}
    expected.update(Path(relative_path) for relative_path in specs)
    expected.add(Path("placeholders") / "avatar.svg")
    for path in STATIC_DIR.rglob("*"):
        if path.is_file() and path.relative_to(STATIC_DIR) not in expected:
            path.unlink()
            stats.removed += 1


def apply_updates_to_database(food_filename_map: dict[int, str], db_updates: dict[str, dict[int, str]], banner_updates: dict[str, str]) -> None:
    if pymysql is None:
        return
    try:
        connection = pymysql.connect(**DB_CONFIG)
    except Exception:
        return
    try:
        with connection.cursor() as cursor:
            for food_id in sorted(food_filename_map):
                cursor.execute("UPDATE food SET cover_img = %s WHERE food_id = %s", (f"/images/foods/{food_filename_map[food_id]}", food_id))
            for recipe_id, local_path in db_updates.get("food_recipe", {}).items():
                cursor.execute("UPDATE food_recipe SET cover_img = %s WHERE recipe_id = %s", (local_path, recipe_id))
            for article_id, local_path in db_updates.get("food_article", {}).items():
                cursor.execute("UPDATE food_article SET cover_img = %s WHERE article_id = %s", (local_path, article_id))
            for post_id, local_path in db_updates.get("community_post", {}).items():
                cursor.execute("UPDATE community_post SET img_list = %s WHERE post_id = %s", (local_path, post_id))
            for goods_id, local_path in db_updates.get("points_goods", {}).items():
                cursor.execute("UPDATE points_goods SET cover_img = %s WHERE goods_id = %s", (local_path, goods_id))
            for title, local_path in banner_updates.items():
                cursor.execute("UPDATE sys_banner SET img_url = %s WHERE title = %s", (local_path, title))
        connection.commit()
    except Exception:
        connection.rollback()
    finally:
        connection.close()


def main() -> None:
    ensure_dirs()
    food_filename_map = parse_food_filename_map()
    specs = build_default_specs()
    db_updates: dict[str, dict[int, str]] = {}
    banner_updates: dict[str, str] = {}
    for sql_file in SQL_FILES:
        rewrite_sql_file(sql_file, food_filename_map, specs, db_updates, banner_updates)
    stats = Stats()
    render_food_images(food_filename_map, stats)
    render_spec_images(specs.values(), stats)
    write_avatar_svg(PLACEHOLDERS_DIR / "avatar.svg")
    write_update_sql(food_filename_map, db_updates, banner_updates)
    apply_updates_to_database(food_filename_map, db_updates, banner_updates)
    cleanup_unused_images(food_filename_map, specs, stats)
    print(f"Downloaded {stats.downloaded} Baidu images.")
    print(f"Generated {stats.generated} local fallback covers.")
    print(f"Removed {stats.removed} stale local images.")
    print("Rewrote SQL image fields to canonical local /images paths.")


if __name__ == "__main__":
    main()
