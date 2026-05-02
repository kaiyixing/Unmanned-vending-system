-- 插入测试订单数据

USE `vending_db`;

-- 插入测试订单
INSERT INTO `orders` (`order_no`, `user_id`, `cabinet_id`, `total_amount`, `status`, `pay_time`) VALUES
('ORD001', 3, 1, 10.50, 2, NOW() - INTERVAL 1 HOUR),
('ORD002', 3, 1, 7.00, 2, NOW() - INTERVAL 2 HOUR),
('ORD003', 3, 2, 12.50, 2, NOW() - INTERVAL 3 HOUR),
('ORD004', 3, 1, 5.50, 2, NOW() - INTERVAL 4 HOUR),
('ORD005', 3, 3, 8.00, 2, NOW() - INTERVAL 5 HOUR);

-- 插入订单项
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `quantity`, `unit_price`) VALUES
-- ORD001: 可口可乐 + 农夫山泉
(1, 1, '可口可乐', 2, 3.50),
(1, 3, '农夫山泉', 1, 2.00),
-- ORD002: 百事可乐 + 卫龙辣条
(2, 2, '百事可乐', 1, 3.50),
(2, 6, '卫龙辣条', 2, 1.50),
-- ORD003: 康师傅红烧牛肉面 + 乐事薯片
(3, 4, '康师傅红烧牛肉面', 1, 5.00),
(3, 5, '乐事薯片', 1, 7.50),
-- ORD004: 农夫山泉 + 卫龙辣条
(4, 3, '农夫山泉', 2, 2.00),
(4, 6, '卫龙辣条', 1, 1.50),
-- ORD005: 可口可乐 + 乐事薯片
(5, 1, '可口可乐', 1, 3.50),
(5, 5, '乐事薯片', 1, 7.50);
