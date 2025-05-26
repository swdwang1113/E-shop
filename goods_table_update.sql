-- 为goods表添加评分字段
ALTER TABLE `goods` ADD COLUMN `rating` DECIMAL(2,1) NOT NULL DEFAULT 0.0 COMMENT '商品评分，1-5分' AFTER `image_url`;

-- 为goods表添加销量字段
ALTER TABLE `goods` ADD COLUMN `sales_volume` INT NOT NULL DEFAULT 0 COMMENT '商品销量' AFTER `rating`;

-- 为已有商品设置随机初始评分（3.5-5.0之间）
UPDATE `goods` SET `rating` = ROUND(3.5 + RAND() * 1.5, 1) WHERE `rating` = 0; 