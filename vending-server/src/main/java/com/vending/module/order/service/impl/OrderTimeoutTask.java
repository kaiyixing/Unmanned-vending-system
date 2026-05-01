package com.vending.module.order.service.impl;

import com.vending.module.order.entity.Order;
import com.vending.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderService orderService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void cancelExpiredOrders() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30);

        List<Order> expiredOrders = orderService.lambdaQuery()
            .eq(Order::getStatus, 0)
            .lt(Order::getCreateTime, timeoutThreshold)
            .list();

        for (Order order : expiredOrders) {
            order.setStatus(3);
            orderService.updateById(order);
        }
    }
}
