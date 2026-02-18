-- =====================================================
-- 校园二手交易与共享平台 - 数据库建表SQL
-- 版本：v1.2
-- 生成时间：2026-02-18
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `XianQi` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `XianQi`;

-- =====================================================
-- 1. 用户表 (user)
-- =====================================================
CREATE TABLE `user` (
  `user_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `student_id` VARCHAR(50) DEFAULT NULL COMMENT '学号',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `college` VARCHAR(100) DEFAULT NULL COMMENT '学院',
  `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
  `credit_score` INT UNSIGNED DEFAULT 100 COMMENT '信用分数',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-封禁',
  `is_verified` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否实名认证：0-否 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `idx_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_credit_score` (`credit_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 分类表 (category)
-- =====================================================
CREATE TABLE `category` (
  `category_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
  `icon` VARCHAR(500) DEFAULT NULL COMMENT '分类图标',
  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序，数字越小越靠前',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`category_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- =====================================================
-- 3. 商品表 (product)
-- =====================================================
CREATE TABLE `product` (
  `product_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `seller_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家ID',
  `title` VARCHAR(200) NOT NULL COMMENT '商品标题',
  `description` TEXT COMMENT '商品描述',
  `category_id` INT UNSIGNED NOT NULL COMMENT '分类ID',
  `price` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '价格（元）',
  `original_price` DECIMAL(10,2) UNSIGNED DEFAULT NULL COMMENT '原价（元）',
  `condition_level` TINYINT UNSIGNED NOT NULL DEFAULT 9 COMMENT '成色：1-10，10为全新',
  `cover_image_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '封面图片ID',
  `image_count` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '图片数量',
  `location` VARCHAR(200) DEFAULT NULL COMMENT '交易地点',
  `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-在售 2-已售 3-预订',
  `view_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `favorite_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`product_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_price` (`price`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_location` (`location`(50)),
  FULLTEXT KEY `idx_title_desc` (`title`, `description`),
  CONSTRAINT `fk_product_seller` FOREIGN KEY (`seller_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- =====================================================
-- 4. 商品图片表 (product_image)
-- =====================================================
CREATE TABLE `product_image` (
  `image_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `image_thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
  `image_medium_url` VARCHAR(500) DEFAULT NULL COMMENT '中等尺寸图片URL',
  `sort_order` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序顺序，数字越小越靠前',
  `file_size` INT UNSIGNED DEFAULT NULL COMMENT '文件大小（字节）',
  `width` SMALLINT UNSIGNED DEFAULT NULL COMMENT '图片宽度',
  `height` SMALLINT UNSIGNED DEFAULT NULL COMMENT '图片高度',
  `is_cover` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否为封面：0-否 1-是',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`image_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_is_cover` (`is_cover`),
  KEY `idx_sort_order` (`sort_order`),
  CONSTRAINT `fk_product_image_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

-- =====================================================
-- 5. 商品收藏表 (product_favorite)
-- =====================================================
CREATE TABLE `product_favorite` (
  `favorite_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`favorite_id`),
  UNIQUE KEY `idx_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_favorite_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';

-- =====================================================
-- 6. 浏览历史表 (product_view_history)
-- =====================================================
CREATE TABLE `product_view_history` (
  `history_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `view_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
  `view_duration` INT UNSIGNED DEFAULT NULL COMMENT '浏览时长（秒）',
  PRIMARY KEY (`history_id`),
  KEY `idx_user_product_view` (`user_id`, `product_id`),
  KEY `idx_view_time` (`view_time`),
  CONSTRAINT `fk_view_history_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_view_history_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='浏览历史表';

-- =====================================================
-- 7. 共享物品表 (share_item)
-- =====================================================
CREATE TABLE `share_item` (
  `share_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '共享物品ID',
  `owner_id` BIGINT UNSIGNED NOT NULL COMMENT '所有者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '物品标题',
  `description` TEXT COMMENT '描述',
  `category_id` INT UNSIGNED NOT NULL COMMENT '分类ID',
  `deposit` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '押金（元）',
  `daily_rent` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '日租金（元）',
  `cover_image_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '封面图片ID',
  `image_count` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '图片数量',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-可借用 2-借用中',
  `available_times` JSON DEFAULT NULL COMMENT '可借用时间段，JSON格式',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`share_id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_share_item_owner` FOREIGN KEY (`owner_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='共享物品表';

-- =====================================================
-- 8. 共享物品图片表 (share_item_image)
-- =====================================================
CREATE TABLE `share_item_image` (
  `image_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID',
  `share_id` BIGINT UNSIGNED NOT NULL COMMENT '共享物品ID',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `image_thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
  `image_medium_url` VARCHAR(500) DEFAULT NULL COMMENT '中等尺寸图片URL',
  `sort_order` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `file_size` INT UNSIGNED DEFAULT NULL COMMENT '文件大小（字节）',
  `width` SMALLINT UNSIGNED DEFAULT NULL COMMENT '图片宽度',
  `height` SMALLINT UNSIGNED DEFAULT NULL COMMENT '图片高度',
  `is_cover` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否为封面：0-否 1-是',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`image_id`),
  KEY `idx_share_id` (`share_id`),
  KEY `idx_is_cover` (`is_cover`),
  KEY `idx_sort_order` (`sort_order`),
  CONSTRAINT `fk_share_item_image_share` FOREIGN KEY (`share_id`) REFERENCES `share_item` (`share_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='共享物品图片表';

-- =====================================================
-- 9. 订单表 (order)
-- =====================================================
CREATE TABLE `order` (
  `order_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  `buyer_id` BIGINT UNSIGNED NOT NULL COMMENT '买家ID',
  `seller_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家ID',
  `product_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '商品ID',
  `share_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '共享物品ID',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '类型：1-购买 2-共享',
  `amount` DECIMAL(10,2) UNSIGNED NOT NULL COMMENT '交易金额（元）',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-待确认 1-进行中 2-已完成 3-已取消 4-退款中',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_share_id` (`share_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_order_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_order_seller` FOREIGN KEY (`seller_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_order_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_order_share` FOREIGN KEY (`share_id`) REFERENCES `share_item` (`share_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =====================================================
-- 10. 评价表 (evaluation)
-- =====================================================
CREATE TABLE `evaluation` (
  `eval_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `from_user_id` BIGINT UNSIGNED NOT NULL COMMENT '评价人ID',
  `to_user_id` BIGINT UNSIGNED NOT NULL COMMENT '被评价人ID',
  `score` TINYINT UNSIGNED NOT NULL COMMENT '评分：1-5星',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `tags` JSON DEFAULT NULL COMMENT '标签，JSON数组：如["发货快","描述准确"]',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`eval_id`),
  UNIQUE KEY `idx_order_id` (`order_id`),
  KEY `idx_from_user_id` (`from_user_id`),
  KEY `idx_to_user_id` (`to_user_id`),
  KEY `idx_score` (`score`),
  CONSTRAINT `fk_eval_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_eval_from_user` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_eval_to_user` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- =====================================================
-- 11. 会话表 (conversation)
-- =====================================================
CREATE TABLE `conversation` (
  `conversation_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `conversation_type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '会话类型：1-单聊 2-群聊',
  `user_id_1` BIGINT UNSIGNED NOT NULL COMMENT '用户1ID（单聊时使用）',
  `user_id_2` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户2ID（单聊时使用）',
  `related_order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联订单ID',
  `last_message_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_content` VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息内容（冗余字段）',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
  `unread_count_user1` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户1的未读消息数',
  `unread_count_user2` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户2的未读消息数',
  `is_muted_user1` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户1是否免打扰：0-否 1-是',
  `is_muted_user2` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户2是否免打扰：0-否 1-是',
  `remark_user1` VARCHAR(100) DEFAULT NULL COMMENT '用户1对会话的备注名',
  `remark_user2` VARCHAR(100) DEFAULT NULL COMMENT '用户2对会话的备注名',
  `is_archived_user1` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户1是否归档：0-否 1-是',
  `is_archived_user2` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户2是否归档：0-否 1-是',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-删除 2-置顶',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`conversation_id`),
  KEY `idx_user_id_1` (`user_id_1`),
  KEY `idx_user_id_2` (`user_id_2`),
  KEY `idx_last_message_time` (`last_message_time`),
  KEY `idx_related_order_id` (`related_order_id`),
  CONSTRAINT `fk_conversation_user1` FOREIGN KEY (`user_id_1`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_conversation_user2` FOREIGN KEY (`user_id_2`) REFERENCES `user` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_conversation_order` FOREIGN KEY (`related_order_id`) REFERENCES `order` (`order_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- =====================================================
-- 12. 消息表 (message)
-- =====================================================
CREATE TABLE `message` (
  `message_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `from_user_id` BIGINT UNSIGNED NOT NULL COMMENT '发送者ID',
  `to_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '接收者ID（单聊时使用）',
  `content` TEXT COMMENT '消息内容',
  `type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '类型：1-文本 2-图片 3-商品卡片 4-订单卡片 5-系统通知 6-引用消息',
  `parent_message_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '引用的父消息ID',
  `is_read` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `send_status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '发送状态：0-发送中 1-成功 2-失败',
  `delivered_time` DATETIME DEFAULT NULL COMMENT '送达时间',
  `extra_data` JSON DEFAULT NULL COMMENT '扩展数据，存储卡片详细信息',
  `reply_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '被回复次数',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-正常 1-撤回 2-删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`message_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_from_user_id` (`from_user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_parent_message_id` (`parent_message_id`),
  CONSTRAINT `fk_message_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`conversation_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_message_from_user` FOREIGN KEY (`from_user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_message_to_user` FOREIGN KEY (`to_user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_message_parent` FOREIGN KEY (`parent_message_id`) REFERENCES `message` (`message_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- =====================================================
-- 13. 会话成员表 (conversation_member) - 群聊场景使用
-- =====================================================
CREATE TABLE `conversation_member` (
  `member_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '成员ID',
  `conversation_id` BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '群昵称',
  `unread_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '未读消息数',
  `is_muted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否免打扰：0-否 1-是',
  `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `last_read_message_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '最后阅读的消息ID',
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `idx_conversation_user` (`conversation_id`, `user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_conversation_member_conv` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`conversation_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_conversation_member_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话成员表';

-- =====================================================
-- 14. 黑名单表 (blacklist)
-- =====================================================
CREATE TABLE `blacklist` (
  `blacklist_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `blocked_user_id` BIGINT UNSIGNED NOT NULL COMMENT '被拉黑的用户ID',
  `reason` VARCHAR(200) DEFAULT NULL COMMENT '拉黑原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`blacklist_id`),
  UNIQUE KEY `idx_user_blocked` (`user_id`, `blocked_user_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_blacklist_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_blacklist_blocked_user` FOREIGN KEY (`blocked_user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

-- =====================================================
-- 15. 快捷回复模板表 (quick_reply)
-- =====================================================
CREATE TABLE `quick_reply` (
  `reply_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `user_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户ID，0表示系统预设',
  `title` VARCHAR(100) NOT NULL COMMENT '模板标题',
  `content` VARCHAR(500) NOT NULL COMMENT '回复内容',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类：交易-询问/交易-确认/其他',
  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序',
  `is_system` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否系统预设：0-否 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`reply_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='快捷回复模板表';

-- =====================================================
-- 16. 举报记录表 (report)
-- =====================================================
CREATE TABLE `report` (
  `report_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` BIGINT UNSIGNED NOT NULL COMMENT '举报人ID',
  `reported_user_id` BIGINT UNSIGNED NOT NULL COMMENT '被举报人ID',
  `conversation_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '会话ID',
  `message_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '消息ID',
  `reason` VARCHAR(50) NOT NULL COMMENT '举报原因：欺诈/骚扰/虚假信息/其他',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `evidence_images` JSON DEFAULT NULL COMMENT '证据图片，JSON数组',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理 1-已处理 2-已驳回',
  `admin_note` VARCHAR(500) DEFAULT NULL COMMENT '管理员备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`report_id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_reported_user_id` (`reported_user_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_report_reported_user` FOREIGN KEY (`reported_user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_report_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`conversation_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_report_message` FOREIGN KEY (`message_id`) REFERENCES `message` (`message_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报记录表';

-- =====================================================
-- 17. 系统通知表 (system_notification)
-- =====================================================
CREATE TABLE `system_notification` (
  `notification_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT COMMENT '通知内容，支持富文本',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '通知类型：1-系统公告 2-活动通知 3-账户提醒 4-交易提醒',
  `target_type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '目标类型：1-全部用户 2-指定用户 3-指定等级',
  `target_users` JSON DEFAULT NULL COMMENT '指定用户ID列表，JSON数组',
  `target_level` INT UNSIGNED DEFAULT NULL COMMENT '目标用户等级',
  `is_read` JSON DEFAULT NULL COMMENT '已读用户ID列表，JSON数组',
  `link_type` TINYINT UNSIGNED DEFAULT NULL COMMENT '跳转链接类型：1-无 2-网页 3-商品详情 4-订单详情',
  `link_url` VARCHAR(500) DEFAULT NULL COMMENT '跳转URL',
  `link_product_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联商品ID',
  `link_order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联订单ID',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-已撤回',
  `priority` TINYINT UNSIGNED NOT NULL DEFAULT 2 COMMENT '优先级：1-低 2-中 3-高',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`notification_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知表';

-- =====================================================
-- 18. 操作日志表 (operation_log)
-- =====================================================
CREATE TABLE `operation_log` (
  `log_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '操作用户ID，0表示系统操作',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名（冗余字段）',
  `module` VARCHAR(50) NOT NULL COMMENT '操作模块：user/product/order/share_item/system等',
  `action` VARCHAR(50) NOT NULL COMMENT '操作类型：login/create/update/delete/query/export等',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法：GET/POST/PUT/DELETE',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` JSON DEFAULT NULL COMMENT '请求参数，JSON格式',
  `response_result` JSON DEFAULT NULL COMMENT '响应结果，JSON格式',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
  `execute_time` INT UNSIGNED DEFAULT NULL COMMENT '执行时长（毫秒）',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '执行状态：1-成功 0-失败',
  `error_message` TEXT COMMENT '错误信息（失败时记录）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_action` (`action`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_ip_address` (`ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =====================================================
-- 19. 系统配置表 (system_config)
-- =====================================================
CREATE TABLE `system_config` (
  `config_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键（唯一）',
  `config_value` TEXT COMMENT '配置值',
  `config_type` VARCHAR(20) NOT NULL DEFAULT 'string' COMMENT '配置类型：string/number/boolean/json',
  `value_options` JSON DEFAULT NULL COMMENT '可选值列表，JSON格式',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '配置说明',
  `group_name` VARCHAR(50) NOT NULL COMMENT '分组名称：basic/upload/payment/email/sms等',
  `is_public` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否公开：0-否 1-是（公开的配置前端可访问）',
  `is_system` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否系统配置：0-否 1-是（系统配置不可删除）',
  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `idx_config_key` (`config_key`),
  KEY `idx_group_name` (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- =====================================================
-- 20. 敏感词表 (sensitive_word)
-- =====================================================
CREATE TABLE `sensitive_word` (
  `word_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
  `word` VARCHAR(100) NOT NULL COMMENT '敏感词',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '类型：1-禁止词 2-敏感词 3-替换词',
  `replace_word` VARCHAR(100) DEFAULT NULL COMMENT '替换词（type=3时使用）',
  `level` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '等级：1-一般 2-严重（严重等级直接拦截）',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`word_id`),
  UNIQUE KEY `idx_word` (`word`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- =====================================================
-- 21. 轮播图表 (banner)
-- =====================================================
CREATE TABLE `banner` (
  `banner_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `title` VARCHAR(200) NOT NULL COMMENT '轮播图标题',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `image_thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
  `link_type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '链接类型：1-无 2-外链 3-商品详情 4-功能页面',
  `link_url` VARCHAR(500) DEFAULT NULL COMMENT '跳转URL（外链时使用）',
  `link_product_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联商品ID（link_type=3时使用）',
  `link_page_path` VARCHAR(200) DEFAULT NULL COMMENT '功能页面路径（link_type=4时使用）',
  `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序，数字越小越靠前',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始展示时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束展示时间',
  `click_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点击次数',
  `exposure_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '曝光次数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`banner_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_start_end_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- =====================================================
-- 22. 用户反馈表 (user_feedback)
-- =====================================================
CREATE TABLE `user_feedback` (
  `feedback_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID（可为空，匿名反馈）',
  `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式（手机/邮箱）',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '反馈类型：1-功能建议 2-Bug反馈 3-投诉 4-其他',
  `title` VARCHAR(200) NOT NULL COMMENT '反馈标题',
  `content` TEXT NOT NULL COMMENT '反馈内容',
  `images` JSON DEFAULT NULL COMMENT '图片列表，JSON数组',
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理 1-处理中 2-已处理 3-已驳回',
  `handler_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人ID（管理员）',
  `handle_note` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`feedback_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 初始化系统配置数据
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `group_name`, `is_public`, `is_system`, `sort_order`) VALUES
-- 基础配置
('app_name', '校园易购', 'string', '应用名称', 'basic', 1, 1, 1),
('app_version', '1.0.0', 'string', '应用版本号', 'basic', 1, 1, 2),
('icp_license', '粤ICP备xxxxxxxx号', 'string', 'ICP备案号', 'basic', 1, 1, 3),
('copyright', '© 2026 校园易购', 'string', '版权信息', 'basic', 1, 1, 4),

-- 上传配置
('upload_max_size', '5242880', 'number', '最大上传文件大小(5MB)', 'upload', 1, 0, 1),
('upload_max_count', '9', 'number', '单次最多上传数量', 'upload', 1, 0, 2),
('upload_allowed_types', 'jpg,jpeg,png,webp', 'string', '允许的文件类型', 'upload', 1, 0, 3),

-- 安全配置
('password_min_length', '6', 'number', '密码最小长度', 'security', 1, 1, 1),
('password_max_length', '20', 'number', '密码最大长度', 'security', 1, 1, 2),
('token_expire_time', '604800', 'number', 'Token过期时间(秒,7天)', 'security', 1, 1, 3),
('max_login_attempts', '5', 'number', '最大登录尝试次数', 'security', 1, 1, 4),
('login_lock_time', '1800', 'number', '登录锁定时长(秒,30分钟)', 'security', 1, 1, 5),

-- 业务配置
('product_max_images', '9', 'number', '商品最大图片数', 'business', 1, 0, 1),
('product_title_max_length', '50', 'number', '商品标题最大长度', 'business', 1, 0, 2),
('product_desc_max_length', '2000', 'number', '商品描述最大长度', 'business', 1, 0, 3),
('order_auto_close_time', '1800', 'number', '订单自动关闭时间(秒,30分钟)', 'business', 1, 0, 4),
('order_auto_finish_time', '604800', 'number', '订单自动完成时间(秒,7天)', 'business', 1, 0, 5),
('credit_score_initial', '100', 'number', '初始信用分数', 'business', 1, 0, 6),

-- 内容审核配置
('sensitive_word_enabled', 'true', 'boolean', '是否启用敏感词过滤', 'content', 1, 0, 1),
('sensitive_word_replace', '***', 'string', '敏感词替换字符', 'content', 1, 0, 2),
('product_audit_required', 'false', 'boolean', '商品是否需要审核后发布', 'content', 1, 0, 3);

-- 初始化快捷回复模板（系统预设）
INSERT INTO `quick_reply` (`user_id`, `title`, `content`, `category`, `is_system`, `sort_order`) VALUES
(0, '询问商品', '还在吗？', '交易-询问', 1, 1),
(0, '询问价格', '能便宜点吗？', '交易-询问', 1, 2),
(0, '询问面交', '可以面交吗？', '交易-询问', 1, 3),
(0, '确认交易', '好的，没问题', '交易-确认', 1, 4),
(0, '已下单', '已下单，请尽快发货', '交易-确认', 1, 5),
(0, '收到货', '收到货了，谢谢', '交易-确认', 1, 6),
(0, '委婉拒绝', '不好意思，不需要了', '其他', 1, 7);

-- 初始化分类数据（补充到8条）
INSERT INTO `category` (`name`, `parent_id`, `icon`, `sort_order`, `status`) VALUES
('数码电子', 0, 'digital', 1, 1),
('书籍教材', 0, 'book', 2, 1),
('生活用品', 0, 'life', 3, 1),
('运动装备', 0, 'sports', 4, 1),
('服饰鞋包', 0, 'fashion', 5, 1),
('美妆护肤', 0, 'beauty', 6, 1),
('娱乐影音', 0, 'entertainment', 7, 1),
('其他', 0, 'other', 99, 1);

-- 初始化敏感词数据（更新为实际的敏感词）
INSERT INTO `sensitive_word` (`word`, `type`, `level`, `status`) VALUES
-- 禁止词（类型1，严重程度2）
('欺诈', 1, 2, 1),
('诈骗', 1, 2, 1),
('刷单', 1, 2, 1),
('洗钱', 1, 2, 1),

-- 敏感词（类型2，严重程度1）
('假货', 2, 1, 1),
('骗子', 2, 1, 1),
('高仿', 2, 1, 1),
('二手东', 2, 1, 1),
('破解', 2, 1, 1),
('盗版', 2, 1, 1),

-- 替换词（类型3）
('微信', 3, 1, 1),
('QQ', 3, 1, 1),
('支付宝', 3, 1, 1);

-- 为替换词设置替换内容
UPDATE `sensitive_word` SET `replace_word` = 'wx' WHERE `word` = '微信';
UPDATE `sensitive_word` SET `replace_word` = 'QQ' WHERE `word` = 'QQ';
UPDATE `sensitive_word` SET `replace_word` = '支付宝' WHERE `word` = '支付宝';

-- =====================================================
-- 建表完成
-- 版本：v1.2
-- 更新内容：
-- 1. 为主要业务表添加逻辑删除字段 deleted
-- 2. 补充分类数据到8条
-- 3. 补充敏感词数据（禁止词4条、敏感词6条、替换词3条）
-- 4. user表添加 is_verified 字段
-- =====================================================
