-- 初始化分类数据（仅在表为空时插入）
INSERT IGNORE INTO categories (id, name, parent_id) VALUES
(1, '数码电子', NULL),
(2, '书籍教材', NULL),
(3, '生活用品', NULL),
(4, '服饰鞋包', NULL),
(5, '运动户外', NULL),
(6, '其他', NULL);
