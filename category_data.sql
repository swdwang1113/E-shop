-- 清空现有商品分类数据（如果需要）
-- TRUNCATE TABLE `goods_category`;

-- 插入一级分类
INSERT INTO `goods_category` (`name`, `parent_id`, `level`, `sort`, `create_time`, `update_time`)
VALUES 
('手机数码', 0, 1, 1, NOW(), NOW()),
('电脑办公', 0, 1, 2, NOW(), NOW()),
('家用电器', 0, 1, 3, NOW(), NOW()),
('服装鞋包', 0, 1, 4, NOW(), NOW()),
('食品生鲜', 0, 1, 5, NOW(), NOW()),
('美妆护肤', 0, 1, 6, NOW(), NOW()),
('运动户外', 0, 1, 7, NOW(), NOW()),
('图书音像', 0, 1, 8, NOW(), NOW());

-- 插入手机数码二级分类
INSERT INTO `goods_category` (`name`, `parent_id`, `level`, `sort`, `create_time`, `update_time`)
VALUES 
('手机', 1, 2, 1, NOW(), NOW()),
('平板电脑', 1, 2, 2, NOW(), NOW()),
('摄影摄像', 1, 2, 3, NOW(), NOW()),
('智能设备', 1, 2, 4, NOW(), NOW()),
('手机配件', 1, 2, 5, NOW(), NOW());

-- 插入电脑办公二级分类
INSERT INTO `goods_category` (`name`, `parent_id`, `level`, `sort`, `create_time`, `update_time`)
VALUES 
('笔记本电脑', 2, 2, 1, NOW(), NOW()),
('台式电脑', 2, 2, 2, NOW(), NOW()),
('显示器', 2, 2, 3, NOW(), NOW()),
('打印机', 2, 2, 4, NOW(), NOW()),
('办公设备', 2, 2, 5, NOW(), NOW());

-- 插入家用电器二级分类
INSERT INTO `goods_category` (`name`, `parent_id`, `level`, `sort`, `create_time`, `update_time`)
VALUES 
('电视', 3, 2, 1, NOW(), NOW()),
('冰箱', 3, 2, 2, NOW(), NOW()),
('洗衣机', 3, 2, 3, NOW(), NOW()),
('空调', 3, 2, 4, NOW(), NOW()),
('厨房电器', 3, 2, 5, NOW(), NOW());

-- 插入服装鞋包二级分类
INSERT INTO `goods_category` (`name`, `parent_id`, `level`, `sort`, `create_time`, `update_time`)
VALUES 
('男装', 4, 2, 1, NOW(), NOW()),
('女装', 4, 2, 2, NOW(), NOW()),
('童装', 4, 2, 3, NOW(), NOW()),
('鞋靴', 4, 2, 4, NOW(), NOW()),
('箱包', 4, 2, 5, NOW(), NOW());

-- 插入食品生鲜二级分类
INSERT INTO `goods_category` (`name`, `parent_id`, `level`, `sort`, `create_time`, `update_time`)
VALUES 
('生鲜水果', 5, 2, 1, NOW(), NOW()),
('肉禽蛋品', 5, 2, 2, NOW(), NOW()),
('休闲食品', 5, 2, 3, NOW(), NOW()),
('饮料冲调', 5, 2, 4, NOW(), NOW()),
('粮油调味', 5, 2, 5, NOW(), NOW());

-- 插入美妆护肤二级分类
INSERT INTO `goods_category` (`name`, `parent_id`, `level`, `sort`, `create_time`, `update_time`)
VALUES 
('面部护肤', 6, 2, 1, NOW(), NOW()),
('彩妆', 6, 2, 2, NOW(), NOW()),
('香水', 6, 2, 3, NOW(), NOW()),
('个人护理', 6, 2, 4, NOW(), NOW()),
('美发护发', 6, 2, 5, NOW(), NOW());

-- 插入运动户外二级分类
INSERT INTO `goods_category` (`name`, `parent_id`, `level`, `sort`, `create_time`, `update_time`)
VALUES 
('运动服饰', 7, 2, 1, NOW(), NOW()),
('健身器材', 7, 2, 2, NOW(), NOW()),
('户外装备', 7, 2, 3, NOW(), NOW()),
('运动鞋', 7, 2, 4, NOW(), NOW()),
('游泳用品', 7, 2, 5, NOW(), NOW());

-- 插入图书音像二级分类
INSERT INTO `goods_category` (`name`, `parent_id`, `level`, `sort`, `create_time`, `update_time`)
VALUES 
('文学小说', 8, 2, 1, NOW(), NOW()),
('教育考试', 8, 2, 2, NOW(), NOW()),
('人文社科', 8, 2, 3, NOW(), NOW()),
('经济管理', 8, 2, 4, NOW(), NOW()),
('少儿读物', 8, 2, 5, NOW(), NOW());

-- 插入手机三级分类
INSERT INTO `goods_category` (`name`, `parent_id`, `level`, `sort`, `create_time`, `update_time`)
VALUES 
('苹果手机', 9, 3, 1, NOW(), NOW()),
('华为手机', 9, 3, 2, NOW(), NOW()),
('小米手机', 9, 3, 3, NOW(), NOW()),
('三星手机', 9, 3, 4, NOW(), NOW()),
('OPPO手机', 9, 3, 5, NOW(), NOW());

-- 插入笔记本电脑三级分类
INSERT INTO `goods_category` (`name`, `parent_id`, `level`, `sort`, `create_time`, `update_time`)
VALUES 
('游戏本', 14, 3, 1, NOW(), NOW()),
('轻薄本', 14, 3, 2, NOW(), NOW()),
('商务本', 14, 3, 3, NOW(), NOW()),
('学生本', 14, 3, 4, NOW(), NOW()),
('二合一平板笔记本', 14, 3, 5, NOW(), NOW()); 