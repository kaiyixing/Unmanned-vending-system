package com.vending.module.pickup.controller;

import com.vending.common.result.Result;
import com.vending.module.pickup.entity.PickupCode;
import com.vending.module.pickup.service.PickupCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pickup-code")
@RequiredArgsConstructor
public class PickupCodeController {

    private final PickupCodeService pickupCodeService;

    @GetMapping("/{orderId}")
    public Result<PickupCode> getByOrderId(@PathVariable Long orderId) {
        PickupCode pickupCode = pickupCodeService.lambdaQuery()
                .eq(PickupCode::getOrderId, orderId)
                .one();
        return Result.success(pickupCode);
    }
}
