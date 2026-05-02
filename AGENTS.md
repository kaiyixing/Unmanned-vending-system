# AGENTS.md - 无人售货系统

## 项目架构

| 组件 | 路径 | 技术栈 |
|------|------|--------|
| 后端 | `vending-server/` | Spring Boot 3.2.5, Java 17, MyBatis-Plus 3.5.5 |
| 用户端 | `vending-web/` | Vue 3 + Vite + Element Plus + Pinia |
| 管理后台 | `vending-admin/` | Vue 3 + Vite + Element Plus + ECharts |

## 启动命令

### 数据层（Docker）
```powershell
docker-compose up -d
```
- MySQL: `localhost:3306` (vending / vending1234)
- Redis: `localhost:6379`
- MinIO: `localhost:9000` (API), `localhost:9001` (控制台)

### 初始化数据库
```powershell
docker exec -i vending_mysql mysql -u vending -pvending1234 vending_db < vending-server/sql/schema.sql
docker exec -i vending_mysql mysql -u vending -pvending1234 vending_db < vending-server/sql/init_data.sql
```

### 后端
```powershell
cd vending-server
mvn spring-boot:run
```

### 前端（各开终端）
```powershell
# 用户端
cd vending-web && npm install && npm run dev
# 管理后台
cd vending-admin && npm install && npm run dev
```

访问：`http://localhost:5173`（用户端）、`http://localhost:5174`（管理后台）

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 超级管理员 | admin | 123456 |
| 运营管理员 | operator1 | 123456 |
| 普通用户 | user001 | 123456 |

## 关键实现

- **后端包**：`com.vending.module.{user|product|cabinet|inventory|order|payment|pickup|refund|statistics|admin}`
- **公共模块**：`com.vending.common` — Result/ResultCode/异常处理/JWT/CORS
- **安全**：Spring Security + JWT，role字段存数字(0/1/2)，映射在 `JwtAuthenticationFilter.getRoleName()`
- **库存并发**：Redis SETNX 分布式锁 + MySQL 乐观锁

## 前端要点

- 路由使用 `createWebHashHistory()`（Hash 模式，URL 带 `#`）
- 登录后用 `window.location.href = '/#/'` 跳转（router.push 在部分浏览器有缓存问题）
- API封装在 `src/api/request.js`，直接读 localStorage 的 token

## 数据库

9张表：`sys_user`, `product`, `cabinet`, `inventory`, `orders`, `order_item`, `payment_record`, `pickup_code`, `refund`

DDL在 `vending-server/sql/schema.sql`，**执行脚本创建**。

## 订单状态

- 0(待支付) → 1(已支付) → 2(已完成)
- 0(待支付) → 3(已取消) [30分钟超时自动取消]
- 1(已支付) → 4(退款中) → 5(已退款)

## 参考文档

- `开发文档.md` — 代码示例、DDL、部署
- `无人售货系统-完整报告.md` — 学术报告