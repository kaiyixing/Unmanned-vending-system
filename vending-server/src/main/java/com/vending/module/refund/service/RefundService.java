package com.vending.module.refund.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vending.module.refund.entity.Refund;

public interface RefundService extends IService<Refund> {
    void applyRefund(Long userId, Long orderId, String reason);
    void auditRefund(Long refundId, Boolean approved, String remark);
}
