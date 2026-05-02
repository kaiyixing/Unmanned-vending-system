-- 无人售货系统初始化数据
-- 使用前请先执行 schema.sql 创建表结构

USE `vending_db`;

-- 插入测试用户
-- 注意：密码为 123456 的 Bcrypt 加密哈希
-- 如果登录失败，请使用以下代码生成新的哈希：
--   System.out.println(new BCryptPasswordEncoder().encode("123456"));
INSERT INTO `sys_user` (`username`, `password`, `phone`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7GAt6OSaW', '13800138000', 2, 1),
('operator1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7GAt6OSaW', '13800138001', 1, 1),
('user001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7GAt6OSaW', '13900139000', 0, 1);

-- 插入测试货柜
INSERT INTO `cabinet` (`cabinet_code`, `name`, `city`, `address`, `image_url`, `latitude`, `longitude`, `capacity`, `status`) VALUES
('CAB001', '科技园A栋货柜', '深圳', '深圳市南山区科技园南路A栋一楼', 'https://placehold.co/400x300/3498db/ffffff?text=Cabinet+A', 22.5329080, 113.9431060, 50, 1),
('CAB002', '科技园B栋货柜', '深圳', '深圳市南山区科技园南路B栋一楼', 'https://placehold.co/400x300/2ecc71/ffffff?text=Cabinet+B', 22.5331080, 113.9441060, 40, 1),
('CAB003', '大学城货柜', '深圳', '深圳市南山区大学城C区', 'https://placehold.co/400x300/9b59b6/ffffff?text=Cabinet+C', 22.5900080, 113.9701060, 60, 1);

-- 插入测试商品
INSERT INTO `product` (`name`, `description`, `category`, `price`, `cost_price`, `spec`, `image_url`, `status`) VALUES
('可口可乐', '经典口味 330ml', '饮料', 3.50, 2.00, '330ml/罐', 'https://placehold.co/400x400/ff6b6b/ffffff?text=Coca-Cola', 1),
('百事可乐', '经典口味 330ml', '饮料', 3.50, 2.00, '330ml/罐', 'https://placehold.co/400x400/3498db/ffffff?text=Pepsi', 1),
('农夫山泉', '天然饮用水', '饮料', 2.00, 1.00, '550ml/瓶', 'https://placehold.co/400x400/2ecc71/ffffff?text=Water', 1),
('康师傅红烧牛肉面', '经典方便面', '零食', 5.00, 3.00, '105g/袋', 'https://placehold.co/400x400/f39c12/ffffff?text=Noodles', 1),
('乐事薯片', '原味薯片', '零食', 7.50, 4.50, '70g/袋', 'https://placehold.co/400x400/9b59b6/ffffff?text=Chips', 1),
('卫龙辣条', '经典辣条', '零食', 1.50, 0.80, '65g/袋', 'https://placehold.co/400x400/e74c3c/ffffff?text=Spicy', 1);

-- 插入测试库存
INSERT INTO `inventory` (`cabinet_id`, `product_id`, `quantity`, `threshold`) VALUES
(1, 1, 20, 5), (1, 2, 15, 5), (1, 3, 30, 10), (1, 4, 10, 3), (1, 5, 8, 3), (1, 6, 25, 5),
(2, 1, 18, 5), (2, 3, 25, 10), (2, 4, 12, 3), (2, 5, 6, 3),
(3, 1, 30, 10), (3, 2, 25, 10), (3, 3, 50, 15), (3, 4, 20, 5), (3, 5, 15, 5), (3, 6, 40, 10);
