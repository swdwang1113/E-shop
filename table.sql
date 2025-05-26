SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '账号',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL UNIQUE COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '密码',
  `gender` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin COMMENT '性别',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '商品名称',
  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `description` text CHARACTER SET utf8 COLLATE utf8_bin COMMENT '商品介绍',
  `category_id` int DEFAULT NULL COMMENT '商品类别',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1-上架 0-下架',
  `image_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '商品图片地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES ('1', '草莓', '4.00', '可口草莓', '0', '4', '1', '/img/goods/goods003.jpg', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('2', '苹果', '2.00', '红富士苹果', '0', '2', '1', '/img/2.jpg', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('3', '橙子', '4.00', '金黄的大橙子', '0', '4', '1', '/img/goods/goods001.jpg', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('4', '葡萄', '12.00', '吐鲁番葡萄', '0', '12', '1', '/img/goods/goods002.jpg', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('5', '大虾', '5.00', '山东大虾', '2', '5', '1', '/img/goods/goods018.jpg', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('6', '带鱼', '50.00', '好吃的带鱼', '2', '50', '1', '/img/goods/goods020.jpg', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('7', '扇贝', '20.00', '大神快来快来', '2', '20', '1', '/img/goods/goods019.jpg', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('8', '皮皮虾', '10.00', '啥的客户', '2', '10', '1', '/img/goods/goods021.jpg', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('9', '脉动', '4.00', '不在状态，脉动一下', '1', '4', '1', '/img/goods/maidong.png', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('10', '汽水', '8.00', '栓双', '1', '8', '1', '/img/goods/qishui.png', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('11', '沙棘汁', '3.00', '好喝', '1', '3', '1', '/img/goods/shajizhi.png', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('12', '和其正', '5.00', 'dsa asd', '1', '5', '1', '/img/goods/heqizheng.png', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('13', '菠菜', '2.00', '大', '3', '2', '1', '/img/goods/bocai.png', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('14', '生菜', '2.00', 'asdajlskdja', '3', '2', '1', '/img/goods/shengcai.png', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('15', '番茄', '1.00', '好吃', '3', '1', '1', '/img/goods/fanqie.png', '2024-04-01 10:00:00', '2024-04-01 10:00:00');
INSERT INTO `goods` VALUES ('16', '胡萝卜', '2.00', 'asdkjlajkld', '3', '2', '1', '/img/goods/huluobo.png', '2024-04-01 10:00:00', '2024-04-01 10:00:00');


SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `goods_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_goods` (`user_id`, `goods_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

-- 订单主表
CREATE TABLE orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    order_no VARCHAR(50) NOT NULL UNIQUE,  -- 订单编号
    total_amount DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,  -- 订单状态: 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消
    payment_type TINYINT,  -- 支付方式: 1-支付宝 2-微信
    payment_time DATETIME,  -- 支付时间
    shipping_time DATETIME,  -- 发货时间
    completion_time DATETIME,  -- 完成时间
    address_id INT,  -- 收货地址ID
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no)
);

-- 订单商品表（用于存储订单中的商品信息）
CREATE TABLE order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    goods_id INT NOT NULL,
    goods_name VARCHAR(100) NOT NULL,  -- 冗余存储，避免商品名称变更影响历史订单
    goods_image VARCHAR(255),
    goods_price DECIMAL(10,2) NOT NULL,  -- 下单时的商品价格
    quantity INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
);

CREATE TABLE user_address (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL, 
    district VARCHAR(50) NOT NULL,
    detail_address VARCHAR(200) NOT NULL,
    is_default TINYINT NOT NULL DEFAULT 0,  -- 是否默认地址: 1-是 0-否
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
);

CREATE TABLE goods_review (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    goods_id INT NOT NULL,
    order_id INT NOT NULL,
    rating TINYINT NOT NULL,  -- 评分: 1-5
    content TEXT,
    images VARCHAR(1000),  -- 存储图片URL，多个URL用逗号分隔
    like_count INT NOT NULL DEFAULT 0,  -- 点赞数
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_goods_id (goods_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
);

CREATE TABLE user_favorite (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    goods_id INT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_goods (user_id, goods_id),
    INDEX idx_user_id (user_id)
);

CREATE TABLE goods_category (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    parent_id INT DEFAULT 0,  -- 父分类ID，0表示一级分类
    level INT NOT NULL,  -- 分类级别: 1-一级 2-二级 3-三级
    sort INT NOT NULL DEFAULT 0,  -- 排序值
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id)
);