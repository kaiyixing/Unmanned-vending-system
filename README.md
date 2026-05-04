# 无人售货系统

一个基于 Spring Boot + Vue 3 的智能无人售货系统，支持多货柜、在线支付、取货码取货等功能。

---

## ✨ 功能特性

### 用户端
- 📱 用户注册/登录
- 🏪 按城市/货柜浏览商品
- 🛒 购物车管理
- 💰 在线支付（微信/支付宝模拟）
- 📦 取货码生成与核销
- 📊 订单查询与历史
- 📝 退款申请

### 管理后台
- 📈 数据统计仪表盘
- 🛍️ 商品管理（上架/下架/编辑）
- 🚪 货柜管理（添加/编辑/图片）
- 📦 库存管理（按货柜添加商品/库存调整）
- 📋 订单管理（查看/处理）
- 🔄 退款审核
- 📊 销售统计（订单趋势/销售排行）

---

## 🛠️ 技术栈

| 层级 | 技术选型 | 版本 |
|------|---------|------|
| 后端框架 | Spring Boot | 3.2.5 |
| 编程语言 | Java | 17 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 7+ |
| 对象存储 | MinIO | 8.5.7 |
| 安全认证 | Spring Security + JWT | - |
| 用户端 | Vue 3 + Vite + Element Plus | - |
| 管理后台 | Vue 3 + Vite + Element Plus + ECharts | - |

---

## 📂 项目结构

```
Unmanned-vending-system/
├── docs/                          # 项目文档
│   ├── AGENTS.md                  # 技术栈与快速启动
│   ├── 开发文档.md                # 开发指南
│   └── 数据层本地部署指南.md      # 数据库部署
│
├── test/                          # 测试文件
│   └── jmeter/                    # JMeter 压力测试
│
├── vending-server/                # 后端服务
│   ├── sql/                       # 数据库脚本
│   └── src/main/java/com/vending/
│       ├── common/                # 公共模块
│       ├── module/                # 业务模块
│       └── security/              # 安全认证
│
├── vending-web/                   # 用户端
├── vending-admin/                 # 管理后台
└── docker-compose.yml             # Docker 编排
```

---

## 🚀 快速开始

### 1. 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7+
- MinIO (可选)

### 2. 数据层部署

使用 Docker 快速启动数据服务：

```bash
docker-compose up -d
```

或参考 [数据层本地部署指南](docs/数据层本地部署指南.md) 手动部署。

### 3. 数据库初始化

```bash
# 1. 连接 MySQL
mysql -u root -p

# 2. 执行建表脚本
source vending-server/sql/schema.sql

# 3. 导入初始数据
source vending-server/sql/init_data.sql
```

### 4. 后端启动

```bash
cd vending-server

# 修改 application.yml 中的数据库、Redis 连接信息

# 启动服务
mvn spring-boot:run
```

后端服务将在 http://localhost:9000 启动

### 5. 用户端启动

```bash
cd vending-web
npm install
npm run dev
```

用户端将在 http://localhost:5173 启动

### 6. 管理后台启动

```bash
cd vending-admin
npm install
npm run dev
```

管理后台将在 http://localhost:5174 启动

---

## 🔑 默认账号

### 管理后台
- 用户名: `admin`
- 密码: `123456`

### 测试用户
- 用户名: `test01`
- 密码: `123456`

---

## 📖 文档

- [快速启动指南](docs/AGENTS.md)
- [开发文档](docs/开发文档.md)
- [数据层部署指南](docs/数据层本地部署指南.md)
- [项目结构说明](PROJECT_STRUCTURE.md)

---

## 📊 核心业务流程

```
用户注册
  ↓
选择货柜
  ↓
加入购物车
  ↓
在线支付
  ↓
生成取货码
  ↓
核销取货
  ↓
订单完成
```

---

## 🤝 贡献指南

欢迎提交 Issue 和 PR！

---

## 📄 许可证

MIT License

---

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！
