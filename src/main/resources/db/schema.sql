-- 开启外键约束，保证商品、订单、图片与用户之间的数据一致性。
PRAGMA foreign_keys = ON;

-- 用户表：保存登录身份、角色和账号状态。
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    phone TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'USER',
    status TEXT NOT NULL DEFAULT 'NORMAL',
    created_at TEXT NOT NULL
);

-- 商品表：保存交易核心信息，审核状态和售卖状态都在这里管理。
CREATE TABLE IF NOT EXISTS goods (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    seller_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    original_price REAL NOT NULL,
    current_price REAL NOT NULL,
    condition_level INTEGER NOT NULL,
    description TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (seller_id) REFERENCES users(id)
);

-- 订单表：记录成交结果，成交后对应商品应转为已售。
CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no TEXT NOT NULL UNIQUE,
    goods_id INTEGER NOT NULL,
    buyer_id INTEGER NOT NULL,
    seller_id INTEGER NOT NULL,
    deal_price REAL NOT NULL,
    status TEXT NOT NULL DEFAULT 'FINISHED',
    created_at TEXT NOT NULL,
    FOREIGN KEY (goods_id) REFERENCES goods(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES users(id)
);

-- 评价表：首版仅做结构预留，后续扩展时基于订单补充评价流程。
CREATE TABLE IF NOT EXISTS reviews (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL UNIQUE,
    goods_id INTEGER NOT NULL,
    reviewer_id INTEGER NOT NULL,
    seller_id INTEGER NOT NULL,
    rating INTEGER NOT NULL,
    content TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (goods_id) REFERENCES goods(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    FOREIGN KEY (seller_id) REFERENCES users(id)
);

-- 商品图片表：支持最多多张图片，并可标记主图用于列表展示。
CREATE TABLE IF NOT EXISTS goods_images (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    goods_id INTEGER NOT NULL,
    image_path TEXT NOT NULL,
    is_primary INTEGER NOT NULL DEFAULT 0,
    display_order INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (goods_id) REFERENCES goods(id) ON DELETE CASCADE
);

-- 管理员日志表：记录审核、删除、封禁等后台操作。
CREATE TABLE IF NOT EXISTS admin_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    admin_id INTEGER NOT NULL,
    action TEXT NOT NULL,
    target_type TEXT NOT NULL,
    target_id INTEGER,
    detail TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY (admin_id) REFERENCES users(id)
);
