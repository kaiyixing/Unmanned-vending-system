package com.vending.module.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vending.common.exception.BusinessException;
import com.vending.common.result.ResultCode;
import com.vending.module.inventory.entity.Inventory;
import com.vending.module.inventory.mapper.InventoryMapper;
import com.vending.module.inventory.service.InventoryService;
import com.vending.module.order.entity.OrderItem;
import com.vending.module.order.mapper.OrderItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    private final StringRedisTemplate redisTemplate;
    private final OrderItemMapper orderItemMapper;

    private static final String INVENTORY_LOCK_KEY = "inventory:lock:";

    @Override
    public boolean checkStock(Long cabinetId, Long productId, Integer quantity) {
        Inventory inventory = this.lambdaQuery()
                .eq(Inventory::getCabinetId, cabinetId)
                .eq(Inventory::getProductId, productId)
                .one();
        return inventory != null && inventory.getQuantity() >= quantity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStock(Long cabinetId, Long orderId) {
        String lockKey = INVENTORY_LOCK_KEY + cabinetId;

        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(ResultCode.INVENTORY_ERROR, "库存操作繁忙，请稍后重试");
        }

        try {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>()
                            .eq(OrderItem::getOrderId, orderId));

            for (OrderItem item : items) {
                int affected = this.getBaseMapper().deductStock(
                        cabinetId, item.getProductId(), item.getQuantity());
                if (affected == 0) {
                    throw new BusinessException(ResultCode.INSUFFICIENT_STOCK);
                }
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackStock(Long cabinetId, Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId));

        for (OrderItem item : items) {
            this.getBaseMapper().addStock(cabinetId, item.getProductId(), item.getQuantity());
        }
    }
}
