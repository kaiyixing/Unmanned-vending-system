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

## 环境变量配置

项目支持通过环境变量配置敏感信息，避免硬编码：

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `SERVER_PORT` | 后端服务端口 | 8080 |
| `DB_URL` | 数据库连接 URL | `jdbc:mysql://localhost:3306/vending_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | 数据库用户名 | vending |
| `DB_PASSWORD` | 数据库密码 | vending1234 |
| `REDIS_HOST` | Redis 主机 | localhost |
| `REDIS_PORT` | Redis 端口 | 6379 |
| `REDIS_DATABASE` | Redis 数据库 | 0 |
| `JWT_SECRET` | JWT 密钥 | `vending-system-secret-key-2026-must-be-at-least-256-bits` |
| `JWT_EXPIRATION` | JWT 过期时间（毫秒） | 7200000 |
| `MINIO_ENDPOINT` | MinIO 地址 | `http://localhost:9000` |
| `MINIO_ACCESS_KEY` | MinIO 访问密钥 | minioadmin |
| `MINIO_SECRET_KEY` | MinIO 密钥 | minioadmin1234 |
| `MINIO_BUCKET` | MinIO 存储桶 | vending-images |
| `LOG_LEVEL` | 日志级别 | info |
| `SECURITY_LOG_LEVEL` | 安全日志级别 | info |

### 使用环境变量（Windows）
```powershell
# 临时设置
$env:DB_PASSWORD="your_secure_password"
$env:JWT_SECRET="your_secure_jwt_secret"

# 然后启动服务
mvn spring-boot:run
```

### 使用环境变量（Linux/Mac）
```bash
# 临时设置
export DB_PASSWORD="your_secure_password"
export JWT_SECRET="your_secure_jwt_secret"

# 然后启动服务
mvn spring-boot:run
```

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 超级管理员 | admin | 123456 |
| 运营管理员 | operator1 | 123456 |
| 普通用户 | test01 | 123456 |

## 核心实现

- **后端包**：`com.vending.module.{user,product,cabinet,inventory,order,payment,pickup,refund,statistics}`
- **公共模块**：`com.vending.common` — Result/ResultCode/异常处理/JWT/CORS/Redis缓存
- **安全**：Spring Security + JWT 双Token机制
  - Access Token：短期（2小时）
  - Refresh Token：长期（7天）
  - JWT黑名单机制：支持主动注销Token
  - Token刷新接口：`/api/user/refresh`
  - `role` 存数字(0/1/2)，映射在 `JwtAuthenticationFilter.getRoleName()`
- **权限控制**：
  - `/api/admin/**` → ADMIN/SUPER_ADMIN
  - `/api/inventory/**` → ADMIN/SUPER_ADMIN
  - `/api/refund/admin/**` → ADMIN/SUPER_ADMIN
  - `/api/user/list` → ADMIN/SUPER_ADMIN
- **Redis缓存**：
  - 商品列表缓存：`cache:product:list:`
  - 货柜列表缓存：`cache:cabinet:list:`
  - 货柜商品缓存：`cache:cabinet:products:`
  - JWT黑名单：`jwt:blacklist:`
  - Refresh Token存储：`jwt:refresh:`
  - 缓存清除优化：使用 SCAN 替代 KEYS，分批删除避免阻塞
- **并发控制**：Redis SETNX 分布式锁（10秒超时）+ MySQL 乐观锁（库存扣减和回滚都有锁保护）

## 前端要点

- 路由使用 `createWebHashHistory()`（Hash 模式，URL 带 `#`）
- 登录后使用 Pinia 统一管理状态
- API 在 `src/api/request.js`，统一管理 Token 刷新和错误处理
- 支持自动刷新 Token：Access Token 过期时自动使用 Refresh Token 刷新
- 前端 API 基础地址：`import.meta.env.VITE_API_BASE_URL || '/api'`

## 数据库

9张表：`sys_user`, `product`, `cabinet`, `inventory`, `orders`, `order_item`, `payment_record`, `pickup_code`, `refund`

DDL 在 `vending-server/sql/schema.sql`。

## 订单状态

- 0(待支付) → 1(已支付) → 2(已完成)
- 0(待支付) → 3(已取消) [30分钟超时自动取消]
- 1(已支付) → 4(退款中) → 5(已退款)

## 参考文档

- `开发文档.md` — 代码示例、DDL、部署
- `数据层本地部署指南.md` — 数据库、Redis、MinIO 部署
- `无人售货系统-完整报告.md` — 项目完整报告
