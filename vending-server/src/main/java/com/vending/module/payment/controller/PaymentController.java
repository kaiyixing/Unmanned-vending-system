package com.vending.module.payment.controller;

import com.vending.common.result.Result;
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
            @RequestParam Long orderId,
            @RequestParam(defaultValue = "mock") String payChannel) {
        orderService.payOrder(orderId, payChannel);
        return Result.success();
    }
}
