# 无人售货系统 - 项目结构说明

## 项目总览

```
Unmanned-vending-system/
├── docs/                          # 项目文档目录
│   ├── AGENTS.md                  # 技术栈与快速启动指南
│   ├── 开发文档.md                # 详细开发文档
│   ├── 数据层本地部署指南.md      # 数据库部署指南
│   └── 无人售货系统-完整报告.md  # 学术报告
│
├── test/                          # 测试相关文件
│   └── jmeter/                    # JMeter 测试配置
│       ├── HTTP请求.jmx           # JMeter 测试脚本
│       └── JMeter压力测试配置指南.md # 测试配置文档
│
├── vending-server/                # 后端服务 (Spring Boot)
│   ├── sql/                       # 数据库脚本
│   │   ├── schema.sql             # 表结构初始化
│   │   ├── init_data.sql          # 初始数据
│   │   ├── insert_test_orders.sql # 测试订单数据
│   │   └── migrations/            # 数据库升级脚本
│   │       └── update_001_add_cabinet_image.sql
│   ├── src/
│   │   ├── main/java/com/vending/
│   │   │   ├── common/            # 公共模块
│   │   │   │   ├── annotation/    # 注解
│   │   │   │   ├── config/        # 配置类
│   │   │   │   ├── exception/     # 异常处理
│   │   │   │   ├── result/        # 统一响应
│   │   │   │   ├── service/       # 公共服务
│   │   │   │   └── util/          # 工具类
│   │   │   ├── module/            # 业务模块
│   │   │   │   ├── user/          # 用户模块
│   │   │   │   ├── product/       # 商品模块
│   │   │   │   ├── cabinet/       # 货柜模块
│   │   │   │   ├── inventory/     # 库存模块
│   │   │   │   ├── order/         # 订单模块
│   │   │   │   ├── payment/       # 支付模块
│   │   │   │   ├── pickup/        # 取货码模块
│   │   │   │   ├── refund/        # 退款模块
│   │   │   │   ├── statistics/    # 统计模块
│   │   │   │   └── admin/         # 管理后台模块
│   │   │   └── security/          # 安全认证模块
│   │   └── main/resources/        # 配置文件
│   │       └── application.yml
│   └── pom.xml
│
├── vending-web/                   # 用户端 (Vue 3)
│   ├── src/
│   │   ├── api/                   # API 请求封装
│   │   ├── components/            # 公共组件
│   │   ├── views/                 # 页面组件
│   │   ├── router/                # 路由配置
│   │   ├── stores/                # 状态管理 (Pinia)
│   │   └── assets/                # 静态资源
│   └── package.json
│
├── vending-admin/                 # 管理后台 (Vue 3)
│   ├── src/
│   │   ├── api/                   # API 请求封装
│   │   ├── views/                 # 页面组件
│   │   ├── router/                # 路由配置
│   │   ├── stores/                # 状态管理
│   │   └── assets/                # 静态资源
│   └── package.json
│
├── .gitignore                     # Git 忽略文件
└── docker-compose.yml             # Docker 编排文件
```

---

## 目录说明

### docs/ - 项目文档

存放所有项目相关的文档：
- 技术栈说明
- 开发指南
- 部署文档
- 项目报告

---

### test/ - 测试相关

存放所有测试文件：
- jmeter/ - JMeter 压力测试配置

---

### vending-server/ - 后端服务

#### sql/ - 数据库脚本

| 文件 | 说明 |
|------|------|
| schema.sql | 表结构定义，首次部署时执行 |
| init_data.sql | 初始数据，包含测试用户、商品、货柜等 |
| insert_test_orders.sql | 测试订单数据，可选执行 |
| migrations/ | 数据库升级脚本，按顺序执行 |

#### src/main/java/com/vending/

**common/ - 公共模块**
- config/ - Spring 配置
- exception/ - 全局异常处理
- result/ - 统一响应封装
- util/ - 工具类

**module/ - 业务模块**
每个业务模块按结构分层：
```
{module}/
├── controller/    # 控制器层
├── service/       # 业务逻辑层
├── mapper/        # 数据访问层
├── entity/        # 实体类
└── dto/           # 数据传输对象
```

---

### vending-web/ - 用户端

```
src/
├── api/          # API 请求
├── components/   # 公共组件
├── views/        # 页面
├── router/       # 路由
├── stores/       # Pinia 状态管理
└── assets/       # 样式、图片等
```

---

### vending-admin/ - 管理后台

```
src/
├── api/          # API 请求
├── views/        # 页面
├── router/       # 路由
├── stores/       # 状态管理
└── assets/       # 样式等
```

---

## 技术栈总结

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2.5 + Java 17 |
| ORM | MyBatis-Plus 3.5.5 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 |
| 对象存储 | MinIO |
| 用户端 | Vue 3 + Vite + Element Plus |
| 管理后台 | Vue 3 + Vite + Element Plus + ECharts |

---

## 快速开始

详见 `docs/AGENTS.md`
