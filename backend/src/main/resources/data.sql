-- 初始化分类数据（仅在表为空时插入）
INSERT IGNORE INTO categories (id, name, parent_id) VALUES
(1, '数码电子', NULL),
(2, '书籍教材', NULL),
(3, '生活用品', NULL),
(4, '服饰鞋包', NULL),
(5, '运动户外', NULL),
(6, '其他', NULL);

-- 搜索性能索引（MySQL 8 安全添加方式）
-- 如果索引已存在则跳过，不会导致启动失败
-- 对 title 列加普通索引以加速 LIKE 查询
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE table_schema = DATABASE() AND table_name = 'products' AND index_name = 'idx_products_title') = 0,
    'ALTER TABLE products ADD INDEX idx_products_title (title)',
    'SELECT ''idx_products_title already exists'' AS msg'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
