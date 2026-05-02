package com.vending.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.result.Result;
import com.vending.module.order.entity.Order;
import com.vending.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping("/list")
    public Result<Page<Order>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long cabinetId,
            @RequestParam(required = false) Boolean todayOnly) {
        Page<Order> orderPage = new Page<>(page, size);
        var query = orderService.lambdaQuery();
        
        if (status != null) {
            query.eq(Order::getStatus, status);
        }
        if (cabinetId != null) {
            query.eq(Order::getCabinetId, cabinetId);
        }
        if (Boolean.TRUE.equals(todayOnly)) {
            // 只查询今日订单（从00:00:00开始）
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            query.ge(Order::getCreateTime, todayStart);
        }
        
        orderPage = query.orderByDesc(Order::getCreateTime).page(orderPage);
        return Result.success(orderPage);
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        Order order = orderService.getById(id);
        return Result.success(order);
    }
}
