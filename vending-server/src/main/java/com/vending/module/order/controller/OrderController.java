package com.vending.module.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.exception.BusinessException;
import com.vending.common.result.Result;
import com.vending.common.result.ResultCode;
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
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "mock") String payChannel) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该订单");
        }
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
    public Result<Order> getById(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该订单");
        }
        return Result.success(order);
    }

    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId) {
        orderService.cancelOrder(userId, orderId);
        return Result.success();
    }
}
