-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `user_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(20),
    `email` VARCHAR(100),
    `real_name` VARCHAR(50),
    `avatar` VARCHAR(255),
    `role` INT DEFAULT 0,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `product_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `category` VARCHAR(50),
    `price` DECIMAL(10,2) NOT NULL,
    `cost_price` DECIMAL(10,2),
    `image_url` VARCHAR(255),
    `images` TEXT,
    `spec` VARCHAR(100),
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 货柜表
CREATE TABLE IF NOT EXISTS `cabinet` (
    `cabinet_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `cabinet_code` VARCHAR(50) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `city` VARCHAR(50),
    `address` VARCHAR(255),
    `image_url` VARCHAR(255),
    `latitude` DECIMAL(10,6),
    `longitude` DECIMAL(10,6),
    `capacity` INT DEFAULT 0,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 库存表
CREATE TABLE IF NOT EXISTS `inventory` (
    `inventory_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `cabinet_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `order_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(50) NOT NULL UNIQUE,
    `user_id` BIGINT,
    `cabinet_id` BIGINT,
    `total_amount` DECIMAL(10,2) NOT NULL,
    `status` INT DEFAULT 0,
    `payment_time` DATETIME,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 订单项表
CREATE TABLE IF NOT EXISTS `order_item` (
    `order_item_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `product_name` VARCHAR(100),
    `price` DECIMAL(10,2),
    `quantity` INT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 支付记录表
CREATE TABLE IF NOT EXISTS `payment_record` (
    `payment_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `payment_method` VARCHAR(20),
    `amount` DECIMAL(10,2),
    `status` INT DEFAULT 0,
    `transaction_id` VARCHAR(100),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 取货码表
CREATE TABLE IF NOT EXISTS `pickup_code` (
    `pickup_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `pickup_code` VARCHAR(20) NOT NULL UNIQUE,
    `status` INT DEFAULT 0,
    `expire_time` DATETIME,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 退款表
CREATE TABLE IF NOT EXISTS `refund` (
    `refund_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `refund_amount` DECIMAL(10,2),
    `reason` TEXT,
    `status` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);
