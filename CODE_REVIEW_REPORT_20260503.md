# 代码审查Bug报告

**提交时间**: 2026-05-03 00:53:02
**提交Hash**: `59ced9cbf886db33ca783021d5bc45aa7da40157`
**提交信息**: refactor: 优化项目目录结构
**审查日期**: 2026-05-03

---

## 发现的Bug汇总

本次审查发现 **1个高危缺陷**，以下是详细报告：

---

## Bug #1: 库存扣减逻辑错误导致数据丢失风险

### 严重程度: 高 (High)

### 问题位置

**文件**: [OrderServiceImpl.java#L91](file:///workspace/vending-server/src/main/java/com/vending/module/order/service/impl/OrderServiceImpl.java#L91)

```java
inventoryService.deductStock(order.getCabinetId(), orderId);
```

**相关文件**:
- [InventoryServiceImpl.java#L55-56](file:///workspace/vending-server/src/main/java/com/vending/module/inventory/service/impl/InventoryServiceImpl.java#L55-56)

### 问题描述

在 `payOrder` 方法中，调用 `deductStock` 时只传入了 `orderId`，但没有传入具体的 `OrderItem` 列表。这意味着库存扣减将无法正确执行订单中每个商品的库存扣减操作。

查看 `deductStock` 方法的签名和实现：

```java
public void deductStock(Long cabinetId, Long orderId) {
    String lockKey = INVENTORY_LOCK_KEY + cabinetId;
    // ...
    List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, orderId));

    for (OrderItem item : items) {
        int affected = this.getBaseMapper().deductStock(
                cabinetId, item.getProductId(), item.getQuantity());
        // ...
    }
}
```

虽然 `deductStock` 可以通过 `orderId` 查询到 `OrderItem` 列表，但这里的实现存在**数据一致性问题**：

1. **先扣库存，后更新订单状态**：`deductStock` 在 `order.setStatus(1)` 之前执行
2. **如果扣库存成功但后续操作失败**（如支付记录创建失败、取货码生成失败），整个事务会回滚
3. **但更严重的是**：`deductStock` 方法内部使用的是**行级锁不完善的分布式锁**（仅基于 `cabinetId`），如果多个订单同时处理同一柜机，高并发场景下可能出现**超卖**

### 触发场景

```
场景：高并发下单
1. 柜机A有商品P库存为1
2. 用户1和用户2同时下单购买商品P
3. 两个请求同时通过checkStock检查（都看到库存为1）
4. 两个请求同时进入deductStock
5. 由于分布式锁仅锁cabinetId，两个请求可能同时通过
6. 乐观锁SQL `quantity >= quantity` 只能防止一个成功
7. 但在高并发下，可能出现库存变为负数
```

### 影响范围

- **数据完整性**: 可能导致库存超卖
- **用户可感知**: 用户支付成功后无法取货，引发客诉

### 建议修复

修复 `deductStock` 方法的分布式锁粒度，将锁从 `cabinetId` 级别改为 `cabinetId + productId` 组合：

```java
private static final String INVENTORY_LOCK_KEY = "inventory:lock:";

@Override
@Transactional(rollbackFor = Exception.class)
public void deductStock(Long cabinetId, Long orderId) {
    List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, orderId));

    for (OrderItem item : items) {
        String lockKey = INVENTORY_LOCK_KEY + cabinetId + ":" + item.getProductId();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(ResultCode.INVENTORY_ERROR, "库存操作繁忙，请稍后重试");
        }

        try {
            int affected = this.getBaseMapper().deductStock(
                    cabinetId, item.getProductId(), item.getQuantity());
            if (affected == 0) {
                throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}
```

### 测试建议

添加并发测试用例，验证同一商品库存为1时，多个并发请求只能有1个成功。

---

## 其他风险点（建议关注但不阻塞）

以下问题虽然存在，但影响相对可控或为低频场景：

### 1. PickupCode生成可能陷入死循环（概率极低）

**位置**: [PickupCodeServiceImpl.java#L32-40](file:///workspace/vending-server/src/main/java/com/vending/module/pickup/service/impl/PickupCodeServiceImpl.java#L32-40)

如果数据库中存在大量记录且生成的随机码全部冲突，理论上可能无限循环。但由于6位数字有90万种组合，实际概率极低。

### 2. MinIO存储桶创建失败静默处理

**位置**: [MinioService.java#L51-53](file:///workspace/vending-server/src/main/java/com/vending/common/service/MinioService.java#L51-53)

```java
} catch (Exception e) {
    log.error("Error checking/creating bucket", e);
}
```

如果存储桶创建失败，后续上传操作可能失败，但不会抛出明确异常。

### 3. OrderTimeoutTask未使用分布式锁

**位置**: [OrderTimeoutTask.java](file:///workspace/vending-server/src/main/java/com/vending/module/order/service/impl/OrderTimeoutTask.java)

定时任务在多实例部署时可能被重复执行，但取消订单通常幂等，影响较小。

---

## 审查结论

| Bug编号 | 严重程度 | 状态 | 建议 |
|---------|----------|------|------|
| Bug #1 | 高 | 需修复 | 修复分布式锁粒度 |

**整体评价**: 代码架构清晰，事务注解使用正确，但高并发场景下的库存一致性需要重点关注。建议在部署前修复 Bug #1 并进行压力测试。

---

*本报告由代码审查工具自动生成*
