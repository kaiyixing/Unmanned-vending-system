package com.vending.common.util;

import com.vending.common.exception.BusinessException;
import com.vending.common.result.ResultCode;
import com.vending.module.pickup.entity.PickupCode;
import com.vending.module.pickup.mapper.PickupCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class CodeGenerator {

    private final PickupCodeMapper pickupCodeMapper;
    private final Random random = new Random();

    public String generatePickupCode() {
        String codeStr;
        do {
            int code = 100000 + random.nextInt(900000);
            codeStr = String.valueOf(code);
        } while (pickupCodeMapper.existsByCodeValue(codeStr) > 0);
        return codeStr;
    }
}
