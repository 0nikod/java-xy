INSERT OR IGNORE INTO users (id, username, password_hash, phone, role, status, created_at)
VALUES
    (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '13800000000', 'ADMIN', 'NORMAL', '2026-05-17 00:00:00'),
    (2, 'demo_user', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', '13900000000', 'USER', 'NORMAL', '2026-05-17 00:00:00');

INSERT OR IGNORE INTO goods (id, seller_id, title, category, original_price, current_price, condition_level, description, status, reject_reason, created_at, updated_at)
VALUES
    (1, 2, '高等数学教材', '教材', 59.80, 29.90, 8, '八成新，适合课程复习使用，支持校园自提。', 'ON_SALE', NULL, '2026-05-17 00:00:00', '2026-05-17 00:00:00'),
    (2, 2, '二手平板电脑', '数码', 1999.00, 1099.00, 7, '正常使用，无暗病，附充电器。', 'PENDING', NULL, '2026-05-17 00:00:00', '2026-05-17 00:00:00');

