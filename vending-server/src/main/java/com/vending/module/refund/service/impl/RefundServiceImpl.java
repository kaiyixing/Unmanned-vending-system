package com.vending.module.refund.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vending.common.exception.BusinessException;
import com.vending.common.result.ResultCode;
import com.vending.module.inventory.service.InventoryService;
import com.vending.module.order.entity.Order;
import com.vending.module.order.entity.OrderItem;
import com.vending.module.order.mapper.OrderItemMapper;
import com.vending.module.order.service.OrderService;
import com.vending.module.refund.entity.Refund;
import com.vending.module.refund.mapper.RefundMapper;
import com.vending.module.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl extends ServiceImpl<RefundMapper, Refund> implements RefundService {

    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(Long userId, Long orderId, String reason) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该订单");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "只有已支付订单才能申请退款");
        }

        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setOrderNo(order.getOrderNo());
        refund.setUserId(userId);
        refund.setReason(reason);
        refund.setAmount(order.getTotalAmount());
        refund.setStatus(0);

        this.save(refund);

        order.setStatus(4);
        orderService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRefund(Long refundId, Boolean approved, String remark) {
        Refund refund = this.getById(refundId);
        if (refund == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "退款申请不存在");
        }

        if (approved) {
            refund.setStatus(3);
            refund.setAuditRemark(remark);
            this.updateById(refund);

            Order order = orderService.getById(refund.getOrderId());
            order.setStatus(5);
            orderService.updateById(order);

            inventoryService.rollbackStock(order.getCabinetId(), order.getOrderId());
        } else {
            refund.setStatus(2);
            refund.setAuditRemark(remark);
            this.updateById(refund);

            Order order = orderService.getById(refund.getOrderId());
            order.setStatus(1);
            orderService.updateById(order);
        }
    }
}
