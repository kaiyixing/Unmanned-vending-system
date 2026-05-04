# AGENTS.md - 无人售货系统

## 项目架构

| 组件 | 路径 | 技术栈 |
|------|------|--------|
| 后端 | `vending-server/` | Spring Boot 3.2.5, Java 17, MyBatis-Plus 3.5.5 |
| 用户端 | `vending-web/` | Vue 3 + Vite + Element Plus + Pinia, 端口 5173 |
| 管理后台 | `vending-admin/` | Vue 3 + Vite + Element Plus + ECharts, 端口 5174 |

## 启动命令

### 1. 数据层（Docker）
```powershell
docker-compose up -d
```
- MySQL: `localhost:3306` (vending / vending1234)
- Redis: `localhost:6379`
- MinIO: `localhost:9000` (API), `localhost:9001` (控制台)

### 2. 初始化数据库
```powershell
docker exec -i vending_mysql mysql -u vending -pvending1234 vending_db < vending-server/sql/schema.sql
docker exec -i vending_mysql mysql -u vending -pvending1234 vending_db < vending-server/sql/init_data.sql
```

### 3. 启动服务
```powershell
# 后端
cd vending-server && mvn spring-boot:run

# 前端（各开终端）
cd vending-web && npm run dev
cd vending-admin && npm run dev
```

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 超级管理员 | admin | 123456 |
| 运营管理员 | operator1 | 123456 |
| 普通用户 | user001 | 123456 |

## 核心实现

- **后端包**：`com.vending.module.{user,product,cabinet,inventory,order,payment,pickup,refund,statistics}`
- **公共模块**：`com.vending.common` — Result/ResultCode/异常处理/JWT/CORS/Redis缓存
- **安全**：Spring Security + JWT 双Token机制
  - Access Token：短期（2小时）
  - Refresh Token：长期（7天）
  - JWT黑名单机制：支持主动注销Token
  - Token刷新接口：`/api/user/refresh`
  - `role` 存数字(0/1/2)，映射在 `JwtAuthenticationFilter.getRoleName()`
- **Redis缓存**：
  - 商品列表缓存：`cache:product:list:`
  - 货柜列表缓存：`cache:cabinet:list:`
  - 货柜商品缓存：`cache:cabinet:products:`
  - JWT黑名单：`jwt:blacklist:`
  - Refresh Token存储：`jwt:refresh:`
- **并发**：Redis SETNX 分布式锁 + MySQL 乐观锁

## 前端要点

- 路由使用 `createWebHashHistory()`（Hash 模式，URL 带 `#`）
- 登录后用 `window.location.href = '/#/'` 跳转
- API 在 `src/api/request.js`，直接读 localStorage 的 token

## 数据库

9张表：`sys_user`, `product`, `cabinet`, `inventory`, `orders`, `order_item`, `payment_record`, `pickup_code`, `refund`

DDL 在 `vending-server/sql/schema.sql`。

## 订单状态

- 0(待支付) → 1(已支付) → 2(已完成)
- 0(待支付) → 3(已取消) [30分钟超时自动取消]
- 1(已支付) → 4(退款中) → 5(已退款)

## 参考文档

- `开发文档.md` — 代码示例、DDL、部署
- `无人售货系统-完整报告.md` — 学术报告