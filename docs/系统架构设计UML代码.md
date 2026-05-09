# 无人售货系统 - 系统架构设计UML代码

本文件包含系统架构设计相关的PlantUML代码，可在支持PlantUML的工具中渲染为图形。

---

## 1. 系统整体架构图

```plantuml
@startuml 系统整体架构图

!define RECTANGLE class

skinparam componentStyle rectangle
skinparam backgroundColor #FEFEFE
skinparam handwritten false

title 无人售货系统 - 整体架构图

package "表现层 (Presentation Layer)" {
    [Web用户端 (Vue 3)] as UserWeb
    [Web管理后台 (Vue 3)] as AdminWeb
}

package "API网关/负载均衡" {
    [Nginx] as Nginx
}

package "业务服务层 (Business Layer)" {
    [Spring Boot 3.2.5\n单体应用] as SpringBoot {
        package "业务模块" {
            [用户模块] as UserModule
            [商品模块] as ProductModule
            [订单模块] as OrderModule
            [支付模块] as PaymentModule
            [库存模块] as InventoryModule
            [货柜模块] as CabinetModule
            [退款模块] as RefundModule
            [统计模块] as StatsModule
        }
        
        package "公共服务" {
            [安全认证\n(JWT+Security)] as Security
            [Redis缓存服务] as CacheService
            [全局异常处理] as ExceptionHandler
            [Swagger文档] as Swagger
        }
    }
}

package "数据层 (Data Layer)" {
    database "MySQL 8.0\n(主数据库)" as MySQL
    database "Redis 7.0\n(缓存/分布式锁)" as Redis
    database "MinIO 8.5\n(图片对象存储)" as MinIO
}

UserWeb -down-> Nginx : HTTPS/JSON
AdminWeb -down-> Nginx : HTTPS/JSON
Nginx -down-> SpringBoot : API请求

SpringBoot -down-> MySQL : JDBC/MyBatis
SpringBoot -down-> Redis : Redis Client
SpringBoot -down-> MinIO : S3 Client

note right of MySQL
  核心业务数据表
  - sys_user (用户表)
  - product (商品表)
  - cabinet (货柜表)
  - inventory (库存表)
  - orders (订单表)
  - order_item (订单明细)
  - payment_record (支付记录)
  - pickup_code (取货码表)
  - refund (退款表)
end note

note right of Redis
  缓存内容
  - 商品列表缓存
  - 货柜列表缓存
  - 货柜商品缓存
  - JWT黑名单
  - Refresh Token
  - 分布式锁
end note

note right of MinIO
  存储内容
  - 商品图片
  - 货柜图片
  - 用户头像
end note

@enduml
```

---

## 2. 模块依赖关系图

```plantuml
@startuml 模块依赖关系图

skinparam componentStyle rectangle
skinparam linetype ortho
title 无人售货系统 - 模块依赖关系图

left to right direction

package "业务模块" {
    [用户服务模块] as UserModule
    [商品服务模块] as ProductModule
    [订单服务模块] as OrderModule
    [支付服务模块] as PaymentModule
    [库存服务模块] as InventoryModule
    [货柜信息模块] as CabinetModule
    [退款服务模块] as RefundModule
    [数据统计模块] as StatsModule
}

package "公共服务" {
    [安全认证服务] as Security
    [Redis缓存服务] as Cache
    [MinIO对象存储] as MinIO
    [异常处理服务] as Exception
}

OrderModule .down.> UserModule : 依赖(校验用户)
OrderModule .down.> ProductModule : 依赖(校验商品)
OrderModule .down.> InventoryModule : 依赖(扣减库存)
OrderModule .right.> PaymentModule : 依赖(发起支付)
OrderModule .right.> CabinetModule : 依赖(获取货柜信息)

PaymentModule .up.> OrderModule : 更新订单状态

RefundModule .down.> OrderModule : 依赖
RefundModule .down.> InventoryModule : 依赖(回滚库存)
RefundModule .down.> UserModule : 依赖(校验用户)

InventoryModule .down.> CabinetModule : 依赖(获取货柜)
InventoryModule .down.> ProductModule : 依赖(获取商品)

StatsModule ..> UserModule : 读取数据
StatsModule ..> ProductModule : 读取数据
StatsModule ..> OrderModule : 读取数据
StatsModule ..> PaymentModule : 读取数据
StatsModule ..> RefundModule : 读取数据

UserModule ..> Security : 认证
ProductModule ..> Cache : 缓存
ProductModule ..> MinIO : 图片存储
CabinetModule ..> Cache : 缓存
CabinetModule ..> MinIO : 图片存储
InventoryModule ..> Cache : 分布式锁

note bottom of Cache
  其他业务模块均可能依赖
  公共服务模块
end note

@enduml
```

---

## 3. 核心实体类图

```plantuml
@startuml 核心实体类图

skinparam classAttributeIconSize 0
skinparam linetype ortho
title 无人售货系统 - 核心实体类图

abstract class BaseEntity {
    +Long id
    +LocalDateTime createTime
    +LocalDateTime updateTime
}

class User {
    +Long userId
    +String username
    +String password
    +String phone
    +String email
    +String realName
    +String avatar
    +Integer role
    +Integer status
}

class Product {
    +Long productId
    +String name
    +String description
    +String category
    +BigDecimal price
    +BigDecimal costPrice
    +String imageUrl
    +List<String> images
    +String spec
    +Integer status
}

class Cabinet {
    +Long cabinetId
    +String cabinetCode
    +String name
    +String city
    +String address
    +String imageUrl
    +BigDecimal latitude
    +BigDecimal longitude
    +Integer capacity
    +Integer status
}

class Inventory {
    +Long inventoryId
    +Long cabinetId
    +Long productId
    +Integer quantity
    +Integer threshold
}

class Order {
    +Long orderId
    +String orderNo
    +Long userId
    +Long cabinetId
    +BigDecimal totalAmount
    +Integer status
    +LocalDateTime payTime
    +String payChannel
    +String remark
}

class OrderItem {
    +Long itemId
    +Long orderId
    +Long productId
    +String productName
    +Integer quantity
    +BigDecimal unitPrice
}

class PaymentRecord {
    +Long recordId
    +Long orderId
    +String orderNo
    +String channel
    +BigDecimal amount
    +Integer status
    +String tradeNo
    +LocalDateTime payTime
}

class PickupCode {
    +Long codeId
    +Long orderId
    +String orderNo
    +String codeValue
    +String qrCodeUrl
    +Integer status
    +LocalDateTime expireTime
    +LocalDateTime useTime
}

class Refund {
    +Long refundId
    +Long orderId
    +String orderNo
    +Long userId
    +String reason
    +BigDecimal amount
    +Integer status
    +String auditRemark
}

' 继承关系
User --|> BaseEntity
Product --|> BaseEntity
Cabinet --|> BaseEntity
Inventory --|> BaseEntity
Order --|> BaseEntity
OrderItem --|> BaseEntity
PaymentRecord --|> BaseEntity
PickupCode --|> BaseEntity
Refund --|> BaseEntity

' 关联关系
User "1" -- "N" Order : 创建
User "1" -- "N" Refund : 发起
Order "1" -- "N" OrderItem : 包含
Order "1" -- "1" PaymentRecord : 对应
Order "1" -- "1" PickupCode : 生成
Order "1" -- "0..1" Refund : 可能有
Cabinet "1" -- "N" Inventory : 拥有
Cabinet "1" -- "N" Order : 属于
Product "1" -- "N" Inventory : 在
Product "1" -- "N" OrderItem : 出现在

note right of Order
  订单状态
  0-待支付
  1-已支付
  2-已完成
  3-已取消
  4-退款中
  5-已退款
end note

note right of User
  角色
  0-普通用户
  1-运营管理员
  2-超级管理员
end note

@enduml
```

---

## 4. 用户下单支付时序图

```plantuml
@startuml 用户下单支付时序图

skinparam sequenceMessageAlign center
title 无人售货系统 - 用户下单支付完整流程

actor "消费者" as User
participant "Web用户端" as Web
participant "订单模块" as OrderModule
participant "商品模块" as ProductModule
participant "库存模块" as InventoryModule
participant "Redis" as Redis
participant "支付模块" as PaymentModule
participant "取货码模块" as PickupModule
participant "MySQL" as DB

== 步骤1: 浏览货柜与商品 ==
User -> Web : 打开页面
Web -> OrderModule : GET /api/cabinet/list
OrderModule -> Redis : 查询货柜列表缓存
alt 缓存命中
    Redis --> OrderModule : 返回缓存数据
else 缓存未命中
    OrderModule -> DB : SELECT * FROM cabinet
    DB --> OrderModule : 返回货柜列表
    OrderModule -> Redis : 写入缓存(30分钟)
end
OrderModule --> Web : 返回货柜列表
Web -> OrderModule : GET /api/cabinet/{id}/products
OrderModule -> Redis : 查询货柜商品缓存
alt 缓存命中
    Redis --> OrderModule : 返回缓存数据
else 缓存未命中
    OrderModule -> DB : 查询货柜商品库存
    DB --> OrderModule : 返回商品列表
    OrderModule -> Redis : 写入缓存(30分钟)
end
OrderModule --> Web : 返回商品列表
Web --> User : 展示商品

== 步骤2: 创建订单 ==
User -> Web : 添加商品到购物车\n点击"立即下单"
Web -> OrderModule : POST /api/order/create
activate OrderModule

OrderModule -> ProductModule : 校验商品是否存在/上架
ProductModule --> OrderModule : 商品信息

loop 订单明细
    OrderModule -> InventoryModule : 检查库存
    InventoryModule -> Redis : 加分布式锁
    InventoryModule -> DB : SELECT quantity FROM inventory
    InventoryModule -> Redis : 释放锁
    InventoryModule --> OrderModule : 库存充足/不足
end

OrderModule -> DB : INSERT orders (状态=0待支付)
OrderModule -> DB : INSERT order_item

OrderModule --> Web : 返回订单信息
deactivate OrderModule
Web --> User : 展示订单详情

== 步骤3: 支付 ==
User -> Web : 点击"立即支付"
Web -> PaymentModule : POST /api/payment/pay
activate PaymentModule

PaymentModule -> OrderModule : 校验订单状态
OrderModule --> PaymentModule : 订单待支付

PaymentModule -> DB : INSERT payment_record (状态=0)
PaymentModule -> PaymentModule : 模拟支付(2秒)

alt 支付成功
    PaymentModule -> DB : UPDATE payment_record\nstatus=1 支付成功
    PaymentModule -> OrderModule : 回调更新订单
    activate OrderModule
    
    OrderModule -> InventoryModule : 扣减库存
    activate InventoryModule
    
    loop 扣减每个商品库存
        InventoryModule -> Redis : 获取分布式锁\n(inventory:lock:{cabinetId}:{productId})
        InventoryModule -> DB : UPDATE inventory\nSET quantity = quantity - ?\nWHERE cabinet_id = ?\nAND product_id = ?\nAND quantity >= ?
        alt 扣减成功
            DB --> InventoryModule : 影响行数=1
        else 库存不足
            DB --> InventoryModule : 影响行数=0
            InventoryModule -> InventoryModule : 回滚已扣减库存
            InventoryModule -> Redis : 释放锁
            InventoryModule --> OrderModule : 抛出"库存不足"异常
        end
        InventoryModule -> Redis : 释放锁
    end
    
    InventoryModule -> Redis : 删除货柜商品缓存
    deactivate InventoryModule
    
    OrderModule -> DB : UPDATE orders\nstatus=1 已支付
    OrderModule -> PickupModule : 生成取货码
    PickupModule -> DB : INSERT pickup_code
    OrderModule -> Redis : 删除货柜列表缓存
    deactivate OrderModule
    
    PaymentModule --> Web : 支付成功
else 支付失败
    PaymentModule -> DB : UPDATE payment_record\nstatus=2 支付失败
    PaymentModule --> Web : 支付失败
end

deactivate PaymentModule

Web --> User : 展示支付结果\n显示取货码

@enduml
```

---

## 5. 管理员退款审核时序图

```plantuml
@startuml 管理员退款审核时序图

skinparam sequenceMessageAlign center
title 无人售货系统 - 退款审核流程

actor "运营管理员" as Admin
participant "管理后台" as AdminWeb
participant "退款模块" as RefundModule
participant "订单模块" as OrderModule
participant "库存模块" as InventoryModule
participant "Redis" as Redis
participant "MySQL" as DB

== 步骤1: 查询退款申请 ==
Admin -> AdminWeb : 登录管理后台
AdminWeb -> RefundModule : GET /api/refund/admin/list\n(需要ADMIN角色)
RefundModule -> DB : SELECT * FROM refund\nWHERE status=0
DB --> RefundModule : 退款申请列表
RefundModule --> AdminWeb : 返回列表
AdminWeb --> Admin : 展示退款申请

== 步骤2: 审核退款 ==
Admin -> AdminWeb : 点击"审核通过"
AdminWeb -> RefundModule : POST /api/refund/admin/audit
activate RefundModule

RefundModule -> OrderModule : 校验订单状态
OrderModule -> DB : SELECT * FROM orders\nWHERE order_id=?
DB --> OrderModule : 订单信息(已支付状态)
OrderModule --> RefundModule : 订单有效

RefundModule -> InventoryModule : 回滚库存
activate InventoryModule

loop 每个订单明细
    InventoryModule -> Redis : 获取分布式锁
    InventoryModule -> DB : UPDATE inventory\nSET quantity = quantity + ?\nWHERE cabinet_id=? AND product_id=?
    DB --> InventoryModule : 更新成功
    InventoryModule -> Redis : 释放锁
end

InventoryModule -> Redis : 删除货柜商品缓存
deactivate InventoryModule

RefundModule -> OrderModule : 更新订单状态
OrderModule -> DB : UPDATE orders\nstatus=5 已退款
OrderModule -> Redis : 删除订单相关缓存
RefundModule -> DB : UPDATE refund\nstatus=1 审核通过

RefundModule -> Redis : 删除货柜列表缓存
RefundModule --> AdminWeb : 审核成功
deactivate RefundModule

AdminWeb --> Admin : 提示审核成功

@enduml
```

---

## 6. 库存扣减并发控制流程图

```plantuml
@startuml 库存扣减并发控制流程图

skinparam activity {
    BackgroundColor #f8f9fa
    BorderColor #2c3e50
}
start
title 无人售货系统 - 库存扣减并发控制流程

:接收库存扣减请求\n(cabinetId, productId, quantity);

:生成锁Key\ninventory:lock:{cabinetId}:{productId};

:尝试获取Redis分布式锁\n(setIfAbsent, 过期时间10秒);

if (锁获取成功?) then (是)
    :查询当前库存\nSELECT quantity FROM inventory;
    
    if (库存 >= 扣减数量?) then (是)
        :执行库存扣减\nUPDATE inventory\nSET quantity = quantity - ?\nWHERE cabinet_id=?\nAND product_id=?\nAND quantity >= ?;
        
        if (更新成功?) then (是)
            :删除相关缓存\n货柜商品缓存;
            :返回"扣减成功";
        else (否)
            :返回"库存不足";
        endif
    else (否)
        :返回"库存不足";
    endif
    
    :释放Redis分布式锁;
else (否)
    :等待100毫秒后重试;\
    :最多重试3次;
    
    if (重试成功?) then (是)
        goto 锁获取成功
    else (否)
        :返回"系统繁忙,请稍后再试";
    endif
endif

stop

@enduml
```

---

## 7. 部署架构图

```plantuml
@startuml 部署架构图

skinparam componentStyle rectangle
title 无人售货系统 - 生产环境部署架构

node "云服务商 (阿里云/腾讯云)" {
    node "负载均衡 SLB" as SLB
    
    node "Web服务器集群" {
        [Nginx\nVue静态文件] as Web1
        [Nginx\nVue静态文件] as Web2
    }
    
    node "应用服务器集群" {
        [Spring Boot\n实例1] as App1
        [Spring Boot\n实例2] as App2
    }
    
    node "数据存储" {
        node "MySQL主从" {
            [MySQL Master] as MySQLMaster
            [MySQL Slave] as MySQLSlave
        }
        
        node "Redis哨兵模式" {
            [Redis Master] as RedisMaster
            [Redis Slave 1] as RedisSlave1
            [Redis Slave 2] as RedisSlave2
        }
        
        node "MinIO集群" {
            [MinIO Node 1] as MinIO1
            [MinIO Node 2] as MinIO2
            [MinIO Node 3] as MinIO3
        }
    }
}

actor "消费者" as User
actor "运营管理员" as Admin

User -down-> SLB : HTTPS(443)
Admin -down-> SLB : HTTPS(443)

SLB -down-> Web1 : 负载分发
SLB -down-> Web2 : 负载分发

Web1 -down-> App1 : API请求
Web1 -down-> App2 : API请求
Web2 -down-> App1 : API请求
Web2 -down-> App2 : API请求

App1 -down-> MySQLMaster : 写入
App1 -down-> MySQLSlave : 读取
App2 -down-> MySQLMaster : 写入
App2 -down-> MySQLSlave : 读取

App1 -down-> RedisMaster : 写入/分布式锁
App1 -down-> RedisSlave1 : 读取
App1 -down-> RedisSlave2 : 读取
App2 -down-> RedisMaster : 写入/分布式锁
App2 -down-> RedisSlave1 : 读取
App2 -down-> RedisSlave2 : 读取

App1 -down-> MinIO1 : 图片上传下载
App1 -down-> MinIO2 : 图片上传下载
App1 -down-> MinIO3 : 图片上传下载
App2 -down-> MinIO1 : 图片上传下载
App2 -down-> MinIO2 : 图片上传下载
App2 -down-> MinIO3 : 图片上传下载

MySQLMaster -down-> MySQLSlave : 主从复制
RedisMaster -down-> RedisSlave1 : 主从复制
RedisMaster -down-> RedisSlave2 : 主从复制

note bottom of SLB
  负载均衡策略
  - 轮询
  - 会话保持
end note

@enduml
```

---

## 使用说明

1. **PlantUML工具**：可使用以下工具渲染上述代码
   - VS Code + PlantUML 插件
   - IntelliJ IDEA + PlantUML 插件
   - 在线工具：http://www.plantuml.com/plantuml/
   - 本地安装 PlantUML

2. **语法说明**：
   - 所有代码均使用标准PlantUML语法
   - 支持中文注释和标签
   - 可根据需要调整颜色、布局等

3. **导出格式**：
   - PNG/JPG 图片格式
   - SVG 矢量格式
   - PDF 文档格式
