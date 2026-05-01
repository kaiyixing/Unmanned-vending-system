# AGENTS.md - 无人售货系统 (Vending System)

## 项目概览

纯软件无人售货管理平台（软件工程结课作业），采用**单体架构**。无硬件/IoT集成。

| 组件 | 路径 | 技术栈 |
|------|------|--------|
| 后端 | `vending-server/` | Spring Boot 3.2.5, Java 17, MyBatis-Plus 3.5.5 |
| 用户端 | `vending-web/` | Vue 3 + Vite + Element Plus + Pinia |
| 管理后台 | `vending-admin/` | Vue 3 + Vite + Element Plus + ECharts |
| 数据库 | `vending-server/sql/` | MySQL 8.0 + Redis 6.2 + MinIO |

## 关键命令

### 启动数据层（Docker）
```powershell
docker-compose up -d
```
连接信息：
- MySQL: `localhost:3306` (用户: vending, 密码: vending1234)
- Redis: `localhost:6379` (无密码)
- MinIO: `localhost:9000` (API), `localhost:9001` (控制台)
- MinIO默认账号密码: minioadmin / minioadmin1234

### 初始化数据库
```powershell
docker exec -i vending_mysql mysql -u vending -pvending1234 vending_db < vending-server/sql/schema.sql
docker exec -i vending_mysql mysql -u vending -pvending1234 vending_db < vending-server/sql/init_data.sql
```

### 启动后端
```powershell
cd vending-server
mvn spring-boot:run
# 或直接在 IDEA 中运行 VendingApplication.java
```

### 启动前端
```powershell
# 用户端
cd vending-web
npm install
npm run dev

# 管理后台（需另开终端）
cd vending-admin
npm install
npm run dev
```

访问：`http://localhost:5173`（用户端）、`http://localhost:5174`（管理后台）

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 超级管理员 | admin | 123456 | 拥有所有权限 |
| 运营管理员 | operator1 | 123456 | 可管理商品、订单、库存 |
| 普通用户 | user001 | 123456 | 消费者账号 |

## 架构要点

- **后端包结构**：`com.vending.module.{user|product|cabinet|inventory|order|payment|pickup|refund|statistics|admin}`
- **公共模块**：`com.vending.common` — Result/ResultCode/异常处理/JWT/CORS/MyBatis配置
- **安全**：Spring Security + JWT，role字段存储数字(0/1/2)，在 `JwtAuthenticationFilter.getRoleName()` 中映射
- **库存并发**：Redis `SETNX` 分布式锁 + MySQL 乐观扣减

## 前端注意事项

### 路由模式
- 使用 `createWebHashHistory()`（Hash 模式），URL 带 `#`，如 `http://localhost:5173/#/`
- 路由守卫在 `router/index.js` 中实现，未登录自动跳转登录页

### 登录跳转
- 登录成功后使用 `window.location.href = '/#/'` 强制跳转，比 `router.push()` 更可靠
- 原因：部分浏览器（如Chrome）因缓存问题导致 router.push 不生效

### API请求封装
- `src/api/request.js` 是 Axios 封装，直接从 localStorage 获取 token（避免 Pinia 循环依赖）
- 所有 API 模块（`user.js`、`order.js` 等）都基于 request.js 封装

### 状态管理
- `src/stores/user.js` — 用户信息（token、用户资料）
- `src/stores/cart.js` — 购物车（items、cabinetId、cabinetName）
- **注意**：`cartStore.cabinetId` 是 ref，访问时需使用 `.value`

### 图标使用
- 避免使用 `@element-plus/icons-vue` 中可能不存在的图标
- 推荐使用 Emoji 代替，或使用内置的通用图标如 `User`、`ShoppingBag` 等

## 数据库

9张表：`sys_user`, `product`, `cabinet`, `inventory`, `orders`, `order_item`, `payment_record`, `pickup_code`, `refund`

DDL 在 `vending-server/sql/schema.sql`，**执行脚本创建表，不要手动创建**。

## 业务流程

### 用户端购物流程
1. 登录/注册 → 首页浏览货柜 → 选择货柜查看商品 → 加入购物车 → 结算支付 → 获取取货码

### 订单状态流转
- 0(待支付) → 1(已支付) → 2(已完成)
- 0(待支付) → 3(已取消) [超时30分钟自动取消]
- 1(已支付) → 4(退款中) → 5(已退款)

### 管理后台功能
- 商品管理：增删改查、上下架
- 货柜管理：增删改查、状态管理
- 库存管理：库存调整、预警查看
- 订单管理：订单查询、退款审核
- 数据统计：销售报表、热销排行

## 设计文档

- `开发文档.md` — 开发指导（含完整代码示例、DDL、部署）
- `无人售货系统-完整报告.md` — 学术报告格式
