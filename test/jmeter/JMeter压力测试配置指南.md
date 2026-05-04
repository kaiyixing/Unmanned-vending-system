# JMeter 压力测试配置指南

## 一、测试环境配置

| 配置项 | 值 |
|--------|-----|
| 协议 | http |
| 服务器名称或IP | localhost |
| 端口号 | 8080 |

## 二、JWT 双 Token 机制说明

系统采用 JWT 双 Token 机制：
- **Access Token**：短期令牌（2小时），用于 API 访问认证
- **Refresh Token**：长期令牌（7天），用于刷新 Access Token
- 登录接口同时返回两个 Token，后续请求使用 Access Token

---

## 二、测试计划结构

```
测试计划
└── 线程组 (用户端场景)
    ├── HTTP请求默认值
    ├── HTTP Cookie 管理器
    ├── 1. 用户登录
    ├── 2. 查看货柜列表
    ├── 3. 查看货柜商品
    ├── 4. 查看商品详情
    ├── 5. 创建订单
    ├── 6. 支付订单
    ├── 查看结果树
    ├── 汇总报告
    └── 图形结果
```

---

## 三、详细配置

### 1. 线程组配置

| 配置项 | 推荐值 |
|--------|--------|
| 线程数 (用户) | 50 (可调整) |
| Ramp-Up 时间 (秒) | 10 |
| 循环次数 | 10 (或勾选永远) |
| 调度器 | 可选 |

---

### 2. HTTP 请求配置详解

#### 2.1 用户登录

**HTTP 请求**
- 名称：用户登录
- 方法：POST
- 路径：/api/user/login
- 内容编码：UTF-8

**参数 (消息体数据**
```json
{
  "username": "user001",
  "password": "123456"
}
```

**JSON 提取器 (提取 token)**
- 引用名称：token
- JSON Path 表达式：$.data.token
- 匹配号：0

---

#### 2.2 查看货柜列表

**HTTP 请求**
- 名称：查看货柜列表
- 方法：GET
- 路径：/api/cabinet/list
- 参数：
  - page: 1
  - size: 20

**JSON 提取器 (提取第一个货柜ID)**
- 引用名称：cabinetId
- JSON Path 表达式：$.data.records[0].cabinetId
- 匹配号：0

---

#### 2.3 查看货柜商品

**HTTP 请求**
- 名称：查看货柜商品
- 方法：GET
- 路径：/api/cabinet/${cabinetId}/products

**JSON 提取器 (提取第一个商品ID)**
- 引用名称：productId
- JSON Path 表达式：$[0].productId
- 匹配号：0

---

#### 2.4 查看商品详情

**HTTP 请求**
- 名称：查看商品详情
- 方法：GET
- 路径：/api/product/${productId}

---

#### 2.5 创建订单

**HTTP 请求**
- 名称：创建订单
- 方法：POST
- 路径：/api/order/create
- 内容编码：UTF-8

**HTTP 头部信息**
- 名称：Authorization
- 值：Bearer ${accessToken}

**参数 (消息体数据)**
```json
{
  "cabinetId": ${cabinetId},
  "items": [
    {
      "productId": ${productId},
      "quantity": 1
    }
  ]
}
```

**JSON 提取器 (提取订单ID)**
- 引用名称：orderId
- JSON Path 表达式：$.data.orderId
- 匹配号：0

---

#### 2.6 支付订单

**HTTP 请求**
- 名称：支付订单
- 方法：POST
- 路径：/api/order/pay/${orderId}
- 参数：
  - payChannel: mock

---

## 五、监听器配置

### 查看结果树
- 用于调试时开启，查看每个请求的响应

### 汇总报告
- 用于查看整体性能指标：
  - 样本数
  - 平均值 (平均响应时间)
  - 中位数
  - 90% 百分位
  - 最小值
  - 最大值
  - 错误率
  - 吞吐量
  - 接收 KB/秒
  - 发送 KB/秒

### 图形结果
- 用于可视化响应时间趋势

---

## 六、单独接口测试配置

### 测试单个接口也可以单独测试以下接口：

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 商品列表 | GET | /api/product/list | 无需认证 |
| 货柜列表 | GET | /api/cabinet/list | 无需认证 |
| 货柜商品 | GET | /api/cabinet/{id}/products | 无需认证 |
| 用户登录 | POST | /api/user/login | 获取双 Token |
| Token 刷新 | POST | /api/user/refresh | 使用 Refresh Token 刷新 |

---

### 2.7 Token 刷新接口（可选，用于长时间测试）

**HTTP 请求**
- 名称：刷新 Token
- 方法：POST
- 路径：/api/user/refresh
- 内容编码：UTF-8

**参数 (消息体数据)**
```json
{
  "refreshToken": "${refreshToken}"
}
```

**JSON 提取器 (提取新的 accessToken)**
- 引用名称：accessToken
- JSON Path 表达式：$.data.accessToken
- 匹配号：0

---

## 七、测试数据准备

执行以下 SQL 确保测试数据存在：

```sql
-- 确认测试用户存在
INSERT IGNORE INTO `sys_user` (`username`, `password`, `phone`, `role`, `status`) VALUES
('user001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7gH9jK0L', '13900139000', 0, 1);

-- 确认货柜存在
INSERT IGNORE INTO `cabinet` (`cabinet_code`, `name`, `city`, `address`, `image_url`, `latitude`, `longitude`, `capacity`, `status`) VALUES
('CAB001', '科技园A栋货柜', '深圳', '深圳市南山区科技园南路A栋一楼', 'https://placehold.co/400x300/3498db/ffffff?text=Cabinet+A', 22.5329080, 113.9431060, 50, 1);

-- 确认商品存在
INSERT IGNORE INTO `product` (`name`, `description`, `category`, `price`, `cost_price`, `spec`, `image_url`, `status`) VALUES
('可口可乐', '经典口味 330ml', '饮料', 3.50, 2.00, '330ml/罐', 'https://placehold.co/400x400/ff6b6b/ffffff?text=Coca-Cola', 1);

-- 确认库存存在
INSERT IGNORE INTO `inventory` (`cabinet_id`, `product_id`, `quantity`, `threshold`) VALUES
(1, 1, 100, 10);
```

---

## 八、压力测试建议

### 第一阶段：基准测试
- 线程数：10
- 循环次数：20
- 目标：建立性能基准

### 第二阶段：逐步加压
- 线程数：20 → 50 → 100
- 观察：系统表现

### 第三阶段：稳定性测试
- 线程数：50
- 持续时间：10分钟
- 目标：验证系统稳定性

---

## 九、关键性能指标参考

| 指标 | 目标值 |
|------|--------|
| 平均响应时间 | < 500ms |
| 90% 响应时间 | < 1000ms |
| 吞吐量 | > 100 请求/秒 |
| 错误率 | < 1% |

---

## 十、注意事项

1. 确保后端服务已启动
2. 确保 MySQL 和 Redis 已启动
3. 测试前执行数据准备 SQL
4. 先小范围测试，确认无误后再加压
5. 关注服务器资源使用情况 (CPU、内存、IO)
6. Redis 用于缓存热点数据和存储 Token 信息
7. 长时间测试建议定期刷新 Token（可选，通过后置处理器实现）
