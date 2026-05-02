-- 为货柜表添加图片字段
USE `vending_db`;

ALTER TABLE `cabinet` ADD COLUMN `image_url` VARCHAR(255) DEFAULT NULL COMMENT '货柜图片URL' AFTER `address`;