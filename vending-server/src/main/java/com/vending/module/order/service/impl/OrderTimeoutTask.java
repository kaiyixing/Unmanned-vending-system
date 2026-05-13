package com.vending.module.order.service.impl;

import com.vending.module.inventory.service.InventoryService;
import com.vending.module.order.entity.Order;
import com.vending.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderService orderService;
    private final InventoryService inventoryService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void cancelExpiredOrders() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30);

        List<Order> expiredOrders = orderService.lambdaQuery()
            .eq(Order::getStatus, 0)
            .lt(Order::getCreateTime, timeoutThreshold)
            .list();

        for (Order order : expiredOrders) {
            cancelOrderWithRollback(order);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderWithRollback(Order order) {
        try {
            inventoryService.rollbackStock(order.getCabinetId(), order.getOrderId());
            order.setStatus(3);
            orderService.updateById(order);
            log.info("订单超时已取消并回滚库存: orderId={}", order.getOrderId());
        } catch (Exception e) {
            log.error("取消订单并回滚库存失败: orderId={}, error={}", order.getOrderId(), e.getMessage());
        }
    }
}
