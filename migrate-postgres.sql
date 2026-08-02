-- =============================================
-- 校园二手交易平台 — 本地 MySQL → Render PostgreSQL 数据迁移
-- 前提: 后端已在 Render 部署成功 (Hibernate 已建表)
-- 执行: Render PostgreSQL → Query 面板粘贴运行, 或 psql -f 执行
-- 注意: 脚本会清空所有表后导入, 请确认目标是全新数据库
-- =============================================

BEGIN;

-- 1. 清空所有表并重置序列 (防止后端已自动初始化 admin/分类)
TRUNCATE activities, categories, conversations, favorites, follows, messages,
         notifications, orders, product_comments, product_images, products,
         reports, reviews, users
RESTART IDENTITY CASCADE;

-- 2. 分类 (id 1-6, 与本地一致)
INSERT INTO categories (id, name, parent_id) VALUES
(1, '数码电子', NULL),
(2, '书籍教材', NULL),
(3, '生活用品', NULL),
(4, '服饰鞋包', NULL),
(5, '运动户外', NULL),
(6, '其他', NULL);

-- 3. 用户 (密码为 BCrypt 哈希, 登录密码不变: admin/admin123, yuz30/?, yuz31/?, yuz32/?)
INSERT INTO users (id, username, password, nickname, phone, avatar, campus, rating_avg, rating_count, role, status, created_at, updated_at) VALUES
(1, 'yuz30', '$2a$10$wgm6Otor44USD6mW/U7RSu2NkRwrNQkHC3QyK.1YDvH22C6es997K', 'yuz', '', NULL, '', NULL, NULL, 'USER', 'ACTIVE', '2026-07-12 16:40:44.755694', '2026-07-12 16:40:44.756694'),
(2, 'admin', '$2a$10$b18C0Ir5DM5KV5iGWuK3TecOHcXjJueoMCx/z.jMKiMd7Kb1l2SJq', '系统管理员', NULL, NULL, NULL, 0.00, 0, 'ADMIN', 'ACTIVE', '2026-07-13 20:32:52.696946', '2026-07-13 20:32:52.696946'),
(3, 'yuz31', '$2a$10$NUw8fGrcrYlgkp3g6wRDHOef4QIl06V0gpyxp.HawEfCevgp7mZE6', 'yuz1', '', NULL, '', 0.00, 0, 'USER', 'ACTIVE', '2026-07-13 20:36:10.483643', '2026-07-13 20:36:10.483643'),
(4, 'yuz32', '$2a$10$6M6SxOcjFe8.59gMMqrDmenYyFLNF.Aj4Z77bNeSzhZFdgyRNINHi', 'yuz2', '', NULL, '', 0.00, 0, 'USER', 'ACTIVE', '2026-07-13 20:37:18.746355', '2026-07-13 20:37:18.746355');

-- 4. 商品
INSERT INTO products (id, category_id, cover_image, created_at, description, original_price, price, seller_id, status, title, updated_at, view_count) VALUES
(1, 1, '/uploads/358812e8-c32d-452f-87c4-200a28256f20.png', '2026-07-13 20:35:47.306160', '', 160.00, 100.00, 1, 'SOLD', '弗洛洛', '2026-07-13 20:36:19.666644', 4);

-- 5. 商品图片
INSERT INTO product_images (id, product_id, sort_order, url) VALUES
(1, 1, 0, '/uploads/2a147cd4-3535-48da-8a8f-0aa2ad308974.png'),
(2, 1, 1, '/uploads/a2806211-1a3d-4543-97c7-273834a9d9c9.png');

-- 6. 订单
INSERT INTO orders (id, order_no, buyer_id, seller_id, product_id, amount, status, remark, created_at, updated_at) VALUES
(1, '20260713203619BF3D37', 3, 1, 1, 100.00, 'CONFIRMED', '', '2026-07-13 20:36:19.638346', '2026-07-13 20:38:36.276833');

-- 7. 会话
INSERT INTO conversations (id, buyer_id, seller_id, product_id, last_message, last_message_at, created_at) VALUES
(8, 1, 2, 0, 'test123', '2026-07-15 10:11:55.425206', '2026-07-14 22:06:49.000000'),
(12, 1, 3, 0, '你好', '2026-07-16 14:25:07.996461', '2026-07-15 10:40:39.733703');

-- 8. 消息
INSERT INTO messages (id, conversation_id, sender_id, content, is_read, created_at) VALUES
(11, 8, 2, 'test123', TRUE, '2026-07-15 10:11:55.425206'),
(12, 12, 1, '你好！', TRUE, '2026-07-15 10:40:39.766498'),
(13, 12, 1, '你好', TRUE, '2026-07-15 21:32:03.359786'),
(14, 12, 1, '你好', TRUE, '2026-07-15 21:32:12.803566'),
(15, 12, 3, '你好', FALSE, '2026-07-16 14:25:07.997459');

-- 9. 关注
INSERT INTO follows (id, follower_id, following_id, created_at) VALUES
(1, 1, 3, '2026-07-14 16:53:32.739725'),
(2, 3, 1, '2026-07-16 14:25:04.689929');

-- 10. 通知
INSERT INTO notifications (id, user_id, type, title, content, is_read, related_id, created_at) VALUES
(1, 1, 'ORDER', '新订单通知', 'yuz1 拍下了「弗洛洛」，请尽快确认', TRUE, 1, '2026-07-13 20:36:19.653628'),
(2, 3, 'ORDER', '订单已确认', '卖家已确认订单「弗洛洛」，请与卖家协商面交', TRUE, 1, '2026-07-13 20:38:36.269898'),
(3, 3, 'SYSTEM', '新粉丝', 'yuz 关注了您', TRUE, NULL, '2026-07-14 16:53:32.848383'),
(4, 1, 'MESSAGE', '新私信', '系统管理员 给您发了一条私信', TRUE, 8, '2026-07-15 10:11:55.427213'),
(5, 3, 'MESSAGE', '新私信', 'yuz 给您发了一条私信', TRUE, 12, '2026-07-15 10:40:39.782395'),
(6, 3, 'MESSAGE', '新私信', 'yuz 给您发了一条私信（关于「私信对话」）', TRUE, 12, '2026-07-15 21:32:03.414325'),
(7, 3, 'MESSAGE', '新私信', 'yuz 给您发了一条私信（关于「私信对话」）', TRUE, 12, '2026-07-15 21:32:12.807569'),
(8, 1, 'SYSTEM', '新粉丝', 'yuz1 关注了您', FALSE, NULL, '2026-07-16 14:25:04.710559'),
(9, 1, 'MESSAGE', '新私信', 'yuz1 给您发了一条私信（关于「私信对话」）', FALSE, 12, '2026-07-16 14:25:08.004459');

-- 11. 重置自增序列 (关键! 否则后续插入会主键冲突)
SELECT setval(pg_get_serial_sequence('categories', 'id'), (SELECT MAX(id) FROM categories));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('products', 'id'), (SELECT MAX(id) FROM products));
SELECT setval(pg_get_serial_sequence('product_images', 'id'), (SELECT MAX(id) FROM product_images));
SELECT setval(pg_get_serial_sequence('orders', 'id'), (SELECT MAX(id) FROM orders));
SELECT setval(pg_get_serial_sequence('conversations', 'id'), (SELECT MAX(id) FROM conversations));
SELECT setval(pg_get_serial_sequence('messages', 'id'), (SELECT MAX(id) FROM messages));
SELECT setval(pg_get_serial_sequence('follows', 'id'), (SELECT MAX(id) FROM follows));
SELECT setval(pg_get_serial_sequence('notifications', 'id'), (SELECT MAX(id) FROM notifications));

COMMIT;
