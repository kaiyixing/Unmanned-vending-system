package com.vending.module.refund.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.result.Result;
import com.vending.module.refund.entity.Refund;
import com.vending.module.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/apply")
    public Result<Void> applyRefund(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> request) {
        Long orderId = Long.valueOf(request.get("orderId").toString());
        String reason = request.get("reason").toString();
        refundService.applyRefund(userId, orderId, reason);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<Page<Refund>> getMyRefunds(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Refund> refundPage = new Page<>(page, size);
        refundPage = refundService.lambdaQuery()
                .eq(Refund::getUserId, userId)
                .orderByDesc(Refund::getCreateTime)
                .page(refundPage);
        return Result.success(refundPage);
    }


}
