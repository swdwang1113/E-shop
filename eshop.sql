/*
 Navicat MySQL Data Transfer

 Source Server         : wjj
 Source Server Type    : MySQL
 Source Server Version : 80028
 Source Host           : localhost:3306
 Source Schema         : eshop

 Target Server Type    : MySQL
 Target Server Version : 80028
 File Encoding         : 65001

 Date: 30/05/2025 09:11:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `goods_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_user_goods`(`user_id`, `goods_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '商品名称',
  `price` decimal(10, 2) NOT NULL COMMENT '商品价格',
  `description` text CHARACTER SET utf8 COLLATE utf8_bin NULL COMMENT '商品介绍',
  `category_id` int NULL DEFAULT NULL COMMENT '商品类别',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 1-上架 0-下架',
  `image_url` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '商品图片地址',
  `rating` decimal(2, 1) NOT NULL DEFAULT 0.0 COMMENT '商品评分，1-5分',
  `sales_volume` int NOT NULL DEFAULT 0 COMMENT '商品销量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for goods_category
-- ----------------------------
DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `parent_id` int NULL DEFAULT 0,
  `level` int NOT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 70 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for goods_review
-- ----------------------------
DROP TABLE IF EXISTS `goods_review`;
CREATE TABLE `goods_review`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `goods_id` int NOT NULL,
  `order_id` int NOT NULL,
  `rating` tinyint NOT NULL,
  `content` text CHARACTER SET utf8 COLLATE utf8_bin NULL,
  `images` varchar(1000) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `like_count` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_items
-- ----------------------------
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `goods_id` int NOT NULL,
  `goods_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `goods_image` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `goods_price` decimal(10, 2) NOT NULL,
  `quantity` int NOT NULL,
  `total_price` decimal(10, 2) NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `order_no` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `total_amount` decimal(10, 2) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0,
  `payment_type` tinyint NULL DEFAULT NULL,
  `payment_time` datetime NULL DEFAULT NULL,
  `shipping_time` datetime NULL DEFAULT NULL,
  `completion_time` datetime NULL DEFAULT NULL,
  `address_id` int NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_order_no`(`order_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for review_like
-- ----------------------------
DROP TABLE IF EXISTS `review_like`;
CREATE TABLE `review_like`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `review_id` int NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_review`(`user_id`, `review_id`) USING BTREE,
  INDEX `idx_review_id`(`review_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '账号',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '密码',
  `gender` varchar(10) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '性别',
  `role` tinyint(1) NOT NULL DEFAULT 0 COMMENT '用户角色: 0-普通用户, 1-管理员',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '电话号码',
  `email` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '头像URL',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `receiver_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `receiver_phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `province` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `city` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `district` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `detail_address` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `is_default` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `goods_id` int NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_user_goods`(`user_id`, `goods_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for shipping_info
-- ----------------------------
DROP TABLE IF EXISTS `shipping_info`;
CREATE TABLE `shipping_info` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL COMMENT '关联订单ID',
  `shipping_company` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '物流公司',
  `tracking_number` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '物流单号',
  `sender_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '发货地址',
  `sender_longitude` decimal(10,6) DEFAULT NULL COMMENT '发货地址经度',
  `sender_latitude` decimal(10,6) DEFAULT NULL COMMENT '发货地址纬度',
  `receiver_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '收货地址',
  `receiver_longitude` decimal(10,6) DEFAULT NULL COMMENT '收货地址经度',
  `receiver_latitude` decimal(10,6) DEFAULT NULL COMMENT '收货地址纬度',
  `estimated_time` datetime DEFAULT NULL COMMENT '预计送达时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for shipping_tracking
-- ----------------------------
DROP TABLE IF EXISTS `shipping_tracking`;
CREATE TABLE `shipping_tracking` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '物流跟踪ID',
  `order_id` int(11) NOT NULL COMMENT '订单ID',
  `status` tinyint(4) NOT NULL COMMENT '物流状态：1-已发货 2-运输中 3-已送达',
  `location` varchar(255) NOT NULL COMMENT '当前位置描述',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注信息',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流跟踪表';

SET FOREIGN_KEY_CHECKS = 1;
