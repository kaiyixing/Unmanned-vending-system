package com.vending.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.result.Result;
import com.vending.module.refund.entity.Refund;
import com.vending.module.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/refund")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;

    @PostMapping("/audit")
    public Result<Void> auditRefund(@RequestBody Map<String, Object> request) {
        Long refundId = Long.valueOf(request.get("refundId").toString());
        Boolean approved = (Boolean) request.get("approved");
        String remark = request.get("remark").toString();
        refundService.auditRefund(refundId, approved, remark);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<Page<Refund>> getAllRefunds(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Refund> refundPage = new Page<>(page, size);
        refundPage = refundService.page(refundPage);
        return Result.success(refundPage);
    }
}
