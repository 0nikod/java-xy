#!/usr/bin/env python3
"""Generate demo goods text data, SQL snippets, and placeholder images.

This script is intentionally deterministic:
- the same arguments always produce the same goods text and SQL snippet
- image text is generated from goods id and image index
- no online dependency is required for metadata generation

Typical usage:

    uv run --with pillow python scripts/generate_seed_demo_assets.py

If Pillow is not available and you only want text data / SQL snippets:

    python3 scripts/generate_seed_demo_assets.py --skip-images
"""

from __future__ import annotations

import argparse
import json
from dataclasses import asdict
from dataclasses import dataclass
from datetime import datetime
from datetime import timedelta
from pathlib import Path
from typing import Dict
from typing import Iterable
from typing import List
from typing import Sequence


DEFAULT_FONT_PATH = Path("/usr/share/fonts/noto-cjk/NotoSansCJK-Regular.ttc")
DEFAULT_IMAGE_ROOT = Path("data/images")
DEFAULT_MANIFEST_PATH = Path("data/seed/demo_goods_manifest.json")
DEFAULT_SQL_SNIPPET_PATH = Path("data/seed/demo_goods_seed_snippet.sql")
BASE_CREATED_AT = datetime(2026, 5, 20, 9, 0, 0)
SELLER_ID = 2
BUYER_ID = 3

CATEGORIES: Sequence[str] = ("教材", "数码", "服饰", "生活用品", "其他")
STATUS_SEQUENCE: Sequence[str] = (
    ("ON_SALE",) * 24
    + ("PENDING",) * 12
    + ("SOLD",) * 8
    + ("OFF_SHELF",) * 6
)

CATEGORY_TITLES: Dict[str, Sequence[str]] = {
    "教材": (
        "高等数学辅导书",
        "数据结构教材",
        "离散数学笔记",
        "大学英语练习册",
        "计算机网络复习资料",
        "概率论课堂讲义",
        "操作系统习题册",
        "Java 程序设计教材",
    ),
    "数码": (
        "机械键盘",
        "无线鼠标",
        "蓝牙耳机",
        "护眼台灯",
        "移动硬盘",
        "显示器支架",
        "平板保护壳",
        "便携充电器",
    ),
    "服饰": (
        "连帽卫衣",
        "运动外套",
        "牛仔裤",
        "帆布鞋",
        "针织开衫",
        "休闲衬衫",
        "运动长裤",
        "双肩包",
    ),
    "生活用品": (
        "宿舍收纳箱",
        "保温杯",
        "床上书桌",
        "小风扇",
        "折叠晾衣架",
        "简易鞋架",
        "宿舍台灯",
        "午休抱枕",
    ),
    "其他": (
        "桌面摆件",
        "卡通抱枕",
        "雨伞",
        "手账礼盒",
        "拼图摆台",
        "便携棋盘",
        "文创收纳袋",
        "创意马克杯",
    ),
}

CATEGORY_SUFFIXES: Dict[str, Sequence[str]] = {
    "教材": ("期末复习版", "课堂自用版", "少量笔记版", "整洁保存版"),
    "数码": ("双模版", "青轴版", "轻量版", "宿舍自用版"),
    "服饰": ("M 码", "L 码", "宽松版", "通勤款"),
    "生活用品": ("大号款", "折叠款", "宿舍款", "简约款"),
    "其他": ("简约款", "收藏版", "桌面款", "礼物款"),
}

CATEGORY_USAGE: Dict[str, Sequence[str]] = {
    "教材": (
        "适合期末复习和日常查阅",
        "章节划分清晰，拿来就能继续用",
        "适合补基础或课程配套使用",
    ),
    "数码": (
        "功能正常，适合宿舍和自习室使用",
        "日常连接稳定，适合学习办公",
        "放宿舍继续用完全没问题",
    ),
    "服饰": (
        "适合日常通勤或换季穿搭",
        "上身效果自然，学生日常好搭配",
        "洗护方便，适合校内日常穿着",
    ),
    "生活用品": (
        "放宿舍很实用，日常整理更省心",
        "适合校内生活场景，拿回去即可继续用",
        "实用性强，适合学生党节省预算",
    ),
    "其他": (
        "适合桌面装饰或日常小场景使用",
        "作为小物件转让，性价比比较高",
        "适合送同学或自己留作日常使用",
    ),
}

SOURCE_PHRASES: Sequence[str] = (
    "上学期自用",
    "毕业整理闲置",
    "宿舍收纳清理",
    "买来后使用时间不长",
    "换季腾地方顺手出掉",
    "最近很少再用",
)


@dataclass
class GoodsRecord:
    """Single goods row plus image and order metadata."""

    goods_id: int
    seller_id: int
    buyer_id: int | None
    title: str
    category: str
    original_price: float
    current_price: float
    condition_level: int
    description: str
    status: str
    created_at: str
    updated_at: str
    image_paths: List[str]
    order_id: int | None
    order_no: str | None
    order_created_at: str | None


def parse_args() -> argparse.Namespace:
    """Parse command-line arguments for demo asset generation."""

    parser = argparse.ArgumentParser(
        description="批量生成演示商品文字数据、SQL 片段和白底文字商品图。"
    )
    parser.add_argument("--start-id", type=int, default=4, help="起始商品编号，默认 4。")
    parser.add_argument("--count", type=int, default=50, help="生成商品数量，默认 50。")
    parser.add_argument(
        "--images-per-goods",
        type=int,
        default=1,
        help="每个商品生成几张图，默认 1。",
    )
    parser.add_argument(
        "--image-root",
        type=Path,
        default=DEFAULT_IMAGE_ROOT,
        help="图片输出根目录，默认 data/images。",
    )
    parser.add_argument(
        "--manifest-path",
        type=Path,
        default=DEFAULT_MANIFEST_PATH,
        help="商品清单 JSON 输出路径。",
    )
    parser.add_argument(
        "--sql-snippet-path",
        type=Path,
        default=DEFAULT_SQL_SNIPPET_PATH,
        help="seed SQL 片段输出路径。",
    )
    parser.add_argument(
        "--font-path",
        type=Path,
        default=DEFAULT_FONT_PATH,
        help="用于渲染中文的字体文件路径。",
    )
    parser.add_argument(
        "--image-width",
        type=int,
        default=960,
        help="导出图片宽度，默认 960。",
    )
    parser.add_argument(
        "--image-height",
        type=int,
        default=720,
        help="导出图片高度，默认 720。",
    )
    parser.add_argument(
        "--font-size",
        type=int,
        default=52,
        help="主文字字号，默认 52。",
    )
    parser.add_argument(
        "--skip-images",
        action="store_true",
        help="只生成文字数据和 SQL 片段，不写 PNG。",
    )
    return parser.parse_args()


def build_goods_records(
    start_id: int,
    count: int,
    images_per_goods: int,
    image_root: Path,
) -> List[GoodsRecord]:
    """Build stable demo goods metadata from deterministic templates."""

    records: List[GoodsRecord] = []
    sold_order_counter = 2
    for ordinal in range(count):
        goods_id = start_id + ordinal
        category = CATEGORIES[ordinal % len(CATEGORIES)]
        status = STATUS_SEQUENCE[ordinal % len(STATUS_SEQUENCE)]
        title = build_title(category, ordinal)
        condition_level = 6 + (ordinal % 5)
        current_price = build_current_price(category, ordinal)
        original_price = round(current_price * (1.25 + 0.08 * (ordinal % 4)), 2)
        created_at = BASE_CREATED_AT + timedelta(hours=ordinal * 3)
        updated_at = created_at + timedelta(minutes=45)
        description = build_description(category, status, ordinal, condition_level)
        image_paths = build_image_paths(image_root, goods_id, images_per_goods)

        order_id = None
        order_no = None
        order_created_at = None
        buyer_id = None
        if status == "SOLD":
            order_id = sold_order_counter
            order_no = "ORD-{}-DEMO{:02d}".format(created_at.strftime("%Y%m%d%H%M%S"), ordinal + 1)
            order_created_at = (updated_at + timedelta(days=2)).strftime("%Y-%m-%d %H:%M:%S")
            buyer_id = BUYER_ID
            sold_order_counter += 1

        records.append(
            GoodsRecord(
                goods_id=goods_id,
                seller_id=SELLER_ID,
                buyer_id=buyer_id,
                title=title,
                category=category,
                original_price=original_price,
                current_price=current_price,
                condition_level=condition_level,
                description=description,
                status=status,
                created_at=created_at.strftime("%Y-%m-%d %H:%M:%S"),
                updated_at=updated_at.strftime("%Y-%m-%d %H:%M:%S"),
                image_paths=image_paths,
                order_id=order_id,
                order_no=order_no,
                order_created_at=order_created_at,
            )
        )
    return records


def build_title(category: str, ordinal: int) -> str:
    """Assemble realistic demo titles by category rotation."""

    titles = CATEGORY_TITLES[category]
    suffixes = CATEGORY_SUFFIXES[category]
    base_title = titles[(ordinal // len(CATEGORIES)) % len(titles)]
    suffix = suffixes[ordinal % len(suffixes)]
    return "{} {}".format(base_title, suffix)


def build_current_price(category: str, ordinal: int) -> float:
    """Compute stable current prices without random numbers."""

    price_base = {
        "教材": 18.0,
        "数码": 48.0,
        "服饰": 22.0,
        "生活用品": 16.0,
        "其他": 14.0,
    }[category]
    step = {
        "教材": 4.5,
        "数码": 12.0,
        "服饰": 6.0,
        "生活用品": 5.0,
        "其他": 4.0,
    }[category]
    span_index = (ordinal * 3) % 7
    return round(price_base + step * span_index, 2)


def build_description(category: str, status: str, ordinal: int, condition_level: int) -> str:
    """Generate concise but stable descriptions for seed data."""

    source = SOURCE_PHRASES[ordinal % len(SOURCE_PHRASES)]
    usage = CATEGORY_USAGE[category][ordinal % len(CATEGORY_USAGE[category])]

    if condition_level >= 9:
        condition_text = "整体几乎无明显使用痕迹"
    elif condition_level >= 7:
        condition_text = "整体成色较新，只有轻微使用痕迹"
    else:
        condition_text = "有正常使用痕迹，但不影响继续使用"

    status_text = {
        "ON_SALE": "目前处于在售状态",
        "PENDING": "已经整理完毕，等待审核上架",
        "SOLD": "已完成成交，保留作历史订单演示",
        "OFF_SHELF": "暂时下架，保留作个人中心演示",
    }[status]
    return "，".join((source, condition_text, usage, status_text)) + "。"


def build_image_paths(image_root: Path, goods_id: int, images_per_goods: int) -> List[str]:
    """Build runtime-relative image paths matching the Java storage contract."""

    image_paths = []
    for image_index in range(1, images_per_goods + 1):
        image_paths.append(
            (image_root / "goods-{}".format(goods_id) / "{}_seed.png".format(str(image_index).zfill(2))).as_posix()
        )
    return image_paths


def write_manifest(path: Path, records: Sequence[GoodsRecord], arguments: argparse.Namespace) -> None:
    """Persist the generated goods metadata as UTF-8 JSON."""

    path.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "generated_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "arguments": {
            "start_id": arguments.start_id,
            "count": arguments.count,
            "images_per_goods": arguments.images_per_goods,
            "image_root": str(arguments.image_root),
            "skip_images": arguments.skip_images,
        },
        "records": [asdict(record) for record in records],
    }
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_sql_snippet(path: Path, records: Sequence[GoodsRecord]) -> None:
    """Write SQL blocks that can be pasted into seed.sql later."""

    path.parent.mkdir(parents=True, exist_ok=True)

    goods_lines = []
    goods_image_lines = []
    order_lines = []
    goods_image_id = 4

    for record in records:
        goods_lines.append(
            "    ({goods_id}, {seller_id}, '{title}', '{category}', {original_price:.2f}, "
            "{current_price:.2f}, {condition_level}, '{description}', '{status}', "
            "'{created_at}', '{updated_at}')".format(
                goods_id=record.goods_id,
                seller_id=record.seller_id,
                title=escape_sql(record.title),
                category=escape_sql(record.category),
                original_price=record.original_price,
                current_price=record.current_price,
                condition_level=record.condition_level,
                description=escape_sql(record.description),
                status=record.status,
                created_at=record.created_at,
                updated_at=record.updated_at,
            )
        )
        for display_order, image_path in enumerate(record.image_paths):
            goods_image_lines.append(
                "    ({image_id}, {goods_id}, '{image_path}', {is_primary}, {display_order})".format(
                    image_id=goods_image_id,
                    goods_id=record.goods_id,
                    image_path=escape_sql(image_path),
                    is_primary=1 if display_order == 0 else 0,
                    display_order=display_order,
                )
            )
            goods_image_id += 1
        if record.order_id is not None:
            order_lines.append(
                "    ({order_id}, '{order_no}', {goods_id}, {buyer_id}, {seller_id}, "
                "{deal_price:.2f}, 'FINISHED', '{created_at}')".format(
                    order_id=record.order_id,
                    order_no=record.order_no,
                    goods_id=record.goods_id,
                    buyer_id=record.buyer_id,
                    seller_id=record.seller_id,
                    deal_price=record.current_price,
                    created_at=record.order_created_at,
                )
            )

    blocks = [
        "-- Generated by scripts/generate_seed_demo_assets.py",
        "-- Goods block for direct paste into src/main/resources/db/seed.sql",
        "INSERT OR IGNORE INTO goods (id, seller_id, title, category, original_price, current_price, "
        "condition_level, description, status, created_at, updated_at)",
        "VALUES",
        ",\n".join(goods_lines) + ";",
        "",
        "-- Goods images block",
        "INSERT OR REPLACE INTO goods_images (id, goods_id, image_path, is_primary, display_order)",
        "VALUES",
        ",\n".join(goods_image_lines) + ";",
        "",
    ]
    if order_lines:
        blocks.extend(
            [
                "-- Orders block for SOLD goods",
                "INSERT OR IGNORE INTO orders (id, order_no, goods_id, buyer_id, seller_id, deal_price, status, created_at)",
                "VALUES",
                ",\n".join(order_lines) + ";",
                "",
            ]
        )

    path.write_text("\n".join(blocks), encoding="utf-8")


def generate_placeholder_images(
    image_root: Path,
    records: Sequence[GoodsRecord],
    font_path: Path,
    image_width: int,
    image_height: int,
    font_size: int,
) -> None:
    """Render PNG placeholder images with white background and Chinese text."""

    try:
        from PIL import Image
        from PIL import ImageDraw
        from PIL import ImageFont
    except ImportError as exc:
        raise SystemExit(
            "Pillow 未安装。请使用 `uv run --with pillow python scripts/generate_seed_demo_assets.py` 再执行。"
        ) from exc

    if not font_path.exists():
        raise SystemExit("字体文件不存在: {}".format(font_path))

    image_root.mkdir(parents=True, exist_ok=True)
    font = ImageFont.truetype(str(font_path), font_size)
    footer_font = ImageFont.truetype(str(font_path), max(24, font_size // 2))

    for record in records:
        for image_index, image_path_text in enumerate(record.image_paths, start=1):
            output_path = Path(image_path_text)
            output_path.parent.mkdir(parents=True, exist_ok=True)
            image = Image.new("RGB", (image_width, image_height), "white")
            draw = ImageDraw.Draw(image)

            main_text = "示例商品图片 {} - 第{}张".format(record.goods_id, image_index)
            footer_text = "{} | {} | {}".format(record.title, record.category, record.status)
            draw_centered_text(
                draw=draw,
                lines=wrap_text(draw, main_text, font, image_width - 120),
                font=font,
                width=image_width,
                height=image_height,
                text_fill=(24, 24, 24),
                top_offset=-40,
            )
            draw_footer_text(
                draw=draw,
                text=footer_text,
                font=footer_font,
                width=image_width,
                height=image_height,
            )
            image.save(output_path, format="PNG")


def draw_centered_text(
    draw: "ImageDraw.ImageDraw",
    lines: Iterable[str],
    font: "ImageFont.FreeTypeFont",
    width: int,
    height: int,
    text_fill: tuple[int, int, int],
    top_offset: int,
) -> None:
    """Draw centered multi-line text onto the placeholder image."""

    lines = list(lines)
    spacing = 16
    line_heights = [measure_text(draw, line, font)[1] for line in lines]
    total_height = sum(line_heights) + spacing * max(0, len(lines) - 1)
    current_y = (height - total_height) // 2 + top_offset

    for line, line_height in zip(lines, line_heights):
        line_width, _ = measure_text(draw, line, font)
        current_x = (width - line_width) // 2
        draw.text((current_x, current_y), line, font=font, fill=text_fill)
        current_y += line_height + spacing


def draw_footer_text(
    draw: "ImageDraw.ImageDraw",
    text: str,
    font: "ImageFont.FreeTypeFont",
    width: int,
    height: int,
) -> None:
    """Draw a small footer line to make each placeholder easier to inspect."""

    text_width, text_height = measure_text(draw, text, font)
    draw.text(
        ((width - text_width) // 2, height - text_height - 42),
        text,
        font=font,
        fill=(96, 96, 96),
    )


def wrap_text(
    draw: "ImageDraw.ImageDraw",
    text: str,
    font: "ImageFont.FreeTypeFont",
    max_width: int,
) -> List[str]:
    """Wrap Chinese text by glyph width instead of by spaces."""

    if not text:
        return [""]

    lines: List[str] = []
    current = ""
    for char in text:
        candidate = current + char
        candidate_width, _ = measure_text(draw, candidate, font)
        if current and candidate_width > max_width:
            lines.append(current)
            current = char
        else:
            current = candidate
    if current:
        lines.append(current)
    return lines


def measure_text(
    draw: "ImageDraw.ImageDraw",
    text: str,
    font: "ImageFont.FreeTypeFont",
) -> tuple[int, int]:
    """Measure text using the active Pillow draw context."""

    left, top, right, bottom = draw.textbbox((0, 0), text, font=font)
    return right - left, bottom - top


def escape_sql(value: str) -> str:
    """Escape a Python string for direct embedding into a SQL literal."""

    return value.replace("'", "''")


def print_summary(
    records: Sequence[GoodsRecord], manifest_path: Path, sql_snippet_path: Path, skip_images: bool
) -> None:
    """Print a compact execution summary for terminal use."""

    sold_count = sum(1 for record in records if record.status == "SOLD")
    print("已生成商品记录: {}".format(len(records)))
    print("其中已售商品: {}".format(sold_count))
    print("JSON 清单: {}".format(manifest_path))
    print("SQL 片段: {}".format(sql_snippet_path))
    if skip_images:
        print("本次跳过 PNG 导出。")
    else:
        print("PNG 图片已按 goods-<id>/NN_seed.png 导出。")


def main() -> None:
    """Generate metadata first, then optionally render images."""

    arguments = parse_args()
    records = build_goods_records(
        start_id=arguments.start_id,
        count=arguments.count,
        images_per_goods=arguments.images_per_goods,
        image_root=arguments.image_root,
    )
    write_manifest(arguments.manifest_path, records, arguments)
    write_sql_snippet(arguments.sql_snippet_path, records)

    if not arguments.skip_images:
        generate_placeholder_images(
            image_root=arguments.image_root,
            records=records,
            font_path=arguments.font_path,
            image_width=arguments.image_width,
            image_height=arguments.image_height,
            font_size=arguments.font_size,
        )

    print_summary(
        records=records,
        manifest_path=arguments.manifest_path,
        sql_snippet_path=arguments.sql_snippet_path,
        skip_images=arguments.skip_images,
    )


if __name__ == "__main__":
    main()
