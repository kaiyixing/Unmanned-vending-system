package com.vending.module.pickup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vending.common.exception.BusinessException;
import com.vending.common.result.ResultCode;
import com.vending.module.order.entity.Order;
import com.vending.module.pickup.entity.PickupCode;
import com.vending.module.pickup.mapper.PickupCodeMapper;
import com.vending.module.pickup.service.PickupCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PickupCodeServiceImpl extends ServiceImpl<PickupCodeMapper, PickupCode> implements PickupCodeService {

    private final Random random = new Random();
    private static final int MAX_RETRY_COUNT = 100;

    @Override
    public void generatePickupCode(Order order) {
        PickupCode pickupCode = new PickupCode();
        pickupCode.setOrderId(order.getOrderId());
        pickupCode.setOrderNo(order.getOrderNo());
        pickupCode.setCodeValue(generateCode());
        pickupCode.setStatus(0);
        pickupCode.setExpireTime(LocalDateTime.now().plusHours(2));

        this.save(pickupCode);
    }

    private String generateCode() {
        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            int code = 100000 + random.nextInt(900000);
            String codeStr = String.valueOf(code);
            
            if (!this.lambdaQuery().eq(PickupCode::getCodeValue, codeStr).exists()) {
                return codeStr;
            }
            retryCount++;
        }
        throw new BusinessException(ResultCode.PAYMENT_FAILED, "取货码生成失败，请重试");
    }
}
