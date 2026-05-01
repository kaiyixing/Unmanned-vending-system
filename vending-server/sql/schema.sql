-- 无人售货系统数据库脚本
-- 数据库名称：vending_db

CREATE DATABASE IF NOT EXISTS `vending_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `vending_db`;

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码(Bcrypt加密)',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户 1-运营管理员 2-超级管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `product_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
    `category` VARCHAR(50) NOT NULL COMMENT '商品分类',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '价格(元)',
    `cost_price` DECIMAL(10, 2) DEFAULT NULL COMMENT '成本价(元)',
    `image_url` VARCHAR(255) DEFAULT NULL COMMENT '主图URL',
    `images` JSON DEFAULT NULL COMMENT '图片列表(JSON数组)',
    `spec` VARCHAR(255) DEFAULT NULL COMMENT '规格(如: 500ml/瓶)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`product_id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 货柜表
CREATE TABLE IF NOT EXISTS `cabinet` (
    `cabinet_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '货柜ID',
    `cabinet_code` VARCHAR(50) NOT NULL COMMENT '货柜编号(唯一)',
    `name` VARCHAR(100) NOT NULL COMMENT '货柜名称',
    `city` VARCHAR(50) NOT NULL COMMENT '所在城市',
    `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '经度',
    `capacity` INT NOT NULL DEFAULT 50 COMMENT '货柜容量(商品格数)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-营业中 2-维护中',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`cabinet_id`),
    UNIQUE KEY `uk_cabinet_code` (`cabinet_code`),
    KEY `idx_city` (`city`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='货柜表';

-- 库存表
CREATE TABLE IF NOT EXISTS `inventory` (
    `inventory_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存ID',
    `cabinet_id` BIGINT NOT NULL COMMENT '货柜ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '当前库存数量',
    `threshold` INT NOT NULL DEFAULT 5 COMMENT '预警阈值',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`inventory_id`),
    UNIQUE KEY `uk_cabinet_product` (`cabinet_id`, `product_id`),
    KEY `idx_cabinet` (`cabinet_id`),
    KEY `idx_product` (`product_id`),
    KEY `idx_quantity` (`quantity`),
    CONSTRAINT `fk_inventory_cabinet` FOREIGN KEY (`cabinet_id`) REFERENCES `cabinet` (`cabinet_id`),
    CONSTRAINT `fk_inventory_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- 订单主表
CREATE TABLE IF NOT EXISTS `orders` (
    `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号(业务唯一)',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `cabinet_id` BIGINT NOT NULL COMMENT '货柜ID',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待支付 1-已支付 2-已完成 3-已取消 4-退款中 5-已退款',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `pay_channel` VARCHAR(20) DEFAULT NULL COMMENT '支付渠道: wechat/alipay',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`order_id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_cabinet_id` (`cabinet_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`),
    CONSTRAINT `fk_order_cabinet` FOREIGN KEY (`cabinet_id`) REFERENCES `cabinet` (`cabinet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS `order_item` (
    `item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称(快照)',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `unit_price` DECIMAL(10, 2) NOT NULL COMMENT '购买时单价',
    PRIMARY KEY (`item_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`),
    CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS `payment_record` (
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `channel` VARCHAR(20) NOT NULL COMMENT '支付渠道: wechat/alipay/mock',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待支付 1-支付成功 2-支付失败 3-已退款',
    `trade_no` VARCHAR(64) DEFAULT NULL COMMENT '第三方交易流水号',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付成功时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`record_id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_trade_no` (`trade_no`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- 取货码表
CREATE TABLE IF NOT EXISTS `pickup_code` (
    `code_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '取货码ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `code_value` VARCHAR(10) NOT NULL COMMENT '6位数字取货码',
    `qr_code_url` VARCHAR(255) DEFAULT NULL COMMENT '二维码图片URL',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-未使用 1-已使用 2-已过期',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`code_id`),
    UNIQUE KEY `uk_code_value` (`code_value`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`),
    CONSTRAINT `fk_pickup_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='取货码表';

-- 退款表
CREATE TABLE IF NOT EXISTS `refund` (
    `refund_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '退款ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '申请用户ID',
    `reason` VARCHAR(255) NOT NULL COMMENT '退款原因',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '退款金额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-申请中 1-审核通过 2-审核拒绝 3-已退款',
    `audit_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`refund_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_refund_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
    CONSTRAINT `fk_refund_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款表';
