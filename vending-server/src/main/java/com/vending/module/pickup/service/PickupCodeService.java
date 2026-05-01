package com.vending.module.pickup.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vending.module.order.entity.Order;
import com.vending.module.pickup.entity.PickupCode;

public interface PickupCodeService extends IService<PickupCode> {
    void generatePickupCode(Order order);
}
