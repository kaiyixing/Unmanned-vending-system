package com.vending.module.pickup.controller;

import com.vending.common.exception.BusinessException;
import com.vending.common.result.Result;
import com.vending.common.result.ResultCode;
import com.vending.module.order.entity.Order;
import com.vending.module.order.service.OrderService;
import com.vending.module.pickup.entity.PickupCode;
import com.vending.module.pickup.service.PickupCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/pickup-code")
@RequiredArgsConstructor
public class PickupCodeController {

    private final PickupCodeService pickupCodeService;
    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public Result<PickupCode> getByOrderId(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该订单");
        }
        PickupCode pickupCode = pickupCodeService.lambdaQuery()
                .eq(PickupCode::getOrderId, orderId)
                .one();
        return Result.success(pickupCode);
    }

    @PostMapping("/verify")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> verifyPickupCode(@RequestParam String codeValue) {
        PickupCode pickupCode = pickupCodeService.lambdaQuery()
                .eq(PickupCode::getCodeValue, codeValue)
                .one();

        if (pickupCode == null) {
            throw new BusinessException(ResultCode.FAILURE.getCode(), "取货码无效");
        }

        if (pickupCode.getStatus() == 1) {
            throw new BusinessException(ResultCode.PICKUP_CODE_USED);
        }

        if (pickupCode.getStatus() == 2) {
            throw new BusinessException(ResultCode.PICKUP_CODE_EXPIRED);
        }

        if (pickupCode.getExpireTime().isBefore(LocalDateTime.now())) {
            pickupCode.setStatus(2);
            pickupCodeService.updateById(pickupCode);
            throw new BusinessException(ResultCode.PICKUP_CODE_EXPIRED);
        }

        // 更新取货码状态
        pickupCode.setStatus(1);
        pickupCode.setUseTime(LocalDateTime.now());
        pickupCodeService.updateById(pickupCode);

        // 更新订单状态为已完成
        Order order = orderService.getById(pickupCode.getOrderId());
        if (order != null && order.getStatus() == 1) {
            order.setStatus(2);
            orderService.updateById(order);
        }

        return Result.success();
    }
}
