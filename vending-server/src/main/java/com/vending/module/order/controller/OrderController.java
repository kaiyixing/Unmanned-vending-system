package com.vending.module.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.result.Result;
import com.vending.module.order.dto.CreateOrderRequest;
import com.vending.module.order.dto.OrderVO;
import com.vending.module.order.entity.Order;
import com.vending.module.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<OrderVO> createOrder(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderVO order = orderService.createOrder(userId, request);
        return Result.success(order);
    }

    @PostMapping("/pay/{orderId}")
    public Result<Void> payOrder(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "mock") String payChannel) {
        orderService.payOrder(orderId, payChannel);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<Page<Order>> getMyOrders(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Order> orderPage = new Page<>(page, size);
        orderPage = orderService.lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime)
                .page(orderPage);
        return Result.success(orderPage);
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        return Result.success(order);
    }
}
