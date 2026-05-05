package com.vending.module.payment.controller;

import com.vending.common.exception.BusinessException;
import com.vending.common.result.Result;
import com.vending.common.result.ResultCode;
import com.vending.module.order.entity.Order;
import com.vending.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;

    @PostMapping("/pay")
    public Result<Void> pay(
            @RequestAttribute("userId") Long userId,
            @RequestParam Long orderId,
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
}
