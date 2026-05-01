package com.vending.module.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vending.module.order.entity.Order;
import com.vending.module.payment.entity.PaymentRecord;

public interface PaymentService extends IService<PaymentRecord> {
    PaymentRecord createPaymentRecord(Order order, String channel);
}
