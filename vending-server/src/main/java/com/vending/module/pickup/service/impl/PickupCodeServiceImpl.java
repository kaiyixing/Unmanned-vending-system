package com.vending.module.pickup.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vending.module.order.entity.Order;
import com.vending.module.pickup.entity.PickupCode;
import com.vending.module.pickup.mapper.PickupCodeMapper;
import com.vending.module.pickup.service.PickupCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickupCodeServiceImpl extends ServiceImpl<PickupCodeMapper, PickupCode> implements PickupCodeService {

    private final Random random = new Random();
    private static final int MAX_RETRY = 10;

    @Override
    public void generatePickupCode(Order order) {
        PickupCode pickupCode = new PickupCode();
        pickupCode.setOrderId(order.getOrderId());
        pickupCode.setOrderNo(order.getOrderNo());
        pickupCode.setStatus(0);
        pickupCode.setExpireTime(LocalDateTime.now().plusHours(2));

        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            String codeStr = generateUniqueCode();
            pickupCode.setCodeValue(codeStr);
            
            try {
                this.save(pickupCode);
                return;
            } catch (DuplicateKeyException e) {
                log.warn("取货码冲突，重试生成: attempt={}", attempt + 1);
            }
        }
        
        throw new RuntimeException("无法生成唯一取货码，请稍后重试");
    }

    private String generateUniqueCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
