-- ========================================
-- 批量生成测试数据 - 商品、货柜、库存
-- ========================================

-- ========================================
-- 1. 批量插入商品数据
-- ========================================
INSERT INTO product (name, description, category, price, cost_price, spec, image_url, status, create_time, update_time) VALUES
('可口可乐', '经典口味330ml罐装', '饮料', 3.50, 1.80, '330ml/罐', 'https://placehold.co/400x400/ff6b6b/ffffff?text=Coca-Cola', 1, NOW(), NOW()),
('百事可乐', '经典口味330ml罐装', '饮料', 3.50, 1.80, '330ml/罐', 'https://placehold.co/400x400/3498db/ffffff?text=Pepsi', 1, NOW(), NOW()),
('农夫山泉', '天然矿泉水550ml', '饮料', 2.00, 0.90, '550ml/瓶', 'https://placehold.co/400x400/2ecc71/ffffff?text=MineralWater', 1, NOW(), NOW()),
('康师傅冰红茶', '柠檬味冰红茶500ml', '饮料', 3.00, 1.50, '500ml/瓶', 'https://placehold.co/400x400/f39c12/ffffff?text=IceTea', 1, NOW(), NOW()),
('红牛', '维生素功能饮料250ml', '饮料', 6.00, 3.00, '250ml/罐', 'https://placehold.co/400x400/e74c3c/ffffff?text=RedBull', 1, NOW(), NOW()),
('乐事薯片', '原味薯片70g', '零食', 7.50, 4.00, '70g/袋', 'https://placehold.co/400x400/9b59b6/ffffff?text=Chips', 1, NOW(), NOW()),
('乐事薯片', '番茄味薯片70g', '零食', 7.50, 4.00, '70g/袋', 'https://placehold.co/400x400/e74c3c/ffffff?text=TomatoChips', 1, NOW(), NOW()),
('乐事薯片', '黄瓜味薯片70g', '零食', 7.50, 4.00, '70g/袋', 'https://placehold.co/400x400/2ecc71/ffffff?text=CucumberChips', 1, NOW(), NOW()),
('奥利奥', '巧克力夹心饼干116g', '零食', 8.50, 4.50, '116g/盒', 'https://placehold.co/400x400/1abc9c/ffffff?text=Oreo', 1, NOW(), NOW()),
('旺仔牛奶', '复原乳125ml×4盒', '零食', 12.00, 6.00, '125ml×4盒', 'https://placehold.co/400x400/e67e22/ffffff?text=Wangzai', 1, NOW(), NOW()),
('三只松鼠', '坚果礼盒装', '零食', 29.90, 15.00, '500g/盒', 'https://placehold.co/400x400/d35400/ffffff?text=Snacks', 1, NOW(), NOW()),
('士力架', '花生夹心巧克力51g', '零食', 4.00, 2.00, '51g/条', 'https://placehold.co/400x400/8e44ad/ffffff?text=Snickers', 1, NOW(), NOW()),
('德芙巧克力', '丝滑牛奶巧克力43g', '零食', 9.90, 5.00, '43g/块', 'https://placehold.co/400x400/795548/ffffff?text=Dove', 1, NOW(), NOW()),
('康师傅红烧牛肉面', '经典方便面105g', '零食', 5.00, 2.50, '105g/袋', 'https://placehold.co/400x400/ff9800/ffffff?text=Noodle', 1, NOW(), NOW()),
('统一老坛酸菜面', '酸菜牛肉面120g', '零食', 5.50, 2.80, '120g/袋', 'https://placehold.co/400x400/673ab7/ffffff?text=PickleNoodle', 1, NOW(), NOW()),
('卫龙大面筋', '辣条106g', '零食', 3.50, 1.80, '106g/袋', 'https://placehold.co/400x400/f44336/ffffff?text=Weilong', 1, NOW(), NOW()),
('口香糖', '绿箭无糖薄荷糖35粒', '零食', 10.00, 5.00, '35粒/盒', 'https://placehold.co/400x400/4caf50/ffffff?text=Gum', 1, NOW(), NOW()),
('益达', '无糖口香糖56g', '零食', 9.00, 4.50, '56g/瓶', 'https://placehold.co/400x400/00bcd4/ffffff?text=Extra', 1, NOW(), NOW()),
('咖啡', '星巴克拿铁咖啡270ml', '饮料', 15.00, 7.50, '270ml/罐', 'https://placehold.co/400x400/795548/ffffff?text=Coffee', 1, NOW(), NOW()),
('鲜橙多', '橙汁饮料450ml', '饮料', 3.50, 1.80, '450ml/瓶', 'https://placehold.co/400x400/ff5722/ffffff?text=OrangeJuice', 1, NOW(), NOW()),
('王老吉', '凉茶310ml', '饮料', 4.00, 2.00, '310ml/罐', 'https://placehold.co/400x400/f44336/ffffff?text=Wanglaoji', 1, NOW(), NOW()),
('脉动', '维生素饮料600ml', '饮料', 5.00, 2.50, '600ml/瓶', 'https://placehold.co/400x400/2196f3/ffffff?text=Mizone', 1, NOW(), NOW()),
('雪碧', '柠檬味汽水500ml', '饮料', 3.50, 1.80, '500ml/瓶', 'https://placehold.co/400x400/00e676/ffffff?text=Sprite', 1, NOW(), NOW()),
('七喜', '柠檬味汽水500ml', '饮料', 3.50, 1.80, '500ml/瓶', 'https://placehold.co/400x400/76ff03/ffffff?text=7Up', 1, NOW(), NOW()),
('美年达', '橙味汽水500ml', '饮料', 3.50, 1.80, '500ml/瓶', 'https://placehold.co/400x400/ff6d00/ffffff?text=Mirinda', 1, NOW(), NOW()),
('康师傅方便面', '香辣牛肉面105g', '零食', 5.00, 2.50, '105g/袋', 'https://placehold.co/400x400/ff5252/ffffff?text=SpicyNoodle', 1, NOW(), NOW()),
('统一方便面', '藤椒牛肉面110g', '零食', 5.50, 2.80, '110g/袋', 'https://placehold.co/400x400/18ffff/ffffff?text=PepperNoodle', 1, NOW(), NOW()),
('旺旺雪饼', '雪饼84g', '零食', 6.00, 3.00, '84g/袋', 'https://placehold.co/400x400/ffe082/ffffff?text=SnowCake', 1, NOW(), NOW()),
('旺旺仙贝', '仙贝56g', '零食', 5.00, 2.50, '56g/袋', 'https://placehold.co/400x400/ffecb3/ffffff?text=Xianbei', 1, NOW(), NOW()),
('奥利奥巧心结', '巧克力味威化饼72g', '零食', 8.00, 4.00, '72g/盒', 'https://placehold.co/400x400/ffcc80/ffffff?text=OreoWaffle', 1, NOW(), NOW());

-- ========================================
-- 2. 批量插入货柜数据
-- ========================================
INSERT INTO cabinet (cabinet_code, name, city, address, image_url, latitude, longitude, capacity, status, create_time, update_time) VALUES
('CAB001', '科技园A栋1楼货柜', '深圳', '深圳市南山区科技园南路A栋1楼大厅', 'https://placehold.co/400x300/3498db/ffffff?text=Cabinet-A', 22.532908, 113.943106, 60, 1, NOW(), NOW()),
('CAB002', '科技园B栋2楼货柜', '深圳', '深圳市南山区科技园南路B栋2楼电梯口', 'https://placehold.co/400x300/2ecc71/ffffff?text=Cabinet-B', 22.533108, 113.944106, 50, 1, NOW(), NOW()),
('CAB003', '大学城图书馆货柜', '深圳', '深圳市南山区大学城图书馆一楼', 'https://placehold.co/400x300/9b59b6/ffffff?text=Cabinet-C', 22.590008, 113.970106, 50, 1, NOW(), NOW()),
('CAB004', '海岸城购物广场货柜', '深圳', '深圳市南山区海岸城购物广场负一楼', 'https://placehold.co/400x300/e74c3c/ffffff?text=Cabinet-D', 22.523456, 113.956789, 40, 1, NOW(), NOW()),
('CAB005', '科技园C栋货柜', '深圳', '深圳市南山区科技园C栋1楼大厅', 'https://placehold.co/400x300/1abc9c/ffffff?text=Cabinet-E', 22.534567, 113.945678, 60, 1, NOW(), NOW()),
('CAB006', '坂田华为基地货柜', '深圳', '深圳市龙岗区坂田华为基地A区', 'https://placehold.co/400x300/f39c12/ffffff?text=Cabinet-F', 22.634567, 114.067890, 50, 1, NOW(), NOW()),
('CAB007', '福田CBD中心货柜', '深圳', '深圳市福田区CBD中心商务大厦1楼', 'https://placehold.co/400x300/8e44ad/ffffff?text=Cabinet-G', 22.543210, 114.054321, 40, 1, NOW(), NOW()),
('CAB008', '罗湖火车站货柜', '深圳', '深圳市罗湖区深圳火车站候车室', 'https://placehold.co/400x300/e67e22/ffffff?text=Cabinet-H', 22.535342, 114.112345, 40, 1, NOW(), NOW());

-- ========================================
-- 3. 批量为每个货柜添加商品库存
-- ========================================
-- 先获取货柜和商品的ID（假设商品ID从1开始，货柜ID从1开始）

-- 货柜1 (CAB001) - 全量商品
INSERT INTO inventory (cabinet_id, product_id, quantity, threshold) VALUES
(1, 1, 50, 10), (1, 2, 45, 10), (1, 3, 80, 20), (1, 4, 40, 10),
(1, 5, 30, 8), (1, 6, 25, 8), (1, 7, 20, 8), (1, 8, 20, 8),
(1, 9, 18, 5), (1, 10, 15, 5), (1, 11, 10, 3), (1, 12, 35, 10),
(1, 13, 20, 5), (1, 14, 30, 10), (1, 15, 28, 10), (1, 16, 45, 15),
(1, 17, 20, 5), (1, 18, 15, 5), (1, 19, 25, 8), (1, 20, 30, 10),
(1, 21, 35, 10), (1, 22, 45, 10), (1, 23, 40, 10), (1, 24, 40, 10),
(1, 25, 30, 10), (1, 26, 25, 8), (1, 27, 25, 8), (1, 28, 20, 5),
(1, 29, 20, 5), (1, 30, 20, 5);

-- 货柜2 (CAB002) - 精选商品
INSERT INTO inventory (cabinet_id, product_id, quantity, threshold) VALUES
(2, 1, 40, 10), (2, 2, 35, 10), (2, 3, 70, 20), (2, 4, 30, 10),
(2, 5, 25, 8), (2, 6, 20, 8), (2, 9, 15, 5), (2, 12, 28, 10),
(2, 14, 25, 10), (2, 15, 22, 10), (2, 16, 35, 15), (2, 20, 25, 10),
(2, 21, 30, 10), (2, 22, 35, 10), (2, 25, 25, 10);

-- 货柜3 (CAB003) - 大学城热销品（饮料零食为主）
INSERT INTO inventory (cabinet_id, product_id, quantity, threshold) VALUES
(3, 1, 60, 15), (3, 2, 55, 15), (3, 3, 100, 25), (3, 4, 50, 15),
(3, 5, 40, 10), (3, 6, 30, 10), (3, 7, 30, 10), (3, 8, 30, 10),
(3, 9, 25, 8), (3, 10, 20, 8), (3, 12, 40, 12), (3, 13, 25, 8),
(3, 14, 35, 12), (3, 15, 32, 12), (3, 16, 50, 18), (3, 19, 30, 10),
(3, 20, 35, 12), (3, 21, 40, 12), (3, 22, 50, 15), (3, 25, 30, 10);

-- 货柜4 (CAB004) - 购物广场热销
INSERT INTO inventory (cabinet_id, product_id, quantity, threshold) VALUES
(4, 1, 35, 10), (4, 2, 30, 10), (4, 3, 60, 18), (4, 4, 28, 10),
(4, 5, 22, 8), (4, 6, 18, 8), (4, 7, 18, 8), (4, 8, 18, 8),
(4, 9, 15, 5), (4, 10, 12, 5), (4, 11, 8, 3), (4, 12, 25, 8),
(4, 13, 15, 5), (4, 14, 22, 8), (4, 15, 20, 8), (4, 16, 30, 10),
(4, 19, 20, 8), (4, 20, 25, 10), (4, 21, 28, 10), (4, 25, 22, 8);

-- 货柜5 (CAB005) - 全量商品
INSERT INTO inventory (cabinet_id, product_id, quantity, threshold) VALUES
(5, 1, 45, 12), (5, 2, 42, 12), (5, 3, 75, 20), (5, 4, 38, 10),
(5, 5, 28, 8), (5, 6, 22, 8), (5, 7, 22, 8), (5, 8, 22, 8),
(5, 9, 18, 5), (5, 10, 15, 5), (5, 11, 10, 3), (5, 12, 32, 10),
(5, 13, 20, 5), (5, 14, 28, 10), (5, 15, 26, 10), (5, 16, 40, 15),
(5, 17, 20, 5), (5, 18, 15, 5), (5, 19, 23, 8), (5, 20, 28, 10),
(5, 21, 32, 10), (5, 22, 42, 12), (5, 23, 38, 10), (5, 24, 38, 10),
(5, 25, 28, 10), (5, 26, 23, 8), (5, 27, 23, 8), (5, 28, 18, 5),
(5, 29, 18, 5), (5, 30, 18, 5);

-- 货柜6 (CAB006) - 华为基地热销
INSERT INTO inventory (cabinet_id, product_id, quantity, threshold) VALUES
(6, 1, 55, 15), (6, 2, 50, 15), (6, 3, 90, 25), (6, 4, 45, 15),
(6, 5, 40, 12), (6, 6, 30, 10), (6, 7, 30, 10), (6, 8, 30, 10),
(6, 9, 22, 8), (6, 12, 40, 12), (6, 13, 25, 8), (6, 14, 32, 12),
(6, 16, 45, 15), (6, 19, 28, 10), (6, 20, 35, 12), (6, 21, 38, 12),
(6, 22, 45, 15), (6, 25, 32, 12);

-- 货柜7 (CAB007) - CBD商务区
INSERT INTO inventory (cabinet_id, product_id, quantity, threshold) VALUES
(7, 1, 30, 10), (7, 2, 28, 10), (7, 3, 50, 15), (7, 4, 25, 8),
(7, 5, 35, 10), (7, 6, 18, 6), (7, 9, 15, 5), (7, 10, 10, 3),
(7, 11, 10, 3), (7, 12, 25, 8), (7, 13, 20, 5), (7, 19, 20, 8),
(7, 20, 22, 8), (7, 21, 25, 8), (7, 22, 28, 10);

-- 货柜8 (CAB008) - 火车站补货
INSERT INTO inventory (cabinet_id, product_id, quantity, threshold) VALUES
(8, 1, 65, 20), (8, 2, 60, 20), (8, 3, 100, 30), (8, 4, 50, 15),
(8, 5, 45, 15), (8, 6, 35, 12), (8, 7, 35, 12), (8, 8, 35, 12),
(8, 9, 28, 10), (8, 10, 20, 8), (8, 11, 15, 5), (8, 12, 45, 15),
(8, 13, 25, 8), (8, 14, 35, 12), (8, 15, 32, 12), (8, 16, 55, 18),
(8, 19, 32, 12), (8, 20, 40, 15), (8, 21, 45, 15), (8, 22, 55, 18),
(8, 25, 35, 12), (8, 26, 30, 10), (8, 27, 30, 10);

-- ========================================
-- 数据生成完成
-- ========================================
SELECT '数据批量生成完成！' AS message;
SELECT '商品数量:' AS type, COUNT(*) AS count FROM product
UNION ALL
SELECT '货柜数量:', COUNT(*) FROM cabinet
UNION ALL
SELECT '库存记录数:', COUNT(*) FROM inventory;
