package com.vending.module.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vending.module.order.entity.Order;
import com.vending.module.payment.entity.PaymentRecord;
import com.vending.module.payment.mapper.PaymentRecordMapper;
import com.vending.module.payment.service.PaymentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentService {

    @Override
    public PaymentRecord createPaymentRecord(Order order, String channel) {
        PaymentRecord record = new PaymentRecord();
        record.setOrderId(order.getOrderId());
        record.setOrderNo(order.getOrderNo());
        record.setChannel(channel);
        record.setAmount(order.getTotalAmount());
        record.setStatus(1);
        record.setTradeNo("MOCK_" + UUID.randomUUID().toString().replace("-", ""));
        record.setPayTime(LocalDateTime.now());

        this.save(record);
        return record;
    }
}
