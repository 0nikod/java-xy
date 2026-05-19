-- 默认管理员与演示用户，便于首次启动后直接体验核心流程。
INSERT OR IGNORE INTO users (id, username, password_hash, phone, role, status, created_at)
VALUES
    (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '13800000000', 'ADMIN', 'NORMAL', '2026-05-17 00:00:00'),
    (2, 'demo_user', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', '13900000000', 'USER', 'NORMAL', '2026-05-17 00:00:00'),
    (3, 'demo_buyer', 'e547bd13228250dfb4c7df1d1ebb78cfd9f2ada56ebb0c425d35829dd3ac4ae8', '13700000000', 'USER', 'NORMAL', '2026-05-17 00:00:00');

-- 演示商品：覆盖在售、待审核、已售和有图/无图两种场景。
INSERT OR IGNORE INTO goods (id, seller_id, title, category, original_price, current_price, condition_level, description, status, created_at, updated_at)
VALUES
    (1, 2, '黄色棉花娃娃', '其他', 69.00, 59.00, 8, '看起来很可爱。', 'ON_SALE', '2026-05-17 00:00:00', '2026-05-17 00:00:00'),
    (2, 2, '红色棉花娃娃', '其他', 69.00, 49.00, 8, '看起来很漂亮。', 'ON_SALE', '2026-05-17 00:00:00', '2026-05-17 00:00:00'),
    (3, 2, '墨绿色棉花娃娃', '其他', 69.00, 39.00, 9, '看起来很酷。', 'ON_SALE', '2026-05-17 00:00:00', '2026-05-17 00:00:00'),
    (4, 2, '数据结构笔记', '教材', 35.00, 18.00, 8, '重点章节整理完整，适合期末冲刺。', 'SOLD', '2026-05-17 00:00:00', '2026-05-17 00:00:00');

-- 演示商品图片：每个商品一张主图，路径指向运行时图片目录。
INSERT OR REPLACE INTO goods_images (id, goods_id, image_path, is_primary, display_order)
VALUES
    (1, 1, 'data/images/good-1/image_1.png', 1, 0),
    (2, 2, 'data/images/good-2/image_1.png', 1, 0),
    (3, 3, 'data/images/good-3/image_1.png', 1, 0);

-- 演示订单：确保个人中心、统计页和答辩流程有现成成交数据。
INSERT OR IGNORE INTO orders (id, order_no, goods_id, buyer_id, seller_id, deal_price, status, created_at)
VALUES
    (1, 'ORD-20260517000000-DEMO01', 4, 3, 2, 18.00, 'FINISHED', '2026-05-17 00:00:00');

