-- 聊天会话表
CREATE TABLE `chat_session` (
  `id` varchar(36) NOT NULL COMMENT '会话ID',
  `customer_id` bigint(20) NOT NULL COMMENT '客户ID',
  `admin_id` bigint(20) DEFAULT NULL COMMENT '管理员ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `last_update_time` datetime NOT NULL COMMENT '最后更新时间',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态（0进行中/1已结束）',
  `title` varchar(100) DEFAULT NULL COMMENT '会话标题',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE `chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `content` text NOT NULL COMMENT '消息内容',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `sender_type` varchar(20) NOT NULL COMMENT '发送者类型（customer用户/admin管理员）',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `receiver_type` varchar(20) NOT NULL COMMENT '接收者类型（customer用户/admin管理员）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态（0未读/1已读）',
  `session_id` varchar(36) NOT NULL COMMENT '会话ID',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_sender` (`sender_id`,`sender_type`),
  KEY `idx_receiver` (`receiver_id`,`receiver_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表'; 