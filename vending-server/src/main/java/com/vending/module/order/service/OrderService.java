package com.vending.module.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vending.module.order.dto.CreateOrderRequest;
import com.vending.module.order.dto.OrderVO;
import com.vending.module.order.entity.Order;

public interface OrderService extends IService<Order> {
    OrderVO createOrder(Long userId, CreateOrderRequest request);
    void payOrder(Long orderId, String payChannel);
    void cancelOrder(Long userId, Long orderId);
}
