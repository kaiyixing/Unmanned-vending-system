# AGENTS.md - 无人售货系统 (Vending System)

## 项目概览

纯软件无人售货管理平台（软件工程结课作业），采用**单体架构**。无硬件/IoT集成。

| 组件 | 路径 | 技术栈 |
|------|------|--------|
| 后端 | `vending-server/` | Spring Boot 3.2.5, Java 17, MyBatis-Plus 3.5.5 |
| 前端演示 | `vending-demo/` | 原生 HTML/CSS/JS (3个文件) |
| 数据库 | `vending-server/sql/` | MySQL 8.0 + Redis 6.2 + MinIO |
| 设计文档 | `开发文档.md`, `无人售货系统-完整报告.md` | — |

## 关键命令

### 启动数据层（Docker）
```powershell
docker-compose up -d
```
连接信息：MySQL `localhost:3306` (vending/vending1234), Redis `localhost:6379`, MinIO `localhost:9000` / `localhost:9001`

### 初始化数据库
```powershell
docker exec -i vending_mysql mysql -u root -proot1234 vending_db < vending-server/sql/schema.sql
docker exec -i vending_mysql mysql -u vending -pvending1234 vending_db < vending-server/sql/init_data.sql
```

### 启动后端
```powershell
cd vending-server
mvn spring-boot:run
# 或直接在 IDEA 中运行 VendingApplication.java
```
启动后访问 `http://localhost:8080`

## 架构要点

- **包结构**：`com.vending.module.{user|product|cabinet|inventory|order|payment|pickup|refund|statistics|admin}`，每个模块包含 `controller/service/mapper/entity`
- **公共模块**：`com.vending.common` — Result/ResultCode/异常处理/JWT/CORS/MyBatis/MinIO配置
- **安全**：Spring Security + JWT，role字段存储数字(0/1/2)，在 `JwtAuthenticationFilter.getRoleName()` 中映射为字符串角色名
- **库存并发**：使用 Redis `SETNX` 分布式锁 + MySQL 乐观扣减(`UPDATE ... WHERE quantity >= ?`)
- **订单超时**：30分钟未支付自动取消，需在启动类添加 `@EnableScheduling`（开发文档中有示例代码）

## 数据库

9张表：`sys_user`, `product`, `cabinet`, `inventory`, `orders`, `order_item`, `payment_record`, `pickup_code`, `refund`

DDL 和初始化数据在 `vending-server/sql/` 中，**不要手动在数据库中创建表**，执行脚本即可。

## 前端说明

`vending-demo/` 仅包含3个文件的演示页面，不是完整的前端项目。完整的用户端/管理后台前端尚未创建（参考开发文档第6章的结构）。

## 设计文档

- `开发文档.md` — 开发指导（含完整代码示例、DDL、部署、测试用例）
- `无人售货系统-完整报告.md` — 学术报告格式（架构、UML、测试计划）
- `数据层本地部署指南.md` — Docker 和手动安装指南

**注意**：两份核心文档已统一架构描述（单体架构，HTTPS/JSON，MinIO）。如有不一致，以 `开发文档.md` 为准。

## 其他目录

- `molda-template/` — 从模板王下载的3D动画工作室前端模板（参考资料，与项目无关）
- `uml/` — PlantUML 图文件
- `.claude/settings.local.json` — Claude 权限配置
